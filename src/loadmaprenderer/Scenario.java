/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class Scenario {
    private static TillageType tillageType = TillageType.Conventional;
    private ScenarioDesign design;
    private BMPScenerioBaseType baseType = BMPScenerioBaseType.Historic;
    
    private String scenarioDB;

    private ScenarioType scenarioType;
    private String scenarioName;
    private String scenarioDescription;
    private Calendar createTime;
    private Calendar lastModifiedTime;
    private Calendar lastSWATRunTime;
    private String projectPath;
    private boolean isNewBMPSelector;
    private boolean isNewControl;
    private BMPScenerioBaseType baseScenarioType = BMPScenerioBaseType.Historic;
    private BMPSelectionLevelType cropBMPLevel = BMPSelectionLevelType.Unknown; 
    
    private ScenarioIntegratedResult scnearioResult;
    
    private Map<BMPType, List<Integer>> scenarioBMPSelector = new HashMap();
    
    public Scenario() {
        createTime = Calendar.getInstance();
        lastModifiedTime = Calendar.getInstance();
        scenarioType = ScenarioType.Normal;
    }
        
    public Scenario(String prjPath, String descri, String db, 
            ScenarioType type, BMPScenerioBaseType baseType, 
            BMPSelectionLevelType cropLevel, String name) throws Exception{
        projectPath = prjPath;
        scenarioDescription = descri;
        scenarioName = name;
        scenarioDB = db;
        scenarioType = type;
        cropBMPLevel = cropLevel;

        if (type == ScenarioType.Base_Conventional)
            baseScenarioType = BMPScenerioBaseType.Conventional;
        else if (type == ScenarioType.Base_Historical)
            baseScenarioType = BMPScenerioBaseType.Historic;
        else baseScenarioType = baseType;

        createTime = Calendar.getInstance();
        lastModifiedTime = Calendar.getInstance();
    }   
    
    public BMPScenerioBaseType GetBMPScenerioBaseType(){
        return baseType;
    }
    
    public TillageType GetTillageType(){
        return tillageType;
    }
    
    public String GetScenarioDB(){
        if (scenarioDB == "" || scenarioDB == null)
            System.out.println("Scenario database missing! Please check the "
                    + "path!");
        return scenarioDB;
    }
    public void SetScenarioDB(String value){
        scenarioDB = value;
    }
    
    public int GetStartYear(){
        return 1991;
    }
    
    public int GetEndYear(){
        return 2010;
    }
    
    public BMPSelectionLevelType GetCropBMPLevel(){
        return cropBMPLevel;
    }
    public void SetCropBMPLevel(BMPSelectionLevelType value){
        cropBMPLevel = value;
    }
    
    public ScenarioDesign GetScenarioDesign(){
        if (design == null){
            return new ScenarioDesign(GetScenarioDB());
        }
        return design;
    }
    public void SetScenarioDesign(ScenarioDesign value){
        design = value;
    }
    
    public ScenarioType GetScenarioType(){
        return scenarioType;
    }
    
    public void SetBMPSelector(BMPType type, List<Integer> ids){
        scenarioBMPSelector.put(type, ids);
    }
    
    public void SaveDesign(Project project, boolean resetSWAT, boolean resetEconomic) throws Exception{
        if (scenarioBMPSelector == null || scenarioBMPSelector.size() == 0) return;
        if (GetScenarioType() != ScenarioType.Normal) return;  //don't save two base scenarios

        for (BMPType type : scenarioBMPSelector.keySet())
        {
            Connection conn = Query.OpenConnection(scenarioDB);
            this.GetScenarioDesign().SaveDesignIDs(conn, scenarioBMPSelector.get(type), type, project, this); 
            // !! Shao.H selector.getSelectedBMPs() returns the list of BMPItems that are selected by user
        }
        
        /*
        for (BMPType type : scenarioBMPSelector.keySet())
        {
            String path = Project.GetSpatialDir() + 
                    Project.GetSpatialBMPTableName(type) + ".dbf";
            String query = "Select * from " + Project.GetSpatialBMPTableName(type);
            Connection conn = Query.OpenConnection(path);
            ResultSet rs = Query.GetDataTable(query, path);
            List<BMPItem> items = new ArrayList();
            for (BMPItem item : BMPItem.FromFeatureLayer(rs, type, project, this).values()){
                items.add(item);
            }
            this.GetScenarioDesign().SaveDesignBMPItems(conn, items, type, project, this); 
            // !! Shao.H selector.getSelectedBMPs() returns the list of BMPItems that are selected by user
        }
        */

        lastModifiedTime = Calendar.getInstance();

        //scenario changed, the result needs to be updated
        //if(resetSWAT) this.getResult().getSWATResult().setHasResult(false);
        //if(resetEconomic) this.getResult().getEconomicResult().setHasResult(false);
        this.GetScenarioDesign().Update();   //update design cache

        // !! Shao.H when save the design, update the check box list in result display BMP selector,
        //    update the economic list in the result display column selector
        //if (onSaveDesign != null) onSaveDesign(this, new EventArgs());
    }
    
    public ScenarioIntegratedResult GetResult(){
        if (scnearioResult == null){
            scnearioResult = new ScenarioIntegratedResult(GetScenarioDB());
            scnearioResult.SetStartYear(this.GetStartYear());
            scnearioResult.SetEndYear(this.GetEndYear());
        }
            
        return scnearioResult;
    }
    public void SetResult(ScenarioIntegratedResult value){
        scnearioResult = value;
    }    
    
    public Scenario BaseScenario(Project project){
        for (Scenario s : project.getScenarios())
            if (s.GetScenarioType() != ScenarioType.Normal && s.GetBMPScenerioBaseType() == this.GetBMPScenerioBaseType())
                return s;
        return null;
    }       
    
    public void RunEconomic() throws Exception{
        //save first
        //SaveDesign(project,false,true);

        //run economic
        ScenarioEconomicModel Eco = new ScenarioEconomicModel(this.GetScenarioDB());
        Eco.SetScenario(this);
        Eco.RunEconomic();
        GetResult().GetEconomicResult().SetHasResult(true);

        GetResult().GetEconomicResult().Update();
    }
    
    public void RunSWAT() throws Exception{
        //save first
        //SaveDesign(project,false,true);
        
        //run SWAT        
        GetResult().GetSWATResult().RunSWAT(this);
        
        GetResult().GetSWATResult().SetHasResult(true);
        
        GetResult().GetSWATResult().Update();
    }
    
}
