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
package org.talend.repository.ui.swt.utils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.formtools.LabelledCheckboxCombo;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.IMetadataContextModeManager;
import org.talend.core.model.metadata.builder.connection.FileConnection;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.utils.FileConnectionContextUtils.EFileParamName;
import org.talend.repository.ui.wizards.metadata.MetadataContextModeManager;

/**
 * ggu class global comment. Detailled comment
 */
public abstract class AbstractFileStepForm extends AbstractForm {

    protected int maximumRowsToPreview = CorePlugin.getDefault().getPreferenceStore().getInt(
            ITalendCorePrefConstants.PREVIEW_LIMIT);

    protected FileConnection connection;

    private IMetadataContextModeManager contextModeManager;

    public AbstractFileStepForm(Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    public IMetadataContextModeManager getContextModeManager() {
        return this.contextModeManager;
    }

    public void setContextModeManager(IMetadataContextModeManager contextModeManager) {
        this.contextModeManager = contextModeManager;
    }

    protected FileConnection getConnection() {
        return (FileConnection) connectionItem.getConnection();
    }

    protected boolean initRowsToSkip(LabelledCheckboxCombo combo, String value) {
        if (value != null && !"".equals(value.trim()) && ContextParameterUtils.isContainContextParam(value)) { //$NON-NLS-1$
            combo.setText(value);
        } else {
            int i = ConnectionContextHelper.convertValue(value);
            if (i > 0) {
                combo.setText(value);
                // combo.select(i); //the value will be added one number.
            } else {
                combo.setText(""); //$NON-NLS-1$
                combo.getCombo().setEnabled(false);
                combo.getCheckbox().setSelection(false);
                return false; // not checked
            }
        }
        return true;
    }

    @Override
    protected void exportAsContext() {
        collectConnParams();
        super.exportAsContext();
        if (getContextModeManager() != null) {
            getContextModeManager().setDefaultContextType(getConnection());
        }
    }

    protected void collectConnParams() {
        addContextParams(EFileParamName.File, true);
        addContextParams(EFileParamName.Encoding, true);
    }

    protected void initGuessSchema() {
        if (getParent().getChildren().length == 1) { // only open table
            if (getContextModeManager() == null) { // first
                setContextModeManager(new MetadataContextModeManager());
                ConnectionContextHelper.checkContextMode(connectionItem);
            }
            if (connectionItem.getConnection().isContextMode()) {
                ContextType contextTypeForContextMode = ConnectionContextHelper.getContextTypeForContextMode(getShell(),
                        connectionItem.getConnection());
                getContextModeManager().setSelectedContextType(contextTypeForContextMode);
            }

        }
    }
}
