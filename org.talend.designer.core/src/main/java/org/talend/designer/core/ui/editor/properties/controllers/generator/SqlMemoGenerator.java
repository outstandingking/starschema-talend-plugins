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
package org.talend.designer.core.ui.editor.properties.controllers.generator;

import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;
import org.talend.designer.core.ui.editor.properties.controllers.SqlMemoController;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: SqlMemoGenerator.java 1 2006-12-22 下午04:44:08 +0000 (下午04:44:08) yzhang $
 * 
 */
public class SqlMemoGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#generate()
     */
    public AbstractElementPropertySectionController generate() {
        return new SqlMemoController(dp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.properties.controllers.generator.IControllerGenerator#setDynamicProperty(org.talend.designer.core.ui.editor.properties.controllers.generator.IDynamicProperty)
     */
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }

}
