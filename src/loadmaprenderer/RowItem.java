/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 *
 * @author Shao
 */
public class RowItem {
    protected ResultSet _row;
    
    public RowItem(ResultSet row) {
        _row = row;
    }
    
    private boolean isColumnValueInvalid(String columnName) throws SQLException{
        if (_row == null) return false;
        if (!hasColumn(_row, columnName)) return false;
        if (!isColumnNameEmpty(_row)) return false;

        return true;
    }
    
    public String getColumnValue_String(String columnName) throws SQLException{
        if (isColumnValueInvalid(columnName)) return _row.getString(columnName);
        return "";
    }

    public int getColumnValue_Int(String columnName) throws SQLException
    {
        if (isColumnValueInvalid(columnName)) return _row.getInt(columnName);
        return -1;
    }

    public double getColumnValue_Double(String columnName) throws SQLException
    {
        if (isColumnValueInvalid(columnName)) return _row.getDouble(columnName);
        return -99.0;
    }
    
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    } 
    
    public static boolean isColumnNameEmpty(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (rsmd.getColumnName(x).isEmpty()) {
                return false;
            }
        }
        return true;
    } 
}
