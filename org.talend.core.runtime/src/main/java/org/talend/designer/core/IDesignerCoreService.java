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
package org.talend.designer.core;

import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.talend.core.IService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.Problem;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.UpdateResult;
import org.talend.core.utils.CsvArray;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.runprocess.ProcessorException;

/**
 * Provides Designer core services for other components <br/>
 * .
 * 
 * $Id: IDesignerCore.java 1 2006-12-19 上午10:16:43 bqian
 * 
 */
public interface IDesignerCoreService extends IService {

    // ¨Process will be loaded from XML File with this method, so it can be slow
    // This won't load the graphics of the job, only the datas
    public IProcess getProcessFromProcessItem(ProcessItem processItem, boolean loadScreenshots);

    public IProcess getProcessFromProcessItem(ProcessItem processItem);

    // item will be ProcessItem of JobletProcessItem
    public IProcess getProcessFromItem(Item item);

    public List<IProcess2> getOpenedProcess(IEditorReference[] reference);

    public List<IProcess> getProcessForJobTemplate();

    public Item getProcessItem(MultiPageEditorPart talendEditor);

    // Used for generating HTML only
    public List<Map> getMaps();

    // Ends
    public String getRepositoryAliasName(ConnectionItem connectionItem);

    public ILabelProvider getGEFEditorNodeLabelProvider();

    // add for feature 840
    public void saveJobBeforeRun(IProcess activeProcess);

    public IProcess getCurrentProcess();

    public void synchronizeDesignerUI(PropertyChangeEvent evt);

    public String getPreferenceStore(String key);

    public IPreferenceStore getDesignerCorePreferenceStore();

    public PaletteRoot createPalette(IComponentsFactory factory);

    public PaletteRoot createPalette(IComponentsFactory factory, boolean isFavorite);

    public PaletteRoot getAllNodeStructure(IComponentsFactory factory);

    public void setPaletteFilter(String filter);

    public PaletteRoot createPalette(IComponentsFactory compFac, PaletteRoot root);

    public PaletteRoot createPalette(IComponentsFactory compFac, PaletteRoot root, boolean isFavorite);

    public PaletteDrawer createTalendPaletteDrawer(String family);

    public IAction getCreateProcessAction(boolean isToolbar);

    public IAction getCreateBeanAction(boolean isToolbar);

    /**
     * tang Comment method "getProcessFromJobletProcessItem".
     * 
     * @param item
     * @return
     */
    public IProcess getProcessFromJobletProcessItem(JobletProcessItem item);

    /**
     * yzhang Comment method "createJobletEtnry".
     */
    public List<PaletteEntry> createJobletEtnry();

    public boolean isTalendEditor(IEditorPart activeEditor);

    public INode getRefrenceNode(String componentName);

    public boolean executeUpdatesManager(List<UpdateResult> results, boolean onlySimpleShow);

    public Map<String, Date> getLastGeneratedJobsDateMap();

    /**
     * 
     * DOC YeXiaowei Comment method "getDisplayForProcessParameterFromName".
     * 
     * @param name
     * @return
     */
    public String getDisplayForProcessParameterFromName(final String name);

    public void removeConnection(INode node, String schemaName);

    public CsvArray convertNode(ConnectionItem connectionItem, String tableName) throws ProcessorException;

    public void updateTraceColumnValues(IConnection conn, Map<String, String> changedNameColumns, Set<String> addedColumns);

    public void setTraceFilterParameters(INode node, IMetadataTable table, Set<String> preColumnSet,
            Map<String, String> changedNameColumns);

    public IConnection getConnection(List<? extends IConnection> connections, IMetadataTable table);

    public void createStatsLogAndImplicitParamter(Project project);

    public void removeJobLaunch(IRepositoryViewObject objToDelete);

    public void renameJobLaunch(IRepositoryViewObject obj, String originalName);

    public boolean isDummyComponent(IComponent component);

    public void addProblems(Problem problem);

    public void reloadParamFromProjectSettings(ParametersType processType, String paramName);

    public Set<String> getNeededLibrariesForProcess(IProcess process, boolean withChildrens);

    public void switchToCurContextsView();

    public void switchToCurComponentSettingsView();

    public void switchToCurJobSettingsView();

    /**
     * When database connection is renamed, refresh the connection label in the component view of job.
     * 
     * @param item
     */
    public void refreshComponentView(Item item);

    /**
     * DOC guanglong.du Comment method "createEmptyPalette".
     * 
     * @return
     */
    public PaletteRoot createEmptyPalette();

}
