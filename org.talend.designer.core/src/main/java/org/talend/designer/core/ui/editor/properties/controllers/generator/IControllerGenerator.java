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
package org.talend.designer.core.ui.editor.properties.controllers.generator;

import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: ControllerGenerator.java 1 2006-12-22 下午03:13:13 +0000 (下午03:13:13) yzhang $
 * 
 */
public interface IControllerGenerator {

    public AbstractElementPropertySectionController generate();

    public void setDynamicProperty(IDynamicProperty dp);

}
