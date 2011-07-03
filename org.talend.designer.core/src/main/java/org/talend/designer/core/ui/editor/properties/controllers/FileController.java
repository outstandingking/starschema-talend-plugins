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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;

import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.xml.XmlUtil;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.utils.PathExtractor;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;

/**
 * yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: FileController.java 1 2006-12-12 上午11:18:38 +0000 (上午11:18:38) yzhang $
 * 
 */
public class FileController extends AbstractElementPropertySectionController {

    private static final String FILE = "FILE"; //$NON-NLS-1$

    private boolean dragAndDropActionBool = false;

    /**
     * yzhang FileController constructor comment.
     * 
     * @param parameterBean
     */
    public FileController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * This method will never be called.
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createCommand
     * ()
     */
    public Command createCommand() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    public Command createCommand(Button button) {

        FileDialog dial = new FileDialog(composite.getShell(), SWT.NONE);
        String propertyName = (String) button.getData(PARAMETER_NAME);
        Text filePathText = (Text) hashCurControls.get(propertyName);
        String extractedFilePath = PathExtractor.extractPath(filePathText.getText());
        dial.setFileName(new Path(extractedFilePath).toOSString());
        String file = dial.open();
        if (file != null) {
            if (!file.equals("")) { //$NON-NLS-1$
                if (!elem.getPropertyValue(propertyName).equals(file)) {
                    String portableValue = Path.fromOSString(file).toPortableString();
                    filePathText.setText(TalendTextUtils.addQuotes(portableValue));
                    return new PropertyChangeCommand(elem, propertyName, TalendTextUtils.addQuotes(portableValue));
                }
            }
        }
        return null;
    }

    private void setDragAndDropActionBool() {
        IElementParameter propertyParam = elem.getElementParameter("PROPERTY:REPOSITORY_PROPERTY_TYPE");
        try {
            if (propertyParam != null && propertyParam.getValue() != null && !"".equals(propertyParam.getValue())) {
                Item linkedRepositoryItem = null;
                IRepositoryViewObject repository = DesignerPlugin.getDefault().getProxyRepositoryFactory()
                        .getLastVersion(propertyParam.getValue().toString());
                if (repository != null && repository.getProperty() != null) {
                    linkedRepositoryItem = repository.getProperty().getItem();
                }
                if (linkedRepositoryItem != null && linkedRepositoryItem instanceof XmlFileConnectionItem) {
                    XmlFileConnectionItem xci = (XmlFileConnectionItem) linkedRepositoryItem;
                    XmlFileConnection cc = (XmlFileConnection) xci.getConnection();
                    String xmlFilePath = cc.getXmlFilePath();
                    if (xmlFilePath != null && XmlUtil.isXSDFile(TalendQuoteUtils.removeQuotes(xmlFilePath))) {
                        dragAndDropActionBool = true;
                    }
                }
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createControl()
     */
    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.setDragAndDropActionBool();
        this.curParameter = param;
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        FormData data;

        btnEdit.setImage(ImageProvider.getImage(CorePlugin.getImageDescriptor(DOTS_BUTTON)));

        data = new FormData();
        data.left = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), -STANDARD_BUTTON_WIDTH);
        data.right = new FormAttachment(((numInRow * MAX_PERCENT) / nbInRow), 0);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btnEdit.setLayoutData(data);
        btnEdit.setData(NAME, FILE);
        btnEdit.setData(PARAMETER_NAME, param.getName());
        btnEdit.setEnabled(dragAndDropActionBool == true || !param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);

        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("FileController.decoration.description")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        Text filePathText = (Text) dField.getControl();
        filePathText.setData(PARAMETER_NAME, param.getName());
        cLayout.setBackground(subComposite.getBackground());

        editionControlHelper.register(param.getName(), filePathText);
        if (!elem.isReadOnly()) {
            if (param.isRepositoryValueUsed() && dragAndDropActionBool == false) {
                addRepositoryPropertyListener(filePathText);
            }
            filePathText.setEditable(dragAndDropActionBool == true || !param.isRepositoryValueUsed());
        } else {
            filePathText.setEditable(dragAndDropActionBool == true || false);
        }

        addDragAndDropTarget(filePathText);
        if (elem instanceof Node) {
            filePathText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        hashCurControls.put(param.getName(), filePathText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName()); //$NON-NLS-1$
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment(btnEdit, 0);
        data.top = new FormAttachment(btnEdit, 0, SWT.CENTER);
        cLayout.setLayoutData(data);

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return btnEdit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new TextControlCreator());
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
    }

    SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {

        }

        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand((Button) e.getSource());
            executeCommand(cmd);
        }

    };

    @Override
    public void refresh(IElementParameter param, boolean checkErrorsWhenViewRefreshed) {
        final String name2 = param.getName();
        Text labelText = (Text) hashCurControls.get(name2);
        Object value = param.getValue();
        if (labelText == null || labelText.isDisposed()) {
            return;
        }

        boolean valueChanged = false;
        if (value == null) {
            labelText.setText(""); //$NON-NLS-1$
        } else {
            if (!value.equals(labelText.getText())) {
                labelText.setText((String) value);
                valueChanged = true;
            }
        }
        if (checkErrorsWhenViewRefreshed || valueChanged) {
            checkErrorsForPropertiesOnly(labelText);
        }
        fixedCursorPosition(param, labelText, value, valueChanged);
    }

}
