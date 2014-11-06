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
    private BMPScenarioBaseType baseScenarioType = BMPScenarioBaseType.Historic;
    private BMPSelectionLevelType cropBMPLevel = BMPSelectionLevelType.Unknown;
    private boolean hasSWATResult = false;
    private boolean hasEcoResult = false;
    
    private ScenarioIntegratedResult scnearioResult;
    
    private Map<BMPType, List<Integer>> scenarioBMPSelector = new HashMap();
    
    public Scenario() {
        createTime = Calendar.getInstance();
        lastModifiedTime = Calendar.getInstance();
        scenarioType = ScenarioType.Normal;
    }
        
    public Scenario(String prjPath, String descri, String db, 
            ScenarioType type, BMPScenarioBaseType baseType, 
            BMPSelectionLevelType cropLevel, String name) throws Exception{
        projectPath = prjPath;
        scenarioDescription = descri;
        scenarioName = name;
        scenarioDB = db;
        scenarioType = type;
        cropBMPLevel = cropLevel;

        if (type == ScenarioType.Base_Conventional)
            baseScenarioType = BMPScenarioBaseType.Conventional;
        else if (type == ScenarioType.Base_Historical)
            baseScenarioType = BMPScenarioBaseType.Historic;
        else baseScenarioType = baseType;

        createTime = Calendar.getInstance();
        lastModifiedTime = Calendar.getInstance();
    }   
    
    
    public String getName(){
        return scenarioName;
    }
    public void setName(String value){
        scenarioName = value;
    }
    
    public String getDescription(){
        return scenarioDescription;
    }
    public void setDescription(String value){
        scenarioDescription = value;
    }
    
    public BMPScenarioBaseType getBMPScenerioBaseType(){
        return baseScenarioType;
    }
    
    public TillageType getTillageType(){
        return tillageType;
    }
    
    public String getScenarioDB(){
        if (scenarioDB == "" || scenarioDB == null)
            System.out.println("Scenario database missing! Please check the "
                    + "path!");
        return scenarioDB;
    }
    public void setScenarioDB(String value){
        scenarioDB = value;
    }
    
    public int getStartYear(){
        return 1991;
    }
    
    public int getEndYear(){
        return 2010;
    }
    
    public BMPSelectionLevelType getCropBMPLevel(){
        return cropBMPLevel;
    }
    public void SetCropBMPLevel(BMPSelectionLevelType value){
        cropBMPLevel = value;
    }
    
    public ScenarioDesign getScenarioDesign(){
        if (design == null){
            return new ScenarioDesign(getScenarioDB());
        }
        return design;
    }
    public void setScenarioDesign(ScenarioDesign value){
        design = value;
    }
    
    public ScenarioType getScenarioType(){
        return scenarioType;
    }
    
    public void setBMPSelector(BMPType type, List<Integer> ids){
        scenarioBMPSelector.put(type, ids);
    }
    
    public boolean getHasSWATResult(){
        return hasSWATResult;
    }
    public void setHasSWATResult(boolean value){
        hasSWATResult = value;
    }
    
    public boolean getHasEcoResult(){
        return hasEcoResult;
    }
    public void setHasEcoResult(boolean value){
        hasEcoResult = value;
    }
    
    public void saveDesign(Project project, boolean resetSWAT, boolean resetEconomic) throws Exception{
        if (scenarioBMPSelector == null || scenarioBMPSelector.size() == 0) return;
        if (getScenarioType() != ScenarioType.Normal) return;  //don't save two base scenarios

        for (BMPType type : scenarioBMPSelector.keySet())
        {
            Connection conn = Query.OpenConnection(scenarioDB);
            this.getScenarioDesign().SaveDesignIDs(conn, scenarioBMPSelector.get(type), type, this); 
            // !! Shao.H selector.getSelectedBMPs() returns the list of BMPItems that are selected by user
        }
        
        /*
        for (BMPType type : scenarioBMPSelector.keySet())
        {
            String path = Project.getSpatialDir() + 
                    Project.GetSpatialBMPTableName(type) + ".dbf";
            String query = "Select * from " + Project.GetSpatialBMPTableName(type);
            Connection conn = Query.OpenConnection(path);
            ResultSet rs = Query.GetDataTable(query, path);
            List<BMPItem> items = new ArrayList();
            for (BMPItem item : BMPItem.FromFeatureLayer(rs, type, project, this).values()){
                items.add(item);
            }
            this.getScenarioDesign().SaveDesignBMPItems(conn, items, type, project, this); 
            // !! Shao.H selector.getSelectedBMPs() returns the list of BMPItems that are selected by user
        }
        */

        lastModifiedTime = Calendar.getInstance();

        //scenario changed, the result needs to be updated
        //if(resetSWAT) this.getResult().getSWATResult().setHasResult(false);
        //if(resetEconomic) this.getResult().getEconomicResult().setHasResult(false);
        this.getScenarioDesign().Update();   //update design cache

        // !! Shao.H when save the design, update the check box list in result display BMP selector,
        //    update the economic list in the result display column selector
        //if (onSaveDesign != null) onSaveDesign(this, new EventArgs());
    }
    
    public ScenarioIntegratedResult getResult(){
        if (scnearioResult == null){
            scnearioResult = new ScenarioIntegratedResult(getScenarioDB());
            scnearioResult.SetStartYear(this.getStartYear());
            scnearioResult.SetEndYear(this.getEndYear());
        }
            
        return scnearioResult;
    }
    public void setResult(ScenarioIntegratedResult value){
        scnearioResult = value;
    }    
    
    public Scenario getBaseScenario(Project project){
        for (Scenario s : project.getScenarios())
            if (s.getScenarioType() != ScenarioType.Normal && s.getBMPScenerioBaseType() == this.getBMPScenerioBaseType())
                return s;
        return null;
    }       
    
    public void runEconomic() throws Exception{
        //save first
        //SaveDesign(project,false,true);

        //run economic
        ScenarioEconomicModel Eco = new ScenarioEconomicModel(this.getScenarioDB());
        Eco.SetScenario(this);
        Eco.RunEconomic();
        getResult().GetEconomicResult().SetHasResult(true);

        getResult().GetEconomicResult().Update();
    }
    
    public void runSWAT() throws Exception{
        //save first
        //SaveDesign(project,false,true);
        
        //run SWAT        
        getResult().GetSWATResult().RunSWAT(this);
        
        getResult().GetSWATResult().SetHasResult(true);
        
        getResult().GetSWATResult().Update();
    }
    
    public void update(){
        hasEcoResult = false;
        hasSWATResult = false;
    }
    
}
