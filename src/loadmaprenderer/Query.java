/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class Query{    

    public static void CloseConnection(String path){
        Connection connection = null;
        String path_abs = new File(path).getAbsolutePath();
        String path_par = new File(path).getParent();
        if (activeConnections.containsKey(path))
            connection = (Connection)activeConnections.get(path_abs);

        if (connection != null)
            try
            {
                if (!connection.isClosed())
                    connection.close();
            }
            catch (SQLException e)
            {
                connection = null;
            }

        connection = null;
    }

    private static Map<String,Connection> activeConnections = new HashMap();
    
    public static Connection OpenConnection(String path){
        //If the connection has already been established;
        Connection connection = null;
        String path_abs = new File(path).getAbsolutePath();
        String path_par = new File(path).getParent();
        if (activeConnections.containsKey(path))
            connection = (Connection) activeConnections.get(path_abs);
        //Check if the file exists;
        if (new File(path_abs).exists()) {
            //ActiveStatus.UpdateInnerStatus(new FileOpStatus("The file could not be found or created in accessing a database.", Environment.StackTrace, Path.GetFullPath(path)));
            //Check if the directory exists;
            if (!new File(path_par).exists()) new File(path_par).mkdir();                    

            try 
            {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + path_abs);
            }
            catch (ClassNotFoundException | SQLException e) 
            { 
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                connection = null; 
            }
            //Update status if the connection fails;
            if (connection != null) activeConnections.put(path_abs, connection);     
        }        

        //Return null if connection will not open;
        return connection;
    }
    
    public static boolean PrepareTable(String pathDB, String tableName, IAccessColumn ... cols){
        //Initialize the database connection;
        Connection conn = OpenConnection(pathDB);
        if (conn != null)
        {
            try
            {
                Statement stmt = conn.createStatement();
                //try to create the table
                String sql = CreateTableDeclaration(tableName, cols);
                stmt.executeUpdate(sql);

                //empty all the data
                sql = "delete from " + tableName;
                stmt.executeUpdate(sql);

                return true;
            }
            catch (SQLException e) { return false; }
        }

        //Return false if the command did not execute successfully;
        return false;
    } 
    
    public static String CreateTableDeclaration(String tableName, IAccessColumn[] cols)   {
        //Collect unique columns or primary keys;
        List<String> primaryKey = new ArrayList<>();
        List<String> unique = new ArrayList<>();
        for (IAccessColumn col : cols)
        {
            if (col.isPrimaryKey())
                primaryKey.add(col.getColumnName());
            if (col.isUnique())
                unique.add(col.getColumnName());
        }

        //Remove the existing table;
        String sql = ""; //String.Format("drop table if exists {0};", tableName);

        //Create the new table declaration;
        sql += String.format("create table if not exists %s(", tableName);

        //Declare the columns;
        for (int i = 0; i < cols.length; i++)
        {

            //Append the column definition;
            sql += BuildColumnDefinition(cols[i]);

            //Append a comma or close the bracket;
            if (i < cols.length - 1)
                sql += ",";
            else
            {

                //Add constraints;
                if (primaryKey.size() > 0)
                {
                    String[] sz = new String[primaryKey.size()];
                    sz = primaryKey.toArray(sz);
                    sql += String.format(",primary key ({%s}) on conflict ignore", String.join(",", sz).toString());
                }
                if (unique.size() > 0)
                {
                    String[] sz = new String[unique.size()];
                    sz = unique.toArray(sz);
                    sql += String.format(",unique ({%s}) on conflict ignore", String.join(",", sz));
                }

                //Close command;
                sql += ");";
            }
        }

        //Return the declaration;
        return sql;
    }
    
    public static String BuildColumnDefinition(Object col) {
        //if (col instanceof DataColumn)
        //    return buildColumnDeclarationDataColumn((DataColumn)col);
        if (col instanceof IAccessColumn)
            return buildColumnDeclarationAccessColumn((IAccessColumn)col);
        else
            return "";
    }
        
    private static String buildColumnDeclarationAccessColumn(IAccessColumn col){

        //Column name;
        String sql = String.format("%s ", col.getColumnName());

        //Determine the appropriate data type;
        Class dataType = col.getType();

        //Save the type;
        if (dataType.equals(Float.TYPE) || dataType.equals(Double.TYPE))
            sql += "real";
        else if (dataType.equals(Integer.TYPE))
            sql += "int";
        else
            sql += "text";

        //Default value;
        Object defaultValue = col.getDefaultValue();
        if (defaultValue != null)
        {
            sql += " default ";
            String sz = "";
            if (!dataType.equals(Double.TYPE) || !dataType.equals(Integer.TYPE) || !dataType.equals(Float.TYPE))
                sz = "'";
            sql += String.format("%s%s%s", sz, defaultValue, sz);
        }

        //Return the declaration;
        return sql;
    }
    
    public static ResultSet GetDataTable(String query, String path){
        //Build the data table;
        ResultSet fetchedTable = null;

        //Configure the data access service;
        try{
            Statement stmt = Query.OpenConnection(path).createStatement();
            fetchedTable = stmt.executeQuery(query);
        }  catch (SQLException e) {
            fetchedTable = null;
        }
        
        //Return the table;
        return fetchedTable;        
    }
    
    public static ResultSet GetDataTable(String query, String path, String filter){
        ResultSet rs = null;
        if (!filter.isEmpty()) query += filter;
        rs = GetDataTable(query, path);
        return rs;
    }
    
    public static ResultSet ComputeRS(ResultSet rs, String filter) throws SQLException{
        if (rs == null) return null;
        if (filter.equals("")) return rs;
        
        ResultSet rs_new = null;
        String tableName = "";
        String sql;
        
        try {
            tableName = rs.getMetaData().getTableName(1);
            sql = "SELECT * FROM " + tableName + " WHERE " + filter;        

            Statement stmt = rs.getStatement();
            rs_new = stmt.executeQuery(sql);
        } catch (Exception e) {
            rs_new = null;
        }        
        return rs_new;    
    }
    
    public static Map<Integer, Interval> GetQuantileIntervals(List<Double> values, int intervalNum){
        System.out.println("-----------Start calculating intervals-----------");
        Map<Integer, Interval> intervals = new HashMap();
        System.out.println("Input values size: " + values.size());
        values.sort(null);
        List<Double> dtValues = new ArrayList<Double>(new LinkedHashSet<Double>(values));
        System.out.println("Required intervals: " + intervalNum);        
        System.out.println("Distinct values size: " + dtValues.size());
        System.out.println("Distinct values after sorted: " + dtValues.toString());
        
        if (dtValues.size() < intervalNum){
            System.out.println("Required intervals " + intervalNum
            + " is larger than the value size " + dtValues.size());
            intervalNum = dtValues.size();
            System.out.println("Change intervals to " + dtValues.size());
        }
        
        int binSize = (int)Math.floor(dtValues.size() / (double)intervalNum);
        for (int iBreak = 1; iBreak <= intervalNum; iBreak++)
        {
            if (iBreak == 1) {
                intervals.put(
                    iBreak, new Interval(dtValues.get(iBreak - 1),
                        dtValues.get(binSize * iBreak - 1) + 0.000001));
                continue;
            }
            if (iBreak == intervalNum) {
                intervals.put(
                    iBreak, 
                    new Interval(dtValues.get(binSize * (iBreak - 1) - 1) + 0.000001,
                    dtValues.get(dtValues.size() - 1)));
                continue;
            }
                
            if (binSize * iBreak < dtValues.size())
            {                
                intervals.put(iBreak, 
                    new Interval(dtValues.get(binSize * (iBreak - 1) - 1) + 0.000001,
                    dtValues.get(binSize * iBreak - 1) + 0.000001));
            }
        }
        for (int iBreak = 1; iBreak <= intervalNum; iBreak++){
            int size = 0;
            if (iBreak == 1){
                size = values.lastIndexOf(intervals.get(iBreak).GetUpper() - 0.000001) + 1;
            } else if (iBreak == intervalNum){
                size = values.lastIndexOf(intervals.get(iBreak).GetUpper()) - 
                    values.lastIndexOf(intervals.get(iBreak).GetLower() - 0.000001);
            } else {
                size = values.lastIndexOf(intervals.get(iBreak).GetUpper() - 0.000001) - 
                    values.lastIndexOf(intervals.get(iBreak).GetLower() - 0.000001);
            }
            System.out.println(String.format(
            "Interval #%d, Size is %d Lower limit: %f; Upper limit: %f", iBreak, size,
            intervals.get(iBreak).GetLower(), intervals.get(iBreak).GetUpper()));
        }
            
        System.out.println("----------Finish calculating intervals----------");
        return intervals;
    }
    
    public static int CheckInterval(Map<Integer, Interval> intervals, double checkValue){
        for (int level : intervals.keySet()){
            Interval interval = intervals.get(level);
            if (checkValue >= interval.GetLower() 
                    && checkValue <= interval.GetUpper()) return level;
        }
        return 0;
    }
}
