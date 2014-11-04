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
public class CropEconomic {
    protected double _yield = 0.0;
    protected double _revenue= 0.0;
    protected double _cost= 0.0;
    protected double _net_return= 0.0;
    
    public CropEconomic() { }
    
    public CropEconomic(Double yield, Double revenue, Double cost, Double netreturn) {
        _yield = yield;
        _revenue = revenue;
        _cost = cost;
        _net_return = netreturn;
    }
    
    public Double getYield(){
        return _yield;
    }
    
    public Double getRevenue(){
        return _revenue;
    }
    
    public Double getEconomicCost(){
        return _cost;
    }
    
    public Double getNetReturn(){
        return _net_return;
    }
    
    public String InsertSQL(String prefix, int startYear, int endYear){ // !! virtual
        return String.format("%f,%f,%f,%f",
        getYield(),getRevenue(),getEconomicCost(),getNetReturn());
    }

}
