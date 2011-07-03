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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQItemService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Generate grammar java source files and store them to routines for tStandardizeRow. see feature 18851
 * 
 * DOC ytao class global comment. Detailled comment
 */
public class GenerateGrammarController extends AbstractElementPropertySectionController {

    public GenerateGrammarController(IDynamicProperty dp) {
        super(dp);
    }

    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void refresh(IElementParameter param, boolean check) {

    }

    SelectionListener listenerSelection = new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e) {

        }

        public void widgetSelected(SelectionEvent e) {
            generateJavaFile();

            IRepositoryView viewPart = (IRepositoryView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .findView(IRepositoryView.VIEW_ID);

            viewPart.refreshView();
        }

    };

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CorePlugin.getImageDescriptor(DOTS_BUTTON)));
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnEdit.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /**
     * create a button and a listener
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button btnEdit;
        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CorePlugin.getImageDescriptor(DOTS_BUTTON)));
        FormData data;
        btnEdit.addSelectionListener(listenerSelection);

        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }
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
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);

        return btnEdit;
    }

    /**
     * Generate java source file
     * 
     * DOC ytao Comment method "generateJavaFile".
     */
    private void generateJavaFile() {
        Node node = (Node) elem;

        final String PROJECT_NAME = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel().toLowerCase();
        final String JOB_NAME = node.getProcess().getName().toLowerCase();
        final String COMPONENT_NAME = node.getUniqueName().toLowerCase();

        String javaClassName = StringUtils.capitalize(PROJECT_NAME) + StringUtils.capitalize(JOB_NAME)
                + StringUtils.capitalize(COMPONENT_NAME);
        ITDQItemService service = (ITDQItemService) GlobalServiceRegister.getDefault().getService(ITDQItemService.class);
        File fileCreated = service.fileCreatedInRoutines(node, javaClassName);

        if (fileCreated == null)
            return;

        try {
            RoutineItem returnItem = persistInRoutine(new Path(JOB_NAME), fileCreated, javaClassName);
            addReferenceJavaFile(returnItem, true);
            refreshProject();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        // remove temporary files of grammar
        FilesUtils.removeFolder(new File(fileCreated.getParent()), true);
    }

    /**
     * Persist item in routines
     * 
     * DOC ytao Comment method "persistInRoutine".
     * 
     * @param path, sub folder named with job id
     * @param label, java file name without suffix
     * @param initFile, File handler
     * @param name, job id as package name
     * @return
     */
    private RoutineItem persistInRoutine(IPath inFolder, File fileToFill, String label) {

        // item property to be set
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode("");
        // Label must match pattern ^[a-zA-Z\_]+[a-zA-Z0-9\_]*$
        // Must be composed with JAVA_PORJECT_NAME + JOB NAME + COMPONENT NAME,
        // since all projects share with the same routines
        property.setLabel(label);

        // add properties to item
        RoutineItem routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();
        routineItem.setProperty(property);

        // get the content of java file as byte array.
        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        InputStream stream = null;

        try {
            stream = new FileInputStream(fileToFill);
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            byteArray.setInnerContent(bytes);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        routineItem.setContent(byteArray);

        // persist item in routines
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

        try {
            property.setId(repositoryFactory.getNextId());
            // create folder with name job id: routines/JOBID (seems from TOS)
            repositoryFactory.createParentFoldersRecursively(ERepositoryObjectType.getItemType(routineItem), inFolder);
            // add the item
            repositoryFactory.create(routineItem, inFolder);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        // add required jar packages used to compile java file
        if (routineItem.eResource() != null) {
            addRequiredLib(routineItem);
        }

        return routineItem;
    }

    /**
     * Store file to file system. Actually, it locates src/routines/xx DOC ytao Comment method "addReferenceJavaFile".
     * 
     * @param routineItem
     * @param copyToTemp
     * @return
     * @throws SystemException
     */
    private IFile addReferenceJavaFile(RoutineItem routineItem, boolean copyToTemp) throws SystemException {
        FileOutputStream fos = null;

        try {
            IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
            IProject javaProject = service.getProject(ECodeLanguage.JAVA);
            String label = routineItem.getProperty().getLabel();

            IFile file = javaProject.getFile(JavaUtils.JAVA_SRC_DIRECTORY + "/" + JavaUtils.JAVA_ROUTINES_DIRECTORY + "/" //$NON-NLS-1$ //$NON-NLS-2$
                    + label + JavaUtils.JAVA_EXTENSION);

            if (copyToTemp) {
                String routineContent = new String(routineItem.getContent().getInnerContent());

                if (!label.equals(ITalendSynchronizer.TEMPLATE)) {
                    routineContent = routineContent.replaceAll(ITalendSynchronizer.TEMPLATE, label);
                    File f = file.getLocation().toFile();
                    fos = new FileOutputStream(f);
                    fos.write(routineContent.getBytes());
                    fos.close();
                }
            }
            if (!file.exists()) {
                file.refreshLocal(1, null);
            }
            return file;
        } catch (Exception e) {
            throw new SystemException(e);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                // ignore me even if i'm null
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * refresh the project
     * 
     * DOC ytao Comment method "refreshProject".
     */
    private void refreshProject() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject(JavaUtils.JAVA_PROJECT_NAME);
        try {
            prj.build(IncrementalProjectBuilder.AUTO_BUILD, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * add required libraries to class path
     * 
     * DOC ytao Comment method "addRequiredLib".
     * 
     * @param routineItem
     */
    private void addRequiredLib(RoutineItem routineItem) {
        List<IMPORTType> listRequiredJar = new ArrayList<IMPORTType>();
        String javaLabPath = CorePlugin.getDefault().getLibrariesService().getJavaLibrariesPath() + "/";

        IMPORTType type1 = ComponentFactory.eINSTANCE.createIMPORTType();
        type1.setMODULE("antlr-3.3.jar");
        type1.setUrlPath(javaLabPath + "antlr-3.3.jar");
        type1.setREQUIRED(true);
        type1.setNAME(routineItem.getProperty().getLabel());
        listRequiredJar.add(type1);

        IMPORTType type2 = ComponentFactory.eINSTANCE.createIMPORTType();
        type2.setMODULE("org.talend.dataquality.parser.jar");
        type2.setUrlPath(javaLabPath + "org.talend.dataquality.parser.jar");
        type2.setREQUIRED(true);
        type2.setNAME(routineItem.getProperty().getLabel());
        listRequiredJar.add(type2);

        routineItem.getImports().addAll(listRequiredJar);

        try {
            File url1 = new File(javaLabPath + "antlr-3.3.jar");
            File url2 = new File(javaLabPath + "org.talend.dataquality.parser.jar");

            CorePlugin.getDefault().getLibrariesService().deployLibrary(url1.toURL());
            CorePlugin.getDefault().getLibrariesService().deployLibrary(url2.toURL());
            CorePlugin.getDefault().getProxyRepositoryFactory().save(routineItem);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
    }
}
