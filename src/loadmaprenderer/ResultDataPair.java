/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.util.List;

/**
 *
 * @author Shao
 */
public class ResultDataPair {
    private int year;  // x vlue
    private double data;  // y vlue

    public int getYear() {return year;}
    public double getData() {return data;} 
    public void setData(double value) {data = value;}

    public ResultDataPair(int yearIn, double dataIn) {
        year = yearIn;
        data = dataIn;
    }        
}
