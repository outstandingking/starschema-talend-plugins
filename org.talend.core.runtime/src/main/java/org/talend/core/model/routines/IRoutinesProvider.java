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
package org.talend.core.model.routines;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * bqian class global comment. Detailled comment
 */
public interface IRoutinesProvider {

    public List<URL> getSystemRoutines();

    public URL getTalendRoutinesFolder() throws IOException;

    public List<URL> getTalendRoutines();

}
