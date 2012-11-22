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
package org.talend.core.model.utils;

/**
 * DOC hwang class global comment. Detailled comment
 */
public interface IComponentName {

    public String getDefaultComponentName();

    /**
     * Getter for inputComponentName.
     * 
     * @return the inputComponentName
     */
    public String getInputComponentName();

    /**
     * Getter for outPutComponentName.
     * 
     * @return the outPutComponentName
     */
    public String getOutPutComponentName();
}
