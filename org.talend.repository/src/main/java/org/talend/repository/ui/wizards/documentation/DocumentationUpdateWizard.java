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
package org.talend.repository.ui.wizards.documentation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkbench;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.DocumentationItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.LinkDocumentationItem;
import org.talend.core.model.properties.LinkType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.wizards.CheckLastVersionRepositoryWizard;
import org.talend.repository.ui.wizards.documentation.LinkUtils.LinkInfo;

/**
 * Wizard to update a IDocumentation. <br/>
 * 
 * $Id: DocumentationUpdateWizard.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class DocumentationUpdateWizard extends CheckLastVersionRepositoryWizard implements IDocumentationContext {

    /** Main wizard page. */
    private DocumentationPage mainPage;

    private Item docItem;

    private IPath docFilePath;

    /**
     * Constructs a new DocumentationUpdateWizard.
     */
    public DocumentationUpdateWizard(IWorkbench workbench, IRepositoryViewObject object, IPath destinationPath) {
        super(workbench, false);
        this.pathToSave = destinationPath;
        this.docItem = object.getProperty().getItem();

        this.repositoryObject = object;

        setWindowTitle(Messages.getString("DocumentationUpdateWizard.windowTitle")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(ImageProvider.getImageDesc(ECoreImage.DOCUMENTATION_WIZ));
        setNeedsProgressMonitor(false);
        initLockStrategy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new DocumentationPage(docItem.getProperty(), pathToSave);
        mainPage.setDescription(Messages.getString("DocumentationUpdateWizard.mainPageDescription")); //$NON-NLS-1$
        mainPage.setUpdate(true);
        mainPage.setEditPath(false);
        addPage(mainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        boolean updated = false;
        InputStream stream = null;
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            if (getDocFilePath() != null && getDocFilePath().segmentCount() != 0) {
                String fileStr = getDocFilePath().toString();
                if (LinkUtils.isDocumentationItem(docItem)) {
                    DocumentationItem documentationItem = (DocumentationItem) docItem;
                    if (LinkUtils.isRemoteFile(fileStr)) {
                        File tmpFile = LinkDocumentationHelper.createContentFromRemote(fileStr);
                        if (tmpFile != null) {
                            documentationItem.getContent().setInnerContentFromFile(tmpFile);
                        } else {
                            if (!LinkDocumentationHelper.continueAddDocumentation()) {
                                return false;
                            }
                        }
                    } else {
                        documentationItem.getContent().setInnerContentFromFile(getDocFilePath().toFile());
                    }
                    documentationItem.setName(getDocFilePath().removeFileExtension().lastSegment());
                    documentationItem.setExtension(getDocFilePath().getFileExtension());

                } else if (LinkUtils.isLinkDocumentationItem(docItem)) {
                    LinkDocumentationItem linkDocumentationItem = (LinkDocumentationItem) docItem;
                    LinkType link = linkDocumentationItem.getLink();
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
                }
            }

            repositoryFactory.save(docItem);
            closeLockStrategy();
            updated = true;
        } catch (PersistenceException e) {
            MessageBoxExceptionHandler.process(e);
        } catch (IOException ioe) {
            MessageBoxExceptionHandler.process(ioe);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }
        return updated;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.IDocumentationContext#getDocFilePath()
     */
    public IPath getDocFilePath() {
        return docFilePath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.repository.ui.wizards.IDocumentationContext#setDocFilePath(org.eclipse.core.runtime.IPath)
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

    /**
     * Getter for docOriginalExtension.
     * 
     * @return the docOriginalExtension
     */
    public String getDocOriginalExtension() {
        if (getDocFilePath() != null) {
            return LinkUtils.DOT + getDocFilePath().getFileExtension();
        } else {
            if (LinkUtils.isDocumentationItem(docItem)) {
                return LinkUtils.DOT + ((DocumentationItem) docItem).getExtension();
            } else if (LinkUtils.isLinkDocumentationItem(docItem)) {
                return LinkUtils.DOT + ((LinkDocumentationItem) docItem).getExtension();
            }
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Sets the docOriginalExtension.
     * 
     * @param docOriginalExtension the docOriginalExtension to set
     */
    public void setDocOriginalExtension(String docOriginalExtension) {
        if (LinkUtils.isDocumentationItem(docItem)) {
            ((DocumentationItem) docItem).setExtension(docOriginalExtension);
        } else if (LinkUtils.isLinkDocumentationItem(docItem)) {
            ((LinkDocumentationItem) docItem).setExtension(docOriginalExtension);
        }
    }

    /**
     * Getter for docOriginalName.
     * 
     * @return the docOriginalName
     */
    public String getDocOriginalName() {
        if (getDocFilePath() != null) {
            return getDocFilePath().lastSegment();
        } else {
            if (LinkUtils.isDocumentationItem(docItem)) {
                DocumentationItem documentationItem = (DocumentationItem) docItem;
                return documentationItem.getName() + LinkUtils.DOT + documentationItem.getExtension();
            } else if (LinkUtils.isLinkDocumentationItem(docItem)) {
                LinkDocumentationItem linkDocumentationItem = (LinkDocumentationItem) docItem;
                return linkDocumentationItem.getName() + LinkUtils.DOT + linkDocumentationItem.getExtension();
            }
        }
        return docItem.getProperty().getLabel();
    }

    /**
     * Sets the docOriginalName.
     * 
     * @param docOriginalName the docOriginalName to set
     */
    public void setDocOriginalName(String docOriginalName) {
        if (LinkUtils.isDocumentationItem(docItem)) {
            ((DocumentationItem) docItem).setName(docOriginalName);
        } else if (LinkUtils.isLinkDocumentationItem(docItem)) {
            ((LinkDocumentationItem) docItem).setName(docOriginalName);
        }
    }

    @Override
    public Item getVersionItem() {
        return this.docItem;
    }

}
