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
package org.talend.repository.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 * 
 * $Id: URIHelper.java 77219 2012-01-24 01:14:15Z mhirt $
 * 
 */
public class URIHelper {

    private static final String RESOURCE = "resource"; //$NON-NLS-1$

    private static final String PLATFORM = "platform"; //$NON-NLS-1$

    public static IFile getFile(URI uri) {
        if (uri != null) {
            IPath path = convert(uri);
            if (path != null) {
                return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            }
        }
        return null;
    }

    public static IPath convert(URI uri) {
        if (uri != null)
            if (PLATFORM.equals(uri.scheme()) && uri.segmentCount() > 1 && RESOURCE.equals(uri.segment(0))) {
                StringBuffer platformResourcePath = new StringBuffer();
                for (int i = 1, size = uri.segmentCount(); i < size; ++i) {
                    platformResourcePath.append('/');
                    platformResourcePath.append(URI.decode(uri.segment(i)));
                }

                return new Path(platformResourcePath.toString());
            }

        return null;
    }

    public static URI convert(IPath path) {
        return URI.createPlatformResourceURI(path.toString());
    }
}
