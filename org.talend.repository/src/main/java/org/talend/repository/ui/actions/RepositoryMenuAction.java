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
package org.talend.repository.ui.actions;

import org.eclipse.jface.action.Action;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public abstract class RepositoryMenuAction extends Action {

    protected IRepositoryView view;

    public RepositoryMenuAction() {
        super();
    }

    public void initialize(IRepositoryView view) {
        this.view = view;
    }
}
