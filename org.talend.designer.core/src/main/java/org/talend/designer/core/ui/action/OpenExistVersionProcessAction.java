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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.ui.IUIRefresher;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.ui.wizards.OpenExistVersionProcessWizard;
import org.talend.repository.editor.JobEditorInput;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.EditPropertiesAction;

/**
 * DOC xye class global comment. Detailled comment
 */
public class OpenExistVersionProcessAction extends EditPropertiesAction {

    private static final String ACTION_LABEL = Messages.getString("OpenExistVersionProcess.open"); //$NON-NLS-1$

    public OpenExistVersionProcessAction() {
        super();

        this.setText(ACTION_LABEL);
        this.setToolTipText(ACTION_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_ICON));
    }

    @Override
    protected void doRun() {
        ISelection selection = getSelection();
        Object obj = ((IStructuredSelection) selection).getFirstElement();
        RepositoryNode node = (RepositoryNode) obj;

        IPath path = RepositoryNodeUtilities.getPath(node);
        String originalName = node.getObject().getLabel();

        RepositoryObject repositoryObj = new RepositoryObject(node.getObject().getProperty());
        repositoryObj.setRepositoryNode(node.getObject().getRepositoryNode());
        OpenExistVersionProcessWizard wizard = new OpenExistVersionProcessWizard(repositoryObj);
        PropertyManagerWizardDialog dialog = new PropertyManagerWizardDialog(Display.getCurrent().getActiveShell(), wizard);
        dialog.setPageSize(300, 250);
        dialog.setTitle(Messages.getString("OpenExistVersionProcess.open.dialog")); //$NON-NLS-1$
        if (dialog.open() == Dialog.OK) {
            refresh(node);
            // refresh the corresponding editor's name
            IEditorPart part = getCorrespondingEditor(node);
            if (part != null && part instanceof IUIRefresher) {
                ((IUIRefresher) part).refreshName();
            } else {
                processRoutineRenameOperation(originalName, node, path);
            }
        }
    }

    public class PropertyManagerWizardDialog extends WizardDialog {

        /**
         * DOC xye PropertyManagerWizardDialog constructor comment.
         * 
         * @param parentShell
         * @param newWizard
         */
        public PropertyManagerWizardDialog(Shell parentShell, IWizard newWizard) {
            super(parentShell, newWizard);
        }

        public Button getFinishButton() {
            return getButton(IDialogConstants.FINISH_ID);
        }

    }

    protected IEditorPart getCorrespondingEditor(RepositoryNode node) {
        IEditorReference[] eidtors = getActivePage().getEditorReferences();

        for (int i = 0; i < eidtors.length; i++) {
            try {
                IEditorInput input = eidtors[i].getEditorInput();
                if (!(input instanceof JobEditorInput)) {
                    continue;
                }

                JobEditorInput repositoryInput = (JobEditorInput) input;
                checkUnLoadedNodeForProcess(repositoryInput);
                if (repositoryInput.getItem().equals(node.getObject().getProperty().getItem())) {

                    IPath path = repositoryInput.getFile().getLocation();

                    return eidtors[i].getEditor(false);
                }
            } catch (PartInitException e) {
                continue;
            }
        }
        return null;
    }

    private void checkUnLoadedNodeForProcess(JobEditorInput fileEditorInput) {
        if (fileEditorInput == null || fileEditorInput.getLoadedProcess() == null) {
            return;
        }
        IProcess2 loadedProcess = fileEditorInput.getLoadedProcess();
        List<NodeType> unloadedNode = loadedProcess.getUnloadedNode();
        if (unloadedNode != null && !unloadedNode.isEmpty()) {

            String message = "Some Component are not loaded:\n";
            for (int i = 0; i < unloadedNode.size(); i++) {
                message = message + unloadedNode.get(i).getComponentName() + "\n";
            }
            if (!CommonsPlugin.isHeadless() && PlatformUI.isWorkbenchRunning()) {
                Display display = Display.getCurrent();
                if (display == null) {
                    display = Display.getDefault();
                }
                if (display != null) {
                    final Display tmpDis = display;
                    final String tmpMess = message;
                    display.syncExec(new Runnable() {

                        public void run() {
                            Shell shell = null;
                            final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            if (activeWorkbenchWindow != null) {
                                shell = activeWorkbenchWindow.getShell();
                            } else {
                                if (tmpDis != null) {
                                    shell = tmpDis.getActiveShell();
                                } else {
                                    shell = new Shell();
                                }
                            }
                            MessageDialog.openWarning(shell, "Warning", tmpMess);
                        }
                    });
                }
            }
        }
    }
}
