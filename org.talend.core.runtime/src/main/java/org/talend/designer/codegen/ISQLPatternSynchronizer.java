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
package org.talend.designer.codegen;

import org.eclipse.core.resources.IFile;
import org.talend.commons.exception.SystemException;
import org.talend.core.model.properties.SQLPatternItem;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public interface ISQLPatternSynchronizer {

    public void syncSQLPattern(SQLPatternItem routineItem, boolean copyToTemp) throws SystemException;

    public IFile getSQLPatternFile(SQLPatternItem routineItem) throws SystemException;
}
