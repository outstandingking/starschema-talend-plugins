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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.repository.ui.views.IRepositoryView;

public class PerformancePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CheckBoxFieldEditor dbConnTimeoutActive;

    private IntegerFieldEditor dbConnTimeout;

    public PerformancePreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        IBrandingService breaningService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        if (breaningService.isPoweredOnlyCamel()) {
            addField(new BooleanFieldEditor(ITalendCorePrefConstants.DEACTIVE_REPOSITORY_UPDATE,
                    Messages.getString("PerformancePreferencePage.display.deactiveRepositoryUpdate"),//$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK,
                    Messages.getString("PerformancePreferencePage.propertyCodeCheck"), getFieldEditorParent())); //$NON-NLS-1$

            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB,
                    Messages.getString("PerformancePreferencePage.generateCode"),//$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION,
                    Messages.getString("PerformancePreferencePage.checkVersion"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT_VARIABLE,
                    Messages.getString("PerformancePreferencePage.addOrDeleteVariable"),//$NON-NLS-1$
                    getFieldEditorParent()));

            dbConnTimeoutActive = new CheckBoxFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED,
                    Messages.getString("PerformancePreferencePage.ActivedTimeoutSetting"), getFieldEditorParent()); //$NON-NLS-1$
            dbConnTimeoutActive.getButton().addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    checkDBTimeout();
                }
            });
            dbConnTimeout = new IntegerFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT,
                    Messages.getString("PerformancePreferencePage.ConnectionTimeout"), //$NON-NLS-1$
                    getFieldEditorParent());
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            textControl.setToolTipText(Messages.getString("PerformancePreferencePage.ConnectionTimeoutTip")); //$NON-NLS-1$
            dbConnTimeout.setValidRange(0, Short.MAX_VALUE);
            textControl.setEnabled(getPreferenceStore().getBoolean(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED));

            addField(dbConnTimeoutActive);
            addField(dbConnTimeout);
        } else {
            addField(new BooleanFieldEditor(ITalendCorePrefConstants.DEACTIVE_REPOSITORY_UPDATE,
                    Messages.getString("PerformancePreferencePage.display.deactiveRepositoryUpdate"),//$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK,
                    Messages.getString("PerformancePreferencePage.propertyCodeCheck"), getFieldEditorParent())); //$NON-NLS-1$

            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB,
                    Messages.getString("PerformancePreferencePage.generateCode"),//$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION,
                    Messages.getString("PerformancePreferencePage.checkVersion"), //$NON-NLS-1$
                    getFieldEditorParent()));
            addField(new BooleanFieldEditor(TalendDesignerPrefConstants.PROPAGATE_CONTEXT_VARIABLE,
                    Messages.getString("PerformancePreferencePage.addOrDeleteVariable"),//$NON-NLS-1$
                    getFieldEditorParent()));

            dbConnTimeoutActive = new CheckBoxFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED,
                    Messages.getString("PerformancePreferencePage.ActivedTimeoutSetting"), getFieldEditorParent()); //$NON-NLS-1$
            dbConnTimeoutActive.getButton().addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    checkDBTimeout();
                }
            });
            dbConnTimeout = new IntegerFieldEditor(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT,
                    Messages.getString("PerformancePreferencePage.ConnectionTimeout"), //$NON-NLS-1$
                    getFieldEditorParent());
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            textControl.setToolTipText(Messages.getString("PerformancePreferencePage.ConnectionTimeoutTip")); //$NON-NLS-1$
            dbConnTimeout.setValidRange(0, Short.MAX_VALUE);
            textControl.setEnabled(getPreferenceStore().getBoolean(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED));

            addField(dbConnTimeoutActive);
            addField(dbConnTimeout);

            addField(new BooleanFieldEditor(ITalendCorePrefConstants.ADD_USER_ROUTINES,
                    Messages.getString("PerformancePreferencePage.addAllUserRoutines"),//$NON-NLS-1$
                    getFieldEditorParent()));
            // TDI-8323:remove this one,we do not need this since we always add all system routines for new job
            // addField(new BooleanFieldEditor(ITalendCorePrefConstants.ADD_SYSTEM_ROUTINES, Messages
            //                .getString("PerformancePreferencePage.addAllSystemRoutines"),//$NON-NLS-1$
            // getFieldEditorParent()));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */

    public void init(IWorkbench workbench) {
    }

    @Override
    public void dispose() {
        super.dispose();
        IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
        if (view != null) {
            view.refresh();
        }
    }

    private void checkDBTimeout() {
        if (dbConnTimeout != null) {
            Text textControl = dbConnTimeout.getTextControl(getFieldEditorParent());
            if (textControl != null && dbConnTimeoutActive != null) {
                textControl.setEnabled(dbConnTimeoutActive.getBooleanValue());
            }
        }
    }

    @Override
    protected void performApply() {
        super.performApply();
    }

    @Override
    public boolean performOk() {
        final boolean toReturn = super.performOk();
        return toReturn;
    }

}
