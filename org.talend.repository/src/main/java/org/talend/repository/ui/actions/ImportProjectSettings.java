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
package org.talend.repository.ui.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.talend.core.model.general.Project;
import org.talend.core.model.properties.ComponentSetting;
import org.talend.core.model.properties.ImplicitContextSettings;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.StatAndLogsSettings;
import org.talend.core.model.properties.Status;
import org.talend.core.model.properties.impl.PropertiesFactoryImpl;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ProjectManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *wchen class global comment. Detailled comment
 */
public class ImportProjectSettings {

    private String path;

    private Project pro;

    public ImportProjectSettings(String path) {
        this.path = path;
        this.pro = ProjectManager.getInstance().getCurrentProject();
    }

    public void updateProjectSettings() throws ParserConfigurationException, SAXException, IOException {
        if (this.path == null) {
            return;
        }

        File file = new File(path);
        org.talend.core.model.properties.Project project = pro.getEmfProject();

        final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        DocumentBuilder analyseur = fabrique.newDocumentBuilder();
        analyseur.setErrorHandler(new ErrorHandler() {

            public void error(final SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(final SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(final SAXParseException exception) throws SAXException {
                throw exception;
            }

        });

        final Document document = analyseur.parse(file);
        final NodeList nodes = document.getElementsByTagName("exportParameter"); //$NON-NLS-1$
        List addedComponentSetting = new ArrayList();

        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            final NamedNodeMap attrMap = node.getAttributes();
            final Node typeAttr = attrMap.getNamedItem("type"); //$NON-NLS-1$

            if ("technicalStatus".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                List technical = project.getTechnicalStatus();
                updateStatus(node, attrMap, technical, "technicalStatus"); //$NON-NLS-1$
            } else if ("documentationStatus".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                List documentation = project.getDocumentationStatus();
                updateStatus(node, attrMap, documentation, "documentationStatus"); //$NON-NLS-1$
            } else if ("security".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                project.isHidePassword();
                project.setHidePassword(Boolean.valueOf(node.getTextContent()));
            } else if ("statAndLogs".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                if (project.getStatAndLogsSettings() == null) {
                    TalendFileFactory talendF = TalendFileFactory.eINSTANCE;
                    StatAndLogsSettings stats = PropertiesFactory.eINSTANCE.createStatAndLogsSettings();
                    project.setStatAndLogsSettings(stats);
                    stats.setParameters(talendF.createParametersType());
                }

                List statAndLogs = project.getStatAndLogsSettings().getParameters().getElementParameter();
                updateParameters(node, attrMap, statAndLogs);

            } else if ("implicitContext".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                if (project.getImplicitContextSettings() == null) {
                    TalendFileFactory talendF = TalendFileFactory.eINSTANCE;

                    ImplicitContextSettings implicit = PropertiesFactory.eINSTANCE.createImplicitContextSettings();
                    project.setImplicitContextSettings(implicit);
                    implicit.setParameters(talendF.createParametersType());
                }

                List implicitContexts = project.getImplicitContextSettings().getParameters().getElementParameter();
                updateParameters(node, attrMap, implicitContexts);

            } else if ("palette".equals(typeAttr.getTextContent())) { //$NON-NLS-1$
                List componentSettings = project.getComponentsSettings();
                boolean existed = false;
                String name = attrMap.getNamedItem("name").getTextContent(); //$NON-NLS-1$
                final Node familyAttr = attrMap.getNamedItem("family"); //$NON-NLS-1$
                Boolean hide = Boolean.valueOf(node.getTextContent());

                for (Object obj : componentSettings) {
                    ComponentSetting setting = (ComponentSetting) obj;

                    if (setting.getName().equals(name)) {
                        if (familyAttr != null && familyAttr.getTextContent().equals(setting.getFamily())) {
                            existed = true;
                            setting.setHidden(hide);
                        }
                    }
                }
                if (!existed && familyAttr != null) {
                    ComponentSetting setting = PropertiesFactory.eINSTANCE.createComponentSetting();
                    setting.setFamily(familyAttr.getTextContent());
                    setting.setName(name);
                    setting.setHidden(hide);
                    addedComponentSetting.add(setting);
                }
            }

        }

        project.getComponentsSettings().addAll(addedComponentSetting);

    }

    /**
     * wchen Comment method "updateParameters".
     * 
     * @param node
     * @param attrMap
     * @param statAndLogs
     */
    private void updateParameters(final Node node, final NamedNodeMap attrMap, List statAndLogs) {
        boolean added = false;
        for (Object obj : statAndLogs) {
            ElementParameterType type = (ElementParameterType) obj;
            if (type.getName().equals(attrMap.getNamedItem("name").getTextContent())) { //$NON-NLS-1$
                type.setValue(node.getTextContent());
                added = true;
            }
        }
        // if there is no such parameter in current settings add one
        TalendFileFactory talendF = TalendFileFactory.eINSTANCE;
        if (added == false) {

            ElementParameterType type = talendF.createElementParameterType();
            type.setName(attrMap.getNamedItem("name").getTextContent()); //$NON-NLS-1$
            type.setValue(node.getTextContent());
            statAndLogs.add(type);
        }
    }

    /**
     * wchen Comment method "updateStatus".
     * 
     * @param node
     * @param attrMap
     * @param status
     */
    private void updateStatus(final Node node, final NamedNodeMap attrMap, List status, String statusType) {
        boolean update = false;

        for (int j = 0; j < status.size(); j++) {
            Status s = (Status) status.get(j);
            if (s.getCode().equals(attrMap.getNamedItem("name").getTextContent())) { //$NON-NLS-1$
                s.setLabel(node.getTextContent());
                update = true;
            }
        }

        // add new status and logs
        if (update == false) {
            Status newOne = PropertiesFactoryImpl.init().createStatus();
            newOne.setCode(attrMap.getNamedItem("name").getTextContent()); //$NON-NLS-1$
            newOne.setLabel(node.getTextContent());
            status.add(newOne);
        }

    }
}
