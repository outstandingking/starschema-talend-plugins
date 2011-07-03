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
package org.talend.core.model.process;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.components.IODataComponentContainer;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.designer.core.model.utils.emf.talendfile.AbstractExternalData;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: IExternalNode.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public interface IExternalNode extends INode {

    public void initialize();

    /**
     * Open a modal swt Shell.
     * 
     * @param display
     * @return SWT.OK / SWT.CANCEL
     */
    // functions to implement
    public int open(Display display);

    public int open(Composite parent);

    public void setExternalData(IExternalData persistentData);

    public IExternalData getExternalData();

    public List<Problem> getProblems();

    // functions already implemented in abstract external node

    public void setComponentName(String componentName);

    public void setIncomingConnections(List<? extends IConnection> incomingConnections);

    public void setOutgoingConnections(List<? extends IConnection> outgoingConnections);

    public void setMetadataList(List<IMetadataTable> metadataList);

    public void setPluginFullName(String pluginFullName);

    public void setUniqueName(String uniqueName);

    public void setActivate(boolean activate);

    public void setStart(boolean start);

    public void setSubProcessStart(boolean subProcessStart);

    public abstract void renameInputConnection(String oldName, String newName);

    public abstract void renameOutputConnection(String oldName, String newName);

    public void setIODataComponents(IODataComponentContainer components);

    public IODataComponentContainer getIODataComponents();

    public IComponentDocumentation getComponentDocumentation(String componentName, String tempFolderPath);

    public boolean isRunRefSubProcessAtStart(String connectionName);

    public ImageDescriptor getScreenshot();

    public void setScreenshot(ImageDescriptor screenshot);

    public IExternalData getTMapExternalData();

    public INode getOriginalNode();

    public void setOriginalNode(INode originalNode);

    public AbstractExternalData getExternalEmfData();

    public void setExternalEmfData(AbstractExternalData externalEmfData);

    public void buildExternalData(AbstractExternalData abstractData);
}
