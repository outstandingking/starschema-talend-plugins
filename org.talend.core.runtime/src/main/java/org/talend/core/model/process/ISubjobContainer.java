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
package org.talend.core.model.process;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public interface ISubjobContainer extends IElement {

    /**
     * DOC nrousseau Comment method "updateSubjobContainer".
     */
    public void updateSubjobContainer();

    public boolean isDisplayed();

    public void updateSubjobDisplay();
}
