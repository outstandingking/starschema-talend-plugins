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
package org.talend.repository.plugin.integration;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.talend.core.CorePlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.views.IRepositoryView;
import org.talend.repository.ui.views.RepositoryView;

/**
 * DOC yzhang class global comment. Detailled comment
 */
public class BootTalendAction implements IWorkbenchWindowActionDelegate {

    public final static String TALEND_PERSPECTIVE_ID = "org.talend.rcp.perspective"; //$NON-NLS-1$

    public static final String LOGIN_COUNTER = "loginCounter"; //$NON-NLS-1$

    private IPreferenceStore store;

    /**
     * yzhang BootTalendAction constructor comment.
     */
    public BootTalendAction() {
        store = CorePlugin.getDefault().getPreferenceStore();
    }

    public void dispose() {

    }

    public void init(IWorkbenchWindow window) {

    }

    public void run(IAction action) {

        if (store.getInt(LOGIN_COUNTER) > 1) {
            PlatformUI.getWorkbench().restart();
        }

        int counter = store.getInt(LOGIN_COUNTER);
        store.setValue(LOGIN_COUNTER, ++counter);

        IPerspectiveDescriptor pDescriptor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .getPerspective();

        if (!pDescriptor.getId().equals(TALEND_PERSPECTIVE_ID)) {
            pDescriptor = getPerspective(TALEND_PERSPECTIVE_ID);
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().setPerspective(pDescriptor);
        }

        SwitchProjectAction switchAction = new SwitchProjectAction();
        switchAction.run();
        IRepositoryView view = RepositoryView.show();
        if (view != null) {
            view.refresh();
        }

        return;
    }

    public void selectionChanged(IAction action, ISelection selection) {

    }

    /**
     * DOC bqian Comment method "getPerspective".
     * 
     * @param id
     * @return
     */
    private IPerspectiveDescriptor getPerspective(String id) {
        IPerspectiveDescriptor[] pers = PlatformUI.getWorkbench().getPerspectiveRegistry().getPerspectives();
        for (IPerspectiveDescriptor perspectiveDescriptor : pers) {
            if (perspectiveDescriptor.getId().equals(id)) {
                return perspectiveDescriptor;
            }
        }
        throw new RuntimeException(Messages.getString("BootTalendAction.pluginNotLoaded")); //$NON-NLS-1$
    }

}
