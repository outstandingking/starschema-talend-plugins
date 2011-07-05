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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class MoveFileListFilemaskToFilemaskListForJava extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
    	ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() == ECodeLanguage.PERL || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        try {

            IComponentFilter filter1 = new NameComponentFilter("tFileList"); //$NON-NLS-1$

            IComponentConversion addNewProperty = new IComponentConversion() {

                public void transform(NodeType node) {
                    ComponentUtilities.addNodeProperty(node, "FILES", "TABLE"); //$NON-NLS-1$ //$NON-NLS-2$

                    List<ElementValueType> values = new ArrayList<ElementValueType>();
                    ElementValueType eValue = TalendFileFactory.eINSTANCE.createElementValueType();
                    eValue.setElementRef("FILEMASK"); //$NON-NLS-1$
                    eValue.setValue(ComponentUtilities.getNodePropertyValue(node, "FILEMASK")); //$NON-NLS-1$
                    values.add(eValue);

                    ComponentUtilities.setNodeProperty(node, "FILES", values); //$NON-NLS-1$
                }
            };

            ModifyComponentsAction.searchAndModify(item,processType, filter1, Arrays.<IComponentConversion> asList(addNewProperty));

            return ExecutionResult.SUCCESS_WITH_ALERT;

        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 9, 1, 12, 10, 0);
        return gc.getTime();
    }

}
