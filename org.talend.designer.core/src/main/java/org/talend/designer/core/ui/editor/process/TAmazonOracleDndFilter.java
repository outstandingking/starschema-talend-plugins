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
package org.talend.designer.core.ui.editor.process;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.DefaultRepositoryComponentDndFilter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class TAmazonOracleDndFilter extends DefaultRepositoryComponentDndFilter {

    public TAmazonOracleDndFilter() {
        super();
    }

    @Override
    public boolean except(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        // for tAmazonOracleInput/Output/Connection/Row
        if (component != null && component.getName().startsWith("tAmazonOracle")) { //$NON-NLS-1$
            String dbType = null;
            if (item != null && item instanceof DatabaseConnectionItem) {
                if (((DatabaseConnectionItem) item).getConnection() instanceof DatabaseConnection)
                    dbType = ((DatabaseConnection) ((DatabaseConnectionItem) item).getConnection()).getDatabaseType();
            }
            Node node = new Node(component);
            if (node != null) {
                IElementParameter param = node.getElementParameter("CONNECTION_TYPE");
                if (param != null) {
                    Object[] valuesList = param.getListItemsValue();
                    for (int i = 0; i < valuesList.length; i++) {
                        String conType = EDatabaseTypeName.getTypeFromDbType(valuesList[i].toString()).getDisplayName();
                        if (conType != null && dbType != null && conType.equals(dbType)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean valid(Item item, ERepositoryObjectType type, RepositoryNode seletetedNode, IComponent component,
            String repositoryType) {
        return false; // no valid component
    }

}
