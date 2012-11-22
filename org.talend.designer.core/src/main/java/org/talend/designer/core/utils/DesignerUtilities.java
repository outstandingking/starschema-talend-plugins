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
package org.talend.designer.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.jobsettings.JobSettingsConstants;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.editor.JobEditorInput;

/**
 * DOC bqian class global comment. Detailled comment
 */
public class DesignerUtilities {

    private static final String TRUN_JOB = "tRunJob"; //$NON-NLS-1$

    public static boolean isTRunJobComponent(INode node) {
        return TRUN_JOB.equals(node.getComponent().getName());
    }

    public static boolean isTRunJobComponent(NodeType node) {
        return TRUN_JOB.equals(node.getComponentName());
    }

    public static IProcess2 getCorrespondingProcessFromTRunjob(INode node) {
        if (DesignerUtilities.isTRunJobComponent(node)) {
            Node concreteNode = (Node) node;
            String processId = (String) concreteNode.getPropertyValue(EParameterName.PROCESS_TYPE_PROCESS.getName());
            if (processId != null && !"".equals(processId)) { //$NON-NLS-1$
                ProcessItem processItem = ItemCacheManager.getProcessItem(processId);
                if (processItem != null) {
                    // TODO should use a fake Process here to replace the real Process.
                    // achen modify to fix 0006107
                    IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
                    Process loadedProcess = (Process) service.getProcessFromItem(processItem);
                    // Process loadedProcess = new Process(processItem.getProperty());
                    // loadedProcess.loadXmlFile();
                    return loadedProcess;
                }
            }
        }
        return null;
    }

    public static List<INode> getTRunjobs(IProcess process) {
        List<INode> matchingNodes = new ArrayList<INode>();
        for (INode node : (List<INode>) (process.getGraphicalNodes())) {
            if (DesignerUtilities.isTRunJobComponent(node)) {
                matchingNodes.add(node);
            }
        }
        return matchingNodes;
    }

    /**
     * DOC bqian Comment method "findProcessFromEditors".
     * 
     * @param jobName
     * @param jobVersion
     */
    public static IProcess2 findProcessFromEditors(final String jobId, final String jobVersion) {
        final IProcess2[] process = new IProcess2[1];

        Display.getDefault().syncExec(new Runnable() {

            public void run() {
                IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .getEditorReferences();
                for (IEditorReference editorReference : editors) {
                    IEditorPart editor = editorReference.getEditor(false);
                    IEditorInput input = editor.getEditorInput();
                    if (input instanceof JobEditorInput) {
                        JobEditorInput rInput = (JobEditorInput) input;
                        IProcess2 p = rInput.getLoadedProcess();
                        if (p != null && p.getId().equals(jobId) && p.getVersion().equals(jobVersion)) {
                            process[0] = p;
                            break;
                        }
                    }
                }
            }
        });

        return process[0];
    }

    public static String getParameterVar(EParameterName param) {
        if (param == null) {
            return null;
        }
        return getParameterVar(param.getName());
    }

    public static String getParameterVar(String paramName) {
        if (paramName == null) {
            paramName = ""; //$NON-NLS-1$
        }
        String underLine = "__";//$NON-NLS-1$
        return underLine + paramName + underLine;
    }

    public static void setSchemaDB(IElementParameter schemaDB, Object value) {
        if (schemaDB != null) {
            if (value instanceof String) {
                if (JobSettingsConstants.ORACLE_OUTPUT_SID_ALIAS.equals((String) value)
                        || JobSettingsConstants.ORACLE_OUTPUT_SN_ALIAS.equals((String) value)
                        || JobSettingsConstants.ORACLE_INOUT_SN_ALIAS.equals((String) value)
                        || JobSettingsConstants.ORACLE_INPUT_SID_ALIAS.equals((String) value)) {
                    schemaDB.setRequired(true);
                } else {
                    schemaDB.setRequired(false);
                }
            }
        }

    }

    public static String getRepositoryAliasName(ConnectionItem connectionItem) {
        ERepositoryObjectType repositoryObjectType = ERepositoryObjectType.getItemType(connectionItem);
        String aliasName = repositoryObjectType.getAlias();
        Connection connection = connectionItem.getConnection();
        if (connection instanceof DatabaseConnection) {
            String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
            aliasName += " (" + currentDbType + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return aliasName;
    }
}
