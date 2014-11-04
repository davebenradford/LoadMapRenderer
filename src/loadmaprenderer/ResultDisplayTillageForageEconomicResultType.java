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
public enum ResultDisplayTillageForageEconomicResultType
{
    Yield,
    Revenue,
    Cost,
    net_return,
    BMPCost;
    public static int getInt(ResultDisplayTillageForageEconomicResultType type){
        int i = 0;
        switch (type){
            case Yield: 
                return i;
            case Revenue:
                return i + 1;
            case Cost:
                return i + 2;
            case net_return:
                return i + 3;
            case BMPCost:
                return i + 4;
        }
        return -1;
    }
    
    public static ResultDisplayTillageForageEconomicResultType getEnum(int value){
        switch (value){
            case 0: 
                return Yield;
            case 1:
                return Revenue;
            case 2:
                return Cost;
            case 3:
                return net_return;
            case 4:
                return BMPCost;
        }
        return null;
    }
}
