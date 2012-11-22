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

import org.talend.runprocess.data.ParallelPerformance;

/**
 * Control the statistical message that display on row link for parallel execution, if no parallel execution existed,
 * just delegate to super class.
 */
public class ParallelConnectionPerformance extends ConnectionPerformance {

    private ParallelPerformance parallelPerformance;

    /**
     * ParallelConnectionPerformance constructor.
     * 
     * @param conn
     */
    public ParallelConnectionPerformance(Connection conn) {
        super(conn);
        this.parallelPerformance = new ParallelPerformance(conn.getLineStyle());
    }

    @Override
    public void resetStatus() {
        parallelPerformance.resetStatus();
    }

    @Override
    public void setLabel(String msg) {
        // update label
        String oldLabel = label;
        label = parallelPerformance.getLabel(msg);
        offset = parallelPerformance.computeLabelOffset();
        firePropertyChange(LABEL_PROP, oldLabel, label);
    }
}
