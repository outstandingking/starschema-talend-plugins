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
package org.talend.core.model.update;

import org.talend.core.runtime.i18n.Messages;

/**
 * ggu class global comment. Detailled comment
 */
public enum EUpdateItemType {
    NODE_PROPERTY(Messages.getString("EUpdateItemType.Property")), //$NON-NLS-1$
    NODE_SCHEMA(Messages.getString("EUpdateItemType.Schema")), //$NON-NLS-1$
    NODE_SAP_FUNCTION(Messages.getString("EUpdateItemType.SAPFunction")), //$NON-NLS-1$
    NODE_SAP_IDOC(Messages.getString("EUpdateItemType.SAPIDoc")), //$NON-NLS-1$
    NODE_QUERY(Messages.getString("EUpdateItemType.Query")), //$NON-NLS-1$
    NODE_VALIDATION_RULE(Messages.getString("EUpdateItemType.ValidationRule")), //$NON-NLS-1$
    JOB_PROPERTY_EXTRA(Messages.getString("EUpdateItemType.Property")), //$NON-NLS-1$
    JOB_PROPERTY_STATS_LOGS(Messages.getString("EUpdateItemType.Property")), //$NON-NLS-1$
    JOB_PROPERTY_HEADERFOOTER(Messages.getString("EUpdateItemType.Property")), //$NON-NLS-1$
    CONTEXT(Messages.getString("EUpdateItemType.Variable")), //$NON-NLS-1$
    CONTEXT_GROUP(Messages.getString("EUpdateItemType.ContextGroup")), //$NON-NLS-1$
    JOB_VERSION(Messages.getString("EUpdateItemType.JobVersion")), //$NON-NLS-1$
    RELOAD(Messages.getString("EUpdateItemType.Components")), //$NON-NLS-1$
    // for joblet
    JOBLET_RENAMED(Messages.getString("EUpdateItemType.Name")), //$NON-NLS-1$
    JOBLET_SCHEMA(Messages.getString("EUpdateItemType.Schema")), //$NON-NLS-1$
    JOBLET_CONTEXT(Messages.getString("EUpdateItemType.Variable")); //$NON-NLS-1$

    private String displayName;

    private EUpdateItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.toString();
    }
}
