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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.service.IDesignerPerlService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.StandAloneTalendJavaEditor;
import org.talend.designer.core.ui.wizards.SaveAsRoutineWizard;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.ui.actions.routines.RoutineEditorInput;

/**
 * DOC xtan class global comment. <br/>
 */
public class SaveAsRoutineAction extends Action {

    private final EditorPart editorPart;

    public SaveAsRoutineAction(EditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public void run() {
        SaveAsRoutineWizard processWizard = new SaveAsRoutineWizard(editorPart);

        WizardDialog dlg = new WizardDialog(Display.getCurrent().getActiveShell(), processWizard);
        if (dlg.open() == Window.OK) {

            try {

                RoutineItem routineItem = processWizard.getRoutineItem();

                // get the IFile
                ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                        ICodeGeneratorService.class);
                ITalendSynchronizer routineSynchronizer = null;
                switch (LanguageManager.getCurrentLanguage()) {
                case JAVA:
                    routineSynchronizer = service.createJavaRoutineSynchronizer();
                    break;
                case PERL:
                    routineSynchronizer = service.createPerlRoutineSynchronizer();
                    break;
                default:
                }
                IFile file = routineSynchronizer.getFile(routineItem);

                // Set readonly to false since created job will always be editable.
                RoutineEditorInput routineEditorInput = new RoutineEditorInput(file, routineItem);

                IWorkbenchPage page = getActivePage();

                IRepositoryNode repositoryNode = RepositoryNodeUtilities.getRepositoryNode(routineEditorInput.getItem()
                        .getProperty().getId(), false);
                routineEditorInput.setRepositoryNode(repositoryNode);

                // here really do the normal save as function
                IDocumentProvider provider = ((AbstractTextEditor) this.editorPart).getDocumentProvider();
                provider.aboutToChange(routineEditorInput);
                provider.saveDocument(null, routineEditorInput, provider.getDocument(this.editorPart.getEditorInput()), true);
                provider.changed(routineEditorInput);

                // copy back from the *.java file to *.item file.
                // @see:StandAloneTalendJavaEditor.doSave(IProgressMonitor monitor)
                ByteArray byteArray = routineItem.getContent();
                byteArray.setInnerContentFromFile(routineEditorInput.getFile());
                IProxyRepositoryFactory repFactory = DesignerPlugin.getDefault().getRepositoryService()
                        .getProxyRepositoryFactory();
                repFactory.save(routineItem);

                // close the old editor
                page.closeEditor(this.editorPart, false);

                // open the new editor, because at the same time, there will update the jobSetting/componentSetting view
                switch (LanguageManager.getCurrentLanguage()) {
                case JAVA:
                    page.openEditor(routineEditorInput, StandAloneTalendJavaEditor.ID, true);
                    break;
                case PERL:
                    if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerPerlService.class)) {
                        IDesignerPerlService perlService = (IDesignerPerlService) GlobalServiceRegister.getDefault().getService(
                                IDesignerPerlService.class);
                        page.openEditor(routineEditorInput, perlService.getStandAloneTalendPerlEditorID(), true);
                    }
                    break;
                default:
                }

            } catch (Exception e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error",
                        "Routine could not be saved" + " : " + e.getMessage());
                ExceptionHandler.process(e);
            }
        }
    }

    private IWorkbenchPage getActivePage() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

}
