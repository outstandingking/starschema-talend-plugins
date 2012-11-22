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
package org.talend.designer.core.ui.action;

import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.core.ui.images.OverlayImageProvider;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.ProcessEditorInput;
import org.talend.designer.core.ui.wizards.NewProcessWizard;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.ProjectRepositoryNode;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.AContextualAction;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: CreateProcess.java 83949 2012-05-21 10:15:37Z wchen $
 * 
 */
public class CreateProcess extends AContextualAction implements IIntroAction {

    private static final String CREATE_LABEL = Messages.getString("CreateProcess.createJob"); //$NON-NLS-1$

    private static final String PERSPECTIVE_DI_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$

    public CreateProcess() {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);

        Image folderImg = ImageProvider.getImage(ECoreImage.PROCESS_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    public CreateProcess(boolean isToolbar) {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        setToolbar(isToolbar);
        Image folderImg = ImageProvider.getImage(ECoreImage.PROCESS_ICON);
        this.setImageDescriptor(OverlayImageProvider.getImageWithNew(folderImg));
    }

    public IRepositoryNode getProcessNode() {
        return ProjectRepositoryNode.getInstance().getRootRepositoryNode(ERepositoryObjectType.PROCESS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    protected void doRun() {
        IRepositoryNode node = null;
        NewProcessWizard processWizard = null;
        if (isToolbar()) {
            processWizard = new NewProcessWizard(null);
        } else {
            ISelection selection = getSelection();
            if (selection == null) {
                return;
            }
            Object obj = ((IStructuredSelection) selection).getFirstElement();
            node = (IRepositoryNode) obj;
            ItemCacheManager.clearCache();

            IRepositoryService service = DesignerPlugin.getDefault().getRepositoryService();
            IPath path = service.getRepositoryPath((RepositoryNode) node);
            if (RepositoryConstants.isSystemFolder(path.toString())) {
                // Not allowed to create in system folder.
                return;
            }

            processWizard = new NewProcessWizard(path);
        }

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), processWizard);
        if (dlg.open() == Window.OK) {
            if (processWizard.getProcess() == null) {
                return;
            }

            ProcessEditorInput fileEditorInput;
            try {
                // Set readonly to false since created job will always be editable.
                fileEditorInput = new ProcessEditorInput(processWizard.getProcess(), false, true, false);

                IRepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(fileEditorInput.getItem()
                        .getProperty().getId(), false);
                fileEditorInput.setRepositoryNode(repositoryNode);

                IWorkbenchPage page = getActivePage();
                page.openEditor(fileEditorInput, MultiPageTalendEditor.ID, true);
                // // use project setting true
                // ProjectSettingManager.defaultUseProjectSetting(fileEditorInput.getLoadedProcess());
            } catch (PartInitException e) {
                // TODO Auto-generated catch block
                // e.printStackTrace();
                ExceptionHandler.process(e);
            } catch (PersistenceException e) {
                MessageBoxExceptionHandler.process(e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        boolean canWork = !selection.isEmpty() && selection.size() == 1;
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }
        if (canWork) {
            Object o = selection.getFirstElement();
            RepositoryNode node = (RepositoryNode) o;
            switch (node.getType()) {
            case SIMPLE_FOLDER:
            case SYSTEM_FOLDER:
                ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (nodeType != ERepositoryObjectType.PROCESS) {
                    canWork = false;
                }
                if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                    canWork = false;
                }
                break;
            default:
                canWork = false;
            }
            if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                canWork = false;
            }
        }
        setEnabled(canWork);
    }

    /*
     * only use for creating a process in the intro by url
     */
    public void run(IIntroSite site, Properties params) {
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        if (factory.isUserReadOnlyOnCurrentProject()) {
            MessageDialog.openWarning(null, "User Authority", "Can't create Job! Current user is read-only on this project!");
        } else {
            PlatformUI.getWorkbench().getIntroManager().closeIntro(PlatformUI.getWorkbench().getIntroManager().getIntro());
            selectRootObject(params);
            doRun();
        }
    }

    private void selectRootObject(Properties params) {
        IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (null == workbenchWindow) {
            return;
        }
        IWorkbenchPage workbenchPage = workbenchWindow.getActivePage();
        if (null == workbenchPage) {
            return;
        }

        IPerspectiveDescriptor currentPerspective = workbenchPage.getPerspective();
        if (!IBrandingConfiguration.PERSPECTIVE_DI_ID.equals(currentPerspective.getId())) {
            // show di perspective
            try {
                workbenchWindow.getWorkbench().showPerspective(IBrandingConfiguration.PERSPECTIVE_DI_ID, workbenchWindow);
                workbenchPage = workbenchWindow.getActivePage();
            } catch (WorkbenchException e) {
                ExceptionHandler.process(e);
                return;
            }
        }

        IRepositoryView view = RepositoryManagerHelper.getRepositoryView();
        if (view != null) {
            Object type = params.get("type");
            if (ERepositoryObjectType.PROCESS.name().equals(type)) {
                IRepositoryNode processNode = ((ProjectRepositoryNode) view.getRoot())
                        .getRootRepositoryNode(ERepositoryObjectType.PROCESS);
                if (processNode != null) {
                    setWorkbenchPart(view);
                    final StructuredViewer viewer = view.getViewer();
                    if (viewer instanceof TreeViewer) {
                        ((TreeViewer) viewer).expandToLevel(processNode, 1);
                    }
                    viewer.setSelection(new StructuredSelection(processNode));
                }
            }
        }
    }
}
