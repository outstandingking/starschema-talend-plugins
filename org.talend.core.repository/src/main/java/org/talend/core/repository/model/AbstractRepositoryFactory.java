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
package org.talend.core.repository.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.data.container.RootContainer;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.IRepositoryWorkUnitListener;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.ERepositoryStatus;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public abstract class AbstractRepositoryFactory implements IRepositoryFactory {

    private String name;

    private String id;

    private boolean displayToUser;

    private boolean authenticationNeeded;

    private List<DynamicFieldBean> fields = new ArrayList<DynamicFieldBean>();

    private List<DynamicButtonBean> buttons = new ArrayList<DynamicButtonBean>();

    private List<DynamicChoiceBean> choices = new ArrayList<DynamicChoiceBean>();

    private boolean loggedOnProject = false;

    private List<IRepositoryWorkUnitListener> listeners = new ArrayList<IRepositoryWorkUnitListener>();

    public List<DynamicButtonBean> getButtons() {
        return buttons;
    }

    public List<DynamicChoiceBean> getChoices() {
        return choices;
    }

    /**
     * Getter for authenticationNeeded.
     * 
     * @return the authenticationNeeded
     */
    public boolean isAuthenticationNeeded() {
        return this.authenticationNeeded;
    }

    /**
     * Sets the authenticationNeeded.
     * 
     * @param authenticationNeeded the authenticationNeeded to set
     */
    public void setAuthenticationNeeded(boolean authenticationNeeded) {
        this.authenticationNeeded = authenticationNeeded;
    }

    /**
     * Getter for name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DynamicFieldBean> getFields() {
        return this.fields;
    }

    public void setFields(List<DynamicFieldBean> fields) {
        this.fields = fields;
    }

    public RepositoryContext getRepositoryContext() {
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        return (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Generates the next id for serializable. If no serializable returns 0.
     * 
     * @param project the project to scan
     * 
     * @return the next id for the project
     * @throws PersistenceException
     * @throws PersistenceException if processes cannot be retrieved
     */
    public String getNextId() {
        return EcoreUtil.generateUUID();
    }

    private void collect(RootContainer<String, IRepositoryViewObject> rootContainer, List<ConnectionItem> result)
            throws PersistenceException {
        for (IRepositoryViewObject repositoryObject : rootContainer.getAbsoluteMembers().objects()) {
            ConnectionItem connectionItem = (ConnectionItem) repositoryObject.getProperty().getItem();
            if (getStatus(connectionItem) != ERepositoryStatus.DELETED) {
                result.add(connectionItem);
            }
        }
    }

    // gather all the metadata connections (file / db / etc ...)
    public List<ConnectionItem> getMetadataConnectionsItem(Project project) throws PersistenceException {

        List<ConnectionItem> result = new ArrayList<ConnectionItem>();

        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_DELIMITED), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_POSITIONAL), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_REGEXP), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_XML), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_EXCEL), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_FILE_LDIF), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_CONNECTIONS), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_SAPCONNECTIONS), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_HEADER_FOOTER), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_LDAP_SCHEMA), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_GENERIC_SCHEMA), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_WSDL_SCHEMA), result);
        collect(getMetadata(project, ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA), result);

        return result;
    }

    // gather all the contexts
    public List<ContextItem> getContextItem(Project project) throws PersistenceException {
        List<ContextItem> result = new ArrayList<ContextItem>();

        for (IRepositoryViewObject repositoryObject : getMetadata(project, ERepositoryObjectType.CONTEXT).getAbsoluteMembers()
                .objects()) {
            ContextItem contextItem = (ContextItem) repositoryObject.getProperty().getItem();
            if (getStatus(contextItem) != ERepositoryStatus.DELETED) {
                result.add(contextItem);
            }
        }

        return result;
    }

    public boolean isDisplayToUser() {
        return displayToUser;
    }

    public void setDisplayToUser(boolean displayToUser) {
        this.displayToUser = displayToUser;
    }

    public void beforeLogon(Project project) throws PersistenceException, LoginException {
        // do nothing by default
    }

    public void beforeLogon(Project project, long revisonNum) throws PersistenceException, LoginException {
        // do nothing by default
    }

    public boolean isUserReadOnlyOnCurrentProject() {
        return false;
    }

    public void checkAvailability() {
        // is available by default
    }

    @SuppressWarnings("unchecked")
    public void executeRepositoryWorkUnit(RepositoryWorkUnit workUnit) {
        workUnit.executeRun();
    }

    public void logOffProject() {
        loggedOnProject = false;
        listeners.clear();
    }

    public boolean isLoggedOnProject() {
        return loggedOnProject;
    }

    public void setLoggedOnProject(boolean loggedOnProject) {
        this.loggedOnProject = loggedOnProject;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.talend.repository.model.IRepositoryFactory#addRepositoryWorkUnitListener(org.talend.repository.model.
     * IRepositoryWorkUnitListener)
     */
    public void addRepositoryWorkUnitListener(IRepositoryWorkUnitListener listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    public void runRepositoryWorkUnitListeners() {
        List<IRepositoryWorkUnitListener> list = new ArrayList<IRepositoryWorkUnitListener>();
        synchronized (lock) {
            list.addAll(listeners);
        }
        for (IRepositoryWorkUnitListener listener : list) {
            listener.workUnitFinished();
        }
        synchronized (lock) {
            listeners.removeAll(list);
        }
    }

    private Object lock = new Object();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.model.IRepositoryFactory#readProject(boolean)
     */
    public Project[] readProject(boolean unloadResource) throws PersistenceException, BusinessException {
        return readProject();
    }
    @Override
    public void updateLockStatus() throws PersistenceException {
        // nothing to do, by default
    }
}
