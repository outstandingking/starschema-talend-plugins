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
package org.talend.core.model.process;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.ui.IEditorPart;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.repository.IRepositoryObject;
import org.talend.core.model.update.IUpdateManager;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * This interface should list all functions used only for graphical purpose of the process.
 */
public interface IProcess2 extends IRepositoryObject, IProcess {

    boolean checkReadOnly();

    ProcessType saveXmlFile() throws IOException;

    void setPropertyValue(String id, Object value);

    void updateProperties();

    /**
     * DOC qzhang Comment method "loadXmlFile".
     * 
     * @param process
     */
    void loadXmlFile();

    void loadXmlFile(boolean loadScreenshots);

    /**
     * DOC qzhang Comment method "checkLoadNodes".
     */
    void checkLoadNodes() throws PersistenceException;

    /**
     * DOC qzhang Comment method "setXmlStream".
     * 
     * @param contents
     */
    void setXmlStream(InputStream contents);

    List getElements();

    /**
     * DOC qzhang Comment method "setActivate".
     * 
     * @param b
     */
    void setActivate(boolean b);

    /**
     * DOC qzhang Comment method "checkStartNodes".
     */
    void checkStartNodes();

    /**
     * DOC qzhang Comment method "checkProcess".
     */
    void checkProcess();

    /**
     * DOC qzhang Comment method "setProcessModified".
     * 
     * @param processModified
     */
    void setProcessModified(boolean processModified);

    boolean isProcessModified();

    public CommandStack getCommandStack();

    public List<? extends ISubjobContainer> getSubjobContainers();

    public IUpdateManager getUpdateManager();

    public byte[] getScreenshot();

    public void setScreenshot(byte[] image);

    public void dispose();

    public List<NodeType> getUnloadedNode();

    public void checkTableParameters();

    // for tmap ErrorReject
    public String generateUniqueConnectionName(String baseName, String tableName);

    boolean disableRunJobView();

    public IEditorPart getEditor();

    void removeUniqueNodeName(String uniqueName);

    void addUniqueNodeName(String uniqueName);

    boolean isGridEnabled();

    public void updateSubjobContainers();

    public IContext getLastRunContext();

    public void setLastRunContext(IContext context);

    boolean isSubjobEnabled();

}
