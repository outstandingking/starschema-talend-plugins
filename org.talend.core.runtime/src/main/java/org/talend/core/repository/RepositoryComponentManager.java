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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 * ggu class global comment. Detailled comment
 */
public final class RepositoryComponentManager {

    private static final String EMPTY = ""; //$NON-NLS-1$

    public static final String OUTPUT = "Output"; //$NON-NLS-1$

    public static final String INPUT = "Input"; //$NON-NLS-1$

    private static final List<RepositoryComponentSetting> repComponentSettings;

    private static final List<RepositoryComponentDndFilterSetting> repComponentDndFilterSettings;

    private static final List<SortedComponentSetting> sortedComponentSetting;

    private static final List<SortedComponentSetting> specialSortedComponentSetting;
    static {
        repComponentSettings = new ArrayList<RepositoryComponentSetting>();
        repComponentDndFilterSettings = new ArrayList<RepositoryComponentDndFilterSetting>();
        sortedComponentSetting = new ArrayList<SortedComponentSetting>();
        specialSortedComponentSetting = new ArrayList<SortedComponentSetting>();

        init();

        // sort by level
        Collections.sort(repComponentDndFilterSettings, new Comparator<RepositoryComponentDndFilterSetting>() {

            public int compare(RepositoryComponentDndFilterSetting o1, RepositoryComponentDndFilterSetting o2) {
                return o1.getLevel() - o2.getLevel();
            }
        });
        Collections.sort(sortedComponentSetting, new Comparator<SortedComponentSetting>() {

            public int compare(SortedComponentSetting o1, SortedComponentSetting o2) {
                return o1.getLevel() - o2.getLevel();
            }
        });
        Collections.sort(specialSortedComponentSetting, new Comparator<SortedComponentSetting>() {

            public int compare(SortedComponentSetting o1, SortedComponentSetting o2) {
                return o1.getLevel() - o2.getLevel();
            }
        });
    }

    private static void init() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.core.runtime.repositoryComponent_provider"); //$NON-NLS-1$
        List<String> repositoryComponentNames = new ArrayList<String>();
        List<String> dndFilterIds = new ArrayList<String>();
        List<String> componentIds = new ArrayList<String>();

        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            if (element.getName().equals("ExtensionFilter")) { //$NON-NLS-1$
                //
                List<String> filterAttrs = getFilterAttrs(element, "RepositoryComponentFilter", //$NON-NLS-1$
                        "repositoryComponentName"); //$NON-NLS-1$
                repositoryComponentNames.addAll(filterAttrs);

                //
                filterAttrs = getFilterAttrs(element, "DragAndDropFilterFilter", //$NON-NLS-1$
                        "dndFilterId"); //$NON-NLS-1$
                dndFilterIds.addAll(filterAttrs);

                //
                filterAttrs = getFilterAttrs(element, "SortedComponentFilter", //$NON-NLS-1$
                        "componentId"); //$NON-NLS-1$
                componentIds.addAll(filterAttrs);
            }
        }

        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            if (element.getName().equals("RepositoryComponent")) { //$NON-NLS-1$
                String name = element.getAttribute("name"); //$NON-NLS-1$
                if (repositoryComponentNames.contains(name)) {
                    continue; // filter
                }
                String type = element.getAttribute("type"); //$NON-NLS-1$
                boolean withSchema = Boolean.parseBoolean(element.getAttribute("withSchema")); //$NON-NLS-1$
                String input = element.getAttribute("input"); //$NON-NLS-1$
                String output = element.getAttribute("output"); //$NON-NLS-1$
                String def = element.getAttribute("default"); //$NON-NLS-1$

                IRepositoryComponentAgent agent = null;
                if (element.getAttribute("agent") != null) { //$NON-NLS-1$
                    try {
                        Object object = element.createExecutableExtension("agent"); //$NON-NLS-1$
                        if (object != null && (object instanceof IRepositoryComponentAgent)) {
                            agent = (IRepositoryComponentAgent) object;
                        }
                    } catch (Exception e) {
                        //
                    }
                }

                RepositoryComponentSetting setting = new RepositoryComponentSetting();
                setting.setName(name);
                setting.setRepositoryType(type);
                setting.setWithSchema(withSchema);
                setting.setInputComponent(input);
                setting.setOutputComponent(output);
                setting.setDefaultComponent(def);
                setting.setClasses(retrieveClasses(element));
                setting.setDbTypes(retrieveDBTypes(element));
                setting.setAgent(agent);

                repComponentSettings.add(setting);
            } else if (element.getName().equals("DragAndDropFilter")) { //$NON-NLS-1$
                String id = element.getAttribute("id"); //$NON-NLS-1$
                if (dndFilterIds.contains(id)) {
                    continue; // filter
                }
                String name = element.getAttribute("name"); //$NON-NLS-1$
                int level = parserLevel(element.getAttribute("level")); //$NON-NLS-1$
                IRepositoryComponentDndFilter filter = null;
                try {
                    Object object = element.createExecutableExtension("clazz"); //$NON-NLS-1$
                    if (object == null || !(object instanceof IRepositoryComponentDndFilter)) {
                        throw new IllegalArgumentException("the argument of clazz is wrong");//$NON-NLS-1$
                    }
                    filter = (IRepositoryComponentDndFilter) object;
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }

                RepositoryComponentDndFilterSetting dndSetting = new RepositoryComponentDndFilterSetting();

                dndSetting.setId(id);
                dndSetting.setName(name);
                dndSetting.setLevel(level);
                dndSetting.setFilter(filter);

                repComponentDndFilterSettings.add(dndSetting);
            } else if (element.getName().equals("SortedComponents")) { //$NON-NLS-1$

                retrieveSortedComponent(componentIds, sortedComponentSetting, element, "Component"); //$NON-NLS-1$
                retrieveSortedComponent(componentIds, specialSortedComponentSetting, element, "SpecialComponent"); //$NON-NLS-1$

            }

        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Class<Item>[] retrieveClasses(IConfigurationElement parent) {
        IConfigurationElement[] children = parent.getChildren("Item"); //$NON-NLS-1$
        List<Class<Item>> list = new ArrayList<Class<Item>>();

        for (IConfigurationElement ce : children) {
            String className = ce.getAttribute("clazz"); //$NON-NLS-1$
            try {
                Class clazz = Class.forName(className);
                list.add(clazz);
            } catch (ClassNotFoundException e) {
                ExceptionHandler.process(e);
            }

        }
        return list.toArray(new Class[0]);
    }

    private static EDatabaseTypeName[] retrieveDBTypes(IConfigurationElement parent) {
        IConfigurationElement[] children = parent.getChildren("DBType"); //$NON-NLS-1$
        List<EDatabaseTypeName> list = new ArrayList<EDatabaseTypeName>();
        for (IConfigurationElement ce : children) {
            String type = ce.getAttribute("type"); //$NON-NLS-1$
            EDatabaseTypeName dbType = EDatabaseTypeName.getTypeFromDbType(type, false);
            if (dbType == null) { // process some the types
                for (EDatabaseTypeName dtn : EDatabaseTypeName.values()) {
                    if (dtn.name().equals(type)) {
                        dbType = dtn;
                        break;
                    }
                }
            }
            if (dbType != null) {
                list.add(dbType);

            }
        }
        return list.toArray(new EDatabaseTypeName[0]);
    }

    private static void retrieveSortedComponent(List<String> componentIds, List<SortedComponentSetting> componentSetting,
            IConfigurationElement parent, String extension) {
        IConfigurationElement[] children = parent.getChildren(extension);
        for (IConfigurationElement element : children) {
            String id = element.getAttribute("id"); //$NON-NLS-1$
            if (componentIds.contains(id)) {
                continue; // filter
            }
            String name = element.getAttribute("name"); //$NON-NLS-1$
            String pattern = element.getAttribute("pattern"); //$NON-NLS-1$
            String description = element.getAttribute("description"); //$NON-NLS-1$
            int level = parserLevel(element.getAttribute("level")); //$NON-NLS-1$

            SortedComponentSetting setting = new SortedComponentSetting();
            setting.setId(id);
            setting.setName(name);
            setting.setPattern(pattern);
            setting.setLevel(level);
            setting.setDescription(description);

            componentSetting.add(setting);
        }

    }

    private static int parserLevel(String levelStr, int... defaultValue) {
        int level = 0;
        if (defaultValue != null && defaultValue.length > 0) {
            level = defaultValue[0];
        }
        if (levelStr != null && !EMPTY.equals(levelStr.trim())) {
            try {
                level = Integer.parseInt(levelStr);
            } catch (NumberFormatException e) {
                // keep zero by default
            }
        }
        return level;
    }

    private static List<String> getFilterAttrs(IConfigurationElement parent, String extension, String attr) {
        List<String> filterAttrValues = new ArrayList<String>();
        IConfigurationElement[] children = parent.getChildren(extension);
        for (IConfigurationElement ce : children) {
            String filterAttr = ce.getAttribute(attr);
            if (filterAttr != null && EMPTY.equals(filterAttr.trim())) {
                filterAttrValues.add(filterAttr);
            }
        }
        return filterAttrValues;
    }

    static RepositoryComponentSetting[] getSettings() {
        return repComponentSettings.toArray(new RepositoryComponentSetting[0]);
    }

    static RepositoryComponentDndFilterSetting[] getDndFilterSettings() {
        return repComponentDndFilterSettings.toArray(new RepositoryComponentDndFilterSetting[0]);
    }

    static SortedComponentSetting[] getSortedComponentSetting() {
        return sortedComponentSetting.toArray(new SortedComponentSetting[0]);
    }

    static SortedComponentSetting[] getSpecialSortedComponentSetting() {
        return specialSortedComponentSetting.toArray(new SortedComponentSetting[0]);
    }

    public static RepositoryComponentSetting getSetting(Item item, ERepositoryObjectType type) {
        if (item == null) {
            return null;
        }
        boolean subItem = (type == ERepositoryObjectType.METADATA_CON_TABLE);

        for (RepositoryComponentSetting setting : getSettings()) {
            Class<Item>[] classes = setting.getClasses();
            if (classes != null) {
                for (Class<Item> clazz : classes) {
                    if (clazz.isAssignableFrom(item.getClass())) {
                        if (clazz.isAssignableFrom(DatabaseConnectionItem.class)) { // for db
                            EDatabaseTypeName[] dbTypes = setting.getDbTypes();
                            if (dbTypes != null) {
                                for (EDatabaseTypeName dbType : dbTypes) {
                                    DatabaseConnectionItem dbItem = (DatabaseConnectionItem) item;
                                    DatabaseConnection dbConnection = (DatabaseConnection) dbItem.getConnection();
                                    // use the display name as the database type
                                    if (dbType.getDisplayName().equals(dbConnection.getDatabaseType())) {
                                        if (!subItem) {
                                            return setting;
                                        }
                                        if (subItem && setting.withSchema()) {
                                            return setting;
                                        }
                                    }
                                }
                            }
                        } else {
                            return setting;
                        }
                    }
                }
            }
        }
        return null;

    }

    public static List<IComponent> filterNeededComponents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type) {
        if (!GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsService.class)) {
            return Collections.emptyList();
        }
        IComponentsService service = (IComponentsService) GlobalServiceRegister.getDefault().getService(IComponentsService.class);

        Set<IComponent> components = service.getComponentsFactory().getComponents();
        List<IComponent> neededComponents = new ArrayList<IComponent>();
        List<IComponent> exceptedComponents = new ArrayList<IComponent>();

        for (IComponent component : components) {
            //
            for (RepositoryComponentDndFilterSetting dndFilter : getDndFilterSettings()) {
                IRepositoryComponentDndFilter filter = dndFilter.getFilter();
                if (filter == null) {
                    continue;
                }
                String repositoryType = filter.getRepositoryType(item, type);
                if (repositoryType == null) {
                    continue;
                }
                if (!exceptedComponents.contains(component)
                        && filter.except(item, type, seletetedNode, component, repositoryType)) {
                    exceptedComponents.add(component);
                }
                // if have been excepted, so no need add it to needed component
                if (!exceptedComponents.contains(component) && !neededComponents.contains(component)
                        && filter.valid(item, type, seletetedNode, component, repositoryType)) {
                    neededComponents.add(component);
                }
            }
        }
        // remove all excepted components
        neededComponents.removeAll(exceptedComponents);

        return sortFilteredComponnents(item, seletetedNode, type, neededComponents);
    }

    private static List<IComponent> sortFilteredComponnents(Item item, RepositoryNode seletetedNode, ERepositoryObjectType type,
            List<IComponent> neededComponents) {

        List<IComponent> normalTopComponents = new ArrayList<IComponent>();
        List<IComponent> specialTopComponents = new ArrayList<IComponent>();

        for (IComponent component : neededComponents) {
            if (filterNeededComponent(getSpecialSortedComponentSetting(), component)) {
                specialTopComponents.add(component);
            } else if (filterNeededComponent(getSortedComponentSetting(), component)) {
                normalTopComponents.add(component);
            }
        }

        List<IComponent> sortedComponents = new ArrayList<IComponent>();
        sortedComponents.addAll(specialTopComponents);
        sortedComponents.addAll(normalTopComponents);

        // add the left components
        neededComponents.removeAll(specialTopComponents);
        neededComponents.removeAll(normalTopComponents);
        sortedComponents.addAll(neededComponents);

        return sortedComponents;
    }

    private static boolean filterNeededComponent(SortedComponentSetting[] settings, IComponent component) {
        if (settings != null && settings.length > 0 && component != null) {
            Pattern p = null;
            Matcher matcher = null;
            for (SortedComponentSetting setting : settings) {
                String pattern = setting.getPattern();
                if (pattern != null && !EMPTY.equals(pattern)) {
                    p = Pattern.compile(pattern);
                    matcher = p.matcher(component.getName());
                    if (matcher.find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean validComponent(String componentName) {
        if (componentName != null) {
            for (RepositoryComponentSetting setting : getSettings()) {
                final String inputComponent = setting.getInputComponent();
                final String outputComponent = setting.getOutputComponent();
                final String defaultComponent = setting.getDefaultComponent();
                if (componentName.equals(inputComponent) || componentName.equals(outputComponent)
                        || componentName.equals(defaultComponent)) {
                    return true;
                }
            }
        }
        return false;
    }

}
