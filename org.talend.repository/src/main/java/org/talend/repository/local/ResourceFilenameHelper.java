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
package org.talend.repository.local;

import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.talend.core.model.properties.Property;
import org.talend.repository.model.URIHelper;

/**
 * $Id: ResourceFilenameHelper.java 44501 2010-06-25 01:18:47Z nrousseau $
 */
public class ResourceFilenameHelper {

    private static final char SEPARATOR = '_';

    public static FileName create(Property property) {
        return new FileName(property);
    }

    public static String getExpectedFileName(String label, String version) {
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(label);
        stringBuffer.append(ResourceFilenameHelper.SEPARATOR);
        stringBuffer.append(version);

        return stringBuffer.toString();
    }

    public static IPath getExpectedFilePath(FileName fileName, IPath parentPath, String extension) {
        String expectedFileName = getExpectedFileName(fileName.getProperty().getLabel(), fileName.getProperty().getVersion());
        return parentPath.append(expectedFileName).addFileExtension(extension);
    }

    public static FileName create(Resource resource, Property property, Property lastVersionProperty) {
        FileName fileName = new FileName(resource, property, lastVersionProperty);

        URI uri = resource.getURI();
        String fileNameString = uri.trimFileExtension().lastSegment();
        int index = fileNameString.lastIndexOf(ResourceFilenameHelper.SEPARATOR);

        fileName.setResourceLabel(fileNameString.substring(0, index));
        fileName.setResourceVersion(fileNameString.substring(index + 1));

        return fileName;
    }

    public static IPath getExpectedFilePath(FileName fileName, boolean usePreviousVersion) {
        URI uri = fileName.getResource().getURI();
        String extension = uri.fileExtension();
        IPath parentPath = URIHelper.convert(uri.trimSegments(1));

        String version;
        if (usePreviousVersion) {
            version = fileName.getResourceVersion();
        } else {
            version = fileName.getProperty().getVersion();
        }
        String expectedFileName = getExpectedFileName(fileName.getLastVersionProperty().getLabel(), version);
        return parentPath.append(expectedFileName).addFileExtension(extension);
    }

    public static boolean mustChangeVersion(FileName fileName) {
        return !fileName.getResourceVersion().equals(fileName.getProperty().getVersion());
    }

    public static boolean mustChangeLabel(FileName fileName) {
        return !fileName.getResourceLabel().equals(fileName.getLastVersionProperty().getLabel());
    }

    public static boolean hasSameNameButDifferentCase(FileName fileName) {
        return !fileName.getResourceLabel().equals(fileName.getLastVersionProperty().getLabel())
                && fileName.getResourceLabel().equalsIgnoreCase(fileName.getLastVersionProperty().getLabel());
    }

    /** * */
    public static class FileName {

        private String resourceLabel = null;

        private String resourceVersion = null;

        private Property property;

        private Resource resource;

        private Property lastVersionProperty;

        public FileName(Property property) {
            this.property = property;
        }

        public FileName(Resource resource, Property property, Property lastVersionProperty) {
            this.resource = resource;
            this.property = property;
            this.lastVersionProperty = lastVersionProperty;
        }

        public String getResourceLabel() {
            return this.resourceLabel;
        }

        public void setResourceLabel(String resourceLabel) {
            this.resourceLabel = resourceLabel;
        }

        public String getResourceVersion() {
            return this.resourceVersion;
        }

        public void setResourceVersion(String resourceVersion) {
            this.resourceVersion = resourceVersion;
        }

        public Property getProperty() {
            return this.property;
        }

        public void setProperty(Property property) {
            this.property = property;
        }

        public Resource getResource() {
            return this.resource;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public Property getLastVersionProperty() {
            return this.lastVersionProperty;
        }

        public void setLastVersionProperty(Property lastVersionProperty) {
            this.lastVersionProperty = lastVersionProperty;
        }
    }
}
