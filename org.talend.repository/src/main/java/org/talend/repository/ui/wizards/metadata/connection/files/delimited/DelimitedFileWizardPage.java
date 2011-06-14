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
package org.talend.repository.ui.wizards.metadata.connection.files.delimited;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.metadata.builder.connection.DelimitedFileConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.TableHelper;
import org.talend.repository.ui.swt.utils.AbstractDelimitedFileStepForm;
import org.talend.repository.ui.swt.utils.AbstractForm;

/**
 * Use to create a new connection to a File. Page allows setting a file.
 */
public class DelimitedFileWizardPage extends WizardPage {

    private ConnectionItem connectionItem;

    private int step;

    private AbstractDelimitedFileStepForm currentComposite;

    private final String[] existingNames;

    private boolean isRepositoryObjectEditable;

    private IMetadataContextModeManager contextModeManager;

    /**
     * DOC ocarbone DelimitedFileWizardPage constructor comment.
     * 
     * @param step
     * @param connection
     * @param isRepositoryObjectEditable
     * @param existingNames
     */
    public DelimitedFileWizardPage(int step, ConnectionItem connectionItem, boolean isRepositoryObjectEditable,
            String[] existingNames, IMetadataContextModeManager contextModeManager) {
        super("wizardPage"); //$NON-NLS-1$
        this.step = step;
        this.connectionItem = connectionItem;
        this.existingNames = existingNames;
        this.isRepositoryObjectEditable = isRepositoryObjectEditable;
        this.contextModeManager = contextModeManager;
    }

    /**
     * 
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(final Composite parent) {
        currentComposite = null;

        if (step == 1) {
            currentComposite = new DelimitedFileStep1Form(parent, connectionItem, existingNames, contextModeManager);
        } else if (step == 2) {
            currentComposite = new DelimitedFileStep2Form(parent, connectionItem, contextModeManager);
            currentComposite.setWizardPage(this);
        } else if (step == 3) {
            MetadataTable metadataTable = ConnectionHelper.getTables((DelimitedFileConnection) connectionItem.getConnection())
                    .toArray(new MetadataTable[0])[0]; // hywang
            currentComposite = new DelimitedFileStep3Form(parent, connectionItem, metadataTable, TableHelper.getTableNames(
                    ((DelimitedFileConnection) connectionItem.getConnection()), metadataTable.getLabel()), contextModeManager);
        }
        currentComposite.setReadOnly(!isRepositoryObjectEditable);

        AbstractForm.ICheckListener listener = new AbstractForm.ICheckListener() {

            public void checkPerformed(final AbstractForm source) {

                if (source.isStatusOnError()) {
                    DelimitedFileWizardPage.this.setPageComplete(false);
                    setErrorMessage(source.getStatus());
                } else {
                    DelimitedFileWizardPage.this.setPageComplete(isRepositoryObjectEditable);
                    setErrorMessage(null);
                    setMessage(source.getStatus());
                }
            }
        };

        currentComposite.setListener(listener);
        setControl((Composite) currentComposite);
    }

}
