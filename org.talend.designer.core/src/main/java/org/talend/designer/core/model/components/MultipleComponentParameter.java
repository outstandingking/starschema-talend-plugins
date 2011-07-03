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
package org.talend.designer.core.model.components;

import java.util.StringTokenizer;

import org.talend.core.model.components.IMultipleComponentParameter;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: MultipleComponentParameter.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class MultipleComponentParameter implements IMultipleComponentParameter {

    String sourceComponent;

    String targetComponent;

    String sourceValue;

    String targetValue;

    public MultipleComponentParameter(String source, String target) {
        StringTokenizer token = new StringTokenizer(source, "."); //$NON-NLS-1$
        sourceComponent = token.nextToken();
        sourceValue = token.nextToken();

        token = new StringTokenizer(target, "."); //$NON-NLS-1$
        targetComponent = token.nextToken();
        targetValue = token.nextToken();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getSourceComponent()
     */
    public String getSourceComponent() {
        return this.sourceComponent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getSourceValue()
     */
    public String getSourceValue() {
        return this.sourceValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getTargetComponent()
     */
    public String getTargetComponent() {
        return this.targetComponent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentParameter#getTargetValue()
     */
    public String getTargetValue() {
        return this.targetValue;
    }
}
