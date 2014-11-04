/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class Project {
    
    private static Map<Integer, Scenario> scenarios;
    private String path;
    private int currentSceanrioID = -1;
    
    public static String GetSpatialDB(){
        return "C:\\Users\\radfordd\\Documents\\NetBeansProjects\\LoadMapRenderer\\build\\classes\\loadmaprenderer\\resources\\STC\\Data\\Spatial\\"
                + "spatial.db3";
    }
    
    public static String GetWEBs(){
        return "C:\\Users\\radfordd\\Documents\\NetBeansProjects\\LoadMapRenderer\\build\\classes\\loadmaprenderer\\resources\\STC\\Data\\txtinout\\"
                + "webs.webs";
    }
    
    public static String GetSWATExecutive(){
        return "C:\\Users\\radfordd\\Documents\\NetBeansProjects\\LoadMapRenderer\\build\\classes\\loadmaprenderer\\resources\\STC\\Data\\txtinout\\"
                + "SWAT_STC.exe";
    }
    
    public static String GetSpatialDir(){        
        return "C:\\Users\\radfordd\\Documents\\NetBeansProjects\\LoadMapRenderer\\build\\classes\\loadmaprenderer\\resources\\STC\\Data\\Spatial";

    }
    public static String GetSpatialBMPTableName(BMPType type){
        switch (type)
        {          
            case Small_Dam:
                return "small_dam";
            case Holding_Pond:
                return "Holding_Pond";
            case Grazing:
                return "cattle_yard";
            case Tillage_Field:
            case Forage_Field:
                return "land2010_by_land_id";
            case Tillage_Farm:
            case Forage_Farm:
                return "farm2010";
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return "Subbasin";
            default:
                return "";
        }
    }
    
    public List<Scenario> getScenarios(){
        if (scenarios == null) scenarios = new HashMap();
        return Arrays.asList(scenarios.values().toArray(new Scenario[scenarios.size()]));
    }
    public void addScenario(Scenario s) throws Exception{
        if (getScenarios().contains(s)){
            throw new Exception("Scenario exists already, please check!");
        }
        
        if (s.GetScenarioType() == ScenarioType.Base_Conventional){
            scenarios.put(-1, s);
            currentSceanrioID = -1;
        } else if (s.GetScenarioType() == ScenarioType.Base_Historical){
            scenarios.put(0, s);
            currentSceanrioID = 0;
        } else {
            int maxID = 0;
            for (int i : scenarios.keySet()){
                if (maxID < i) maxID = i;
            }
            scenarios.put(maxID + 1, s);
            currentSceanrioID = maxID + 1;
        }
    }
    
    public int GetScenariosID(Scenario s) throws Exception{
        if (s.GetBMPScenerioBaseType() == BMPScenerioBaseType.Conventional){
            return -1;
        } else if (s.GetBMPScenerioBaseType() == BMPScenerioBaseType.Historic){
            return 0;
        } else {
            for (int i : scenarios.keySet()){
                if (scenarios.get(i) == s) {
                    return i;
                }
            }
            this.addScenario(s);
        }        
        return GetScenariosID(s);
    }
    
    public Scenario GetCurrentScenario(){
        return scenarios.get(currentSceanrioID);
    }
}
