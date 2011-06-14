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

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 * 
 * $Id: TableEntriesTransfer.java 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class XPathTransfer extends ByteArrayTransfer {

    private XmlToSchemaDraggedData draggedData;

    private static final String XML_NODE_TO_XPATH_TYPE_NAME = "XML_NODE_TO_XPATH_ENTRIES"; //$NON-NLS-1$

    private static final int XML_NODE_TO_XPATH_ENTRIES_ID = registerType(XML_NODE_TO_XPATH_TYPE_NAME);

    private static final XPathTransfer INSTANCE = new XPathTransfer();

    public static XPathTransfer getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
     */
    @Override
    protected int[] getTypeIds() {
        return new int[] { XML_NODE_TO_XPATH_ENTRIES_ID };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
     */
    @Override
    protected String[] getTypeNames() {
        return new String[] { XML_NODE_TO_XPATH_TYPE_NAME };
    }

    @Override
    protected void javaToNative(Object object, TransferData transferData) {
        // FIX for issue 1225
        super.javaToNative(new byte[1], transferData);
    }

    @Override
    protected Object nativeToJava(TransferData transferData) {
        return new byte[0];
    }

    public XmlToSchemaDraggedData getDraggedData() {
        return draggedData;
    }

    public void setDraggedData(XmlToSchemaDraggedData draggedData) {
        this.draggedData = draggedData;
    }

}
