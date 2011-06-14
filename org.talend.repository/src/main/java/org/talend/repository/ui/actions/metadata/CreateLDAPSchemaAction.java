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
import org.talend.core.model.properties.LDAPSchemaConnectionItem;
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
import org.talend.repository.ui.wizards.metadata.connection.ldap.LDAPSchemaWizard;

/**
 * Action used to create a new "LDAP schema" metadata.<br/>
 * 
 * $Id: CreateGenericSchemaAction.java 3351 2007-09-03 12:14:00Z ftang $
 * 
 */
public class CreateLDAPSchemaAction extends AbstractCreateAction {

    private String editLabel; //$NON-NLS-1$

    private String createLabel;

    private String openLabel;

    protected static final int WIZARD_WIDTH = 800;

    protected static final int WIZARD_HEIGHT = 475;

    private boolean creation = false;

    private ImageDescriptor defaultImage, createImage;

    private ERepositoryObjectType currentNodeType;

    public CreateLDAPSchemaAction() {
        super();

        // TODO: should to fix the I18N issue.
        createLabel = Messages.getString("CreateLDAPSchemaAction.createLDAP"); //$NON-NLS-1$
        editLabel = Messages.getString("CreateLDAPSchemaAction.editLDAP"); //$NON-NLS-1$
        openLabel = Messages.getString("CreateLDAPSchemaAction.openLDAP"); //$NON-NLS-1$

        // TODO: should change to another icon.
        defaultImage = ImageProvider.getImageDesc(ECoreImage.METADATA_LDAP_SCHEMA_ICON);
        createImage = OverlayImageProvider.getImageWithNew(ImageProvider.getImage(ECoreImage.METADATA_LDAP_SCHEMA_ICON));

        setText(createLabel);
        setToolTipText(createLabel);
        setImageDescriptor(defaultImage);

        currentNodeType = ERepositoryObjectType.METADATA_LDAP_SCHEMA;
    }

    public CreateLDAPSchemaAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    protected void doRun() {
        // RepositoryNode metadataNode = getViewPart().getRoot().getChildren().get(6);
        // RepositoryNode fileLDAPSchemaNode = metadataNode.getChildren().get(6);
        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }

        if (isToolbar()) {
            if (repositoryNode != null && repositoryNode.getContentType() != currentNodeType) {
                repositoryNode = null;
            }
            if (repositoryNode == null) {
                repositoryNode = getRepositoryNodeForDefault(currentNodeType);
            }
        }

        WizardDialog wizardDialog;
        if (isToolbar()) {
            init(repositoryNode);
            LDAPSchemaWizard ldafSchemaWizard = new LDAPSchemaWizard(PlatformUI.getWorkbench(), creation, repositoryNode,
                    getExistingNames(), false);
            ldafSchemaWizard.setToolbar(true);
            wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), ldafSchemaWizard);
        } else {
            wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), new LDAPSchemaWizard(
                    PlatformUI.getWorkbench(), creation, repositoryNode, getExistingNames(), false));
        }

        if (!creation) {
            RepositoryManager.refreshSavedNode(repositoryNode);
        }

        wizardDialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
        wizardDialog.create();

        wizardDialog.open();
        RepositoryManager.refreshCreatedNode(ERepositoryObjectType.METADATA_LDAP_SCHEMA);

    }

    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!currentNodeType.equals(nodeType)) {
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
            this.setText(createLabel);
            collectChildNames(node);
            this.setImageDescriptor(createImage);
            creation = true;
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject())) {
                this.setText(editLabel);
                this.setImageDescriptor(defaultImage);
                collectSiblingNames(node);
            } else {
                this.setText(openLabel);
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
        return LDAPSchemaConnectionItem.class;
    }

}
