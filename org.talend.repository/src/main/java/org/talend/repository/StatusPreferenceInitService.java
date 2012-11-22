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
package org.talend.repository;

import org.talend.core.IStatusPreferenceInitService;
import org.talend.repository.preference.StatusPreferenceInitializer;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public class StatusPreferenceInitService implements IStatusPreferenceInitService {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.IStatusPreferenceInitService#initStatusPreference()
     */
    public void initStatusPreference() {
        // TODO Auto-generated method stub
        new StatusPreferenceInitializer().initializeDefaultPreferences();
    }

}
