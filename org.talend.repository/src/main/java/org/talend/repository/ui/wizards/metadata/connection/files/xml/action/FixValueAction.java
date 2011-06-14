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
package org.talend.repository.ui.wizards.metadata.connection.files.xml.action;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.talend.repository.ui.swt.utils.AbstractXmlStepForm;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.StringUtil;

/**
 * wzhang class global comment. Detailled comment
 */
public class FixValueAction extends SelectionProviderAction {

    private TreeViewer xmlViewer;

    private AbstractXmlStepForm form;

    public FixValueAction(TreeViewer xmlViewer, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
    }

    public FixValueAction(TreeViewer xmlViewer, AbstractXmlStepForm form, String text) {
        super(xmlViewer, text);
        this.xmlViewer = xmlViewer;
        this.form = form;
    }

    public void run() {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node != null) {
            setFixValue(node);
        }
    }

    private void setFixValue(FOXTreeNode node) {
        String label = null; //$NON-NLS-1$
        while (!StringUtil.validateLabelForFixedValue(label)) {
            InputDialog dialog = new InputDialog(null, "Input a fix value", "Input the default value' valid label", "", null);
            int status = dialog.open();
            if (status == InputDialog.OK) {
                label = dialog.getValue().trim();
                if (label != null && label.length() == 0) {
                    break;
                }
            }
            if (status == InputDialog.CANCEL) {
                return;
            }
        }
        node.setDefaultValue(label);
        this.xmlViewer.refresh();
        xmlViewer.expandAll();
        form.redrawLinkers();
        form.updateConnection();
    }

    @Override
    public void selectionChanged(IStructuredSelection selection) {
        FOXTreeNode node = (FOXTreeNode) this.getStructuredSelection().getFirstElement();
        if (node == null) {
            this.setEnabled(false);
        } else {
            if (node.getParent() == null) {
                this.setEnabled(false);
            } else if (node.getColumn() != null) {
                this.setEnabled(false);
            } else if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
        }
    }
}
