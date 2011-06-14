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
package org.talend.repository.ui.wizards.metadata.connection.files.xml.dnd;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class TransferableXPathEntry {

    private String absoluteXPath;

    /**
     * DOC amaumont TransferableXPathEntry constructor comment.
     * 
     * @param absoluteXPath
     */
    public TransferableXPathEntry(String absoluteXPath) {
        this.absoluteXPath = absoluteXPath;
    }

    /**
     * Getter for absoluteXPath.
     * 
     * @return the absoluteXPath
     */
    public String getAbsoluteXPath() {
        return this.absoluteXPath;
    }

    /**
     * Sets the absoluteXPath.
     * 
     * @param absoluteXPath the absoluteXPath to set
     */
    public void setAbsoluteXPath(String absoluteXPath) {
        this.absoluteXPath = absoluteXPath;
    }

}
