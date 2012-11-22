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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.components.IMultipleComponentConnection;
import org.talend.core.model.components.IMultipleComponentItem;
import org.talend.core.model.components.IMultipleComponentManager;
import org.talend.core.model.components.IMultipleComponentParameter;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: MultipleComponentManager.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class MultipleComponentManager implements IMultipleComponentManager {

    private IMultipleComponentItem input;

    private IMultipleComponentItem output;

    private String inputName;

    private String outputName;

    private String connector = null;

    private boolean existsLinkTo = false;

    private List<IMultipleComponentItem> itemList = new ArrayList<IMultipleComponentItem>();

    private List<IMultipleComponentParameter> paramList = new ArrayList<IMultipleComponentParameter>();

    private boolean lookupMode;

    public MultipleComponentManager(boolean lookupMode) {
        this.lookupMode = lookupMode;
    }

    public MultipleComponentManager(String inputName, String outputName) {
        this.inputName = inputName;
        this.outputName = outputName;
    }

    public MultipleComponentManager(String inputName, String outputName, String connector) {
        this(inputName, outputName);
        this.connector = connector;
    }

    public IMultipleComponentItem addItem(String name, String component) {
        MultipleComponentItem currentItem = new MultipleComponentItem();

        if (name.equals(inputName)) {
            input = currentItem;
        }
        if (name.equals(outputName)) {
            output = currentItem;
        }

        currentItem.setName(name);
        currentItem.setComponent(component);
        itemList.add(currentItem);

        return currentItem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IMultipleComponentManager#addParam(java.lang.String, java.lang.String)
     */
    public void addParam(String source, String target) {
        paramList.add(new MultipleComponentParameter(source, target));
    }

    public void addValue(String target, String value) {
        paramList.add(new MultipleComponentParameterValue(target, value));
    }

    public void validateItems() {
        for (IMultipleComponentItem mainItem : itemList) {
            for (IMultipleComponentConnection connection : mainItem.getOutputConnections()) {
                String nameLinkedTo = connection.getNameTarget();
                if (nameLinkedTo != null) {
                    boolean found = false;
                    for (int i = 0; i < itemList.size() && !found; i++) {
                        IMultipleComponentItem linkedItem = itemList.get(i);
                        if (linkedItem.getName().equals(nameLinkedTo)) {
                            connection.setSource(mainItem);
                            connection.setTarget(linkedItem);
                            linkedItem.getInputConnections().add(connection);
                            found = true;
                        }
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentManager#getInput()
     */
    public IMultipleComponentItem getInput() {
        return this.input;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.model.components.IMultipleComponentManager#getOutput()
     */
    public IMultipleComponentItem getOutput() {
        return this.output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.core.model.components.IMultipleComponentManager#getOutputName()
     */
    public String getOutputName() {
        return this.outputName;
    }

    public List<IMultipleComponentItem> getItemList() {
        return this.itemList;
    }

    public List<IMultipleComponentParameter> getParamList() {
        return this.paramList;
    }

    public boolean isSetConnector() {
        return this.connector != null;
    }

    public String getConnector() {
        return this.connector;
    }

    public boolean existsLinkTo() {
        return this.existsLinkTo;
    }

    public void setExistsLinkTo(boolean flag) {
        this.existsLinkTo = flag;
    }

    /**
     * Getter for lookupMode.
     * 
     * @return the lookupMode
     */
    public boolean isLookupMode() {
        return this.lookupMode;
    }

}
