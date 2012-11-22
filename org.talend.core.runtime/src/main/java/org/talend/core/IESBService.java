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
package org.talend.core;

import org.talend.core.model.process.INode;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public interface IESBService extends IService {

    public ERepositoryObjectType getServicesType();

    public String getServiceLabel(Item item, String linkedRepository);

    public void updateOperation(INode node, String linkedRepository, RepositoryNode selectNode);

    public String getWsdlFilePath(Item item);

    public Object getValue(Item item, String value, INode node);

    public void refreshComponentView(Item item);

    public void refreshOperationLabel(String jobID);

    public void editJobName(String originaleObjectLabel, String newLabel);

    public StringBuffer getAllTheJObNames(IRepositoryNode jobList);

    public void deleteOldRelation(String jobID);

    // public void setSelectedItem(Item, )

    // public AbstractMetadataObject getServicesOperation(Connection connection, String operationName);

    // public void changeOperationLabel(RepositoryNode newNode, INode node, Connection connection);

    public boolean isServiceItem(int classifierID);

}
