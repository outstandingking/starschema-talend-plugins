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
package org.talend.repository.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;

/**
 * This class is used for copying file from one place to the other.
 * 
 * $Id: CopyFileUtils.java 2007-3-9,下午07:28:36 ftang $
 * 
 */
public class FileCopyUtils {

    /**
     * 
     */
    private static final int COPY_BUF_SIZE = 512;

    /**
     * This method is used for coping file from one place to the other.
     * 
     * @param srcFilePath
     * @param destFilePath
     * @throws Exception
     */
    public static void copy(String srcFilePath, String destFilePath) {
        try {
            copyFiles(srcFilePath, destFilePath);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * This method is used for coping file from one place to the other.
     * 
     * @param srcFilePath
     * @param destFilePath
     * @throws IOException
     * @throws IOException in case some problems occured
     */
    public static void copyFiles(String srcFilePath, String destFilePath) throws IOException {
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(srcFilePath);
            output = new FileOutputStream(destFilePath);
            copyStreams(input, output);
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * copy is to os.
     * 
     * @param is
     * @param os
     * @throws IOException thrown if copy fails
     */
    public static void copyStreams(InputStream is, OutputStream os) throws IOException {
        byte[] bytearray = new byte[COPY_BUF_SIZE];
        int len = 0;
        while ((len = is.read(bytearray)) != -1) {
            os.write(bytearray, 0, len);
        }
    }

}
