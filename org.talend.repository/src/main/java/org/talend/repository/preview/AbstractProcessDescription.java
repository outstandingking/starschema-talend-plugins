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
package org.talend.repository.preview;

import java.util.List;
import java.util.Map;

import org.talend.core.model.metadata.IMetadataTable;

/**
 * Description of a Process.
 * 
 * $Id: IProcessDescription.java 663 2006-11-21 17:21:53 +0000 (星期二, 21 十一月 2006) cantoine $
 * 
 */
public abstract class AbstractProcessDescription implements IProcessDescription {

    /** Server owning the file. */
    private String server;

    /** Path to the file. */
    private String filepath;

    /** Number of header rows to be skipped. */
    private int headerRow;

    /** Number of footer rows to be skipped. */
    private int footerRow;

    /** Limit the number of rows processed. */
    private int limitRows;

    /** Field separator. */
    private String fieldSeparator;

    /** Row separator. */
    private String rowSeparator;

    /** Pattern. */
    private String pattern;

    private String escapeCharacter;

    private String textEnclosure;

    private boolean removeEmptyRow;

    private String encoding;

    private List<IMetadataTable> schema;

    private String loopQuery;

    private Integer loopLimit;

    private List<Map<String, String>> mapping;

    private boolean splitRecord;

    private boolean isCSVOption;

    /**
     * Getter for escapeCharacter.
     * 
     * @return the escapeCharacter
     */
    public String getEscapeCharacter() {
        return this.escapeCharacter;
    }

    /**
     * Sets the escapeCharacter.
     * 
     * @param escapeCharacter the escapeCharacter to set
     */
    public void setEscapeCharacter(String escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    /**
     * Getter for fieldSeparator.
     * 
     * @return the fieldSeparator
     */
    public String getFieldSeparator() {
        return this.fieldSeparator;
    }

    /**
     * Sets the fieldSeparator.
     * 
     * @param fieldSeparator the fieldSeparator to set
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    /**
     * Getter for filepath.
     * 
     * @return the filepath
     */
    public String getFilepath() {
        return this.filepath;
    }

    /**
     * Sets the filepath.
     * 
     * @param filepath the filepath to set
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Getter for footerRow.
     * 
     * @return the footerRow
     */
    public int getFooterRow() {
        return this.footerRow;
    }

    /**
     * Sets the footerRow.
     * 
     * @param footerRow the footerRow to set
     */
    public void setFooterRow(int footerRow) {
        this.footerRow = footerRow;
    }

    /**
     * Getter for headerRow.
     * 
     * @return the headerRow
     */
    public int getHeaderRow() {
        return this.headerRow;
    }

    /**
     * Sets the headerRow.
     * 
     * @param headerRow the headerRow to set
     */
    public void setHeaderRow(int headerRow) {
        this.headerRow = headerRow;
    }

    /**
     * Getter for rowSeparator.
     * 
     * @return the rowSeparator
     */
    public String getRowSeparator() {
        return this.rowSeparator;
    }

    /**
     * Sets the rowSeparator.
     * 
     * @param rowSeparator the rowSeparator to set
     */
    public void setRowSeparator(String rowSeparator) {
        this.rowSeparator = rowSeparator;
    }

    /**
     * Getter for server.
     * 
     * @return the server
     */
    public String getServer() {
        return this.server;
    }

    /**
     * Sets the server.
     * 
     * @param server the server to set
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Getter for textEnclosure.
     * 
     * @return the textEnclosure
     */
    public String getTextEnclosure() {
        return this.textEnclosure;
    }

    /**
     * Sets the textEnclosure.
     * 
     * @param textEnclosure the textEnclosure to set
     */
    public void setTextEnclosure(String textEnclosure) {
        this.textEnclosure = textEnclosure;
    }

    /**
     * Getter for limitRows.
     * 
     * @return the limitRows
     */
    public int getLimitRows() {
        return this.limitRows;
    }

    /**
     * Sets the limitRows.
     * 
     * @param limitRows the limitRows to set
     */
    public void setLimitRows(int limitRows) {
        this.limitRows = limitRows;
    }

    /**
     * DOC ocarbone Comment method "setRemoveEmptyRow".
     * 
     * @param boolean
     */
    public void setRemoveEmptyRow(boolean selection) {
        this.removeEmptyRow = selection;
    }

    /**
     * DOC ocarbone Comment method "getRemoveEmptyRowsToSkip".
     * 
     * @return boolean
     */
    public boolean getRemoveEmptyRowsToSkip() {
        return removeEmptyRow;
    }

    /**
     * Getter for pattern.
     * 
     * @return the pattern
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Sets the pattern.
     * 
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Getter for encoding.
     * 
     * @return the encoding
     */
    public String getEncoding() {
        return this.encoding;
    }

    /**
     * Sets the encoding.
     * 
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Getter for schema.
     * 
     * @return the schema
     */
    public List<IMetadataTable> getSchema() {
        return this.schema;
    }

    /**
     * Sets the schema.
     * 
     * @param schema the schema to set
     */
    public void setSchema(List<IMetadataTable> schema) {
        this.schema = schema;
    }

    /**
     * Getter for loopQuery.
     * 
     * @return the loopQuery
     */
    public String getLoopQuery() {
        return this.loopQuery;
    }

    /**
     * Sets the loopQuery.
     * 
     * @param loopQuery the loopQuery to set
     */
    public void setLoopQuery(String loopQuery) {
        this.loopQuery = loopQuery;
    }

    /**
     * Getter for loopQuery.
     * 
     * @return the loopQuery
     */
    public Integer getLoopLimit() {
        return this.loopLimit;
    }

    /**
     * Sets the loopLimit.
     * 
     * @param loopLimit the loopLimit to set
     */
    public void setLoopLimit(Integer loopLimit) {
        this.loopLimit = loopLimit;
    }

    /**
     * Getter for mapping.
     * 
     * @return the mapping
     */
    public List<Map<String, String>> getMapping() {
        return this.mapping;
    }

    /**
     * Sets the mapping.
     * 
     * @param mapping the mapping to set
     */
    public void setMapping(List<Map<String, String>> mapping) {
        this.mapping = mapping;
    }

    /**
     * Getter for splitRecord.
     * 
     * @return the splitRecord
     */
    public boolean isSplitRecord() {
        return this.splitRecord;
    }

    /**
     * Sets the splitRecord.
     * 
     * @param splitRecord the splitRecord to set
     */
    public void setSplitRecord(boolean splitRecord) {
        this.splitRecord = splitRecord;
    }

    public boolean isCSVOption() {
        return this.isCSVOption;
    }

    public void setCSVOption(boolean isCSVOption) {
        this.isCSVOption = isCSVOption;
    }

}
