// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core;

import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface ICamelItemService extends IService {

    public ERepositoryObjectType getCamelRepObjType(Item item);
}
