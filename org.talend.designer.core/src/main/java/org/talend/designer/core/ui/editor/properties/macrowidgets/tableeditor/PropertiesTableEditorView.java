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
package org.talend.designer.core.ui.editor.properties.macrowidgets.tableeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView;
import org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView;
import org.talend.commons.ui.swt.proposal.TextCellEditorWithProposal;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorColumn;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreatorNotModifiable.LAYOUT_MODE;
import org.talend.commons.ui.swt.tableviewer.behavior.CellEditorValueAdapter;
import org.talend.commons.ui.swt.tableviewer.behavior.ColumnCellModifier;
import org.talend.commons.ui.swt.tableviewer.behavior.IColumnColorProvider;
import org.talend.commons.ui.swt.tableviewer.behavior.IColumnLabelProvider;
import org.talend.commons.ui.swt.tableviewer.tableeditor.CheckboxTableEditorContent;
import org.talend.commons.utils.data.bean.IBeanPropertyAccessors;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.IEbcdicConstant;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.IRuleConstant;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.EbcdicConnectionItem;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.IEBCDICProviderService;
import org.talend.core.ui.metadata.celleditor.ModuleListCellEditor;
import org.talend.core.ui.metadata.celleditor.RuleCellEditor;
import org.talend.core.ui.metadata.celleditor.SchemaCellEditor;
import org.talend.core.ui.metadata.celleditor.SchemaXPathQuerysCellEditor;
import org.talend.core.ui.metadata.editor.AbstractMetadataTableEditorView;
import org.talend.core.ui.proposal.TalendProposalProvider;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.event.CheckColumnSelectionListener;

/**
 * MetadataTableEditorView2 must be used.
 * 
 * $Id: MetadataTableEditorView.java 801 2006-11-30 16:28:36Z amaumont $
 * 
 * @param <B>
 */
public class PropertiesTableEditorView<B> extends AbstractDataTableEditorView<B> {

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parent
     * @param style
     * @param model
     */
    public PropertiesTableEditorView(Composite parent, int style, PropertiesTableEditorModel model) {
        super(parent, style, model);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     * @param readOnly
     * @param toolbarVisible
     * @param labelVisible TODO
     * @param extendedTableModel
     */
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle, PropertiesTableEditorModel model,
            boolean toolbarVisible, boolean labelVisible) {
        super(parentComposite, mainCompositeStyle, model, model.getElemParameter().isReadOnly(), toolbarVisible, labelVisible);
    }

    /**
     * DOC amaumont MetadataTableEditorView constructor comment.
     * 
     * @param parentComposite
     * @param mainCompositeStyle
     */
    public PropertiesTableEditorView(Composite parentComposite, int mainCompositeStyle) {
        super(parentComposite, mainCompositeStyle);
    }

    public void setMetadataTableEditor(PropertiesTableEditorModel model) {
        setExtendedTableModel(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#initToolBar()
     */
    @Override
    protected ExtendedToolbarView initToolBar() {
        return new PropertiesTableToolbarEditorView(getMainComposite(), SWT.NONE, this.getExtendedTableViewer(),
                (PropertiesTableEditorModel) getExtendedTableModel());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.commons.ui.swt.advanced.dataeditor.AbstractDataTableEditorView#setTableViewerCreatorOptions(org.talend
     * .commons.ui.swt.tableviewer.TableViewerCreator)
     */
    @Override
    protected void setTableViewerCreatorOptions(TableViewerCreator<B> newTableViewerCreator) {
        super.setTableViewerCreatorOptions(newTableViewerCreator);
        newTableViewerCreator.setLayoutMode(LAYOUT_MODE.DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.talend.commons.ui.swt.extended.macrotable.AbstractExtendedTableViewer#createColumns(org.talend.commons.ui
     * .swt.tableviewer.TableViewerCreator, org.eclipse.swt.widgets.Table)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void createColumns(final TableViewerCreator<B> tableViewerCreator, final Table table) {

        // there's two lists of values, one that will be in the table
        // and the other will be stored as the current value in the property
        // there is two lists because of the undo / redo capabilities
        PropertiesTableEditorModel model = getModel();
        TalendProposalProvider processProposalProvider = new TalendProposalProvider(model.getProcess());
        String[] titles = model.getTitles();
        final Object[] itemsValue = model.getItemsValue();
        final String[] items = model.getItems();
        // final Element elem = model.getElement();
        final IElementParameter param = model.getElemParameter();
        final IElement element = model.getElement();
        for (int i = 0; i < titles.length; i++) {
            final int curCol = i;
            final IElementParameter currentParam = (IElementParameter) itemsValue[i];
            // if all is empty, show it always.
            boolean toDisplay = StringUtils.isEmpty(currentParam.getShowIf()) && StringUtils.isEmpty(currentParam.getNotShowIf());
            if (!toDisplay) {
                List<Map<String, Object>> fullTable = (List<Map<String, Object>>) param.getValue();
                for (int curLine = 0; curLine < fullTable.size(); curLine++) {
                    ((ElementParameter) currentParam).setCurrentRow(curLine);
                    if (currentParam.isShow(element.getElementParameters())) {
                        toDisplay = true;
                        break;
                    }
                }
            }

            if (toDisplay) {
                TableViewerCreatorColumn column = new TableViewerCreatorColumn(tableViewerCreator);
                column.setTitle(titles[i]);
                column.setModifiable(true);
                column.setMinimumWidth(100);
                column.setWeight(20);

                switch (currentParam.getFieldType()) {
                case CONTEXT_PARAM_NAME_LIST:
                case CLOSED_LIST:
                case LOOKUP_COLUMN_LIST:
                case COLUMN_LIST:
                case CONNECTION_LIST:
                case DBTYPE_LIST:
                case COMPONENT_LIST:
                case PREV_COLUMN_LIST:
                    final ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(table, currentParam.getListItemsDisplayName());
                    final IElementParameter copyOfTmpParam = currentParam;
                    ((CCombo) cellEditor.getControl()).setEditable(false);
                    ((CCombo) cellEditor.getControl())
                            .setEnabled(!(param.isRepositoryValueUsed() || param.isReadOnly() || currentParam.isReadOnly()));
                    column.setCellEditor(cellEditor, new CellEditorValueAdapter() {

                        @Override
                        public String getColumnText(CellEditor cellEditor, Object bean, Object cellEditorValue) {
                            return (String) cellEditorValue;
                        }

                        @Override
                        public Object getOriginalTypedValue(CellEditor cellEditor, Object cellEditorTypedValue) {
                            Object returnedValue = null;
                            if (cellEditorTypedValue != null && cellEditorTypedValue instanceof Integer) {
                                int index = (Integer) cellEditorTypedValue;
                                String[] namesSet = ((CCombo) cellEditor.getControl()).getItems();
                                if (namesSet.length > 0 && index > -1 && index < namesSet.length) {
                                    returnedValue = namesSet[index];
                                } else {
                                    returnedValue = null;
                                }
                            } else {
                                returnedValue = null;
                            }
                            return returnedValue;
                        };

                        @Override
                        public Object getCellEditorTypedValue(CellEditor cellEditor, Object originalTypedValue) {
                            CCombo combo = (CCombo) cellEditor.getControl();
                            int rowNumber = ((Table) combo.getParent()).getSelectionIndex();
                            String[] listToDisplay = getItemsToDisplay(element, copyOfTmpParam, rowNumber);
                            if (!Arrays.equals(listToDisplay, ((ComboBoxCellEditor) cellEditor).getItems())) {
                                ((ComboBoxCellEditor) cellEditor).setItems(listToDisplay);
                            }
                            Object returnedValue = 0;
                            if (originalTypedValue != null) {
                                String[] namesSet = listToDisplay;
                                for (int j = 0; j < namesSet.length; j++) {
                                    if (namesSet[j].equals(originalTypedValue)) {
                                        returnedValue = j;
                                        break;
                                    }
                                }
                            }
                            return returnedValue;
                        };
                    });
                    cellEditor.addListener(new ICellEditorListener() {

                        @Override
                        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                        }

                        @Override
                        public void cancelEditor() {
                        }

                        @Override
                        public void applyEditorValue() {
                            if (element instanceof Node) {
                                IProcess process = ((Node) element).getProcess();
                                if (process instanceof IProcess2) {
                                    ((IProcess2) process).checkProcess();
                                }
                            }

                        }
                    });
                    break;
                case MODULE_LIST:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    ModuleListCellEditor moduleEditor = new ModuleListCellEditor(table, currentParam, param);
                    moduleEditor.setTableEditorView(this);
                    column.setCellEditor(moduleEditor);
                    break;
                case COLOR:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    // column.setDisplayedValue("");

                    column.setLabelProvider(null);
                    column.setCellEditor(new ColorCellEditor(table) {

                        @Override
                        protected void doSetValue(Object value) {
                            if (value instanceof String) {
                                super.doSetValue(TalendTextUtils.stringToRGB((String) value));
                            } else {
                                super.doSetValue(value);
                            }
                        }

                        @Override
                        protected void updateContents(Object value) {
                            if (value != null) {
                                if (value instanceof String) {
                                    super.updateContents(TalendTextUtils.stringToRGB((String) value));
                                } else {
                                    super.updateContents(value);
                                }
                            }
                        }

                    });
                    column.setColorProvider(new IColumnColorProvider() {

                        @Override
                        public Color getBackgroundColor(Object bean) {
                            Object value = ((Map<String, Object>) bean).get(items[curCol]);
                            if (value == null || (!(value instanceof String))) {
                                return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
                            }
                            return new Color(null, TalendTextUtils.stringToRGB((String) value));
                        }

                        @Override
                        public Color getForegroundColor(Object bean) {
                            return null;
                        }

                    });
                    break;
                case CHECK:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    CheckColumnSelectionListener tableColumnSelectionListener = new CheckColumnSelectionListener(column,
                            tableViewerCreator, currentParam);
                    column.setTableColumnSelectionListener(tableColumnSelectionListener);
                    column.setTableEditorContent(new CheckboxTableEditorContent());
                    Boolean curValue = (Boolean) currentParam.getValue();
                    tableColumnSelectionListener.setChecked(curValue.booleanValue());
                    if (curValue.booleanValue()) {
                        column.setImageHeader(ImageProvider.getImage(EImage.CHECKED_ICON));
                    } else {
                        column.setImageHeader(ImageProvider.getImage(EImage.UNCHECKED_ICON));
                    }
                    column.setDisplayedValue(""); //$NON-NLS-1$
                    break;
                case SCHEMA_TYPE:
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node = (INode) element;
                    // List<IMetadataTable> tables = node.getMetadataList();

                    // if (isEBCDICNode(node)) { // ebcdic
                    column.setLabelProvider(new IColumnLabelProvider() {

                        @Override
                        public String getLabel(Object bean) {
                            if (bean instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                String value = (String) valueMap.get(IEbcdicConstant.FIELD_SCHEMA);
                                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNodeTableName(node,
                                            value);
                                    if (metadataTable != null) {
                                        if (isEBCDICNode(node)) {
                                            if (isRepositorySchemaLine(node, valueMap)) {
                                                return "Repository (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            } else {
                                                return "Built-In (" + metadataTable.getTableName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                            }
                                        } else {
                                            return metadataTable.getTableName();
                                        }
                                    } else {
                                        return value;
                                    }
                                }
                            }
                            return ""; //$NON-NLS-1$
                        }
                    });
                    // } else {
                    // column.setLabelProvider(null);
                    // }
                    SchemaCellEditor schemaEditor = new SchemaCellEditor(table, node);
                    schemaEditor.setTableEditorView(this);
                    column.setCellEditor(schemaEditor);
                    break;

                // hywang add for feature 6484
                case RULE_TYPE:
                    column.setTitle("Rule"); //$NON-NLS-1$
                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node1 = (INode) element;
                    column.setLabelProvider(new IColumnLabelProvider() {

                        @Override
                        public String getLabel(Object bean) {
                            if (bean instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) bean;
                                String value = (String) valueMap.get(IRuleConstant.FIELD_RULE);
                                if (value != null && !"".equals(value)) { //$NON-NLS-1$
                                    IMetadataTable metadataTable = MetadataToolHelper.getMetadataTableFromNodeTableName(node1,
                                            value);
                                    if (metadataTable != null) {
                                        return metadataTable.getTableName();
                                    } else {
                                        return value;
                                    }
                                }
                            }
                            return ""; //$NON-NLS-1$
                        }
                    });
                    RuleCellEditor ruleEditor = new RuleCellEditor(table, node1);
                    ruleEditor.setTableEditorView(this);
                    column.setCellEditor(ruleEditor);
                    break;

                case SCHEMA_XPATH_QUERYS:

                    column.setModifiable((!param.isRepositoryValueUsed()) && (!param.isReadOnly())
                            && (!currentParam.isReadOnly()));
                    final INode node2 = (INode) element;
                    SchemaXPathQuerysCellEditor schemaXPathEditor = new SchemaXPathQuerysCellEditor(table, node2);
                    schemaXPathEditor.setTableEditorView(this);
                    column.setCellEditor(schemaXPathEditor);

                    break;
                default: // TEXT
                    TextCellEditor tcEditor = null;
                    if (((i == 0) && (param.isBasedOnSchema() || param.isBasedOnSubjobStarts()))
                            || (param.isRepositoryValueUsed()) || (param.isReadOnly()) || currentParam.isReadOnly()) {
                        // read only cell
                    } else {
                        // writable cell
                        if (currentParam.isNoContextAssist()) {
                            tcEditor = new TextCellEditor(table);
                        } else {
                            TextCellEditorWithProposal textCellEditor = new TextCellEditorWithProposal(table, column);
                            textCellEditor.setContentProposalProvider(processProposalProvider);
                            tcEditor = textCellEditor;
                        }
                    }
                    if (tcEditor != null) {
                        column.setCellEditor(tcEditor);
                    }
                }
                // for all kinds of column, check if read only or not when edit the field.
                column.setColumnCellModifier(new ColumnCellModifier(column) {

                    @Override
                    public boolean canModify(Object bean) {
                        boolean canModify = super.canModify(bean);
                        if (canModify) {
                            Map<String, Object> valueMap = (Map<String, Object>) bean;
                            List<Map<String, Object>> fullValues = (List<Map<String, Object>>) param.getValue();
                            ((ElementParameter) currentParam).setCurrentRow(fullValues.indexOf(valueMap));
                            if (currentParam.isReadOnly(element.getElementParameters())) {
                                return false;
                            }
                        }
                        return canModify;
                    }

                });
                column.setColorProvider(new IColumnColorProvider<B>() {

                    @Override
                    public Color getBackgroundColor(B bean) {
                        Map<String, Object> valueMap = (Map<String, Object>) bean;
                        List<Map<String, Object>> fullValues = (List<Map<String, Object>>) param.getValue();

                        // hyWang add varriable index for bug 7294
                        int index = fullValues.indexOf(valueMap);
                        if (index >= 0) {
                            ((ElementParameter) currentParam).setCurrentRow(index);
                            if (currentParam.isReadOnly(element.getElementParameters())) {
                                return AbstractMetadataTableEditorView.READONLY_CELL_BG_COLOR;
                            }
                        }
                        return null;
                    }

                    @Override
                    public Color getForegroundColor(B bean) {
                        return null;
                    }

                });
                column.setBeanPropertyAccessors(new IBeanPropertyAccessors<B, Object>() {

                    @Override
                    public Object get(B bean) {
                        Object value = ((Map<String, Object>) bean).get(items[curCol]);
                        if (value == null) {
                            return ""; //$NON-NLS-1$
                        }
                        if (itemsValue[curCol] instanceof IElementParameter) {
                            IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                            switch (tmpParam.getFieldType()) {
                            case CONTEXT_PARAM_NAME_LIST:
                            case CLOSED_LIST:
                            case COMPONENT_LIST:
                            case COLUMN_LIST:
                            case CONNECTION_LIST:
                            case LOOKUP_COLUMN_LIST:
                            case PREV_COLUMN_LIST:
                            case DBTYPE_LIST:
                                String[] namesSet = tmpParam.getListItemsDisplayName();
                                if (namesSet.length == 0) {
                                    return tmpParam.getDefaultClosedListValue();
                                }
                                if (value instanceof String) {
                                    boolean found = false;
                                    int index = 0;
                                    Object[] items = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                    for (int j = 0; j < items.length && !found; j++) {
                                        if (items[j].equals(value)) {
                                            found = true;
                                            index = j;
                                        }
                                    }
                                    value = new Integer(index);
                                }
                                if (value != null && ((Integer) value) >= 0) {
                                    return namesSet[(Integer) value];
                                }
                                return null;
                            case CHECK:
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case RADIO:
                                if (value instanceof String) {
                                    return new Boolean((String) value);
                                }
                                return value;
                            case COLOR:
                                if (value instanceof String) {
                                    return TalendTextUtils.stringToRGB((String) value);
                                }
                                return value; // already RGB
                            default: // TEXT
                                return value;
                            }
                        }
                        return value;
                    }

                    @Override
                    public void set(B bean, Object value) {
                        Object finalValue = value;
                        IElementParameter tmpParam = (IElementParameter) itemsValue[curCol];
                        // TODO should test if this parameter is contained in any other show if / not show if, etc..
                        boolean included = false;
                        for (Object object : param.getListItemsValue()) {
                            if (object instanceof IElementParameter) {
                                if (((IElementParameter) object).getShowIf() != null
                                        && ((IElementParameter) object).getShowIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getNotShowIf() != null
                                        && ((IElementParameter) object).getNotShowIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getReadOnlyIf() != null
                                        && ((IElementParameter) object).getReadOnlyIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                                if (((IElementParameter) object).getNotReadOnlyIf() != null
                                        && ((IElementParameter) object).getNotReadOnlyIf().contains(tmpParam.getName())) {
                                    included = true;
                                    break;
                                }
                            }
                        }
                        if (included) {
                            IElementParameter param = element.getElementParameter(EParameterName.UPDATE_COMPONENTS.getName());
                            if (param != null) {
                                param.setValue(Boolean.TRUE);
                            }
                        }

                        switch (tmpParam.getFieldType()) {
                        case CONTEXT_PARAM_NAME_LIST:
                        case CLOSED_LIST:
                        case COLUMN_LIST:
                        case COMPONENT_LIST:
                        case CONNECTION_LIST:
                        case LOOKUP_COLUMN_LIST:
                        case PREV_COLUMN_LIST:
                            if (value instanceof String) {
                                Object[] itemNames = ((IElementParameter) itemsValue[curCol]).getListItemsDisplayName();
                                Object[] itemValues = ((IElementParameter) itemsValue[curCol]).getListItemsValue();
                                boolean found = false;
                                int index = 0;
                                for (int j = 0; j < itemNames.length && !found; j++) {
                                    if (itemNames[j].equals(value)) {
                                        found = true;
                                        index = j;
                                    }
                                }
                                if (value != null && (index >= 0)) {
                                    finalValue = itemValues[new Integer(index)];
                                }
                            }
                            break;
                        case COLOR:
                            if (value instanceof RGB) {
                                RGB rgb = (RGB) value;
                                finalValue = rgb.red + ";" + rgb.green + ";" + rgb.blue; //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        default:
                        }
                        ((Map<String, Object>) bean).put(items[curCol], finalValue);
                        resetValuesIfNeeded(element, param, (Map<String, Object>) bean);
                    }
                });
            }
        }
    }

    private boolean isEBCDICNode(INode node) {
        if (PluginChecker.isEBCDICPluginLoaded()) {
            IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                    IEBCDICProviderService.class);
            if (service != null) {
                return service.isEbcdicNode(node);
            }
        }
        return false;
    }

    private boolean isRepositorySchemaLine(INode node, Map<String, Object> lineValue) {
        if (PluginChecker.isEBCDICPluginLoaded()) {
            IEBCDICProviderService service = (IEBCDICProviderService) GlobalServiceRegister.getDefault().getService(
                    IEBCDICProviderService.class);
            if (service != null) {
                EbcdicConnectionItem repositoryItem = service.getRepositoryItem(node);
                return repositoryItem != null && service.isRepositorySchemaLine(node, lineValue);
            }
        }
        return false;
    }

    public PropertiesTableToolbarEditorView getToolBar() {
        return (PropertiesTableToolbarEditorView) getExtendedToolbar();
    }

    public PropertiesTableEditorModel getModel() {
        return (PropertiesTableEditorModel) getExtendedTableModel();
    }

    private void resetValuesIfNeeded(IElement element, IElementParameter mainParam, Map<String, Object> currentLine) {
        List<Map<String, Object>> tableValues = (List<Map<String, Object>>) mainParam.getValue();
        int rowNumber = tableValues.indexOf(currentLine);
        if (rowNumber != -1) {
            for (Object item : mainParam.getListItemsValue()) {
                if (item instanceof IElementParameter) {
                    IElementParameter curParam = (IElementParameter) item;
                    switch (curParam.getFieldType()) {
                    case CLOSED_LIST:
                        String[] itemsToDisplay = getItemsToDisplay(element, curParam, rowNumber);
                        Object currentValue = currentLine.get(curParam.getName());
                        String currentDisplay = null;
                        if (currentValue instanceof Integer) {
                            currentDisplay = curParam.getListItemsDisplayName()[(Integer) currentValue];
                        } else if (currentValue instanceof String) {
                            int index = ArrayUtils.indexOf(curParam.getListItemsValue(), currentValue);
                            if (index != -1) {
                                currentDisplay = curParam.getListItemsDisplayName()[index];
                            }
                        }

                        if (currentDisplay != null) {
                            // if the current displayed shouldn't be used anymore, reset the value.
                            if (!ArrayUtils.contains(itemsToDisplay, currentDisplay)) {
                                String newValue = ""; //$NON-NLS-1$
                                if (itemsToDisplay.length > 0) {
                                    int index = ArrayUtils.indexOf(curParam.getListItemsDisplayName(), itemsToDisplay[0]);
                                    newValue = (String) curParam.getListItemsValue()[index];
                                }
                                currentLine.put(curParam.getName(), newValue);
                            }
                        }
                        break;
                    default:
                    }

                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "getItemsToDisplay".
     * 
     * @param element
     * @param param
     * @param rowNumber
     * @return
     */
    private String[] getItemsToDisplay(final IElement element, final IElementParameter param, int rowNumber) {
        if (param instanceof ElementParameter) {
            ((ElementParameter) param).setCurrentRow(rowNumber);
        }
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null && (itemsShowIf.length > 0)) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], element.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (String element2 : originalList) {
                stringToDisplay.add(element2);
            }
        }
        String[] listToDisplay = stringToDisplay.toArray(new String[0]);
        return listToDisplay;
    }

}
