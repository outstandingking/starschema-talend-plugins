// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.update.cmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.HashedMap;
import org.eclipse.gef.commands.Command;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.xml.XmlUtil;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SAPIDocUnit;
import org.talend.core.model.metadata.builder.connection.ValidationRulesConnection;
import org.talend.core.model.metadata.builder.connection.impl.XmlFileConnectionImpl;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IExternalData;
import org.talend.core.model.process.IExternalNode;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.node.IExternalMapTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.IDesignerMapperService;
import org.talend.core.ui.ICDCProviderService;
import org.talend.core.ui.IEBCDICProviderService;
import org.talend.core.ui.IJobletProviderService;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.cmd.ChangeMetadataCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.update.UpdateManagerUtils;
import org.talend.designer.core.utils.SAPParametersUtils;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.ProjectNodeHelper;

/**
 * ggu class global comment. Detailled comment
 * 
 */
public class UpdateNodeParameterCommand extends Command {

    private UpdateResult result;

    public UpdateNodeParameterCommand(UpdateResult result) {
        super();
        this.result = result;
    }

    @Override
    public void execute() {
        if (result == null) {
            return;
        }
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        switch (result.getUpdateType()) {
        case NODE_PROPERTY:
            updateProperty();
            break;
        case NODE_SCHEMA:
            updateSchema();
            break;
        case NODE_QUERY:
            updateQuery();
            break;
        case NODE_SAP_FUNCTION:
            updateSAPParameters();
            break;
        case NODE_SAP_IDOC:
            updateSAPIDocParameters();
            break;
        case NODE_VALIDATION_RULE:
            updateValidationRule();
            break;
        default:
            return;
        }

        if (updateObject instanceof Node) {
            Node node = (Node) updateObject;
            if (node.getProcess() instanceof IProcess2) {
                PropertyChangeCommand pcc = new PropertyChangeCommand(node, EParameterName.UPDATE_COMPONENTS.getName(),
                        Boolean.TRUE);
                ((IProcess2) node.getProcess()).getCommandStack().execute(pcc);
            }
        }
    }

    private void updateSAPIDocParameters() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        boolean builtin = true;
        if (updateObject instanceof Node) {
            Node node = (Node) updateObject;
            if (result.getResultType() == EUpdateResult.UPDATE) {
                if (result.isChecked()) {
                    if (result.getParameter() instanceof SAPIDocUnit) {
                        SAPIDocUnit unit = (SAPIDocUnit) result.getParameter();
                        for (IElementParameter param : node.getElementParameters()) {
                            SAPParametersUtils.getSAPIDocParams(node, unit.getConnection(), param, unit.getName());
                        }
                        builtin = false;
                    }
                }
            }
            if (builtin) { // built-in
                node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                for (IElementParameter param : node.getElementParameters()) {
                    SAPParametersUtils.setNoRepositoryIDocParams(param);
                }
            }
        }

    }

    /**
     * DOC YeXiaowei Comment method "updateSAPParameters".
     */
    private void updateSAPParameters() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        boolean builtin = true;
        if (updateObject instanceof Node) {
            Node node = (Node) updateObject;
            if (result.getResultType() == EUpdateResult.UPDATE) {
                if (result.isChecked()) {
                    if (result.getParameter() instanceof SAPFunctionUnit) {
                        SAPFunctionUnit unit = (SAPFunctionUnit) result.getParameter();
                        for (IElementParameter param : node.getElementParameters()) {
                            SAPParametersUtils.retrieveSAPParams(node, unit.getConnection(), param, unit.getLabel());
                        }
                        builtin = false;
                    }
                }
            }
            if (builtin) { // built-in
                node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                for (IElementParameter param : node.getElementParameters()) {
                    SAPParametersUtils.setNoRepositoryParams(param);
                }
            }
        }

    }

    private void updateValidationRule() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        if (updateObject instanceof Node) {
            Node node = (Node) updateObject;
            if (result.getResultType() == EUpdateResult.UPDATE) {
                if (result.isChecked()) {
                    if (result.getParameter() instanceof ValidationRulesConnection) {
                        ValidationRulesConnection connection = (ValidationRulesConnection) result.getParameter();
                        if (connection != null) {
                            node.setPropertyValue(EParameterName.VALIDATION_RULE_TYPE.getName(), EmfComponent.REPOSITORY);
                        }
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private void updateProperty() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        if (updateObject instanceof Node) { // opened job
            Node node = (Node) updateObject;

            boolean update = false;
            // added by wzhang for bug 9302
            boolean isXsdPath = false;
            Object parameter = result.getParameter();
            if (parameter instanceof XmlFileConnectionImpl) {
                String filePath = ((XmlFileConnectionImpl) parameter).getXmlFilePath();
                if (filePath != null) {
                    if (XmlUtil.isXSDFile(filePath)) {
                        isXsdPath = true;
                    }
                }
            }

            if (result.getResultType() == EUpdateResult.UPDATE) {
                // upgrade from repository
                if (result.isChecked()) {
                    for (IElementParameter param : node.getElementParameters()) {
                        String repositoryValue = param.getRepositoryValue();
                        if ((repositoryValue != null)
                                && (param.isShow(node.getElementParameters())
                                        || (node instanceof INode && ((INode) node).getComponent().getName()
                                                .equals("tAdvancedFileOutputXML")) || (node instanceof INode && ((INode) node)
                                        .getComponent().getName().equals("tESBProviderRequest")))) { //$NON-NLS-1$
                            if (param.getName().equals(EParameterName.PROPERTY_TYPE.getName())
                                    || param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                                continue;
                            }
                            if (param.getFieldType().equals(EParameterFieldType.FILE) && isXsdPath) {
                                continue;
                            }
                            IMetadataTable table = null;
                            if (!node.getMetadataList().isEmpty()) {
                                table = node.getMetadataList().get(0);
                            }
                            Object objectValue = RepositoryToComponentProperty.getValue(
                                    (org.talend.core.model.metadata.builder.connection.Connection) result.getParameter(),
                                    repositoryValue, table);
                            if (objectValue == null || "".equals(objectValue)) {
                                if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                                    IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(
                                            IESBService.class);
                                    if (service != null) {
                                        String propertyValue = (String) node
                                                .getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());

                                        if (node.getComponent().getName().startsWith("tESB")) {
                                            if (propertyValue.contains(" - ")) {
                                                propertyValue = propertyValue.split(" - ")[0];
                                            }
                                        }

                                        IRepositoryViewObject lastVersion = UpdateRepositoryUtils
                                                .getRepositoryObjectById(propertyValue);
                                        if (lastVersion != null) {
                                            Item item = lastVersion.getProperty().getItem();
                                            if (item != null) {
                                                objectValue = service.getValue(item, repositoryValue, node);
                                            }
                                        }
                                    }
                                }
                            }
                            if (param.getName().equals(EParameterName.CDC_TYPE_MODE.getName())) {
                                //
                                String propertyValue = (String) node.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE
                                        .getName());
                                Item item = null;
                                IRepositoryViewObject lastVersion = UpdateRepositoryUtils.getRepositoryObjectById(propertyValue);
                                if (lastVersion != null) {
                                    item = lastVersion.getProperty().getItem();
                                }
                                if (item != null && PluginChecker.isCDCPluginLoaded()) {
                                    ICDCProviderService service = (ICDCProviderService) GlobalServiceRegister.getDefault()
                                            .getService(ICDCProviderService.class);
                                    if (service != null) {
                                        try {
                                            List<IRepositoryViewObject> all;
                                            all = CorePlugin.getDefault().getProxyRepositoryFactory()
                                                    .getAll(ERepositoryObjectType.METADATA_CONNECTIONS);
                                            for (IRepositoryViewObject obj : all) {
                                                Item tempItem = obj.getProperty().getItem();
                                                if (tempItem instanceof DatabaseConnectionItem) {
                                                    String cdcLinkId = service
                                                            .getCDCConnectionLinkId((DatabaseConnectionItem) tempItem);
                                                    if (cdcLinkId != null && item.getProperty().getId().equals(cdcLinkId)) {
                                                        objectValue = RepositoryToComponentProperty.getValue(
                                                                ((DatabaseConnectionItem) tempItem).getConnection(),
                                                                repositoryValue, node.getMetadataList().get(0));
                                                    }
                                                }
                                            }
                                        } catch (PersistenceException e) {
                                            ExceptionHandler.process(e);
                                        }
                                    }
                                }
                            }
                            if (objectValue != null) {
                                if (param.getFieldType().equals(EParameterFieldType.CLOSED_LIST)
                                        && repositoryValue.equals(UpdatesConstants.TYPE)) {
                                    boolean found = false;
                                    String[] items = param.getListRepositoryItems();
                                    for (int i = 0; (i < items.length) && (!found); i++) {
                                        if (objectValue.equals(items[i])) {
                                            found = true;
                                            node.setPropertyValue(param.getName(), param.getListItemsValue()[i]);
                                        }
                                    }
                                } else {
                                    // update tFileInputExcel job
                                    if (param.getFieldType().equals(EParameterFieldType.TABLE)) {
                                        String name = param.getName();
                                        if (name.equals("ROOT") || name.equals("LOOP") || name.equals("GROUP")
                                                && objectValue instanceof List) {
                                            param.setValue(objectValue);

                                        } else if (param.getName().equals("SHEETLIST") && objectValue instanceof List) {
                                            List<Map<String, Object>> paramMaps = (List<Map<String, Object>>) param.getValue();
                                            if (paramMaps == null) {
                                                paramMaps = new ArrayList<Map<String, Object>>();
                                                node.setPropertyValue(param.getName(), paramMaps);
                                            } else {
                                                // hywang add for 9537
                                                List<Map<String, Object>> objectValueList = (List<Map<String, Object>>) objectValue;

                                                if (paramMaps.size() < objectValueList.size()) {
                                                    paramMaps.clear();
                                                    for (int i = 0; i < objectValueList.size(); i++) {
                                                        Map<String, Object> map = objectValueList.get(i);
                                                        paramMaps.add(map);
                                                    }
                                                } else {
                                                    String value = null;
                                                    List<String> repNames = new ArrayList<String>();
                                                    for (int i = 0; i < objectValueList.size(); i++) {
                                                        repNames.add(objectValueList.get(i).get("SHEETNAME").toString());
                                                    }
                                                    for (int j = 0; j < paramMaps.size(); j++) {
                                                        Map<String, Object> map = paramMaps.get(j);
                                                        value = map.get("SHEETNAME").toString();
                                                        if (!repNames.contains(value)) {
                                                            paramMaps.remove(j);
                                                        }
                                                    }
                                                }
                                            }
                                        } else
                                        // fix 18011 :after change the jars in wizard, the update manager can't detect
                                        // it in jobs
                                        if (param.getName().equals("DRIVER_JAR") && objectValue instanceof List) {
                                            param.setValue(objectValue);
                                        }
                                    } else {
                                        node.setPropertyValue(param.getName(), objectValue);
                                    }
                                }
                            } else if (param.getFieldType().equals(EParameterFieldType.TABLE)
                                    && UpdatesConstants.XML_MAPPING.equals(repositoryValue)) {
                                RepositoryToComponentProperty.getTableXMLMappingValue(
                                        (org.talend.core.model.metadata.builder.connection.Connection) result.getParameter(),
                                        (List<Map<String, Object>>) param.getValue(), node);
                            } else if (param.getFieldType().equals(EParameterFieldType.TABLE) && param.getName().equals("PARAMS")) {
                                objectValue = RepositoryToComponentProperty.getValue(
                                        (org.talend.core.model.metadata.builder.connection.Connection) result.getParameter(),
                                        "PARAMS", node.getMetadataList().get(0));
                                List<Map<String, Object>> paramMaps = (List<Map<String, Object>>) param.getValue();
                                if (paramMaps == null) {
                                    paramMaps = new ArrayList<Map<String, Object>>();
                                } else {
                                    paramMaps.clear();
                                }
                                if (objectValue != null) {
                                    List<String> objectValueList = (List<String>) objectValue;
                                    for (int i = 0; i < objectValueList.size(); i++) {
                                        Map<String, Object> map = new HashedMap();
                                        map.put("VALUE", TalendTextUtils.addQuotes(objectValueList.get(i)));
                                        paramMaps.add(map);
                                    }
                                }
                            }
                            if (!("tMDMReceive".equals(node.getComponent().getName()) && "XPATH_PREFIX".equals(param //$NON-NLS-1$ //$NON-NLS-2$
                                    .getRepositoryValue()))) {
                                param.setRepositoryValueUsed(true);
                                param.setReadOnly(true);
                                update = true;
                            }
                        }
                    }
                }
            }
            if (!update) { // bult-in
                node.setPropertyValue(EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN);
                for (IElementParameter param : node.getElementParameters()) {
                    String repositoryValue = param.getRepositoryValue();
                    if (param.isShow(node.getElementParameters()) && (repositoryValue != null)) {
                        if (param.getName().equals(EParameterName.PROPERTY_TYPE.getName())
                                || param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                            continue;
                        }
                        param.setRepositoryValueUsed(false);
                        param.setReadOnly(false);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateSchema() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        if (updateObject instanceof Node) { // opened job
            Node node = (Node) updateObject;

            boolean builtIn = true;

            final IExternalNode externalNode = node.getExternalNode();
            if (result.getResultType() == EUpdateResult.UPDATE) {
                if (result.isChecked()) {
                    if (result.getParameter() instanceof List) {
                        // for ebcdic
                        if (PluginChecker.isEBCDICPluginLoaded()) {
                            IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault()
                                    .getService(IEBCDICProviderService.class);
                            if (service != null) {
                                if (service.isEbcdicNode(node)) {
                                    List<Object> parameter = (List<Object>) result.getParameter();
                                    if (parameter.size() >= 2) {
                                        IMetadataTable newTable = (IMetadataTable) parameter.get(0);
                                        String schemaName = (String) parameter.get(1);
                                        IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(node,
                                                schemaName);
                                        if (metadataTable != null) {
                                            MetadataToolHelper.copyTable(newTable, metadataTable);
                                        }
                                        syncSchemaForEBCDIC(node, metadataTable);
                                    }
                                    return;
                                }
                            }
                        }

                        // for tMap
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
                            IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault()
                                    .getService(IDesignerMapperService.class);
                            if (service == null || externalNode == null || externalNode.getExternalData() == null) {
                                return;
                            }
                            List<Object> parameter = (List<Object>) result.getParameter();
                            if (parameter.size() == 2) {
                                if (node.getComponent() != null && "tMap".equals(node.getComponent().getName())) { //$NON-NLS-1$
                                    IMetadataTable newTable = (IMetadataTable) parameter.get(0);
                                    String schemaId = (String) parameter.get(1);
                                    service.updateMapperTableEntries(externalNode, schemaId, newTable);
                                    node.setMetadataList(externalNode.getMetadataList());
                                    syncSchemaForTMap(node);
                                    // update metadataList,or it will cause bug 21080
                                    for (IExternalMapTable latestTable : externalNode.getExternalData().getOutputTables()) {
                                        for (IMetadataTable tableExsit : node.getMetadataList()) {
                                            // find table,and update the table
                                            if (latestTable.getName().equals(tableExsit.getTableName())) {
                                                List<IMetadataColumn> newColumns = newTable.getListColumns();
                                                for (IMetadataColumn column : tableExsit.getListColumns()) {
                                                    for (IMetadataColumn newColumn : newColumns) {
                                                        if (newColumn.getLabel().equals(column.getLabel())) {
                                                            column.setTalendType(newColumn.getTalendType());
                                                            column.setNullable(newColumn.isNullable());
                                                            column.setComment(newColumn.getComment());
                                                            column.setDefault(newColumn.getDefault());
                                                            column.setLength(newColumn.getLength());
                                                            column.setType(newColumn.getType());
                                                            column.setKey(newColumn.isKey());
                                                            column.setPrecision(newColumn.getPrecision());
                                                            break;
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            return;
                        }

                    } else if (result.getParameter() instanceof IMetadataTable) {
                        IMetadataTable newTable = (IMetadataTable) result.getParameter();
                        // node.getMetadataFromConnector(newTable.getAttachedConnector()).setListColumns(newTable.
                        // getListColumns());
                        if (newTable != null) {

                            for (INodeConnector nodeConnector : node.getListConnector()) {
                                if (nodeConnector.getBaseSchema().equals(newTable.getAttachedConnector())) {
                                    List<IElementParameter> params = node
                                            .getElementParametersFromField(EParameterFieldType.SCHEMA_TYPE);
                                    // IElementParameter param =
                                    // node.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                                    if (params != null) {
                                        for (IElementParameter param : params) {
                                            ChangeMetadataCommand cmd = null;

                                            if (param.getChildParameters() != null
                                                    && param.getChildParameters().get("REPOSITORY_SCHEMA_TYPE") != null
                                                    && result.getContextModeConnectionItem() != null) {
                                                final Object value = param.getChildParameters().get("REPOSITORY_SCHEMA_TYPE")
                                                        .getValue();
                                                String idAndName = result.getContextModeConnectionItem().getProperty().getId()
                                                        + UpdatesConstants.SEGMENT_LINE + newTable.getLabel();
                                                if (idAndName.equals(value)) {
                                                    cmd = new ChangeMetadataCommand(node, param, null, newTable);
                                                }
                                            } else {
                                                cmd = new ChangeMetadataCommand(node, param, null, newTable);
                                            }
                                            if (cmd != null) {
                                                // wzhang added to fix 9251. get the current connection.
                                                String propertyValue = (String) node
                                                        .getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                                                IRepositoryViewObject lastVersion = UpdateRepositoryUtils
                                                        .getRepositoryObjectById(propertyValue);
                                                Connection repositoryConn = null;
                                                if (lastVersion != null) {
                                                    final Item item = lastVersion.getProperty().getItem();
                                                    if (item != null && item instanceof ConnectionItem) {
                                                        repositoryConn = ((ConnectionItem) item).getConnection();
                                                    }
                                                }
                                                cmd.setConnection(repositoryConn);

                                                cmd.setRepositoryMode(true);
                                                cmd.execute(true);
                                            }
                                        }
                                    } else {
                                        MetadataToolHelper.copyTable(newTable,
                                                node.getMetadataFromConnector(nodeConnector.getName()));
                                    }
                                }
                            }
                            builtIn = false;
                        }
                    }
                }
            } else if (result.getResultType() == EUpdateResult.RENAME) {
                List<Object> parameter = (List<Object>) result.getParameter();
                if (parameter.size() >= 3) {
                    IMetadataTable newTable = (IMetadataTable) parameter.get(0);
                    String oldSourceId = (String) parameter.get(1);
                    String newSourceId = (String) parameter.get(2);
                    // for ebcdic
                    if (PluginChecker.isEBCDICPluginLoaded()) {
                        IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                                IEBCDICProviderService.class);
                        if (service != null) {
                            if (service.isEbcdicNode(node)) {
                                String[] sourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName(oldSourceId);
                                final String oldSchemaName = sourceIdAndChildName[1];

                                sourceIdAndChildName = UpdateManagerUtils.getSourceIdAndChildName(newSourceId);
                                final String newSchemaName = sourceIdAndChildName[1];
                                Map<String, Object> lineValue = (Map<String, Object>) parameter.get(3);
                                if (lineValue != null) {
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNode(node,
                                            oldSchemaName);
                                    Object schemaName = lineValue.get(IEbcdicConstant.FIELD_SCHEMA);
                                    if (metadataTable != null && schemaName != null) {
                                        lineValue.put(IEbcdicConstant.FIELD_SCHEMA, newSchemaName);

                                        MetadataToolHelper.copyTable(newTable, metadataTable);
                                        syncSchemaForEBCDIC(node, metadataTable);
                                        metadataTable.setLabel(newSchemaName);

                                    }
                                }
                                return;
                            }
                        }
                    }

                    // for tmap
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerMapperService.class)) {
                        IDesignerMapperService service = (IDesignerMapperService) GlobalServiceRegister.getDefault().getService(
                                IDesignerMapperService.class);
                        if (service == null || externalNode == null || externalNode.getExternalData() == null) {
                            return;
                        }
                        IExternalData externalData = externalNode.getExternalData();
                        parameter = (List<Object>) result.getParameter();
                        if (parameter.size() == 3) {
                            if (node.getComponent() != null && "tMap".equals(node.getComponent().getName())) { //$NON-NLS-1$
                                newTable = (IMetadataTable) parameter.get(0);
                                String schemaId = (String) parameter.get(1);
                                String newSchemaId = (String) parameter.get(2);
                                service.renameMapperTable(externalNode, schemaId, newSchemaId, newTable);
                                node.setMetadataList(externalNode.getMetadataList());
                                syncSchemaForTMap(node);
                            }
                        }
                        return;
                    }

                    String schemaParamName = UpdatesConstants.SCHEMA + UpdatesConstants.COLON
                            + EParameterName.REPOSITORY_SCHEMA_TYPE.getName();
                    IElementParameter repositoryParam = node.getElementParameter(schemaParamName);
                    if (repositoryParam != null && oldSourceId.equals(repositoryParam.getValue())) {
                        node.setPropertyValue(schemaParamName, newSourceId);
                        if (newTable != null) {
                            for (INodeConnector nodeConnector : node.getListConnector()) {
                                if (nodeConnector.getBaseSchema().equals(newTable.getAttachedConnector())) {
                                    MetadataToolHelper
                                            .copyTable(newTable, node.getMetadataFromConnector(nodeConnector.getName()));
                                }
                            }
                        }
                        builtIn = false;
                    }
                    // for tELTAggregate
                    schemaParamName = UpdatesConstants.SCHEMA_TARGET + UpdatesConstants.COLON
                            + EParameterName.REPOSITORY_SCHEMA_TYPE.getName();
                    repositoryParam = node.getElementParameter(schemaParamName);
                    if (repositoryParam != null && oldSourceId.equals(repositoryParam.getValue())) {
                        node.setPropertyValue(schemaParamName, newSourceId);
                        if (newTable != null) {
                            for (INodeConnector nodeConnector : node.getListConnector()) {
                                if (nodeConnector.getBaseSchema().equals(repositoryParam.getContext())) {
                                    MetadataToolHelper
                                            .copyTable(newTable, node.getMetadataFromConnector(nodeConnector.getName()));
                                }
                            }
                        }
                        builtIn = false;
                    }

                }
            } else if (result.getResultType() == EUpdateResult.BUIL_IN) {
                // for tELTAgrregate
                if (UpdatesConstants.SCHEMA_TARGET.equals(result.getParameter())) {
                    node.setPropertyValue(UpdatesConstants.SCHEMA_TARGET + ":" + EParameterName.SCHEMA_TYPE.getName(),
                            EmfComponent.BUILTIN);
                } else {
                    // for ebcdic
                    if (PluginChecker.isEBCDICPluginLoaded()) {
                        IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                                IEBCDICProviderService.class);
                        if (service != null) {
                            if (service.isEbcdicNode(node)) {
                                Object parameter = result.getParameter();
                                if (parameter instanceof Map) {
                                    Map<String, Object> lineValue = (Map<String, Object>) parameter;
                                    lineValue.remove(IEbcdicConstant.FIELD_SCHEMA + IEbcdicConstant.REF_TYPE);
                                }
                                return;
                            }
                        }
                    }

                    if (PluginChecker.isJobLetPluginLoaded()) {
                        IJobletProviderService service = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                                IJobletProviderService.class);
                        if (service != null && service.isJobletInOutComponent(node)) {
                            node.setPropertyValue(
                                    EParameterName.SCHEMA_TYPE.getName() + ":" + EParameterName.SCHEMA_TYPE.getName(),
                                    EmfComponent.BUILTIN);
                        }
                    }
                    node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                    for (IElementParameter param : node.getElementParameters()) {
                        SAPParametersUtils.setNoRepositoryParams(param);
                    }
                }
            } else if (result.getResultType() == EUpdateResult.DELETE) {
                node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
            } else if (result.getResultType() == EUpdateResult.RELOAD) {
                List<Object> parameter = (List<Object>) result.getParameter();
                String connectionId = null;
                String tableLabel = null;
                IRepositoryViewObject toReload = null;
                IMetadataTable tableToReload = null;
                if (parameter instanceof List) {
                    List listParameter = parameter;
                    connectionId = (String) node.getPropertyValue(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
                    tableLabel = ((String) listParameter.get(0)).split(UpdatesConstants.SEGMENT_LINE)[0];
                }
                if (connectionId != null) {
                    try {
                        toReload = ProxyRepositoryFactory.getInstance().getLastVersion(connectionId);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }
                if (toReload != null) {
                    Item item = toReload.getProperty().getItem();
                    if (item != null && item instanceof DatabaseConnectionItem) {
                        DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                        Connection connection = dbItem.getConnection();
                        if (connection instanceof DatabaseConnection) {
                            DatabaseConnection dbConn = (DatabaseConnection) connection;
                            Set<MetadataTable> tables = ProjectNodeHelper.getTablesFromSpecifiedDataPackage(dbConn);
                            if (tables != null && !tables.isEmpty()) {
                                Iterator it = tables.iterator();
                                while (it.hasNext() && tableToReload == null) {
                                    MetadataTable table = (MetadataTable) it.next();
                                    String label = table.getLabel();
                                    if (tableLabel != null) {
                                        if (label != null && label.equals(tableLabel)) {
                                            tableToReload = ConvertionHelper.convert(table);
                                            break;
                                        }
                                    }
                                }
                            }
                            Set<org.talend.core.model.metadata.builder.connection.MetadataTable> newTables = ConnectionHelper
                                    .getTables(connection);
                            if (newTables != null && !newTables.isEmpty() && tableToReload == null) {
                                Iterator it = newTables.iterator();
                                while (it.hasNext() && tableToReload == null) {
                                    MetadataTable table = (MetadataTable) it.next();
                                    String label = table.getLabel();
                                    if (tableLabel != null) {
                                        if (label != null && label.equals(tableLabel)) {
                                            tableToReload = ConvertionHelper.convert(table);
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (tableToReload != null) {
                        int index = -1;
                        List<IMetadataTable> tablesInNode = node.getMetadataList();
                        for (IMetadataTable table : tablesInNode) {
                            if (table.getLabel().equals(tableToReload.getLabel())) {
                                index = tablesInNode.indexOf(table);
                                break;
                            }
                        }
                        if (index >= 0) {
                            IMetadataTable oldTable = tablesInNode.get(index);
                            /* dbms and Connector should be transfer when reloaded the table,20024 */
                            tableToReload.setAttachedConnector(oldTable.getAttachedConnector());
                            tableToReload.setDbms(oldTable.getDbms());
                            tablesInNode.remove(index);
                            tablesInNode.add(index, tableToReload);
                            builtIn = false;
                        }
                    }
                }

            }
            // bug 23326
            if (builtIn) { // bult-in
                node.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
            }
        }
    }

    /**
     * nrousseau Comment method "synchSchemaForEBCDIC".
     */
    private void syncSchemaForEBCDIC(Node node, IMetadataTable metadataTable) {
        for (IConnection conn : node.getOutgoingConnections()) {
            if (conn.getLineStyle() == EConnectionType.FLOW_MAIN
                    && metadataTable.getTableName().equals(conn.getMetadataTable().getTableName())) {
                Node target = (Node) conn.getTarget();
                IElementParameter schemaTypeParam = target.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                if (schemaTypeParam != null) {
                    ChangeMetadataCommand cmd = new ChangeMetadataCommand(target, schemaTypeParam, null, metadataTable);
                    cmd.setRepositoryMode(true);
                    cmd.execute(true);
                }
            }
        }
    }

    private void syncSchemaForTMap(Node node) {
        for (IConnection conn : node.getOutgoingConnections()) {
            if (conn.getLineStyle() == EConnectionType.FLOW_MAIN) {
                IMetadataTable metadataTable = null;
                for (IMetadataTable table : node.getMetadataList()) {
                    if (table.getTableName() != null && table.getTableName().equals(conn.getMetadataTable().getTableName())) {
                        metadataTable = table;
                    }
                }
                if (metadataTable != null) {
                    Node target = (Node) conn.getTarget();
                    IElementParameter schemaTypeParam = target.getElementParameterFromField(EParameterFieldType.SCHEMA_TYPE);
                    if (schemaTypeParam != null) {
                        ChangeMetadataCommand cmd = new ChangeMetadataCommand(target, schemaTypeParam, null, metadataTable);
                        cmd.setRepositoryMode(true);
                        cmd.execute(true);
                    }
                }
            }
        }
    }

    private void updateQuery() {
        Object updateObject = result.getUpdateObject();
        if (updateObject == null) {
            return;
        }
        if (updateObject instanceof Node) { // opened job
            Node node = (Node) updateObject;

            boolean update = false;
            if (result.getResultType() == EUpdateResult.UPDATE) {
                if (result.isChecked()) {
                    Query query = (Query) result.getParameter();
                    if (query != null) {
                        for (IElementParameter param : node.getElementParameters()) {
                            if (param.getFieldType() == EParameterFieldType.MEMO_SQL
                                    && UpdatesConstants.QUERY.equals(param.getName())) {
                                // modefied by hyWang
                                String value = query.getValue();
                                if (!query.isContextMode()) {
                                    value = QueryUtil.checkAndAddQuotes(value);
                                }
                                param.setValue(value);
                                param.setRepositoryValueUsed(true);
                                param.setReadOnly(true);
                                update = true;
                            }
                        }
                    }
                }
            }
            if (!update) {
                node.setPropertyValue(EParameterName.QUERYSTORE_TYPE.getName(), EmfComponent.BUILTIN);
                IElementParameter sqlParam = node.getElementParameterFromField(EParameterFieldType.MEMO_SQL);
                if (sqlParam != null) {
                    sqlParam.setRepositoryValueUsed(false);
                    sqlParam.setReadOnly(false);
                }
            }
        }
    }

}
