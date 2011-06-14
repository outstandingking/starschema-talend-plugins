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
package org.talend.repository.model.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.repository.model.ProxyRepositoryFactory;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40Z nrousseau $
 * 
 */
public class RenameConnectionRunIfOkToOnComponentOkTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.migration.AbstractJobMigrationTask#executeOnProcess(org.talend.core.model.properties.ProcessItem)
     */
    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
		if (processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}	
        try {
            renameConnections(item,processType);
            return ExecutionResult.SUCCESS_WITH_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     * yzhang Comment method "renameConnections".
     * 
     * @param item
     * @param processType 
     * @throws PersistenceException
     */
    private void renameConnections(Item item, ProcessType processType) throws PersistenceException {

        ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

        boolean modified = false;

        for (Object o : processType.getConnection()) {

            ConnectionType currentConnection = (ConnectionType) o;

            if (currentConnection.getConnectorName().equals("RUN_OK")) { //$NON-NLS-1$

                currentConnection.setConnectorName(EConnectionType.ON_COMPONENT_OK.getName());
                currentConnection.setLabel(EConnectionType.ON_COMPONENT_OK.getDefaultLinkName());
                currentConnection.setLineStyle(EConnectionType.ON_COMPONENT_OK.getId());

                modified = true;
            }

        }
        if (modified) {
            factory.save(item,true);
        }

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
