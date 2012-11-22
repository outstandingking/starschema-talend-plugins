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
package org.talend.designer.core.ui.editor.cmd;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Priority;
import org.eclipse.gef.commands.Command;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQRuleService;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.QueryUtil;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.JavaProcessUtil;
import org.talend.repository.utils.DatabaseConnectionParameterUtil;
import orgomg.cwm.objectmodel.core.ModelElement;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * This class is used for generating new query when "Guess Query" button is selected. <br/>
 * 
 */
public class QueryGuessCommand extends Command {

    private final IElement node;

    private String propName;

    private Object oldValue;

    private IMetadataTable newOutputMetadataTable;

    private Map<String, String> dbNameAndDbTypeMap;

    private Map<String, String> dbNameAndSchemaMap;

    private String realTableId;

    private String realTableName;

    private String realDBType;

    private String schema;

    private String dbType;

    private Connection conn; // hywang add for 9594

    private IProcess process;

    /**
     * The property is defined in an element, which can be either a node or a connection.
     * 
     * @param node
     * @param propName
     * @param newOutputMetadataTable
     */
    public QueryGuessCommand(IElement node, IMetadataTable newOutputMetadataTable) {
        this.node = node;
        this.newOutputMetadataTable = newOutputMetadataTable;
    }

    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, Connection conn) {// 9594
        this(node2, metadataTable);
        this.conn = conn;
    }

    /**
     * DOC qzhang QueryGuessCommand constructor comment.
     * 
     * @param node2
     * @param metadataTable
     * @param schema
     * @param dbType
     */
    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, String schema, String dbType) {
        this(node2, metadataTable);
        this.schema = schema;
        this.dbType = dbType;
    }

    public QueryGuessCommand(INode node2, IMetadataTable metadataTable, String schema, String dbType, Connection conn) {// 9594
        this(node2, metadataTable);
        this.schema = schema;
        this.dbType = dbType;
        this.conn = conn;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        IElementParameter dqRuler = node.getElementParameter("DQRULES_LIST");
        String newQuery;
        if (dqRuler == null || "".equals(dqRuler.getValue())) {
            newQuery = generateNewQuery();
        } else {
            newQuery = generateNewQueryFromDQRuler(dqRuler);
        }

        for (IElementParameter param : (List<IElementParameter>) node.getElementParameters()) {
            if (param.getFieldType() == EParameterFieldType.MEMO_SQL) {
                oldValue = node.getPropertyValue(param.getName());
                this.propName = param.getName();
                String sql = null;
                try {
                    // sql = new SQLFormatUtil().formatSQL(newQuery);
                    if (QueryUtil.needFormatSQL(dbType)) {
                        sql = fomatQuery(newQuery);
                    } else {
                        sql = newQuery;
                    }
                    node.setPropertyValue(param.getName(), sql);
                } catch (Exception e) {
                    ExceptionHandler.process(e, Priority.WARN);
                    node.setPropertyValue(param.getName(), newQuery);
                }

                param.setRepositoryValueUsed(false);
            }
        }

        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);

        if (this.node instanceof Node) {
            ((Node) this.node).checkAndRefreshNode();
        }

        // Ends
    }

    private String generateNewQueryFromDQRuler(IElementParameter dqRulerParam) {
        ITDQRuleService rulerService = null;
        try {
            rulerService = (ITDQRuleService) GlobalServiceRegister.getDefault().getService(ITDQRuleService.class);
        } catch (RuntimeException e) {
            // nothing to do
        }
        if (rulerService != null) {
            IElementParameter typeParam = node.getElementParameter("TYPE");
            IElementParameter dbParam = node.getElementParameter(EParameterName.DBNAME.getName());
            IElementParameter schemaParam = node.getElementParameter(EParameterName.SCHEMA_DB.getName());
            IElementParameter tableParam = node.getElementParameterFromField(EParameterFieldType.DBTABLE);
            IElementParameter whereClause = node.getElementParameter("WHERE_CLAUSE");

            List<IMetadataTable> metadataList = null;
            IMetadataTable metadataTable = null;
            if (node instanceof Node) {
                metadataList = ((Node) node).getMetadataList();
                if (metadataList != null && !metadataList.isEmpty()) {
                    metadataTable = metadataList.get(0);
                }
            }
            return rulerService.getQueryByRule(dqRulerParam, typeParam, dbParam, schemaParam, tableParam, metadataTable, node
                    .getElementName().contains("Invalid"), whereClause);
        }
        return "";
    }

    private String generateNewQuery() {
        // used for generating new Query.

        if (realDBType != null) {
            dbType = realDBType;
        }
        //
        if (node != null && node instanceof INode) {
            process = ((INode) node).getProcess();
        }
        if (this.realTableId != null && this.dbNameAndDbTypeMap.containsKey(this.realTableId)) {
            dbType = this.dbNameAndDbTypeMap.get(this.realTableId);
        }
        if (dbType == null || dbType.equals("")) { //$NON-NLS-1$
            IElementParameter ptParam = node.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE);
            if (ptParam != null && ptParam.getRepositoryValue() != null) {
                if (ptParam.getRepositoryValue().endsWith(EDatabaseTypeName.GENERAL_JDBC.getProduct())) {
                    dbType = EDatabaseTypeName.GENERAL_JDBC.getDisplayName();
                }
            }
        }
        boolean isJdbc = false;
        // hywang add for bug 7575
        if (dbType != null && dbType.equals(EDatabaseTypeName.GENERAL_JDBC.getDisplayName())) {
            isJdbc = true;
            String driverClassName = node.getElementParameter("DRIVER_CLASS").getValue().toString(); //$NON-NLS-N$
            driverClassName = TalendTextUtils.removeQuotes(driverClassName);
            //
            if (driverClassName != null && !"".equals(driverClassName)) { //$NON-NLS-N$
                boolean isContextModeDriverClass = ContextParameterUtils.containContextVariables(driverClassName);
                if (isContextModeDriverClass) {
                    driverClassName = JavaProcessUtil.getContextOriginalValue(process, driverClassName);
                }
            }

            // DRIVER_JAR:
            String driverJarName = node.getElementParameter("DRIVER_JAR").getValue().toString(); //$NON-NLS-N$
            if (driverJarName != null && driverJarName.startsWith("[") && driverJarName.endsWith("]")) { //$NON-NLS-N$ //$NON-NLS-N$
                driverJarName = driverJarName.substring(1, driverJarName.length() - 1);
                if (driverJarName != null && driverJarName.startsWith("{") && driverJarName.endsWith("}")) { //$NON-NLS-N$ //$NON-NLS-N$
                    driverJarName = driverJarName.substring(1, driverJarName.length() - 1);
                }
            }
            if (driverJarName != null && !"".equals(driverJarName)) { //$NON-NLS-N$
                boolean isContextMode = ContextParameterUtils.containContextVariables(driverJarName);
                if (isContextMode) {
                    driverJarName = JavaProcessUtil.getContextOriginalValue(process, driverJarName);
                }
                dbType = ExtractMetaDataUtils.getDbTypeByClassNameAndDriverJar(driverClassName, driverJarName);
            } else {
                dbType = ExtractMetaDataUtils.getDbTypeByClassName(driverClassName);
            }

            DatabaseConnection dbConn = null;
            if (dbType == null) { // handle context mode
                if (conn != null) {
                    if (conn instanceof DatabaseConnection) {
                        dbConn = (DatabaseConnection) conn;
                    }
                    driverClassName = DatabaseConnectionParameterUtil.getTrueParamValue(dbConn, driverClassName);
                    dbType = ExtractMetaDataUtils.getDbTypeByClassName(driverClassName);
                }
            }
            // data view， conn=null
            // need add code here for dbtype(oracle)
        }

        if (dbNameAndSchemaMap != null) {
            schema = this.dbNameAndSchemaMap.get(this.realTableId);
        }
        String propertyType = (String) node.getPropertyValue(EParameterName.PROPERTY_TYPE.getName());
        boolean isTeradata = false;
        if (dbType != null) {
            isTeradata = dbType.equals(EDatabaseTypeName.TERADATA.getDisplayName());
        }
        if (propertyType != null && !propertyType.equals(EmfComponent.REPOSITORY)) {
            for (IElementParameter param : this.node.getElementParameters()) {
                if (param.getRepositoryValue() != null) {
                    if ((!isTeradata && param.getRepositoryValue().equals("SCHEMA")) //$NON-NLS-1$
                            || (isTeradata && param.getRepositoryValue().equals("SID"))) {// check if dbtype is //$NON-NLS-1$
                        // Teradata, always keep the
                        // query style like
                        // "dbname.tablename.columnname" on build-in mode
                        schema = (String) param.getValue();
                        schema = schema.replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        schema = schema.replace("\'", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        break;
                    }
                }
            }
        } else if (schema == null) {
            IElementParameter param = node.getElementParameter(EParameterName.REPOSITORY_PROPERTY_TYPE.getName());
            if (param != null) {
                try {
                    IRepositoryViewObject object = DesignerPlugin.getDefault().getRepositoryService().getProxyRepositoryFactory()
                            .getLastVersion((String) param.getValue());
                    if (object != null) {
                        Item item = object.getProperty().getItem();
                        if (item != null && item instanceof DatabaseConnectionItem) {

                            if (isTeradata) {
                                schema = (String) RepositoryToComponentProperty.getValue(
                                        ((DatabaseConnectionItem) item).getConnection(), "SID", null); //$NON-NLS-1$
                            } else {
                                schema = (String) RepositoryToComponentProperty.getValue(
                                        ((DatabaseConnectionItem) item).getConnection(), "SCHEMA", null); //$NON-NLS-1$
                            }
                            schema = TalendTextUtils.removeQuotes(schema);
                        }
                    }
                } catch (PersistenceException e) {
                    //
                }
            }

        }

        if (conn instanceof DatabaseConnection && conn.isContextMode()) {
            schema = DatabaseConnectionParameterUtil.getContextTrueValue((DatabaseConnection) conn, schema);
        }

        String newQuery = null;
        realTableName = QueryUtil.getTableName(node, newOutputMetadataTable, schema, dbType, realTableName);
        if (realTableName.startsWith(TalendTextUtils.QUOTATION_MARK) && realTableName.endsWith(TalendTextUtils.QUOTATION_MARK)
                && realTableName.length() > 2) {
            realTableName = realTableName.substring(1, realTableName.length() - 1);
        }
        if (isJdbc && conn != null) {
            schema = getDefaultSchema(realTableName);
        }
        newQuery = TalendTextUtils.addSQLQuotes(QueryUtil.generateNewQuery(node, newOutputMetadataTable, isJdbc, dbType, schema,
                realTableName));
        return newQuery;
    }

    // get DatabaseConnection
    protected DatabaseConnection getConnection(ConnectionItem connectionItem) {
        return (DatabaseConnection) connectionItem.getConnection();
    }

    @Override
    public void undo() {
        node.setPropertyValue(EParameterName.UPDATE_COMPONENTS.getName(), Boolean.TRUE);
        if (propName != null && oldValue != null) {
            node.setPropertyValue(propName, oldValue);
        }
    }

    @Override
    public void redo() {
        this.execute();
    }

    /**
     * Sets a set of maps what used for generating new Query.
     * 
     * @param dbNameAndDbTypeMap
     * @param dbNameAndSchemaMap
     * @param getRepositoryTableMap
     */
    public void setMaps(Map<String, String> dbNameAndDbTypeMap, Map<String, String> dbNameAndSchemaMap,
            Map<String, IMetadataTable> repositoryTableMap) {
        this.dbNameAndDbTypeMap = dbNameAndDbTypeMap;
        this.dbNameAndSchemaMap = dbNameAndSchemaMap;
    }

    /**
     * Sets the real table id and table name.
     * 
     * @param realTableId
     * @param realTableName
     * @param type
     */
    public void setParameters(String realTableId, String realTableName, String type) {
        this.realTableId = realTableId;
        this.realTableName = realTableName;
        realDBType = type;
    }

    /*
     * 
     * hyWang Method formatQuery
     */
    private String fomatQuery(String query) {
        String lastPartA, lastPartB;
        StringBuffer buffer = new StringBuffer();
        String[] s = query.split(","); //$NON-NLS-1$
        buffer.append(s[0]);
        if (s.length > 1) {
            for (int i = 1; i < s.length - 1; i++) {
                s[i] = s[i].trim();
                buffer.append("," + "\n" + "\t" + "\t" + s[i]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            // for bug 13265
            String[] last = s[s.length - 1].split(" FROM "); //$NON-NLS-1$
            lastPartA = last[0].trim() + "\n"; //$NON-NLS-1$
            lastPartB = "FROM" + "\t" + last[1].trim(); //$NON-NLS-1$ //$NON-NLS-2$
            s[s.length - 1] = lastPartA + lastPartB;
            buffer.append("," + "\n" + "\t" + "\t" + s[s.length - 1]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } else {
            String[] temp = s[0].split("FROM"); //$NON-NLS-1$
            if (temp.length > 1) {
                lastPartA = temp[0] + "\n"; //$NON-NLS-1$
                lastPartB = "FROM" + "\t" + temp[1]; //$NON-NLS-1$ //$NON-NLS-2$
                buffer = new StringBuffer();
                buffer.append(lastPartA);
                buffer.append(lastPartB);
            }
        }
        return buffer.toString();
    }

    private String getDefaultSchema(String realTableName) {
        String schema = "";
        List<Schema> schemas = ConnectionHelper.getSchema(this.conn);
        Set<Catalog> catalog = ConnectionHelper.getAllCatalogs(this.conn);
        for (Schema deScha : schemas) {
            for (ModelElement ele : deScha.getOwnedElement()) {
                String childeleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                        TalendTextUtils.declareString(ele.getName()), dbType, true);
                if (childeleName.startsWith(TalendTextUtils.QUOTATION_MARK)
                        && childeleName.endsWith(TalendTextUtils.QUOTATION_MARK) && childeleName.length() > 2) {
                    childeleName = childeleName.substring(1, childeleName.length() - 1);
                }
                if (childeleName.equals(realTableName)) {
                    return deScha.getName();
                }
            }
        }

        for (Catalog cata : catalog) {
            for (ModelElement ele : cata.getOwnedElement()) {
                if (ele instanceof Schema) {
                    for (ModelElement child : ((Schema) ele).getOwnedElement()) {
                        String childeleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                                TalendTextUtils.declareString(child.getName()), dbType, true);
                        if (childeleName.startsWith(TalendTextUtils.QUOTATION_MARK)
                                && childeleName.endsWith(TalendTextUtils.QUOTATION_MARK) && childeleName.length() > 2) {
                            childeleName = childeleName.substring(1, childeleName.length() - 1);
                        }
                        if (childeleName.equals(realTableName)) {
                            return ele.getName();
                        }
                    }
                } else {
                    String eleName = TalendTextUtils.addQuotesWithSpaceFieldForSQLStringForce(
                            TalendTextUtils.declareString(ele.getName()), dbType, true);
                    if (eleName.startsWith(TalendTextUtils.QUOTATION_MARK) && eleName.endsWith(TalendTextUtils.QUOTATION_MARK)
                            && eleName.length() > 2) {
                        eleName = eleName.substring(1, eleName.length() - 1);
                    }
                    if (eleName.equals(realTableName)) {
                        return cata.getName();
                    }
                }
            }
        }
        return schema;
    }
}
