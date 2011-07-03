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
package org.talend.core.model.process.node;

import java.util.List;

/**
 * cli class global comment. Detailled comment
 */
public interface IExternalMapTable {

    public String getName();

    public List<? extends IExternalMapEntry> returnTableEntries();
}
