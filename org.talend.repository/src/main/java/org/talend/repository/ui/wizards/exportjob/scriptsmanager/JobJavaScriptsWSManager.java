// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.Messages;
import org.apache.axis.wsdl.Java2WSDL;
import org.apache.axis.wsdl.fromJava.Emitter;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.documentation.ExportFileResource;

/**
 * DOC x class global comment. Detailled comment <br/>
 * 
 */
public class JobJavaScriptsWSManager extends JobJavaScriptsManager {

    private static Logger log = Logger.getLogger(ExceptionHandler.class);

    public static final String EXPORT_METHOD = "runJob"; //$NON-NLS-1$

    private static List<String> axisLib = new ArrayList<String>();
    static {
        axisLib.add("axis.jar"); //$NON-NLS-1$
        axisLib.add("jaxrpc.jar"); //$NON-NLS-1$
        axisLib.add("saaj.jar"); //$NON-NLS-1$
        axisLib.add("wsdl4j-1.5.1.jar"); //$NON-NLS-1$
        axisLib.add("commons-discovery-0.2.jar"); //$NON-NLS-1$
        axisLib.add("commons-logging-1.1.jar"); //$NON-NLS-1$
        axisLib.add("mail.jar"); //$NON-NLS-1$
        axisLib.add("activation.jar"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobJavaScriptsManager#getExportResources
     * (org.talend.repository.ui.wizards.exportjob.ExportFileResource[], java.util.Map, java.lang.String,
     * java.lang.String, int, int, java.lang.String[])
     */
    @Override
    public List<ExportFileResource> getExportResources(ExportFileResource[] process, Map<ExportChoice, Object> exportChoice,
            String contextName, String launcher, int statisticPort, int tracePort, String... codeOptions)
            throws ProcessorException {

        List<ExportFileResource> list = new ArrayList<ExportFileResource>();

        boolean needJob = true;
        boolean needSource = isOptionChoosed(exportChoice, ExportChoice.needSourceCode);
        boolean needContext = isOptionChoosed(exportChoice, ExportChoice.needContext);
        ExportFileResource libResource = new ExportFileResource(null, "WEB-INF/lib"); //$NON-NLS-1$
        ExportFileResource contextResource = new ExportFileResource(null, "WEB-INF/classes"); //$NON-NLS-1$
        ExportFileResource srcResource = new ExportFileResource(null, "WEB-INF"); //$NON-NLS-1$
        if (needJob) {
            list.add(libResource);
        }
        if (needContext) {
            list.add(contextResource);
        }
        if (needSource) {
            list.add(srcResource);
        }

        copyServerConfigFileToTempDir();

        // set export config mode now only to be sure that the libraries will be setup for an export mode, and not
        // editor mode.
        ProcessorUtilities.setExportConfig("java", "", ""); //$NON-NLS-1$

        // Gets talend libraries
        List<URL> talendLibraries = getExternalLibraries(true, process);
        libResource.addResources(talendLibraries);

        for (int i = 0; i < process.length; i++) {
            ProcessItem processItem = (ProcessItem) process[i].getItem();

            String selectedJobVersion = processItem.getProperty().getVersion();
            if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                selectedJobVersion = this.getSelectedJobVersion();
            }

            // generate the source files
            String libPath = calculateLibraryPathFromDirectory(process[i].getDirectoryName());
            // use character @ as temporary classpath separator, this one will be replaced during the export.
            String standardJars = libPath + PATH_SEPARATOR + SYSTEMROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR
                    + libPath + PATH_SEPARATOR + USERROUTINE_JAR + ProcessorUtilities.TEMP_JAVA_CLASSPATH_SEPARATOR + "."; //$NON-NLS-1$
            ProcessorUtilities.setExportConfig("java", standardJars, libPath); //$NON-NLS-1$

            if (!isOptionChoosed(exportChoice, ExportChoice.doNotCompileCode)) {
                generateJobFiles(processItem, contextName, selectedJobVersion, statisticPort != IProcessor.NO_STATISTICS,
                        tracePort != IProcessor.NO_TRACES, isOptionChoosed(exportChoice, ExportChoice.applyToChildren),
                        progressMonitor);
            }
            // generate the WSDL file
            ExportFileResource wsdlFile = getWSDLFile(processItem, isOptionChoosed(exportChoice, ExportChoice.needWSDL),
                    talendLibraries);
            list.add(wsdlFile);

            // edit the WSDD file
            editWSDDFile(processItem);

            // add children jobs
            boolean needChildren = true;
            addSubJobResources(process, processItem, needChildren, exportChoice, libResource, contextResource, srcResource,
                    selectedJobVersion);

            // generate the context file
            getContextScripts(processItem, needContext, contextResource, selectedJobVersion);

            // generate jar file for job
            libResource.addResources(getJobScripts(processItem, selectedJobVersion, needJob));

            // dynamic db xml mapping
            addXmlMapping(process[i], isOptionChoosed(exportChoice, ExportChoice.needSourceCode));

        }

        // generate Server Config file
        ExportFileResource serverConfigFile = getServerConfigFile(isOptionChoosed(exportChoice, ExportChoice.needCONFIGFILE));
        list.add(serverConfigFile);

        // generate the WSDD file
        ExportFileResource wsddFile = getWSDDFile(isOptionChoosed(exportChoice, ExportChoice.needWSDD));
        list.add(wsddFile);

        // generate the WEB-INFO folder
        ExportFileResource webInfoFolder = getWebXMLFile(isOptionChoosed(exportChoice, ExportChoice.needWEBXML));
        list.add(webInfoFolder);

        // generate the META-INFO folder
        ExportFileResource metaInfoFolder = genMetaInfoFolder(isOptionChoosed(exportChoice, ExportChoice.needMetaInfo));
        list.add(metaInfoFolder);

        // Gets system routines
        List<URL> systemRoutineList = getSystemRoutine(process, true);
        libResource.addResources(systemRoutineList);
        // Gets user routines
        List<URL> userRoutineList = getUserRoutine(process, true);
        libResource.addResources(userRoutineList);

        // Gets axis libraries
        List<URL> axisLibList = getLib(axisLib, isOptionChoosed(exportChoice, ExportChoice.needAXISLIB));
        libResource.addResources(axisLibList);

        // check the list avoid duplication

        return list;
    }

    private void addSubJobResources(ExportFileResource[] allResources, ProcessItem process, boolean needChildren,
            Map<ExportChoice, Object> exportChoice, ExportFileResource libResource, ExportFileResource contextResource,
            ExportFileResource srcResource, String selectedJobVersion) {

        List<JobInfo> list = new ArrayList<JobInfo>();
        String projectName = getCorrespondingProjectName(process);
        if (needChildren) {

            try {
                List<ProcessItem> processedJob = new ArrayList<ProcessItem>();
                getChildrenJobAndContextName(allResources, process.getProperty().getLabel(), list, process, projectName,
                        processedJob, srcResource, exportChoice, selectedJobVersion);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        for (Iterator<JobInfo> iter = list.iterator(); iter.hasNext();) {
            JobInfo jobInfo = iter.next();
            libResource.addResources(getJobScripts(projectName, jobInfo.getJobName(), jobInfo.getJobVersion(),
                    isOptionChoosed(exportChoice, ExportChoice.needJobScript)));
            addContextScripts(jobInfo.getProcessItem(), jobInfo.getJobName(), jobInfo.getJobVersion(), contextResource,
                    isOptionChoosed(exportChoice, ExportChoice.needContext));
        }

    }

    private void copyServerConfigFileToTempDir() {
        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        String sourceFileName;
        try {
            sourceFileName = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/server-config.wsdd"), null)) //$NON-NLS-1$
                    .getFile();
            String targetFileName = getTmpFolder() + PATH_SEPARATOR + "server-config.wsdd"; //$NON-NLS-1$
            FilesUtils.copyFile(new File(sourceFileName), new File(targetFileName));
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

    }

    protected void getContextScripts(ProcessItem processItem, Boolean needContext, ExportFileResource contextResource,
            String version) {
        String jobName = processItem.getProperty().getLabel();
        addContextScripts(processItem, jobName, version, contextResource, needContext);
    }

    /**
     * DOC x Comment method "genWSDLFolder".
     * 
     * @param list
     */
    private ExportFileResource getWSDLFile(ProcessItem processItem, Boolean needWSDL, List<URL> externalLibs) {
        ExportFileResource wsdl = new ExportFileResource(null, "wsdl"); //$NON-NLS-1$

        List<URL> wsdlUrlList = new ArrayList<URL>();
        try {
            String projectName = getCorrespondingProjectName(processItem);
            String jobName = processItem.getProperty().getLabel();
            String selectedProcessVersion = processItem.getProperty().getVersion();

            if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
                selectedProcessVersion = this.getSelectedJobVersion();
            }

            String jobFolderName = JavaResourcesHelper.getJobFolderName(escapeFileNameSpace(processItem), selectedProcessVersion);

            String classRoot = getClassRootLocation();

            String wsdlFilePath = getTmpFolder() + PATH_SEPARATOR + jobName + ".wsdl"; //$NON-NLS-1$
            String classFileName = classRoot + PATH_SEPARATOR + projectName + PATH_SEPARATOR + jobFolderName + PATH_SEPARATOR
                    + jobName + ".class"; //$NON-NLS-1$

            File classFile = new File(classFileName);
            if (!classFile.exists()) {
                return wsdl;
            }

            StringBuffer libPaths = new StringBuffer();
            char pathSeparator = System.getProperty("path.separator").charAt(0); //$NON-NLS-1$
            if (externalLibs != null) {
                for (URL libUrl : externalLibs) {
                    libPaths.append(libUrl.getFile());
                    libPaths.append(pathSeparator);
                }
            }

            TalendJava2WSDL.generateWSDL(new String[] { "-T1.2", "-yDOCUMENT", "-uLITERAL", "-o" + wsdlFilePath, "-d", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                    "-lhttp://localhost:8080/" + jobName, "-nhttp://talend.org", "-X" + classRoot + pathSeparator + libPaths, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    "-m" + EXPORT_METHOD, projectName + "." + jobFolderName + "." + jobName }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            wsdlUrlList.add(new File(wsdlFilePath).toURL());

            if (!needWSDL) {
                return wsdl;
            }

        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }

        wsdl.addResources(wsdlUrlList);
        return wsdl;
    }

    private ExportFileResource getWSDDFile(Boolean needWSDD) {
        ExportFileResource wsdd = new ExportFileResource(null, "wsdd"); //$NON-NLS-1$

        if (!needWSDD) {
            return wsdd;
        }

        List<URL> wsddUrlList = new ArrayList<URL>();
        try {
            // String projectName = getCorrespondingProjectName(nullxxx);
            String projectName = JavaResourcesHelper.getCurrentProjectName();

            String deployFile = getTmpFolder() + PATH_SEPARATOR + projectName;
            File file = new File(deployFile);
            if (file.exists()) {
                wsddUrlList.add(file.toURL());
            }

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        wsdd.addResources(wsddUrlList);
        return wsdd;
    }

    private ExportFileResource getWebXMLFile(Boolean needWebXMLFile) {
        // generate the web.xml file
        ExportFileResource webInfo = new ExportFileResource(null, "WEB-INF"); //$NON-NLS-1$

        if (!needWebXMLFile) {
            return webInfo;
        }

        List<URL> urlList = new ArrayList<URL>();
        final Bundle b = Platform.getBundle(RepositoryPlugin.PLUGIN_ID);
        try {

            URL webFileUrl = FileLocator.toFileURL(FileLocator.find(b, new Path("resources/web.xml"), null)); //$NON-NLS-1$
            urlList.add(webFileUrl);

        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }

        webInfo.addResources(urlList);
        return webInfo;
    }

    /**
     * DOC x Comment method "genWebInfoForder".
     * 
     * @param list
     */
    private void editWSDDFile(ProcessItem processItem) {
        String projectName = getCorrespondingProjectName(processItem);
        String selectedProcessVersion = processItem.getProperty().getVersion();
        if (!isMultiNodes() && this.getSelectedJobVersion() != null) {
            selectedProcessVersion = this.getSelectedJobVersion();
        }

        String jobFolderName = JavaResourcesHelper.getJobFolderName(escapeFileNameSpace(processItem), selectedProcessVersion);

        String deployFileName = getTmpFolder() + PATH_SEPARATOR + projectName + PATH_SEPARATOR + jobFolderName + PATH_SEPARATOR
                + "deploy.wsdd"; //$NON-NLS-1$
        String serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "server-config.wsdd"; //$NON-NLS-1$

        File deployFile = new File(deployFileName);
        if (!deployFile.exists()) {
            log.error(org.talend.repository.i18n.Messages.getString("JobJavaScriptsWSManager.errorMessage")); //$NON-NLS-1$
            return;
        }
        // edit the server-config.wsdd file
        try {

            File wsddFile = new File(serverConfigFile);
            BufferedReader reader = new BufferedReader(new FileReader(wsddFile));

            SAXReader saxReader = new SAXReader();
            Document doc = saxReader.read(reader);

            BufferedReader wsdlreader = new BufferedReader(new FileReader(deployFile));
            SAXReader wsdlsaxReader = new SAXReader();
            Document wsdldoc = wsdlsaxReader.read(wsdlreader);
            Element wsdlroot = wsdldoc.getRootElement();
            Element element = wsdlroot.element("service"); //$NON-NLS-1$

            List<Element> elements = element.elements("arrayMapping"); //$NON-NLS-1$
            for (Element item : elements) {
                Attribute attribute = item.attribute("qname"); //$NON-NLS-1$
                item.remove(attribute);
                attribute.setValue(attribute.getValue().replaceFirst(">", "")); //$NON-NLS-1$ //$NON-NLS-2$
                item.add(attribute);
            }

            Element root = doc.getRootElement();
            List<Node> content = root.content();
            for (int i = 0; i < content.size(); i++) {
                Node n = content.get(i);
                if (n instanceof Element) {
                    if (n.getName().equals("transport")) { //$NON-NLS-1$
                        content.add(i - 1, element);
                        break;
                    }
                }
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(serverConfigFile), "UTF-8")); //$NON-NLS-1$

            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter output = new XMLWriter(writer, format);
            output.write(doc);
            output.flush();
            output.close();

        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    private ExportFileResource getServerConfigFile(Boolean needConfigFile) {
        ExportFileResource webInfo = new ExportFileResource(null, "WEB-INF"); //$NON-NLS-1$

        if (!needConfigFile) {
            return webInfo;
        }

        String serverConfigFile = getTmpFolder() + PATH_SEPARATOR + "server-config.wsdd"; //$NON-NLS-1$

        List<URL> urlList = new ArrayList<URL>();

        try {
            urlList.add(new File(serverConfigFile).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }

        webInfo.addResources(urlList);

        return webInfo;
    }

    /**
     * DOC x Comment method "genMetaInfoForder".
     * 
     * @param list
     * @return
     */
    private ExportFileResource genMetaInfoFolder(Boolean needMetaInfo) {
        ExportFileResource metaInfoResource = new ExportFileResource(null, "META-INF"); //$NON-NLS-1$
        if (!needMetaInfo) {
            return metaInfoResource;
        }

        // generate the MANIFEST.MF file in the temp folder
        String manifestPath = getTmpFolder() + PATH_SEPARATOR + "MANIFEST.MF"; //$NON-NLS-1$

        Manifest manifest = new Manifest();
        Map<String, Attributes> m = manifest.getEntries();
        Attributes a = new Attributes();
        a.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0"); //$NON-NLS-1$
        a.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Talend Open Studio"); //$NON-NLS-1$
        m.put("talendWebService", a); //$NON-NLS-1$
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(manifestPath);
            manifest.write(fos);
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
        } catch (IOException e1) {
            ExceptionHandler.process(e1);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    ExceptionHandler.process(e);
                }
            }
        }

        List<URL> urlList = new ArrayList<URL>();
        try {
            urlList.add(new File(manifestPath).toURL());
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        }
        metaInfoResource.addResources(urlList);

        return metaInfoResource;
    }

    /**
     * DOC x Comment method "main".
     * 
     * @param args
     */
    static class TalendJava2WSDL extends Java2WSDL {

        Throwable t = null;

        public static void generateWSDL(String[] args) throws Throwable {
            TalendJava2WSDL java2WSDL = new TalendJava2WSDL();
            java2WSDL.run(args);
            if (java2WSDL.t != null) {
                throw new Throwable(java2WSDL.t);
            }
        }

        /**
         * run checks the command-line arguments and runs the tool.
         * 
         * @param args String[] command-line arguments.
         * @return
         */
        protected int run(String[] args) {

            // Parse the arguments
            CLArgsParser argsParser = new CLArgsParser(args, options);

            // Print parser errors, if any
            if (null != argsParser.getErrorString()) {
                System.err.println(Messages.getMessage("j2werror00", argsParser.getErrorString())); //$NON-NLS-1$
                printUsage();

                return (1);
            }

            // Get a list of parsed options
            List clOptions = argsParser.getArguments();
            int size = clOptions.size();

            try {

                // Parse the options and configure the emitter as appropriate.
                for (int i = 0; i < size; i++) {
                    if (!parseOption((CLOption) clOptions.get(i))) {
                        return (1);
                    }
                }

                // validate argument combinations
                if (!validateOptions()) {
                    return (1);
                }

                // Set the namespace map
                if (!namespaceMap.isEmpty()) {
                    emitter.setNamespaceMap(namespaceMap);
                }

                TypeMappingRegistryImpl tmr = new TypeMappingRegistryImpl();
                tmr.doRegisterFromVersion(typeMappingVersion);
                emitter.setTypeMappingRegistry(tmr);

                // Find the class using the name
                emitter.setCls(className);

                // Generate a full wsdl, or interface & implementation wsdls
                if (wsdlImplFilename == null) {
                    emitter.emit(wsdlFilename, mode);
                } else {
                    emitter.emit(wsdlFilename, wsdlImplFilename);
                }

                if (isDeploy) {
                    generateServerSide(emitter, (wsdlImplFilename != null) ? wsdlImplFilename : wsdlFilename);
                }
                // everything is good
                return (0);
            } catch (Throwable t) {

                this.t = t;

                return (1);
            }
        } // run

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.axis.wsdl.Java2WSDL#generateServerSide(org.apache.axis.wsdl.fromJava.Emitter,
         * java.lang.String)
         */
        @Override
        protected void generateServerSide(Emitter j2w, String wsdlFileName) throws Exception {
            org.apache.axis.wsdl.toJava.Emitter w2j = new org.apache.axis.wsdl.toJava.Emitter();
            File wsdlFile = new File(wsdlFileName);
            w2j.setServiceDesc(j2w.getServiceDesc());
            w2j.setQName2ClassMap(j2w.getQName2ClassMap());
            w2j.setOutputDir(wsdlFile.getParent());
            w2j.setServerSide(true);
            w2j.setHelperWanted(true);

            // setup namespace-to-package mapping
            String ns = j2w.getIntfNamespace();
            String pkg = j2w.getCls().getPackage().getName();
            w2j.getNamespaceMap().put(ns, pkg);

            Map nsmap = j2w.getNamespaceMap();
            if (nsmap != null) {
                for (Iterator i = nsmap.keySet().iterator(); i.hasNext();) {
                    pkg = (String) i.next();
                    ns = (String) nsmap.get(pkg);
                    w2j.getNamespaceMap().put(ns, pkg);
                }
            }

            // set 'deploy' mode
            w2j.setDeploy(true);

            if (j2w.getImplCls() != null) {
                w2j.setImplementationClassName(j2w.getImplCls().getName());
            } else {
                if (!j2w.getCls().isInterface()) {
                    w2j.setImplementationClassName(j2w.getCls().getName());
                } else {
                    throw new Exception(
                            org.talend.repository.i18n.Messages.getString("JobJavaScriptsWSManager.impClassNotSpecified")); //$NON-NLS-1$
                }
            }
            // w2j.run(wsdlFileName);
            // Note by xtan: in order to support the "jdk1.6.0_05"
            w2j.run(wsdlFile.toURI().toString());
        }

    }

}
