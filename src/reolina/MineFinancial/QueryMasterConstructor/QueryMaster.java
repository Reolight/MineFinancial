package reolina.MineFinancial.QueryMasterConstructor;

import org.bukkit.craftbukkit.libs.org.eclipse.sisu.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class QueryMaster {
    static private Connection connection;
    static private final String DBURL = "jdbc:sqlite:plugins/MineFinDB.db";
    static Logger log = Logger.getLogger("Minecraft");
    static Map<String, Table> tables = new HashMap();

    static public void QuieryMasterInit(){
        try{
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DBURL);
            DatabaseMetaData meta = connection.getMetaData();
            log.info("Created DB MineFinancial");
        } catch (Exception e) {
            log.info("UN PROBLEMA");
            log.info(e.getStackTrace() + " " + e);
        }
    }
    String where;

    List<rec> records;
    public enum QueryType {CREATE, INSERT, UPDATE, SELECT, DELETE}
    QueryType type;
    StringBuilder query;

    public QueryMaster(QueryType queryType, String TableName) /*Creates instance of SQLite command constructor */{
        switch (queryType){
            case CREATE : query = new StringBuilder("CREATE TABLE IF NOT EXISTS "+TableName.toLowerCase()+" ("); break;
            case INSERT : query = new StringBuilder("INSERT INTO "+TableName.toLowerCase()+" "); break;
            case UPDATE : query = new StringBuilder("UPDATE "+TableName.toLowerCase()+" SET "); break;
            case SELECT : query = new StringBuilder("SELECT * FROM "+TableName.toLowerCase()+" "); break;
            case DELETE : query = new StringBuilder("DELETE FROM "+TableName.toLowerCase()+" "); break;
        }
        type = queryType;
    }
    public void AddNewField(Field field){
        if (type == QueryType.CREATE) {
            if (query.indexOf("(") == query.length() - 1)
                query.append(field.toString());
            else
                query.append(", "+field.toString());
        }
        else
            log.info("This is not CREATE query! curr: "+records);
    }
    public void AddValue(String name, String value){
        if (records == null)
            records = new ArrayList<rec>();
        records.add(new rec(name.toLowerCase(), value));
    }
    public void AddValue(String name, String value, boolean isText){
        if (records == null)
            records = new ArrayList<rec>();
        if (isText)
            records.add(new rec(name.toLowerCase(), "\""+value+"\""));
        else
            AddValue(name, value);
    }
    public void AddWhere(String where){
        if (where != null)
            this.where = " WHERE "+where;
    }
    public void SelectFields(String selFields[]){
        if (type == QueryType.SELECT) {
            StringBuilder str = new StringBuilder("(");
            for (String sf : selFields) {
                str.append(sf.toLowerCase()+",");
            }
            str.replace(str.lastIndexOf(","), str.lastIndexOf(",")+1, ")");
            query.replace(query.indexOf("*"), query.indexOf("*")+1, str.toString());
        } else log.info("This is not SELECT query! curr: "+records);
    }
    public void Execute() throws SQLException {
        Statement statement = connection.createStatement();
        if (type == QueryType.CREATE) {
                query.append(")");
                log.info("Q: "+ query.toString());
                statement.executeUpdate(query.toString());
                statement.close();
            }
        if (type == QueryType.INSERT) {
                StringBuilder str = new StringBuilder("(");
                int i = 1;
                for (rec r : records) {
                    if (i == records.size())
                        str.append(r.name + ") ");
                    else
                        str.append(r.name + ",");
                    i++;
                }
                str.append("VALUES (");
                i = 1;
                for (rec r : records) {
                    if (i == records.size())
                        str.append(r.value + ")");
                    else
                        str.append(r.value + ",");
                    i++;
                }
                query.append(str.toString());
                log.info("Q: "+ query.toString());
                statement.executeUpdate(query.toString());
                statement.close();
            }
        if (type == QueryType.UPDATE) {
                for (rec r : records) {
                    String str = where == null ? query.toString() + r.name + "=" + r.value : query.toString() + r.name + " = " + r.value + where;
                    log.info("Q: "+ str);
                    statement.executeUpdate(str);
                }
                statement.close();
            }
        if (type == QueryType.SELECT) {
            throw new SQLException("YOU MUST USE ExecuteElection TO GET RESULTSET!");
        }
        if (type == QueryType.DELETE) {
            if (where != null)
                query.append(where);
            log.info("Q: "+ query.toString());
            statement.executeUpdate(query.toString());
        }
        statement.close();
        }
    public ResultSet ExecuteElection() throws SQLException {
        if (where != null)
            query.append(where);
        log.info("Q: "+ query.toString());
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query.toString());
        return resultSet;
    }

    static public boolean Create(Table table){
        QueryMaster create = new QueryMaster(QueryType.CREATE, table.Name);
        for (Field f : table.fields){
            create.AddNewField(f);
        }
        try {
            create.Execute();
            return true;
        } catch (SQLException ex){
            log.severe("Creating table "+table+" ERR: "+ex+"\n"+ex.getStackTrace());
            return false;
        }
    }

    static public boolean Insert(String tableName, rec[] records, @Nullable rec where) {
        QueryMaster insert = new QueryMaster(QueryType.INSERT, tableName);
        //Table tab = tables.get(tableName);
        for (rec r : records){
            insert.AddValue(r.name, r.value);
        }
        if (where != null) insert.AddWhere(where.name +" = "+ where.value);
        try {
            insert.Execute();
            return true;
        } catch (SQLException ex){
            log.severe("Inserting in "+tableName+" ERR: "+ex+"\n"+ex.getStackTrace());
            return false;
        }
    }

    static public boolean Update(String tableName, rec[] records, rec where) {
        QueryMaster update = new QueryMaster(QueryType.UPDATE, tableName);
        //Table tab = tables.get(tableName);
        for (rec r : records){
            update.AddValue(r.name,r.value);
        }
        update.AddWhere(where.name +" = "+ where.value);
        try {
            update.Execute();
            return true;
        } catch (SQLException ex){
            log.severe("Updating  in "+tableName+" ERR: "+ex+"\n"+ex.getStackTrace());
            return false;
        }
    }

    static public ResultSet Select(String tableName, @Nullable String[] columnNames, @Nullable rec where){
        //Table tab = tables.get(tableName);
        QueryMaster select = new QueryMaster(QueryType.SELECT, tableName);
        if (columnNames != null)
            select.SelectFields(columnNames);
        if (where != null) {
            select.AddWhere(where.name + " = " + where.value);
            select.where = null;
        }
        try {
            ResultSet rs = select.ExecuteElection();
            return rs;
        } catch (SQLException ex) {
            log.severe("Selecting from " + tableName + " ERR: " + ex + "\n" + ex.getStackTrace());
            return null;
        }
    }

    static public boolean Delete(String tableName, rec where){
        //Table tab = tables.get(tableName);
        QueryMaster delete = new QueryMaster(QueryType.DELETE, tableName);
        delete.AddWhere(where.name +" = "+where.value);
        try {
            delete.Execute();
            return true;
        } catch (SQLException ex) {
            log.severe("Selecting from " + tableName + " ERR: " + ex + "\n" + ex.getStackTrace());
            return false;
        }
    }
}