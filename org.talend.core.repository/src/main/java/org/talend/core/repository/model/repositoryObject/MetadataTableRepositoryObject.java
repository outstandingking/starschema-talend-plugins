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
package org.talend.core.repository.model.repositoryObject;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.builder.connection.AbstractMetadataObject;
import org.talend.core.model.metadata.builder.connection.Concept;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.MDMConnection;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.metadata.builder.connection.SalesforceModuleUnit;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ISubRepositoryObject;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.SubItemHelper;
import org.talend.cwm.helper.SwitchHelpers;
import org.talend.cwm.relational.TdTable;
import org.talend.repository.model.ERepositoryStatus;
import org.talend.repository.model.IRepositoryNode;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.objectmodel.core.Package;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class MetadataTableRepositoryObject extends MetadataTable implements ISubRepositoryObject {

    protected final IRepositoryViewObject viewObject;

    private IRepositoryNode repositoryNode;

    public IRepositoryViewObject getViewObject() {
        return this.viewObject;
    }

    protected org.talend.core.model.metadata.builder.connection.MetadataTable table;

    public MetadataTableRepositoryObject(IRepositoryViewObject repObj,
            org.talend.core.model.metadata.builder.connection.MetadataTable table) {
        this.viewObject = repObj;
        this.table = table;
    }

    public Property getProperty() {
        Property property = viewObject.getProperty();
        // update table
        updateTable(property);
        return property;
    }

    // @Override
    public String getVersion() {
        return viewObject.getVersion();
    }

    @Override
    public String getLabel() {
        return table.getLabel();
    }

    @Override
    public String getId() {
        return table.getId();
    }

    public String getTableType() {
        return table.getTableType();
    }

    public org.talend.core.model.metadata.builder.connection.MetadataTable getTable() {
        return this.table;
    }

    public AbstractMetadataObject getAbstractMetadataObject() {
        return getTable();
    }

    public void removeFromParent() {

        if (table.eContainer() instanceof SAPFunctionUnit) {
            SAPFunctionUnit funUnit = (SAPFunctionUnit) table.eContainer();
            funUnit.getTables().remove(table);
            return;
        }
        if (table.eContainer() instanceof SalesforceModuleUnit) {
            SalesforceModuleUnit moduleUnit = (SalesforceModuleUnit) table.eContainer();
            moduleUnit.getTables().remove(table);
            return;
        }
        if (table.getNamespace() instanceof Package) {
            Package pkg = (Package) table.getNamespace();
            if (pkg.getOwnedElement().contains(table)) {
                pkg.getOwnedElement().remove(table);
            }

        }
        Property property = getProperty();
        // remove mdm schema
        if (property == null) {
            return;
        }
        Connection connection = null;
        Item item = property.getItem();
        if (item instanceof ConnectionItem) {
            ConnectionItem cItem = (ConnectionItem) item;
            connection = cItem.getConnection();
        }
        if (connection instanceof MDMConnection) {
            EList schemas = ((MDMConnection) connection).getSchemas();
            Iterator iterator2 = schemas.iterator();
            while (iterator2.hasNext()) {
                Object object = iterator2.next();
                if (object instanceof Concept) {
                    Concept concept = (Concept) object;
                    if (concept.getLabel() != null && concept.getLabel().equals(table.getLabel())) {
                        iterator2.remove();
                        break;
                    }
                }
            }

        }

        return;
    }

    private void updateTable(Property property) {
        if (property == null) {
            return;
        }

        Connection connection = null;
        Item item = property.getItem();
        if (item instanceof ConnectionItem) {
            ConnectionItem cItem = (ConnectionItem) item;
            connection = cItem.getConnection();
        }
        if (connection == null) {
            return;
        }

        Set tables = null;

        if (table.eContainer() instanceof SAPFunctionUnit) {
            SAPFunctionUnit funUnit = (SAPFunctionUnit) table.eContainer();
            tables = ConnectionHelper.getTables(connection, funUnit);
        } else {
            tables = ConnectionHelper.getTables(connection);
        }

        if (tables != null) {
            Iterator iterator = tables.iterator();
            while (iterator.hasNext()) {
                Object obj = iterator.next();
                if (obj instanceof org.talend.core.model.metadata.builder.connection.MetadataTable) {
                    org.talend.core.model.metadata.builder.connection.MetadataTable repObj = (org.talend.core.model.metadata.builder.connection.MetadataTable) obj;
                    // MOD qiongli 2012-1-13 TDQ-4269.
                    if (repObj == null || table.getLabel() == null || repObj.getLabel() == null
                            || !table.getLabel().equals(repObj.getLabel())) {
                        continue;
                    }
                    // if table name is same,should compare its parent name for TdTable
                    TdTable doSwitch = SwitchHelpers.TABLE_SWITCH.doSwitch(table);
                    if (doSwitch != null) {
                        if (table.eContainer() instanceof ModelElement && repObj.eContainer() instanceof ModelElement) {
                            ModelElement tableP = (ModelElement) table.eContainer();
                            ModelElement repObjP = (ModelElement) repObj.eContainer();
                            if (tableP.getName() != null && tableP.getName().equals(repObjP.getName())) {
                                table = repObj;
                                break;
                            }
                        }
                    } else {
                        table = repObj;
                        break;
                    }
                }// ~
            }

        }
    }

    public User getAuthor() {
        return viewObject.getAuthor();
    }

    public List<IRepositoryViewObject> getChildren() {
        return viewObject.getChildren();
    }

    public Date getCreationDate() {
        return viewObject.getCreationDate();
    }

    public String getDescription() {
        return viewObject.getDescription();
    }

    public ERepositoryStatus getInformationStatus() {
        return viewObject.getInformationStatus();
    }

    public Date getModificationDate() {
        return viewObject.getModificationDate();
    }

    public String getPath() {
        return viewObject.getPath();
    }

    public String getProjectLabel() {
        return viewObject.getProjectLabel();
    }

    public String getPurpose() {
        return viewObject.getPurpose();
    }

    public IRepositoryNode getRepositoryNode() {
        return this.repositoryNode;
    }

    public ERepositoryStatus getRepositoryStatus() {
        return viewObject.getRepositoryStatus();
    }

    public String getStatusCode() {
        return viewObject.getStatusCode();
    }

    public ERepositoryObjectType getRepositoryObjectType() {
        return ERepositoryObjectType.METADATA_CON_TABLE;
    }

    public boolean isDeleted() {
        return SubItemHelper.isDeleted(table);
    }

    public void setRepositoryNode(IRepositoryNode node) {
        this.repositoryNode = node;
    }

    public ModelElement getModelElement() {
        return this.table;
    }

}
