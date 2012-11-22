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

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.CommonsPlugin;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.repository.IRepositoryPrefConstants;
import org.talend.core.model.utils.DesignerColorUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;
import org.talend.repository.ProjectManager;

/**
 * Preference Initializer for the designer.
 * 
 * $Id: PreferenceInitializer.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    public static final String DEFAULT_LOGS_FILE_NAME = "logs_file.txt"; //$NON-NLS-1$

    public static final String DEFAULT_STATS_FILE_NAME = "stats_file.txt"; //$NON-NLS-1$

    public static final String DEFAULT_METER_FILE_NAME = "meter_file.txt"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();

        String logPath = Platform.getLogFileLocation().toOSString();
        int lastIndex = logPath.lastIndexOf(File.separatorChar);
        logPath = TalendTextUtils.addQuotes(logPath.substring(0, lastIndex));

        store.setDefault(TalendDesignerPrefConstants.DEFAULT_LABEL, "__UNIQUE_NAME__"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.DEFAULT_HINT, "<b>__UNIQUE_NAME__</b><br>__COMMENT__"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.DEFAULT_CONNECTION_FORMAT, "row"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.DEFAULT_HINT_USED, false);
        store.setDefault(TalendDesignerPrefConstants.DEFAULT_DISPLAY, false);
        store.setDefault(TalendDesignerPrefConstants.USE_REPOSITORY_NAME, true);

        store.setDefault(TalendEditorPaletteFactory.PALETTE_STATE, FlyoutPaletteComposite.STATE_PINNED_OPEN);
        store.setDefault(TalendDesignerPrefConstants.COMP_DEFAULT_FILE_DIR, Platform.getLocation().toPortableString());
        // MOD by zshen for TDQ_INSTALL_DIR bug 17622
        store.setDefault(TalendDesignerPrefConstants.PRODUCT_ROOT_DIR, Platform.getLocation().removeLastSegments(1)
                .toPortableString());
        // see feature 7758 add project dir to COMP_DEFAULT_PROJECT_DIR.
        if (ProjectManager.getInstance().getCurrentProject() != null) {
            store.setDefault(TalendDesignerPrefConstants.COMP_DEFAULT_PROJECT_DIR, Platform.getLocation().toPortableString()
                    + "/" + ProjectManager.getInstance().getCurrentProject().getLabel());
        }
        store.setDefault(TalendDesignerPrefConstants.PROPERTY_CODE_CHECK, false);
        store.setDefault(TalendDesignerPrefConstants.LARGE_ICONS_SIZE, "24"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.SCHEMA_OPTIONS, "none"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.VIEW_OPTIONS, "default"); //$NON-NLS-1$
        store.setDefault(TalendDesignerPrefConstants.DISPLAY_SUBJOBS, true);
        store.setDefault(TalendDesignerPrefConstants.GENERATE_CODE_WHEN_OPEN_JOB, false);
        // When updating jobs or joblets, check only the last version, and checked by default
        store.setDefault(TalendDesignerPrefConstants.CHECK_ONLY_LAST_VERSION, true);

        // defaults for the stats preferences for java
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.CATCH_REALTIME_STATS.getName(), false); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.CATCH_RUNTIME_ERRORS.getName(), true); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.CATCH_USER_ERRORS.getName(), true); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.CATCH_USER_WARNING.getName(), true); //$NON-NLS-1$

        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.FILE_PATH.getName(), logPath); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.FILENAME_LOGS.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_LOGS_FILE_NAME));
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.FILENAME_STATS.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_STATS_FILE_NAME));
        store.setDefault(ECodeLanguage.JAVA.toString() + "_" + EParameterName.FILENAME_METTER.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_METER_FILE_NAME));

        // defaults for the stats preferences for perl
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.PROPERTY_TYPE.getName(), EmfComponent.BUILTIN); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.CATCH_REALTIME_STATS.getName(), false); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.CATCH_RUNTIME_ERRORS.getName(), true); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.CATCH_USER_ERRORS.getName(), true); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.CATCH_USER_WARNING.getName(), true); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.FILE_PATH.getName(), logPath); //$NON-NLS-1$
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.FILENAME_LOGS.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_LOGS_FILE_NAME));
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.FILENAME_STATS.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_STATS_FILE_NAME));
        store.setDefault(ECodeLanguage.PERL.toString() + "_" + EParameterName.FILENAME_METTER.getName(), TalendTextUtils //$NON-NLS-1$
                .addQuotes(DEFAULT_METER_FILE_NAME));
        // repository refresh
        store.setDefault(IRepositoryPrefConstants.MANUALLY_REFRESH, false);
        store.setDefault(IRepositoryPrefConstants.CREATING_REFRESH, true);
        store.setDefault(IRepositoryPrefConstants.SAVING_REFRESH, true);
        store.setDefault(IRepositoryPrefConstants.DELETING_REFRESH, true);

        // db connection time out
        store.setDefault(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT_ACTIVED, true);
        store.setDefault(ITalendCorePrefConstants.DB_CONNECTION_TIMEOUT, 15);
        // store.setDefault(ITalendCorePrefConstants.ITEM_INDEX, false);

        // Add dependencies routines for job
        store.setDefault(ITalendCorePrefConstants.ADD_USER_ROUTINES, true);
        store.setDefault(ITalendCorePrefConstants.ADD_SYSTEM_ROUTINES, true);

        // have removed this function
        // store.setDefault(ITalendCorePrefConstants.ITEM_INDEX, false);

        store.setDefault(IRepositoryPrefConstants.USE_EXPORT_SAVE, false);
        if (!CommonsPlugin.isHeadless()) {
            Display display = Display.getCurrent();
            if (display == null) {
                display = Display.getDefault();
            }
            if (display != null) {
                display.syncExec(new Runnable() {

                    public void run() {
                        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
                        Font font = new Font(null, "courier", 10, SWT.NONE); //$NON-NLS-1$
                        PreferenceConverter.setDefault(store, TalendDesignerPrefConstants.MEMO_TEXT_FONT, font.getFontData());
                        PreferenceConverter.setDefault(store, TalendDesignerPrefConstants.CONSOLT_TEXT_FONT, font.getFontData());
                    }
                });
            }
            store.setDefault(TalendDesignerPrefConstants.EDITOR_ANTIALIASING, false);
            // store.setDefault(TalendDesignerPrefConstants.EDITOR_INTERPOLATION, false);

            // designer color
            DesignerColorUtils.initPreferenceDefault(store);
        }

    }
}
