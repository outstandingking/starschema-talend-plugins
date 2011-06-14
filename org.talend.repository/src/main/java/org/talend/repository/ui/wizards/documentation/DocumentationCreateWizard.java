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
package org.talend.repository.ui.wizards.documentation;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.MessageBoxExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.properties.LinkType;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.images.ECoreImage;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.documentation.LinkUtils.LinkInfo;

/**
 * Wizard to create a new IDocumentation. <br/>
 * 
 * $Id: DocumentationCreateWizard.java 38013 2010-03-05 14:21:59Z mhirt $
 * 
 */
public class DocumentationCreateWizard extends CheckLastVersionRepositoryWizard implements IDocumentationContext {

    /** Main wizard page. */
    private DocumentationPage mainPage;

    private IPath docFilePath;

    private String docOriginalName;

    private String docOriginalExtension;

    private Property property;

    private DocumentationItem documentationItem;

    /**
     * Constructs a new DocumentationCreateWizard.
     * 
     * @param destinationPath Path in the repository where the documentation must be saved.
     */
    public DocumentationCreateWizard(IWorkbench workbench, IPath destinationPath) {
        super(workbench, true);
        pathToSave = destinationPath;

        setWindowTitle(Messages.getString("DocumentationCreateWizard.windowTitle")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.DOCUMENTATION_WIZ));
        setNeedsProgressMonitor(true);

        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$  

        this.property = PropertiesFactory.eINSTANCE.createProperty();
        this.property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY))
                .getUser());
        this.property.setVersion(VersionUtils.DEFAULT_VERSION);
        this.property.setStatusCode(""); //$NON-NLS-1$

        documentationItem = PropertiesFactory.eINSTANCE.createDocumentationItem();
        documentationItem.setProperty(property);

        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.PROCESS_WIZ));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new DocumentationPage(property, pathToSave);
        mainPage.setDescription(Messages.getString("DocumentationCreateWizard.mainPageDescription")); //$NON-NLS-1$
        mainPage.setUpdate(false);
        addPage(mainPage);

        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        boolean created = false;
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

        try {
            property.setId(repositoryFactory.getNextId());
            Item item = property.getItem();
            if (item != null) {
                String fileStr = getDocFilePath().toString();
                if (item instanceof LinkDocumentationItem) {
                    LinkDocumentationItem linkDocumentationItem = (LinkDocumentationItem) item;

                    LinkType link = PropertiesFactory.eINSTANCE.createLinkType();
                    linkDocumentationItem.setLink(link);
                    if (LinkUtils.isRemoteFile(fileStr)) {
                        if (LinkUtils.testRemoteFile(fileStr) != LinkInfo.LINK_OK) {
                            if (!LinkDocumentationHelper.continueAddDocumentation()) {
                                return false;
                            }
                        }
                        link.setURI(fileStr);
                    } else {
                        link.setURI(getDocFilePath().toOSString());
                    }
                    link.setState(true);
                    linkDocumentationItem.setName(getDocFilePath().removeFileExtension().lastSegment());
                    linkDocumentationItem.setExtension(getDocFilePath().getFileExtension());

                } else if (item instanceof DocumentationItem) {
                    DocumentationItem docItem = (DocumentationItem) item;
                    ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
                    if (LinkUtils.isRemoteFile(fileStr)) {
                        File tmpFile = LinkDocumentationHelper.createContentFromRemote(fileStr);
                        if (tmpFile != null) {
                            byteArray.setInnerContentFromFile(tmpFile);
                        } else {
                            if (!LinkDocumentationHelper.continueAddDocumentation()) {
                                return false;
                            }
                        }
                    } else {
                        byteArray.setInnerContentFromFile(getDocFilePath().toFile());
                    }
                    docItem.setContent(byteArray);
                    docItem.setName(getDocFilePath().removeFileExtension().lastSegment());
                    docItem.setExtension(getDocFilePath().getFileExtension());
                }

                repositoryFactory.create(item, mainPage.getDestinationPath());
                created = true;
            }
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (IOException ioe) {
            MessageBoxExceptionHandler.process(ioe);
        }
        return created;
    }

    /**
     * Getter for docOriginalName.
     * 
     * @return the docOriginalName
     */
    public String getDocOriginalName() {
        if (getDocFilePath() != null) {
            docOriginalName = getDocFilePath().lastSegment();
        } else {
            docOriginalName = property.getLabel();
        }
        return docOriginalName;
    }

    /**
     * Sets the docOriginalName.
     * 
     * @param docOriginalName the docOriginalName to set
     */
    public void setDocOriginalName(String docOriginalName) {
        // do nothing
    }

    public static final String DEFAULT_FILENAME_EXT = "doc"; //$NON-NLS-1$

    /**
     * Getter for docOriginalExtension.
     * 
     * @return the docOriginalExtension
     */
    public String getDocOriginalExtension() {
        if (getDocFilePath() != null) {
            this.docOriginalExtension = getDocFilePath().getFileExtension();
        } else {
            this.docOriginalExtension = DEFAULT_FILENAME_EXT;
        }
        return LinkUtils.DOT + this.docOriginalExtension;
    }

    /**
     * Sets the docOriginalExtension.
     * 
     * @param docOriginalExtension the docOriginalExtension to set
     */
    public void setDocOriginalExtension(String docOriginalExtension) {
        // do nothing
    }

    /**
     * Getter for docFilePath.
     * 
     * @return the docFilePath
     */
    public IPath getDocFilePath() {
        return this.docFilePath;
    }

    /**
     * Sets the docFilePath.
     * 
     * @param docFilePath the docFilePath to set
     */
    public void setDocFilePath(IPath docFilePath) {
        this.docFilePath = docFilePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.IDocumentationContext#isDocNameEditable()
     */
    public boolean isDocNameEditable() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.IDocumentationContext#isDocVersionEditable()
     */
    public boolean isDocVersionEditable() {
        return true;
    }

    @Override
    public Item getVersionItem() {
        return this.documentationItem;
    }

}
