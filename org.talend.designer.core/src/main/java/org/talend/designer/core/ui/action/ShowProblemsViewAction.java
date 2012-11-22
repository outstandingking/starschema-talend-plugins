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
package org.talend.designer.core.ui.action;

import org.talend.commons.ui.swt.actions.AbstractShowViewAction;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: ShowProblemsViewAction.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ShowProblemsViewAction extends AbstractShowViewAction {

    @Override
    public String getDefinitionId() {
        return "showProblemsView"; //$NON-NLS-1$
    }

    @Override
    public String getViewId() {
        return "org.talend.designer.core.ui.views.ProblemsView"; //$NON-NLS-1$
    }

}
