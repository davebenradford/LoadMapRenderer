/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Shao
 */
public class FieldWeight extends ResultBase{
    private int _field;
    private double _area;


    public int getID() {
        return _field;
    }

    public double getArea(){
        return _area;
    }

    public double Weight(SWATResultColumnType type){
        return Result(type);
    }

    public FieldWeight(ResultSet row) throws SQLException {
        RowItem item = new RowItem(row);

        _field = item.getColumnValue_Int("field");
        _area = item.getColumnValue_Double("area");

        for (SWATResultColumnType type : SWATResultColumnType.values())
            this.SetResult(type, item.getColumnValue_Double(type.toString()));
    }
}
