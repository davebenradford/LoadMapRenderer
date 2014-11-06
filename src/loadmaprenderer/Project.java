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
    private static String dataPath;
    private int currentSceanrioID = -1;
    private int maxID = 0;
        
    public void setDataPath(String path){
        dataPath = path;
    }
    
    public static String getSpatialDB(){
        return dataPath + "\\Spatial\\" + "spatial.db3";
    }
    
    public static String getWEBs(){
        return dataPath + "\\txtinout\\" + "webs.webs";
    }
    
    public static String getSWATExecutive(){
        return dataPath + "\\txtinout\\"
                + "SWAT_STC.exe";
    }
    
    public static String getSpatialDir(){        
        return dataPath + "\\Spatial";

    }
    public static String GetSpatialBMPTableName(BMPType type){
        switch (type)
        {          
            case Small_Dams:
                return "small_dam";
            case Holding_Ponds:
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
        
        if (s.getScenarioType() == ScenarioType.Base_Conventional){
            scenarios.put(-1, s);
            currentSceanrioID = -1;
        } else if (s.getScenarioType() == ScenarioType.Base_Historical){
            scenarios.put(0, s);
            currentSceanrioID = 0;
        } else {
            maxID++;
            scenarios.put(maxID, s);
            currentSceanrioID = maxID;
        }
    }
    
    public int getScenariosID(Scenario s) throws Exception{
        if (s.getScenarioType() == ScenarioType.Base_Conventional){
            return -1;
        } else if (s.getScenarioType() == ScenarioType.Base_Historical){
            return 0;
        } else {
            for (int i : scenarios.keySet()){
                if (scenarios.get(i) == s) {
                    return i;
                }
            }
            this.addScenario(s);
        }        
        return getScenariosID(s);
    }
    
    public Scenario getScenario(int ID){
        if (ID > maxID || ID < -1) {
            ID = maxID;
            System.out.println("Searching ID out of boundry!");
        }
        return scenarios.get(ID);
    }
        
    public Scenario getScenario(String name){
        if (name.isEmpty()) return new Scenario();
        for (Scenario s : scenarios.values()){
            if (s.getName() == name) return s;
        }
        System.out.println("Searching scenaro name not found!");
        return new Scenario();
    }
    
    public void setCurrentScenario(int value){
        currentSceanrioID = value;
    }
    public Scenario getCurrentScenario(){
        return scenarios.get(currentSceanrioID);
    }
    
    public List<String> getSameBasedScenarios(Scenario inScen){
        List<String> names = new ArrayList();
        for (int i = -1; i <= maxID; i++){
            Scenario s = this.getScenario(i);
            if (s == inScen) continue;
            if (s.getBMPScenerioBaseType() == inScen.getBMPScenerioBaseType()){
                if ((inScen.getHasSWATResult() && s.getHasSWATResult()) || 
                        (inScen.getHasEcoResult() && s.getHasEcoResult())) {
                    names.add(s.getName());
                }
            }
        }
        return names;
    }
    
    public boolean isNameValid(String name){
        List<Scenario> ss = getScenarios();
        if (ss == null) return true;
        for (Scenario s : ss) if (s.getName() == name) return false;
        return true;        
    }
    
    public int getMaxID(){
        return maxID;
    }
}
