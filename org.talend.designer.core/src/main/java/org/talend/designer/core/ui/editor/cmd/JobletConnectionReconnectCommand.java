package org.talend.designer.core.ui.editor.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataTool;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.INodeConnector;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.process.ConnectionManager;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * Command that allows to change a connection to a new source or a new target when collapse joblet_component. <br/>
 * 
 * $Id: JobletConnectionReconnectCommand.java 51271 2010-11-15 08:40:42Z hwang $
 * 
 */
public class JobletConnectionReconnectCommand extends Command {

    private IConnection connection;

    private INode newSource;

    private INode newTarget;

    private INode oldSource;

    private INode oldTarget;

    private String connectorName;

    private EConnectionType newLineStyle;

    private IMetadataTable oldMetadataTable;

    private String oldSourceSchemaType, newSourceSchemaType;

    private String newTargetSchemaType;

    private EConnectionType oldLineStyle;

    private List<ChangeMetadataCommand> metadataChanges = new ArrayList<ChangeMetadataCommand>();

    /**
     * Initialisation of the command with the given connection. This will initialize the source and target before change
     * them.
     * 
     * @param connection
     */
    public JobletConnectionReconnectCommand(IConnection connection) {
        this.connection = connection;
        this.oldSource = connection.getSource();
        this.oldTarget = connection.getTarget();
        connectorName = connection.getConnectorName();
        oldLineStyle = connection.getLineStyle();
        newLineStyle = oldLineStyle;
        if (oldLineStyle.hasConnectionCategory(IConnectionCategory.DATA)) {
            if (connection.getMetadataTable() != null) {
                oldMetadataTable = connection.getMetadataTable().clone();
            }
        }
        oldSourceSchemaType = (String) oldSource.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
    }

    /**
     * Set a new source for the connection.
     * 
     * @param nodeSource
     */
    public void setNewSource(Node nodeSource) {
        setLabel(Messages.getString("ConnectionReconnectCommand.LabelSource")); //$NON-NLS-1$
        newSource = nodeSource;
        newTarget = null;
    }

    /**
     * Set a new target for the connection.
     * 
     * @param connectionTarget
     */
    public void setNewTarget(Node nodeTarget) {
        setLabel(Messages.getString("ConnectionReconnectCommand.LabelTarget")); //$NON-NLS-1$
        newSource = null;
        newTarget = nodeTarget;
    }

    public boolean canExecute() {
        boolean canExecute = false;
        if (!connection.isActivate()) {
            return false;
        }
        if (newSource != null) {
            // remove the connection for the check
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
            canExecute = checkSourceReconnection(); // check
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
        } else if (newTarget != null) {
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), false);
            canExecute = checkTargetReconnection();
            connection.setPropertyValue(EParameterName.ACTIVATE.getName(), true);
        }
        return canExecute;
    }

    /**
     * Check if a new connection on the selected source is allowed.
     * 
     * @return true / false
     */
    private boolean checkSourceReconnection() {
        if (!ConnectionManager.canConnectToSource(oldSource, newSource, oldTarget, oldLineStyle, connectorName,
                connection.getName())) {
            return false;
        }
        newLineStyle = ConnectionManager.getNewConnectionType();

        return true;
    }

    /**
     * Check if a new connection on the selected target is allowed.
     * 
     * @return true / false
     */
    private boolean checkTargetReconnection() {
        if (!ConnectionManager.canConnectToTarget(oldSource, oldTarget, newTarget, oldLineStyle, connectorName,
                connection.getName())) {
            return false;
        }
        newLineStyle = ConnectionManager.getNewConnectionType();

        return true;
    }

    private void setSchemaToNotBuiltInNode(INode oldNode, INode newNode, IMetadataTable newSchema) {
        if ((newNode.getMetadataList() != null) && newNode.getMetadataList().get(0).getListColumns().size() == 0) {
            // only override if there is no schema defined in the component
            if (oldMetadataTable == null) {
                return;
            }
            String sourceConnector = oldMetadataTable.getAttachedConnector();
            String baseConnector = oldNode.getConnectorFromName(sourceConnector).getBaseSchema();
            for (INodeConnector connector : newNode.getListConnector()) {
                if (connector.getBaseSchema().equals(baseConnector)) {
                    IMetadataTable meta = newNode.getMetadataFromConnector(connector.getName());
                    if (meta == null) {
                        continue;
                    }
                    if (newSchema != null) {
                        meta.setComment(newSchema.getComment());
                        MetadataTool.copyTable(newSchema, meta);
                    }

                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        metadataChanges.clear();
        if (newSource != null) {
            INodeConnector connector = oldSource.getConnectorFromName(connectorName);
            connector.setCurLinkNbOutput(connector.getCurLinkNbOutput() - 1);
            connector = newSource.getConnectorFromName(connectorName);
            connector.setCurLinkNbOutput(connector.getCurLinkNbOutput() + 1);
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                newSourceSchemaType = (String) newSource.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());
                boolean builtInNewSource = newSource.getConnectorFromName(connectorName).isMultiSchema();
                boolean builtInOldSource = oldSource.getConnectorFromName(connectorName).isMultiSchema();
                if ((!builtInNewSource) && (!builtInOldSource)) {
                    setSchemaToNotBuiltInNode(oldSource, newSource, oldMetadataTable);
                    connection.setMetaName(newSource.getUniqueName());
                } else {
                    if (!builtInNewSource) {
                        int num = 0;
                        for (int i = 0; i < oldSource.getMetadataList().size(); i++) {
                            IMetadataTable meta = oldSource.getMetadataList().get(i);
                            if (meta.getTableName().equals(oldMetadataTable.getTableName())) {
                                num = i;
                            }
                        }
                        oldSource.getMetadataList().remove(num);
                        setSchemaToNotBuiltInNode(oldSource, newSource, oldMetadataTable);
                        connection.setMetaName(newSource.getUniqueName());
                    }
                    if (!builtInOldSource) {
                        IMetadataTable meta = oldMetadataTable.clone();
                        meta.setTableName(connection.getUniqueName());
                        newSource.getMetadataList().add(meta);
                        connection.setMetaName(meta.getTableName());
                    }
                    if ((builtInOldSource) && (builtInNewSource)) {
                        int num = 0;
                        for (int i = 0; i < oldSource.getMetadataList().size(); i++) {
                            IMetadataTable meta = oldSource.getMetadataList().get(i);
                            if (meta.getTableName().equals(oldMetadataTable.getTableName())) {
                                num = i;
                            }
                        }
                        oldSource.getMetadataList().remove(num);
                        newSource.getMetadataList().add(oldMetadataTable);
                    }
                }
                if (newSourceSchemaType != null) {

                    newSource.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                }
                if (oldSourceSchemaType != null) {
                    oldSource.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);
                }
            } else {
                connection.setMetaName(newSource.getUniqueName());
            }
            connection.reconnect(newSource, oldTarget, newLineStyle);
            connection.updateName();

            if (newSourceSchemaType != null && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)) {
                IMetadataTable sourceMetadataTable = newSource.getMetadataFromConnector(connector.getName());
                // IMetadataTable targetMetadataTable = oldTarget.getMetadataFromConnector(connector.getName());
                if (oldMetadataTable != null && sourceMetadataTable != null) {
                    boolean sameFlag = oldMetadataTable.sameMetadataAs(sourceMetadataTable, IMetadataColumn.OPTIONS_NONE);
                    // For the auto propagate.
                    if (!sameFlag && oldTarget.getComponent().isSchemaAutoPropagated()
                            && (oldMetadataTable.getListColumns().isEmpty() || getPropagateDialog())) {
                        ChangeMetadataCommand changeMetadataCmd = new ChangeMetadataCommand(oldTarget, null, null,
                                sourceMetadataTable);
                        changeMetadataCmd.execute(true);
                        metadataChanges.add(changeMetadataCmd);
                    }
                }
            }
            // ((Process) newSource.getProcess()).checkStartNodes();
            // ((Process) newSource.getProcess()).checkProcess();
        } else if (newTarget != null) {
            newTargetSchemaType = (String) newTarget.getPropertyValue(EParameterName.SCHEMA_TYPE.getName());

            INodeConnector connector = oldTarget.getConnectorFromType(oldLineStyle);
            connector.setCurLinkNbInput(connector.getCurLinkNbInput() - 1);
            connector = newTarget.getConnectorFromType(newLineStyle);
            connector.setCurLinkNbInput(connector.getCurLinkNbInput() + 1);

            connection.reconnect(oldSource, newTarget, newLineStyle);
            connection.updateName();

            if (newTargetSchemaType != null) {
                if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.DATA)
                        && !connection.getLineStyle().equals(EConnectionType.FLOW_REF)) {
                    IMetadataTable targetOldMetadataTable = newTarget.getMetadataFromConnector(connector.getName());
                    if (oldMetadataTable != null && targetOldMetadataTable != null) {
                        boolean sameFlag = oldMetadataTable.sameMetadataAs(targetOldMetadataTable, IMetadataColumn.OPTIONS_NONE);
                        // For the auto propagate.
                        if (!sameFlag && newTarget.getComponent().isSchemaAutoPropagated()
                                && (targetOldMetadataTable.getListColumns().isEmpty() || getPropagateDialog())) {
                            ChangeMetadataCommand changeMetadataCmd = new ChangeMetadataCommand(newTarget, null, null,
                                    oldMetadataTable);
                            changeMetadataCmd.execute(true);
                            metadataChanges.add(changeMetadataCmd);
                        }
                    }
                }
                newTarget.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), EmfComponent.BUILTIN);

            }
            // ((Process) oldTarget.getProcess()).checkStartNodes();
            // ((Process) oldTarget.getProcess()).checkProcess();
        } else {
            throw new IllegalStateException("Should not happen"); //$NON-NLS-1$
        }
    }

    public void undo() {
        if (newSource != null) {
            INodeConnector connector = oldSource.getConnectorFromName(connectorName);
            connector.setCurLinkNbOutput(connector.getCurLinkNbOutput() + 1);
            connector = newSource.getConnectorFromName(connectorName);
            connector.setCurLinkNbOutput(connector.getCurLinkNbOutput() - 1);
            if (connection.getLineStyle().hasConnectionCategory(IConnectionCategory.FLOW)) {
                boolean builtInNewSource = newSource.getConnectorFromName(connectorName).isMultiSchema();
                boolean builtInOldSource = oldSource.getConnectorFromName(connectorName).isMultiSchema();
                if ((!builtInNewSource) && (!builtInOldSource)) {
                    setSchemaToNotBuiltInNode(newSource, oldSource, oldMetadataTable);
                    connection.setMetaName(oldSource.getUniqueName());
                } else {
                    if (!builtInNewSource) {
                        oldSource.getMetadataList().add(oldMetadataTable);
                        connection.setMetaName(oldMetadataTable.getTableName());
                    }
                    if (!builtInOldSource) {
                        int num = 0;
                        for (int i = 0; i < newSource.getMetadataList().size(); i++) {
                            IMetadataTable meta = newSource.getMetadataList().get(i);
                            if (meta.getTableName().equals(connection.getUniqueName())) {
                                num = i;
                            }
                        }
                        newSource.getMetadataList().remove(num);
                        setSchemaToNotBuiltInNode(newSource, oldSource, oldMetadataTable);
                        connection.setMetaName(oldSource.getUniqueName());
                    }
                    if ((builtInOldSource) && (builtInNewSource)) {
                        int num = 0;
                        for (int i = 0; i < newSource.getMetadataList().size(); i++) {
                            IMetadataTable meta = newSource.getMetadataList().get(i);
                            if (meta.getTableName().equals(oldMetadataTable.getTableName())) {
                                num = i;
                            }
                        }
                        newSource.getMetadataList().remove(num);
                        oldSource.getMetadataList().add(oldMetadataTable);
                    }
                }
                if (newSourceSchemaType != null) {
                    for (ChangeMetadataCommand cmd : metadataChanges) {
                        cmd.undo();
                    }
                    newSource.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), newSourceSchemaType);
                }
                if (oldSourceSchemaType != null) {
                    oldSource.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), oldSourceSchemaType);
                }
            } else {
                connection.setMetaName(oldSource.getUniqueName());
            }
        } else if (newTarget != null) {
            INodeConnector connector = oldTarget.getConnectorFromType(oldLineStyle);
            connector.setCurLinkNbInput(connector.getCurLinkNbInput() + 1);
            connector = newTarget.getConnectorFromType(newLineStyle);
            connector.setCurLinkNbInput(connector.getCurLinkNbInput() - 1);
            if (newTargetSchemaType != null) {
                for (ChangeMetadataCommand cmd : metadataChanges) {
                    cmd.undo();
                }
                newTarget.setPropertyValue(EParameterName.SCHEMA_TYPE.getName(), newTargetSchemaType);
            }
        }
        connection.reconnect(oldSource, oldTarget, oldLineStyle);
        connection.updateName();
        ((Process) oldSource.getProcess()).checkStartNodes();
        ((Process) oldSource.getProcess()).checkProcess();
    }

    private boolean getPropagateDialog() {
        return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                Messages.getString("ChangeMetadataCommand.messageDialog.propagate"), //$NON-NLS-1$
                Messages.getString("ChangeMetadataCommand.messageDialog.questionMessage")); //$NON-NLS-1$

    }
}
