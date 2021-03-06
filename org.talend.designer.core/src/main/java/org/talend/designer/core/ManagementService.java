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
package org.talend.designer.core;

import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.core.IManagementService;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class ManagementService implements IManagementService {

    public IPreferenceStore getDesignerCorePreferenceStore() {
        return DesignerPlugin.getDefault().getPreferenceStore();
    }

}
