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
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeJDBCDriverJarTypeForFeature12879 extends AbstractJobMigrationTask {

    public ChangeJDBCDriverJarTypeForFeature12879() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        String[] componentsName = new String[] {
                "tJDBCConnection", "tJDBCInput", "tJDBCOutput", "tJDBCRow", "tJDBCSP", "tELTJDBCMap" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        IComponentConversion changeJDBCDriverJarType = new IComponentConversion() {

            public void transform(NodeType node) {
                ElementParameterType driverType = ComponentUtilities.getNodeProperty(node, "DRIVER_JAR");
                if (driverType != null) {
                    String value = driverType.getValue();
                    if (value != null && !"".equals(value)) {
                        value = TalendTextUtils.removeQuotes(value);
                    }
                    driverType.setField("TABLE");
                    driverType.setValue(null);
                    ElementValueType valueType = TalendFileFactory.eINSTANCE.createElementValueType();
                    valueType.setElementRef("JAR_NAME");
                    valueType.setValue(value);
                    driverType.getElementValue().add(valueType);

                }
            }
        };

        for (String name : componentsName) {
            IComponentFilter filter = new NameComponentFilter(name); //$NON-NLS-1$

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(changeJDBCDriverJarType));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_WITH_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 6, 10, 16, 0, 0);
        return gc.getTime();
    }

}
