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
package org.talend.designer.core.ui.editor.connections;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;

/**
 * Tracker that allow to move the connection's label. <br/>
 * 
 * $Id: ConnTextTracker.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class ConnTextTracker extends DragEditPartsTracker {

    ConnectionPart connection;

    /**
     * Gives the Connection Part and the ConnectionLabelEditPart to the tracker.
     * 
     * @param source
     * @param connection
     */
    public ConnTextTracker(EditPart source, ConnectionPart connection) {
        super(source);
        this.connection = connection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.tools.TargetingTool#getTargetEditPart()
     */
    protected EditPart getTargetEditPart() {
        return connection;
    }
}
