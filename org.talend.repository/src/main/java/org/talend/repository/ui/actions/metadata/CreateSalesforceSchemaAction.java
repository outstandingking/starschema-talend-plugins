// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.model.properties.SalesforceSchemaConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryManager;
import org.talend.core.ui.images.ECoreImage;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.ui.wizards.metadata.connection.files.salesforce.SalesforceSchemaWizard;

/**
 * DOC yexiaowei class global comment. Detailled comment
 */

public class CreateSalesforceSchemaAction extends AbstractCreateAction {

    private static final String EDIT_LABEL = Messages.getString("CreateSalesforceSchemaAction.action.editTitle"); //$NON-NLS-1$

    private static final String CREATE_LABEL = Messages.getString("CreateSalesforceSchemaAction.action.createTitle"); //$NON-NLS-1$

    private static final String OPEN_LABEL = Messages.getString("CreateSalesforceSchemaAction.action.openTitle"); //$NON-NLS-1$

    protected static final int WIZARD_WIDTH = 800;

    protected static final int WIZARD_HEIGHT = 520;

    private boolean creation = false;

    ImageDescriptor defaultImage = ImageProvider.getImageDesc(ECoreImage.METADATA_SALESFORCE_SCHEMA_ICON);

    ImageDescriptor createImage = OverlayImageProvider.getImageWithNew(ImageProvider
            .getImage(ECoreImage.METADATA_SALESFORCE_SCHEMA_ICON));

    public CreateSalesforceSchemaAction() {
        super();

        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(defaultImage);

    }

    public CreateSalesforceSchemaAction(boolean isToolbar) {
        super();
        setToolbar(isToolbar);
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(defaultImage);
    }

    protected void doRun() {

        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }

        if (isToolbar()) {
            if (repositoryNode != null && repositoryNode.getContentType() != ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
                repositoryNode = null;
            }
            if (repositoryNode == null) {
                repositoryNode = getRepositoryNodeForDefault(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
            }
        }

        WizardDialog wizardDialog = null;
        if (isToolbar()) {
            init(repositoryNode);
            SalesforceSchemaWizard salesForceSchemaWizard = new SalesforceSchemaWizard(PlatformUI.getWorkbench(), creation,
                    repositoryNode, getExistingNames(), false);
            salesForceSchemaWizard.setToolbar(true);
            wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), salesForceSchemaWizard);// TODO send
        } else {
            wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), new SalesforceSchemaWizard(PlatformUI
                    .getWorkbench(), creation, repositoryNode, getExistingNames(), false));
        }

        if (!creation) {
            RepositoryManager.refreshSavedNode(repositoryNode);
        }

        wizardDialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
        wizardDialog.create();
        wizardDialog.open();
        RepositoryManager.refreshCreatedNode(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);

    }

    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA.equals(nodeType)) {
            return;
        }

        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            this.setText(CREATE_LABEL);
            collectChildNames(node);
            creation = true;
            this.setImageDescriptor(createImage);
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject())) {
                this.setText(EDIT_LABEL);
                this.setImageDescriptor(defaultImage);
                collectSiblingNames(node);
            } else {
                this.setText(OPEN_LABEL);
                this.setImageDescriptor(defaultImage);
            }
            collectSiblingNames(node);
            creation = false;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    public Class getClassForDoubleClick() {
        return SalesforceSchemaConnectionItem.class;
    }
}
