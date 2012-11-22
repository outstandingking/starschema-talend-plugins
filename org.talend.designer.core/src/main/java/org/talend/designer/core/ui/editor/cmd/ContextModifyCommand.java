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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.model.utils.PerlResourcesHelper;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.views.contexts.ContextsView;
import org.talend.repository.ProjectManager;

/**
 * Command that will modify a context.
 * 
 * $Id: ContextModifyCommand.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ContextModifyCommand extends Command {

    private String JOB_CONTEXT_FOLDER = "contexts"; //$NON-NLS-1$

    IContext oldContext;

    IContext currentContext;

    IContext tmpContext;

    IContextManager contextManager;

    private IProcess2 process;

    public ContextModifyCommand(IProcess2 process, IContextManager contextManager, IContext oldContext, IContext newContext) {
        this.process = process;
        this.contextManager = contextManager;
        this.oldContext = oldContext;
        this.currentContext = newContext;
        setLabel(Messages.getString("ContextModifyCommand.label")); //$NON-NLS-1$
    }

    /**
     * qzhang Comment method "refreshContextView".
     */
    private void refreshContextView() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IViewPart view2 = page.findView(ContextsView.ID);
        if (view2 instanceof ContextsView) {
            ((ContextsView) view2).updateContextView(true, false);
        }
    }

    public void execute() {
        // check modified type
        for (IContextParameter param : oldContext.getContextParameterList()) {
            String paramName = param.getName();
            IContextParameter newParam = currentContext.getContextParameter(paramName);
            if (!newParam.getType().equals(param.getType())) {
                propagateType(contextManager, newParam);
            }
        }
        contextManager.fireContextsChangedEvent();
        refreshContextView();
        // Removes the attached context files
        try {
            removeContextFiles(process, oldContext);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private void propagateType(IContextManager contextManager, IContextParameter param) {
        for (IContext context : contextManager.getListContext()) {
            IContextParameter paramToModify = context.getContextParameter(param.getName());
            paramToModify.setType(param.getType());
        }
    }

    @Override
    public void redo() {
        tmpContext = currentContext.clone();
        currentContext.setName(oldContext.getName());
        currentContext.setConfirmationNeeded(oldContext.isConfirmationNeeded());

        List<IContextParameter> oldListContextParam = oldContext.getContextParameterList();
        List<IContextParameter> curListContextParam = currentContext.getContextParameterList();

        boolean found;
        IContextParameter oldParam = null;
        IContextParameter curParam;
        String name = null;
        for (int i = 0; i < oldListContextParam.size(); i++) {
            found = false;
            oldParam = oldListContextParam.get(i);
            for (int j = 0; j < curListContextParam.size() && !found; j++) {
                curParam = curListContextParam.get(j);
                name = oldParam.getName();
                if (curParam.getName().equals(name)) {
                    found = true;
                    curParam.setPromptNeeded(oldParam.isPromptNeeded());
                    curParam.setComment(oldParam.getComment());
                    curParam.setPrompt(oldParam.getPrompt());
                    curParam.setType(oldParam.getType());
                    curParam.setValue(oldParam.getValue());
                }
            }
        }

        oldContext = tmpContext;
        for (IContextParameter param : oldContext.getContextParameterList()) {
            String paramName = param.getName();
            IContextParameter newParam = currentContext.getContextParameter(paramName);
            if (!newParam.getType().equals(param.getType())) {
                propagateType(contextManager, newParam);
            }
        }

        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }

    @Override
    public void undo() {
        tmpContext = currentContext.clone();
        currentContext.setName(oldContext.getName());
        currentContext.setConfirmationNeeded(oldContext.isConfirmationNeeded());

        List<IContextParameter> oldListContextParam = oldContext.getContextParameterList();
        List<IContextParameter> curListContextParam = currentContext.getContextParameterList();

        boolean found;
        IContextParameter oldParam = null;
        IContextParameter curParam;
        String name = null;
        for (int i = 0; i < oldListContextParam.size(); i++) {
            found = false;
            oldParam = oldListContextParam.get(i);
            for (int j = 0; j < curListContextParam.size() && !found; j++) {
                curParam = curListContextParam.get(j);
                name = oldParam.getName();
                if (curParam.getName().equals(name)) {
                    found = true;
                    curParam.setPromptNeeded(oldParam.isPromptNeeded());
                    curParam.setComment(oldParam.getComment());
                    curParam.setPrompt(oldParam.getPrompt());
                    curParam.setType(oldParam.getType());
                    curParam.setValue(oldParam.getValue());
                }
            }
        }
        oldContext = tmpContext;
        for (IContextParameter param : oldContext.getContextParameterList()) {
            String paramName = param.getName();
            IContextParameter newParam = currentContext.getContextParameter(paramName);
            if (!newParam.getType().equals(param.getType())) {
                propagateType(contextManager, newParam);
            }
        }

        contextManager.fireContextsChangedEvent();
        refreshContextView();
    }

    private void removeContextFiles(IProcess2 process, IContext context) throws Exception {
        IResource resource = getContextResource(process, context);
        if (resource != null) {
            resource.delete(true, null);
        }
    }

    /**
     * Gets the context file resource according to the project type.
     * 
     * @param process
     * @param context
     * @return
     */
    private IResource getContextResource(IProcess2 process, IContext context) throws Exception {
        switch (ProjectManager.getInstance().getCurrentProject().getLanguage()) {
        case JAVA:
            IPath path = new Path(JavaUtils.JAVA_SRC_DIRECTORY).append(
                    JavaResourcesHelper.getProjectFolderName(process.getProperty().getItem())).append(
                    JavaResourcesHelper.getJobFolderName(process.getName(), process.getVersion())).append(JOB_CONTEXT_FOLDER)
                    .append(context.getName() + JavaUtils.JAVA_CONTEXT_EXTENSION);

            return JavaResourcesHelper.getSpecificResourceInJavaProject(path);
        case PERL:
            String rootProjectName = PerlResourcesHelper.getRootProjectName(process.getProperty().getItem());
            String contextFullName = PerlResourcesHelper.getContextFileName(rootProjectName, process.getName(), process
                    .getVersion(), context.getName());

            return PerlResourcesHelper.getSpecificResourceInPerlProject(new Path(contextFullName));
        }
        return null;
    }

}
