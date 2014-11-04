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
public enum SWATResultColumnType
{
    water,    
    sediment,
    PP,
    DP,
    TP,
    PN,
    DN,
    TN;
    public static int getInt(SWATResultColumnType type){
        int i = 0;
        switch (type){
            case water: 
                return i;
            case sediment:
                return i + 1;
            case PP:
                return i + 2;
            case DP:
                return i + 3;
            case TP:
                return i + 4;
            case PN:
                return i + 5;
            case DN:
                return i + 6;
            case TN:
                return i + 7;
        }
        return -1;
    }
    
    public static SWATResultColumnType getEnum(int value){
        switch (value){
            case 0: 
                return water;
            case 1:
                return sediment;
            case 2:
                return PP;
            case 3:
                return DP;
            case 4:
                return TP;
            case 5:
                return PN;
            case 6:
                return DN;
            case 7:
                return TN;
        }
        return null;
    }
}
