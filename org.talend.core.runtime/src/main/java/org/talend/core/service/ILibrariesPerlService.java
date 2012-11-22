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
package org.talend.core.service;

import org.eclipse.jface.action.Action;
import org.talend.core.IService;
import org.talend.core.model.general.ILibrariesService;
import org.talend.core.model.routines.IRoutinesProvider;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface ILibrariesPerlService extends IService {

    public Action getInstallPerlModulesAction();

    public IRoutinesProvider getTosPerlRoutinesProvider();

    public ILibrariesService getPerlLibrariesService();
}
