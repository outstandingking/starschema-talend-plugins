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
package org.talend.core.repository;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.IComponentName;

/**
 * ggu class global comment. Detailled comment
 */
public class RepositoryComponentSetting implements IComponentName {

    private String name;

    private String repositoryType;

    private boolean withSchema;

    private String inputComponent, outputComponent, defaultComponent;

    private Class<Item>[] classes;

    private EDatabaseTypeName[] dbTypes;

    private IRepositoryComponentAgent agent;

    public RepositoryComponentSetting() {
        super();
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getRepositoryType() {
        if (this.repositoryType == null && this.getDbTypes() != null && this.getDbTypes().length > 0) {
            return "DATABASE:" + this.getDbTypes()[0].getProduct(); //$NON-NLS-1$
        }
        return repositoryType;
    }

    void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public boolean withSchema() {
        return withSchema;
    }

    void setWithSchema(boolean withSchema) {
        this.withSchema = withSchema;
    }

    public String getInputComponent() {
        return inputComponent;
    }

    void setInputComponent(String inputComponent) {
        this.inputComponent = inputComponent;
    }

    public String getOutputComponent() {
        return outputComponent;
    }

    void setOutputComponent(String outputComponent) {
        this.outputComponent = outputComponent;
    }

    public String getDefaultComponent() {
        if (this.defaultComponent == null) { // use input as default
            return this.inputComponent;
        }
        return defaultComponent;
    }

    void setDefaultComponent(String defaultComponent) {
        this.defaultComponent = defaultComponent;
    }

    public Class<Item>[] getClasses() {
        return classes;
    }

    @SuppressWarnings("unchecked")
    void setClass(Class<Item> clazz) {
        if (clazz != null) {
            this.setClasses(new Class[] { clazz });
        }
    }

    void setClasses(Class<Item>[] classes) {
        this.classes = classes;
    }

    public EDatabaseTypeName[] getDbTypes() {
        return dbTypes;
    }

    void setDbTypes(EDatabaseTypeName[] dbTypes) {
        this.dbTypes = dbTypes;
    }

    void setDbType(EDatabaseTypeName dbType) {
        if (dbType != null) {
            this.setDbTypes(new EDatabaseTypeName[] { dbType });
        }
    }

    public IRepositoryComponentAgent getAgent() {
        return agent;
    }

    public void setAgent(IRepositoryComponentAgent agent) {
        this.agent = agent;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof RepositoryComponentSetting)) {
            return false;
        }
        RepositoryComponentSetting other = (RepositoryComponentSetting) obj;
        return other.getName().equals(this.getName());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getDefaultComponentName() {
        return this.getDefaultComponent();
    }

    public String getInputComponentName() {
        return this.getInputComponent();
    }

    public String getOutPutComponentName() {
        return this.getOutputComponent();
    }

}
