// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.gef.commands.Command;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.IGraphicalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.IJobletProviderService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.preferences.StatsAndLogsConstants;
import org.talend.designer.core.ui.views.CodeView;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettings;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.core.utils.ValidationRulesUtil;
import org.talend.designer.runprocess.ItemCacheManager;

/**
 * Command that changes a given property. It will call the set or get property value in an element. This element can be
 * either a node, a connection or a process. <br/>
 * 
 * $Id: PropertyChangeCommand.java 57414 2011-03-25 10:52:23Z hcyi $
 * 
 */
public class PropertyChangeCommand extends Command {

    private final IElement elem;

    private final String propName;

    private Object newValue;

    private Object oldValue;

    private boolean repositoryValueWasUsed;

    private boolean toUpdate;

    private final Map<IElementParameter, Object> oldElementValues;

    private ChangeMetadataCommand changeMetadataCommand;

    private String propertyTypeName;

    private final String updataComponentParamName;

    /**
     * The property is defined in an element, which can be either a node or a connection.
     * 
     * @param elem
     * @param propName
     * @param propValue
     */
    public PropertyChangeCommand(IElement elem, String propName, Object propValue) {
        this.elem = elem;
        this.propName = propName;
        newValue = propValue;
        toUpdate = false;
        oldElementValues = new HashMap<IElementParameter, Object>();
        setLabel(Messages.getString("PropertyChangeCommand.Label")); //$NON-NLS-1$
        // for job settings extra (feature 2710)
        // if (JobSettingsConstants.isExtraParameter(propName) ||
        // propName.equals(EParameterName.IMPLICIT_TCONTEXTLOAD.getName())) {
        // propertyTypeName = JobSettingsConstants.getExtraParameterName(EParameterName.PROPERTY_TYPE.getName());
        // updataComponentParamName =
        // JobSettingsConstants.getExtraParameterName(EParameterName.UPDATE_COMPONENTS.getName());
        // } else {

        IElementParameter currentParam = elem.getElementParameter(propName);
        propertyTypeName = EParameterName.PROPERTY_TYPE.getName();
        for (IElementParameter param : elem.getElementParameters()) {
            if (param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)
                    && param.getCategory().equals(currentParam.getCategory())) {
                propertyTypeName = param.getName() + ":" + EParameterName.PROPERTY_TYPE.getName(); //$NON-NLS-1$
                break;
            }
        }
        updataComponentParamName = EParameterName.UPDATE_COMPONENTS.getName();
        // }
    }

    @Override
    public void execute() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        oldElementValues.clear();
        if (currentParam == null) {
            return;
        }

        if (currentParam.isRepositoryValueUsed()) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                Object queryStoreValue = elem.getPropertyValue(EParameterName.QUERYSTORE_TYPE.getName());
                if (!EmfComponent.BUILTIN.equals(queryStoreValue) || !EmfComponent.TNS_FILE.equals(queryStoreValue)) {
                    elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
                }
                currentParam.setRepositoryValueUsed(false);
            } else {
                toUpdate = true;
                String oldValueString = elem.getPropertyValue(propName).toString();
                if (!oldValueString.endsWith("xsd") && !oldValueString.endsWith("xsd\"")) {
                    elem.setPropertyValue(propertyTypeName, EmfComponent.BUILTIN);
                }
                for (IElementParameter param : elem.getElementParameters()) {
                    if (param.getCategory().equals(currentParam.getCategory())) {
                        param.setRepositoryValueUsed(false);
                    }
                }
            }

            repositoryValueWasUsed = true;
        } else {
            repositoryValueWasUsed = false;
        }

        oldValue = elem.getPropertyValue(propName);
        elem.setPropertyValue(propName, newValue);

        // feature 19312
        if (propName.contains(EParameterName.USE_DYNAMIC_JOB.getName()) && newValue.equals(false)) {
            IElementParameter processParam = elem.getElementParameter(EParameterName.PROCESS.getName());
            IElementParameter processTypeParameter = elem.getElementParameter(EParameterName.PROCESS_TYPE_PROCESS.getName());
            final String parentName = processParam.getName() + ":"; //$NON-NLS-1$
            elem.setPropertyValue(parentName + processTypeParameter.getName(), ""); //$NON-NLS-1$
            elem.setPropertyValue(processParam.getName(), ""); //$NON-NLS-1$
        }
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            boolean isSelectUseDynamic = false;
            IElementParameter useDynamicJobParameter = elem.getElementParameter(EParameterName.USE_DYNAMIC_JOB.getName());
            if (useDynamicJobParameter != null && useDynamicJobParameter instanceof IElementParameter) {
                Object useDynamicJobValue = (Object) useDynamicJobParameter.getValue();
                if (useDynamicJobValue != null && useDynamicJobValue instanceof Boolean) {
                    isSelectUseDynamic = (Boolean) useDynamicJobValue;
                }
            }
            if (isSelectUseDynamic) {
                StringBuffer labels = new StringBuffer("");
                if (newValue != null) {
                    String[] strValues = newValue.toString().split(";");
                    for (int i = 0; i < strValues.length; i++) {
                        String strValue = strValues[i];
                        // newValue is the id of the job
                        ProcessItem processItem = ItemCacheManager.getProcessItem((String) strValue);
                        if (processItem != null) {
                            String label = processItem.getProperty().getLabel();
                            if (i > 0) {
                                labels.append(";");
                            }
                            labels.append(label);
                        }
                    }
                }
                currentParam.getParentParameter().setValue(labels.toString());
            } else {
                // newValue is the id of the job
                ProcessItem processItem = ItemCacheManager.getProcessItem((String) newValue);
                if (processItem != null) {
                    currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
                }
            }
        }
        if (propName.contains(EParameterName.PROCESS_TYPE_VERSION.getName())) {
            // newValue is the id of the job

            // hywang add for feature 6549
            // 1.to see current component if is a jobletComponent by "elem"
            boolean isJobletComponent = false;
            // Node jobletNode = null;
            IJobletProviderService service = null;
            if (PluginChecker.isJobLetPluginLoaded()) {
                service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(IJobletProviderService.class);
            }
            if (elem instanceof Node) {
                // jobletNode = (Node) elem;
                if (service != null) {
                    isJobletComponent = service.isJobletComponent((Node) elem);
                }
            }
            if (isJobletComponent) {
                // 2.if it is a jobletcomponent,reload the component by the version
                String id = service.getJobletComponentItem((Node) elem).getId();
                String version = (String) newValue;
                IComponent newComponent = service.setPropertyForJobletComponent(id, version);
                reloadNode((Node) elem, newComponent);
            } else {

                IElementParameter processIdParam = currentParam.getParentParameter().getChildParameters()
                        .get(EParameterName.PROCESS_TYPE_PROCESS.getName());
                ProcessItem processItem = ItemCacheManager.getProcessItem((String) processIdParam.getValue(), (String) newValue);
                if (processItem != null) {
                    currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
                }
            }

        }
        if (propName.contains(EParameterName.PROCESS_TYPE_CONTEXT.getName())) {
            if (elem instanceof Node) {
                Node node = (Node) elem;
                List<IContext> listContext = node.getProcess().getContextManager().getListContext();
                List<String> values = new ArrayList<String>();
                for (IContext context : listContext) {
                    values.add(context.getName());
                }
                currentParam.setListItemsDisplayName(values.toArray(new String[0]));
                currentParam.setListItemsValue(values.toArray(new String[0]));
                currentParam.setValue(newValue);
            }

        }
        if (propName.equals(EParameterName.VALIDATION_RULES.getName())) {
            if (elem instanceof INode) {
                ValidationRulesUtil.createRejectConnector((INode) elem);
                ValidationRulesUtil.updateRejectMetatable((INode) elem, null);
                if (newValue != null && (!(Boolean) newValue)) {
                    ValidationRulesUtil.removeRejectConnector((INode) elem);
                    ValidationRulesUtil.removeRejectConnection((INode) elem);
                }
            }
        }
        String dbType = "";
        if (newValue instanceof String) {
            dbType = (String) newValue;
        }
        if (propName.equals(EParameterName.DB_TYPE.getName())) {
            IElementParameter elementParameter = elem.getElementParameter(EParameterName.DB_VERSION.getName());
            IElementParameter elementParameter2 = elem.getElementParameter(EParameterName.SCHEMA_DB.getName());
            setDbVersion(elementParameter, dbType);
            DesignerUtilities.setSchemaDB(elementParameter2, newValue);
        } else if (propName.equals(JobSettingsConstants.getExtraParameterName(EParameterName.DB_TYPE.getName()))) {//$NON-NLS-1$
            IElementParameter elementParameter = elem.getElementParameter(JobSettingsConstants
                    .getExtraParameterName(EParameterName.DB_VERSION.getName()));
            IElementParameter elementParameter2 = elem.getElementParameter(JobSettingsConstants
                    .getExtraParameterName(EParameterName.SCHEMA_DB.getName()));
            setDbVersion(elementParameter, dbType);
            DesignerUtilities.setSchemaDB(elementParameter2, newValue);
        }
        if (!toUpdate
                && (currentParam.getFieldType().equals(EParameterFieldType.RADIO)
                        || currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                        || currentParam.getFieldType().equals(EParameterFieldType.CHECK) || currentParam.getFieldType().equals(
                        EParameterFieldType.AS400_CHECK))) {
            toUpdate = false;
            for (int i = 0; i < elem.getElementParameters().size(); i++) {
                IElementParameter testedParam = elem.getElementParameters().get(i);

                String showIf = testedParam.getShowIf();
                String notShowIf = testedParam.getNotShowIf();

                if (showIf != null) {
                    if (showIf.contains(currentParam.getName())) {
                        toUpdate = true;
                    }
                } else {
                    if (notShowIf != null) {
                        if (notShowIf.contains(currentParam.getName())) {
                            toUpdate = true;
                        }
                    }
                }
                if (testedParam.getFieldType() == EParameterFieldType.TABLE) {
                    String[] tmpShowIfs = testedParam.getListItemsShowIf();
                    if (tmpShowIfs != null) {
                        for (String show : tmpShowIfs) {
                            if (show != null && show.contains(currentParam.getName())) {
                                toUpdate = true;
                            }
                        }
                    }
                    tmpShowIfs = testedParam.getListItemsNotShowIf();
                    if (tmpShowIfs != null) {
                        for (String show : tmpShowIfs) {
                            if (show != null && show.contains(currentParam.getName())) {
                                toUpdate = true;
                            }
                        }
                    }
                }
                if (currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
                    if (testedParam.getListItemsShowIf() != null) {
                        for (int j = 0; j < testedParam.getListItemsShowIf().length && !toUpdate; j++) {
                            showIf = testedParam.getListItemsShowIf()[j];
                            notShowIf = testedParam.getListItemsNotShowIf()[j];
                            if (showIf != null) {
                                if (showIf.contains(currentParam.getName())) {
                                    toUpdate = true;
                                }
                            } else {
                                if (notShowIf != null) {
                                    if (notShowIf.contains(currentParam.getName())) {
                                        toUpdate = true;
                                    }
                                }
                            }
                        }
                    }
                }
                setDefaultValues(currentParam, testedParam);
            }
        }

        if (currentParam.getName().equals(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            toUpdate = true;
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, new Boolean(true));
        }
        // see bug 9151:100% CPU when typing text.
        boolean updateCode = false;
        if (getNewValue() instanceof String && elem instanceof INode) {
            INode curNode = (INode) elem;
            String uniqueName = curNode.getUniqueName();
            IProcess process = curNode.getProcess();
            if (process != null && process instanceof IProcess2) {
                IProcess2 process2 = (IProcess2) process;
                List<? extends INode> generatingNodes = null;
                if (process2.isProcessModified()) {
                    process2.setProcessModified(false);
                    generatingNodes = process2.getGeneratingNodes();
                    if (generatingNodes != null) {
                        for (INode genNode : generatingNodes) {
                            if (genNode.getUniqueName().equals(uniqueName)) {
                                IElementParameter genParam = genNode.getElementParameter(propName);
                                if (genParam != null) {
                                    genParam.setValue(newValue);
                                    break;
                                }
                            }
                        }
                    }

                    CodeView.refreshCodeView(elem);
                    process2.setProcessModified(true);
                    updateCode = true;
                }

            }
        }
        // if (!updateCode) {
        // CodeView.refreshCodeView(elem);
        // }
        //
        if (elem instanceof IGraphicalNode) {
            ((IGraphicalNode) elem).checkAndRefreshNode();
        }

        // See feature 3902
        if (needUpdateMonitorConnection()) {
            ((Connection) elem).setMonitorConnection((Boolean) currentParam.getValue());
        }
    }

    private void setDbVersion(IElementParameter elementParameter, String value) {
        if (value.indexOf("Access") != -1) {//$NON-NLS-1$
            elementParameter.setValue(StatsAndLogsConstants.ACCESS_VERSION_DRIVER[1]);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ACCESS_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ACCESS_VERSION_DRIVER);
        } else if (value.indexOf("Oracle") != -1) {//$NON-NLS-1$
            elementParameter.setValue(StatsAndLogsConstants.ORACLE_VERSION_DRIVER[1]);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.ORACLE_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.ORACLE_VERSION_DRIVER);
        } else if (value.indexOf("AS400") != -1) {//$NON-NLS-1$
            elementParameter.setValue(StatsAndLogsConstants.AS400_VERSION_DRIVER[1]);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.AS400_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.AS400_VERSION_DRIVER);
        } else if (value.indexOf("Mysql") != -1) {//$NON-NLS-1$
            elementParameter.setValue(StatsAndLogsConstants.MYSQL_VERSION_DRIVER[1]);
            elementParameter.setListItemsDisplayName(StatsAndLogsConstants.MYSQL_VERSION_DISPLAY);
            elementParameter.setListItemsValue(StatsAndLogsConstants.MYSQL_VERSION_DRIVER);
        }
    }

    private boolean needUpdateMonitorConnection() {

        if (elem instanceof Connection) {
            if (propName.equals(EParameterName.MONITOR_CONNECTION.getName())) {
                return true;
            }
        }

        return false;
    }

    public void setUpdate(boolean update) {
        toUpdate = update;
    }

    /**
     * Set the values to default if needed.
     * 
     * @param currentParam Current parameter that has been modified in the interface
     * @param testedParam Tested parameter, to know if there is a link for the default values between this parameter and
     * the current.
     */
    private void setDefaultValues(IElementParameter currentParam, IElementParameter testedParam) {
        boolean contains = false;

        // zli
        for (IElementParameterDefaultValue value : testedParam.getDefaultValues()) {
            if (value.getIfCondition() != null) {
                if (value.getIfCondition().contains(currentParam.getName())) {
                    contains = true;
                    break;
                }
            }
            if (value.getNotIfCondition() != null) {
                if (value.getNotIfCondition().contains(currentParam.getName())) {
                    contains = true;
                    break;
                }
            }
        }

        if (testedParam.getDefaultValues().size() > 0 && contains) {
            oldElementValues.put(testedParam, testedParam.getValue());

            // if the field is not a schema type, then use standard "set value".
            if (!testedParam.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)) {
                String oldMapping = ""; //$NON-NLS-1$
                if (!testedParam.getFieldType().equals(EParameterFieldType.CHECK)
                        && !testedParam.getFieldType().equals(EParameterFieldType.RADIO)) {
                    oldMapping = (String) testedParam.getValue();
                }
                testedParam.setValueToDefault(elem.getElementParameters());
                if (testedParam.getFieldType().equals(EParameterFieldType.MAPPING_TYPE)) {
                    String newMapping = (String) testedParam.getValue();
                    if (!oldMapping.equals(newMapping)) {
                        Node node = (Node) elem;
                        if (node.getMetadataList().size() > 0) {
                            // to change with:
                            // IMetadataTable metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                            IMetadataTable metadataTable = node.getMetadataList().get(0);
                            metadataTable.setDbms(newMapping);
                        }
                    }
                }
            } else {
                // See issue 975, update the schema.
                Node node = (Node) elem;
                if (node.getMetadataList().size() > 0) {
                    IMetadataTable metadataTable = null;
                    IMetadataTable newMetadataTable = null;
                    if (node.getComponent() != null && "tSalesforceOutput".equals(node.getComponent().getName())) {
                        // for feature 0014652
                        boolean isBuiltIn = false;
                        final IElementParameter elementParameter = node.getElementParameter(EParameterName.PROPERTY_TYPE
                                .getName());
                        if (elementParameter != null) {
                            Object value = elementParameter.getValue();
                            if ("BUILT_IN".equals(value.toString())) {//$NON-NLS-1$
                                isBuiltIn = true;
                            }
                        }
                        if (isBuiltIn) {
                            metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                            testedParam.setValueToDefault(node.getElementParameters());
                            IMetadataTable defaultMetadataTable = (IMetadataTable) testedParam.getValue();
                            if (testedParam.getName().equals("SCHEMA")) {//$NON-NLS-1$
                                newMetadataTable = defaultMetadataTable;
                            }

                        } else {
                            metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                            if (testedParam.getName().equals("SCHEMA")) {//$NON-NLS-1$
                                newMetadataTable = metadataTable;
                            }
                        }
                        IMetadataTable defaultMetadataTable = (IMetadataTable) testedParam.getValue();
                        if (testedParam.getName().equals("SCHEMA_FLOW")) {//$NON-NLS-1$
                            IElementParameter param = node.getElementParameter(EParameterName.SCHEMA.getName());
                            IMetadataTable meta = node.getMetadataFromConnector(param.getContext());
                            newMetadataTable = meta.clone(true);
                            List<IMetadataColumn> toAdd = new ArrayList<IMetadataColumn>();
                            for (IMetadataColumn column : defaultMetadataTable.clone(true).getListColumns()) {
                                boolean found = false;
                                for (IMetadataColumn existingColumn : newMetadataTable.getListColumns()) {
                                    if (existingColumn.getLabel().equals(column.getLabel())) {
                                        found = true;
                                        break;
                                    }
                                    if (!found) {
                                        toAdd.add(column);
                                    }
                                }

                                newMetadataTable.getListColumns().addAll(toAdd);
                            }
                        }
                    } else {
                        metadataTable = node.getMetadataFromConnector(testedParam.getContext());
                        testedParam.setValueToDefault(node.getElementParameters());
                        newMetadataTable = (IMetadataTable) testedParam.getValue();

                    }
                    if (metadataTable != null && newMetadataTable != null) {
                        newMetadataTable.setTableName(metadataTable.getTableName());
                        newMetadataTable.setAttachedConnector(metadataTable.getAttachedConnector());
                        metadataTable.setReadOnly(newMetadataTable.isReadOnly());
                        metadataTable.setListColumns(newMetadataTable.clone(true).getListColumns());
                        changeMetadataCommand = new ChangeMetadataCommand(node, null, metadataTable, newMetadataTable);
                        changeMetadataCommand.execute(true);
                    }
                }
            }
        }
    }

    @Override
    public void undo() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        if (repositoryValueWasUsed) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.REPOSITORY);
            } else {
                elem.setPropertyValue(propertyTypeName, EmfComponent.REPOSITORY);
            }
            for (IElementParameter param : elem.getElementParameters()) {
                String repositoryValue = param.getRepositoryValue();
                if (param.isShow(elem.getElementParameters()) && (repositoryValue != null)
                        && param.getCategory().equals(currentParam.getCategory())) {
                    param.setRepositoryValueUsed(true);
                }
            }
        }
        elem.setPropertyValue(propName, oldValue);
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            // oldValue is the id of the job
            ProcessItem processItem = ItemCacheManager.getProcessItem((String) oldValue);
            if (processItem != null) {
                currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
            }
        }

        for (IElementParameter param : oldElementValues.keySet()) {
            param.setValue(oldElementValues.get(param));
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, new Boolean(true));
        }
        if (changeMetadataCommand != null) {
            changeMetadataCommand.undo();
        }
        CodeView.refreshCodeView(elem);
        ComponentSettings.switchToCurComponentSettingsView();
        JobSettings.switchToCurJobSettingsView();
        refreshTraceConnections();
        refreshResumingConnections();
        if (elem instanceof Node) {
            ((Node) elem).checkAndRefreshNode();
        }

    }

    @Override
    public void redo() {
        IElementParameter currentParam = elem.getElementParameter(propName);
        if (repositoryValueWasUsed) {
            if (currentParam.getFieldType() == EParameterFieldType.MEMO_SQL) {
                elem.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
            } else {
                elem.setPropertyValue(propertyTypeName, EmfComponent.BUILTIN);
            }

            for (IElementParameter param : elem.getElementParameters()) {
                boolean paramFlag = JobSettingsConstants.isExtraParameter(param.getName());
                boolean extraFlag = JobSettingsConstants.isExtraParameter(propertyTypeName);
                if (paramFlag == extraFlag) {
                    // for job settings extra.(feature 2710)
                    param.setRepositoryValueUsed(false);
                }
            }
        }

        elem.setPropertyValue(propName, newValue);
        if (propName.contains(EParameterName.PROCESS_TYPE_PROCESS.getName())) {
            // newValue is the id of the job
            ProcessItem processItem = ItemCacheManager.getProcessItem((String) newValue);
            if (processItem != null) {
                currentParam.getParentParameter().setValue(processItem.getProperty().getLabel());
            }
        }

        if (currentParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST)) {
            for (int i = 0; i < elem.getElementParameters().size(); i++) {
                IElementParameter param = elem.getElementParameters().get(i);
                if (param.getDefaultValues().size() > 0) {
                    param.setValueToDefault(elem.getElementParameters());
                }
            }
        }

        if (toUpdate) {
            elem.setPropertyValue(updataComponentParamName, new Boolean(true));
        }

        if (changeMetadataCommand != null) {
            changeMetadataCommand.redo();
        }
        CodeView.refreshCodeView(elem);
        ComponentSettings.switchToCurComponentSettingsView();
        JobSettings.switchToCurJobSettingsView();
        refreshTraceConnections();
        refreshResumingConnections();
        if (elem instanceof Node) {
            ((Node) elem).checkAndRefreshNode();
        }
    }

    private void refreshTraceConnections() {
        if (propName.equals(EParameterName.TRACES_CONNECTION_ENABLE.getName()) || this.elem instanceof Connection) {
            ((Connection) this.elem).getConnectionTrace().setPropertyValue(EParameterName.TRACES_SHOW_ENABLE.getName(), true);
        }
    }

    private void refreshResumingConnections() {
        if (propName.equals(EParameterName.RESUMING_CHECKPOINT.getName()) || this.elem instanceof Connection) {
            ((Connection) this.elem).getConnectionTrace().setPropertyValue(EParameterName.RESUMING_CHECKPOINT.getName(), true);
        }
    }

    public String getPropName() {
        return this.propName;
    }

    public IElement getElement() {
        return this.elem;
    }

    public Object getOldValue() {
        return this.oldValue;
    }

    public Object getNewValue() {
        return this.newValue;
    }

    private Map<String, Object> createParameters(Node node) {
        if (node == null) {
            Collections.emptyMap();
        }
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (node.getComponent().getComponentType() != EComponentType.JOBLET) {
            if (node.getExternalData() != null) {
                parameters.put(INode.RELOAD_PARAMETER_EXTERNAL_BYTES_DATA, node.getExternalData());
            }
            parameters.put(INode.RELOAD_PARAMETER_METADATA_LIST, node.getMetadataList());
        }
        parameters.put(INode.RELOAD_PARAMETER_ELEMENT_PARAMETERS, node.getElementParameters());
        parameters.put(INode.RELOAD_PARAMETER_CONNECTORS, node.getListConnector());

        return parameters;
    }

    private void reloadNode(Node node, IComponent newComponent) {
        if (node == null || newComponent == null) {
            return;
        }
        Map<String, Object> parameters = createParameters(node);
        if (!parameters.isEmpty()) {
            node.reloadComponent(newComponent, parameters);
        }
    }

}
