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
public interface IAccessColumn
    {
        String getColumnName();
        Class getType();
        Object getDefaultValue();
        boolean isPrimaryKey();
        boolean isUnique();
    }
