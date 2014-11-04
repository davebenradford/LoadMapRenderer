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
public class BMPHoldingPond extends BMPItem
{
    private String _name;
    private int _hru;   //hru index in SWAT, used to find corresponding hru file
    private int _cattles;
    private int _clay_liner;
    private int _plastic_liner;
    private int _wire_fence;
    private double _distance;
    private double _trenching;
    private int _pond_year;

    public BMPHoldingPond(ResultSet row, int feaIndex, Project project, Scenario scenario) throws SQLException{
        super(row, feaIndex, BMPType.Holding_Pond, project, scenario);
    }
    
    //public BMPHoldingPond(System.Data.DataRow row, Project project, Scenario scenario) : base(row, BMPType.Hoding_Pond, project, scenario) { }

    public int getLifeTime() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _pond_year;
        else {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            return hd.getLifeTime(); 
        }  
    }
    public void setLifeTime(int value) throws CloneNotSupportedException{
        if (getDesignItem() == null)
            _pond_year = value;
        else    {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            hd.setLifeTime(value); 
        }  
    }

    public int getCattles() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _cattles;
        else {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            return hd.getCattles(); 
        }  
    }
    public void setCattles(int value) throws CloneNotSupportedException{
        if (getDesignItem() == null)
            _cattles = value;
        else    {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            hd.setCattles(value); 
        }  
    }
    
    public double getDistance() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _distance;
        else {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            return hd.getDistance(); 
        }  
    }
    public void setDistance(double value) throws CloneNotSupportedException{
        if (getDesignItem() == null)
            _distance = value;
        else    {
            BMPHoldingPond hd = (BMPHoldingPond) getDesignItem();
            hd.setDistance(value); 
        }  
    }

    

    @Override
    public double getCost(){
        try{
            double sqrtCattles = Math.sqrt(getCattles());        
            double temp3 = 2.232 * getCattles()+ 11.338 * sqrtCattles;
            double temp = 3.72 * getCattles()+ _trenching * 7.94 * sqrtCattles + 0.844 * getDistance() +
                _clay_liner * temp3;

            double temp2 = (0.5 * 9.5 + 7.47) * temp3;

            double max = 1.38e-10 * Math.pow(temp,2.0) 
                - 5.027e-5 * temp 
                + 6.736 + _clay_liner * temp2
                + _plastic_liner / 0.7 * temp2
                + _wire_fence * (189.0 + Math.sqrt(820.0 * getCattles()))
                + 10000.0;
            max *= 1.1483;

            temp3 = 1.512 * getCattles()+ 9.332 * sqrtCattles;
            temp = 2.52 * getCattles()+ _trenching * 6.54 * sqrtCattles + 0.844 * getDistance() +
                _clay_liner * temp3;
            temp2 = (0.5 * 9.5 + 7.47) * temp3;
            double min = 1.38e-10 * Math.pow(temp, 2.0)
                - 5.027e-5 * temp
                + 6.736 + _clay_liner * temp2
                + _plastic_liner / 0.7 * temp2
                + _wire_fence * (189.0 + Math.sqrt(556.0 * getCattles()))
                + 10000.0;
            min *= 1.1483;

            return min / 2.0 + max / 2.0; 
            }
        catch (Exception e){
            
        }
        return 0.0;
    }

    @Override
    public double getAnnualCost(){
        try {
            if (getLifeTime() > 0)
                return getCost() / getLifeTime();
            else
                return getCost();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPHoldingPond.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }

    public double getMaintenance(){
        try{
            double temp = 0.03048 * Math.pow(Math.sqrt(1.68 * getCattles()) - 6,2.0);
        
            double min = 1.38e-10 * Math.pow(temp, 2.0)
                - 5.027e-5 * temp
                + 6.737
                + _wire_fence * (24.48 + 3.05 * Math.sqrt(getCattles()))
                + 1.25 * getCattles();

            temp = 0.03048 * Math.pow(Math.sqrt(2.48 * getCattles()) - 6, 2.0);
            double max = 1.38e-10 * Math.pow(temp, 2.0)
                - 5.027e-5 * temp
                + 6.737
                + _wire_fence * (24.48 + 3.71 * Math.sqrt(getCattles()))
                + 1.85 * getCattles();

            return min / 2.0 + max / 2.0; 
            }
        catch (Exception e){
            
        }
        return 0.0;
    }

    public String getMaintenance_String(){
        return String.valueOf(getMaintenance());
    }

    public double getTotalAnnualCost(){
        return getAnnualCost() + getMaintenance();
    }

    public String getTotalAnnualCost_String() {
        return String.valueOf(getTotalAnnualCost());
    }

    @Override
    public String toString(){
        return String.valueOf(getID());
        //return String.Format("{0}:{1}", ID, _name);
        //return String.Format("{0}:{1}:{2}:{3}", ID, _name, Cattles, getDistance());
    }

    @Override
    public String InsertSQL_Economic(int year){
        return String.valueOf(getID()) + ","
            + String.valueOf(year) + ","
            + String.valueOf(getTotalAnnualCost()) + ");";
    }

    @Override
    public String InsertSQL(){
        try {
            return String.valueOf(getID()) + ","
                    + String.valueOf(_hru) + ","
                    + String.valueOf(getCattles()) + ","
                    + String.valueOf(_clay_liner) + ","
                    + String.valueOf(_plastic_liner) + ","
                    + String.valueOf(_wire_fence) + ","
                    + String.valueOf(_distance) + ","
                    + String.valueOf(_trenching) + ","
                    + String.valueOf(getLifeTime()) + ","
                    + String.valueOf(getCost()) + ","
                    + String.valueOf(getAnnualCost()) + ","
                    + String.valueOf(getMaintenance()) + ","
                    + String.valueOf(getTotalAnnualCost()) + ");";
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPHoldingPond.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String _column_name_name = "MAP_NUMBER";

    protected void readData() throws SQLException
    {
        super.readData();

        _name = getColumnValue_String(_column_name_name);
        _hru = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondHRU);
        _cattles = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondCattles);
        _clay_liner = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondClayLiner);
        _plastic_liner = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondPlasticLn);
        _wire_fence = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondWireFence);
        _distance = getColumnValue_Double(ScenarioDatabaseStructure.columnNameHoldingPondDistance);
        _trenching = getColumnValue_Double(ScenarioDatabaseStructure.columnNameHoldingPondTrenching);
        _pond_year = getColumnValue_Int(ScenarioDatabaseStructure.columnNameHoldingPondPondYrs);
    }

}
