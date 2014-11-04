/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

/**
 *
 * @author Shao
 */
public class AccessColumn implements IAccessColumn {
    protected String columnName;
    protected Class type;
    protected Object defaultValue;    
    protected boolean primaryKey;
    protected boolean unique;
    
    public AccessColumn AccessColumn(String columnName, Class type){
        AccessColumn ac = new AccessColumn();
        if (!columnName.equals(""))ac.setColumnName(columnName);
        if (!type.equals(null))ac.setType(type);
        return ac;
    }
    /// <summary>
    /// Name of the data column.
    /// </summary>
    public String getColumnName(){// !! virtual
        if (columnName.isEmpty())
            return "";
        else
            return columnName;
    }
    
    public void setColumnName(String value){
        columnName = value;
    }

    /// <summary>
    /// Type of the data t.
    /// </summary>
    public Class<Object> getType(){// !! virtual
        if (type == null)
            return Object.class;
        else
            return type;
    }
    
    public void setType(Class value){
        type = value;
    }
    
    public Object getDefaultValue(){// !! virtual
        return defaultValue;
    }
    
    public void setDefaultValue(Object value){
        defaultValue = value;
    }
    
    /// <summary>
    /// True if the column is the primary key of the table.
    /// </summary>    
    public boolean isPrimaryKey() {// !! virtual
        return primaryKey;
    }
    public void setPrimaryKey(boolean value) {// !! virtual
        primaryKey = value;
    }
    
    /// <summary>
    /// True if the values of the column are unique in combination with the values of another column in the table.
    /// </summary>    
    public boolean isUnique() {// !! virtual
        return unique;
    }
    public void setUnique(boolean value) {// !! virtual
        unique = value;
    }        

}
