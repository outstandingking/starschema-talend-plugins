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
package org.talend.designer.core.ui.editor.cmd;

import org.eclipse.gef.EditPart;

/**
 * 
 */
public class BringForwardCommand extends ZorderCommand {

    public BringForwardCommand(EditPart editPart) {
        super(editPart);
    }

    @Override
    protected int getNewIndex() {
        return oldIndex + 1;
    }

    @Override
    public boolean subCanExecute() {
        return oldIndex < size - 1;
    }
}
