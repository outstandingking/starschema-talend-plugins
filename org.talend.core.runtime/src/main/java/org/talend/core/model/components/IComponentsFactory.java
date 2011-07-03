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
package org.talend.core.model.components;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Defines methods to use components. Implementation from extension point is given by ComponentsFactoryProvider<br/>
 * 
 * $Id: IComponentsFactory.java 46516 2010-08-09 11:55:19Z wchen $
 * 
 */
public interface IComponentsFactory {

    String COMPONENTS_LOCATION = "org.talend.designer.components.localprovider"; //$NON-NLS-1$

    String CAMEL_COMPONENTS_LOCATION = "org.talend.designer.camel.components.localprovider";

    String COMPONENTS_INNER_FOLDER = "components"; //$NON-NLS-1$

    String EXTERNAL_COMPONENTS_INNER_FOLDER = "ext"; //$NON-NLS-1$

    String COMPONENT_DEFINITION = "org.talend.core.component_definition"; //$NON-NLS-1$

    public void reset();

    public void resetCache();

    public void resetSpecificComponents(); // mainly to reload only joblets

    public int size();

    public Set<IComponent> getComponents();

    public List<IComponent> getCustomComponents();

    public IComponent get(String name);

    public URL getComponentPath() throws IOException;

    public List<String> getSkeletons();

    public String getFamilyTranslation(Object component, String text);

    public void loadUserComponentsFromComponentsProviderExtension();

    public Map<String, ImageDescriptor> getAllComponentsCanBeProvided();

    public void initializeComponents(IProgressMonitor monitor);

    public void initializeComponents(IProgressMonitor monitor, boolean duringLogon);
}
