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
public class BMPSmallDam extends BMPItem
{
    private String _name;
    private double _embankment;
    private int _lifetime;

    public BMPSmallDam(ResultSet row, int feaIndex, Project project, Scenario scenario) throws SQLException{
        super(row, feaIndex, BMPType.Small_Dam, project, scenario);
    }
    //public BMPSmallDam(System.Data.DataRow row, Project project, Scenario scenario) : base(row, BMPType.Small_Dam,project,scenario) { }

    public double getEmbankment() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _embankment;
        else {
            BMPSmallDam sd = (BMPSmallDam) getDesignItem();
            return sd.getEmbankment(); 
        }   
    }
    public void setEmbankment(double value) throws CloneNotSupportedException{
        if (getDesignItem() == null) 
            _embankment = value; 
        else {
            BMPSmallDam sd = (BMPSmallDam) getDesignItem();
            sd.setEmbankment(value); 
        }             
    }

    public int getLifeTime() throws CloneNotSupportedException{
        if (getDesignItem() == null)
            return _lifetime;
        else {
            BMPSmallDam sd = (BMPSmallDam) getDesignItem();
            return sd.getLifeTime(); 
        }  
    }
    public void setLifeTime(int value) throws CloneNotSupportedException{
        if (getDesignItem() == null)
            _lifetime = value;
        else    {
            BMPSmallDam sd = (BMPSmallDam) getDesignItem();
            sd.setLifeTime(value); 
        }  
    }

    @Override
    public double getCost(){
        try {
            //change equation coefficient based on modified equation
            //2012-12-19
            return 4.87e-7 * Math.pow(getEmbankment(), 3.0) -
                    4.24e-3 * Math.pow(getEmbankment(), 2.0) +
                    1.28e1 * getEmbankment() + 6.71e3;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPSmallDam.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BMPSmallDam.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0.0;
    }

    @Override
    public String toString(){
        return String.format("{0}:{1}", getID(), _name);
        //return String.Format("{0}:{1}:{2}:{3}", ID, _name,Embankment,LifeTime);
    }

    @Override
    public String InsertSQL_Economic(int year){
        return String.valueOf(getID()) + ","
            + String.valueOf(year) + ","
            + String.valueOf(getAnnualCost()) + ");";
    }

    @Override
    public String InsertSQL(){
        try {
            return String.valueOf(getID()) + ","
                    + String.valueOf(getEmbankment()) + ","
                    + String.valueOf(getLifeTime()) + ");";
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BMPSmallDam.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    protected void readData() throws SQLException{
        super.readData();

        _name = getColumnValue_String("NAME"); ;
        _embankment = getColumnValue_Double(ScenarioDatabaseStructure.columnNameSmallDamEmbankment);
        _lifetime = getColumnValue_Int(ScenarioDatabaseStructure.columnNameSmallDamLifetime);
    } 

}
