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
package org.talend.cwm.i18n;

import java.util.ResourceBundle;

/**
 * 
 * DOC Administrator class global comment. Detailled comment
 */
public class Messages extends MessagesCore {

    private static final String BUNDLE_NAME = "org.talend.cwm.i18n.messages"; //$NON-NLS-1$

    private static final String PLUGIN_ID = "org.talend.model"; //$NON-NLS-1$

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Returns the i18n formatted message for <i>key</i> in the class bundle.
     * 
     * @param key - the key for the desired string
     * @return the string for the given key in the class resource bundle
     * @see MessagesCore#getString(String, ResourceBundle)
     */
    public static String getString(final String key) {
        return getString(key, PLUGIN_ID, resourceBundle);
    }

    /**
     * Returns the i18n formatted message for <i>key</i> and <i>args</i> in the specified bundle.
     * 
     * @param key - the key for the desired string
     * @param args - arg to include in the string
     * @return the string for the given key in the given resource bundle
     * @see MessagesCore#getString(String, ResourceBundle, Object[])
     */
    public static String getString(final String key, final Object... args) {
        return getString(key, PLUGIN_ID, resourceBundle, args);
    }
}
