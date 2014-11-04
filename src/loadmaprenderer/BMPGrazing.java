/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shao
 */
public class BMPGrazing extends BMPItem{
    private double _area;
    private double _unit_cost;
    
    public BMPGrazing(ResultSet row, int feaIndex, Project project, Scenario scenario) throws SQLException {
        super(row, feaIndex, BMPType.Grazing, project, scenario);
    }
    
    public double getArea(){
        return _area;
    }
    
    public double getUnitCost() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _unit_cost;
        else
            return ((BMPGrazing) super.getDesignItem()).getUnitCost();
    }
    public void setUnitCost(double value) throws CloneNotSupportedException{
        if (getDesignItem() == null)
            _unit_cost = value;
        else
            ((BMPGrazing) super.getDesignItem()).setUnitCost(value);
    }
    
    @Override
    public double getCost() {
        try {
            return getArea() * getUnitCost();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPGrazing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -999;
    }

    @Override
    public double getAnnualCost() {
        return 0.0;
    }

    @Override
    public String InsertSQL() {
        String sql = new String();
        try {
            sql = String.valueOf(getID()) + ","
                    + String.valueOf(this.getArea()) + ","
                    + String.valueOf(this.getUnitCost()) + ","
                    + String.valueOf(this.getCost()) + ");";
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPGrazing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sql;
    }

    @Override
    public String InsertSQL_Economic(int year) {
        String sql = new String();
        try {
            sql = String.valueOf(getID()) + ","
                    + String.valueOf(year) + ","
                    + String.valueOf(getUnitCost()) + ");"; //unit result is used for areal BMP, i.e. grazing, tillage and forage conversion
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPGrazing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sql;
    }
    
    @Override
    protected void readData() throws SQLException{
        super.readData();

        _area = getColumnValue_Double(ScenarioDatabaseStructure.columnNameGrazingArea);
        _unit_cost = getColumnValue_Double(ScenarioDatabaseStructure.columnNameGrazingUnitCost);
    }
            
    
}
