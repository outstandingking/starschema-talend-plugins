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
package org.talend.repository.ui.actions.metadata;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumnRepositoryObject;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.SubscriberTable;
import org.talend.core.model.properties.BRMSConnectionItem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.properties.HL7ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.SalesforceModuleRepositoryObject;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;

/**
 * Action used to create table on metadata.<br/>
 * 
 * $Id: CreateTableAction.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class CreateSalesforceModulesAction extends AbstractCreateTableAction {

    protected static Logger log = Logger.getLogger(CreateConnectionAction.class);

    protected static final String PID = RepositoryPlugin.PLUGIN_ID;

    protected static final String CREATE_LABEL = Messages.getString("CreateSalesforceModulesAction.retriveModules"); //$NON-NLS-1$

    protected static final String EDIT_LABEL = Messages.getString("CreateSalesforceModulesAction.retriveModules"); //$NON-NLS-1$

    private RepositoryNode node;

    public CreateSalesforceModulesAction(boolean isToolBar) {
        super();
        setToolbar(isToolBar);
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON));
    }

    public CreateSalesforceModulesAction() {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.METADATA_TABLE_ICON));
    }

    /**
     * yzhang CreateTableAction constructor comment.
     * 
     * @param node
     */
    public CreateSalesforceModulesAction(RepositoryNode node) {
        this();
        this.node = node;
    }

    protected void doRun() {
        RepositoryNode metadataNode = null;
        if (node == null && repositoryNode != null) {
            node = repositoryNode;
        }
        if (node == null) {
            // RepositoryNode metadataNode = getViewPart().getRoot().getChildren().get(6);
            metadataNode = getMetadataNode(getCurrentRepositoryNode());
            IStructuredSelection selection = (IStructuredSelection) getSelection();
            node = (RepositoryNode) selection.getFirstElement();
            // Force focus to the repositoryView and open Metadata and DbConnection nodes
            getViewPart().setFocus();
            getViewPart().expand(metadataNode, true);
        } else {
            metadataNode = getMetadataNode(node);
        }

        // Init the content of the Wizard
        init(node);

        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)
                || ERepositoryObjectType.METADATA_CON_COLUMN.equals(nodeType)) {
            final IRepositoryViewObject object = node.getObject();
            if (object instanceof MetadataTableRepositoryObject) {
                MetadataTable table = ((MetadataTableRepositoryObject) object).getTable();
                if (table instanceof SubscriberTable) {
                    this.node = null;
                    return;
                }
            } else if (object instanceof MetadataColumnRepositoryObject) {
                MetadataTable table = ((MetadataColumnRepositoryObject) object).getTdColumn().getTable();
                this.node = node.getParent().getParent();
                if (table instanceof SubscriberTable) {
                    this.node = null;
                    return;
                }
            }
            ConnectionItem connectionItem = (ConnectionItem) object.getProperty().getItem();
            nodeType = ERepositoryObjectType.getItemType(connectionItem);

        }
        createSalesforceModuleWizard(node, false);

        this.node = null;
    }

    @Override
    public Class getClassForDoubleClick() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.repository.ui.actions.metadata.AbstractCreateAction#init(org.talend.repository.model.RepositoryNode)
     */
    @Override
    protected void init(RepositoryNode node) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
            setEnabled(false);
        } else {
            if (ENodeType.REPOSITORY_ELEMENT.equals(node.getType())) {
                if (node.getObject().getRepositoryStatus() == ERepositoryStatus.DELETED
                        || node.getObject().getRepositoryStatus() == ERepositoryStatus.LOCK_BY_OTHER) {
                    setEnabled(false);
                    return;
                }

                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (ERepositoryObjectType.METADATA_CON_TABLE.equals(nodeType)
                        || ERepositoryObjectType.METADATA_CON_COLUMN.equals(nodeType)) {
                    setEnabled(false);
                    return;
                }
                if (ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.equals(nodeType)) {
                    setText(CREATE_LABEL);
                    collectChildNames(node);
                    if (isLastVersion(node)) {
                        setEnabled(true);
                    }
                    return;
                }
                // if (ERepositoryObjectType.METADATA_CON_QUERY.equals(nodeType)) {
                // setEnabled(false);
                // }
            }
        }
    }
}
