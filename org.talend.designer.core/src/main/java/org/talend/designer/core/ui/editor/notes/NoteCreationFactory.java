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
package org.talend.designer.core.ui.editor.notes;

import org.eclipse.gef.requests.CreationFactory;

/**
 * 
 */
public class NoteCreationFactory implements CreationFactory {

    public Object getNewObject() {
        return new Note();
    }

    public Object getObjectType() {
        return Note.class;
    }

}
