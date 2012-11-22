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
package org.talend.repository.ui.actions.importexport;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 */
public class ExportItemWizard extends Wizard implements IImportWizard {

    private ExportItemWizardPage mainPage;

    private IStructuredSelection selection;

    public ExportItemWizard() {
        super();
    }

    @Override
    public void addPages() {
        super.addPages();
        mainPage = new ExportItemWizardPage(getWindowTitle(), selection);
        addPage(mainPage);
        AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
        IDialogSettings workbenchSettings = plugin.getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection("ExportItemWizard"); //$NON-NLS-1$
        if (section == null) {
            section = workbenchSettings.addNewSection("ExportItemWizard"); //$NON-NLS-1$
        }
        setDialogSettings(section);

    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public boolean performCancel() {
        return mainPage.performCancel();
    }

    @Override
    public boolean performFinish() {
        return mainPage.performFinish();
    }

}
