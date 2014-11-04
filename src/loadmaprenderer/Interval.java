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
public class Interval {
    private double upperLimit = 0.0;
    private double lowerLimit = 0.0;
    public Interval(Double lower, Double upper){
        if (upper < lower){
            System.out.println("Setting intervals errors! Lower larger than upper!");
            return;
        }
        SetUpper(upper);
        SetLower(lower);
    }
    
    public double GetUpper(){
        return upperLimit;
    }
    public void SetUpper(double value){
        upperLimit = value;
    }
    
    public double GetLower(){
        return lowerLimit;
    }
    public void SetLower(double value){
        lowerLimit = value;
    }
    
}
