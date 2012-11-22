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

import org.talend.runprocess.data.IteratePerformance;

/**
 * Control the statistical message that display on iterate link.
 */
public class IterateConnectionPerformance extends ConnectionPerformance {

    private IteratePerformance iteratePerformance;

    /**
     * DOC hcw IterateConnectionPerformance constructor comment.
     * 
     * @param conn
     */
    public IterateConnectionPerformance(Connection conn) {
        super(conn);
        this.iteratePerformance = new IteratePerformance(conn.getLineStyle());
    }

    @Override
    public void resetStatus() {
        iteratePerformance.resetStatus();
    }

    @Override
    public void setLabel(String msg) {
        String oldLabel = label;
        label = iteratePerformance.getLabel(msg);
        firePropertyChange(LABEL_PROP, oldLabel, label);
    }
}
