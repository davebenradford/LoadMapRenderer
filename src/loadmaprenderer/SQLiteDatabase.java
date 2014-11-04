/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shao
 */
public class SQLiteDatabase {
    private String dbPath;
    private static int startYear;
    private static int endYear;
    
    public String GetDatabasePath() {
        return dbPath;
    }
    
    public void SetDatabasePath(String databasePath) {
        dbPath = databasePath;
    }
    
    public static int GetStartYear() {
        return startYear;
    }
    
    public void SetStartYear(int value) {
        startYear = value;
    }
    
    public static int GetEndYear() {
        return endYear;
    }
    
    public void SetEndYear(int value) {
        endYear = value;
    }
    
    public SQLiteDatabase() { }
    
    public SQLiteDatabase(String value)  {
        dbPath = value;
    }
    
    protected void insert(Connection conn, String sql) throws SQLException {
        if (sql == null || sql.isEmpty()) {    
        }
        else {
            String[] sqls = sql.split(";");                   
            
            try {
                conn.setAutoCommit(false);
                for(String s : sqls) {   
                    conn.prepareStatement(s).executeUpdate();                    
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                conn.commit();
            }
        }
    }
    
    public static ResultDisplayTillageForageEconomicResultType asResultDisplayTillageForageEconomicResultType(String str) {
        for (ResultDisplayTillageForageEconomicResultType me : ResultDisplayTillageForageEconomicResultType.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }    
    
    public static SWATResultColumnType asSWATResultColumnType(String str) {
        for (SWATResultColumnType me : SWATResultColumnType.values()) {
            if (me.name().equalsIgnoreCase(str))
                return me;
        }
        return null;
    }
}
