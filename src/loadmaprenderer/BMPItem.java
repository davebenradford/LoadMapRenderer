/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Shao
 */
public abstract class BMPItem extends RowItem {
    public BMPItem(ResultSet row, int feaIndex, BMPType type, Project project, Scenario scenario) throws SQLException {
        super(row);
        _type = type;
        _isCropBMP = IsCropBMP(type);
        _isTillage = IsTillage(type);
        _forDesign = true; // H.Shao
        _featureIndex = feaIndex;
        _project = project;
        _scenario = scenario;

        readData();
    }    
    
    private static String _column_name_id = "ID";
    private static String _column_name_existing = "Existing";
    //private IFeature _fea;
    private int _id;
    private boolean _existing;
    private BMPType _type;
    private boolean _isCropBMP = false;
    private boolean _isTillage = false;
    private BMPItem _designItem;
    private boolean _forDesign = true; // H.Shao
    private boolean _isSelectedInScenario = false;
    private int _featureIndex = -1;
    protected Project _project;
    protected Scenario _scenario;    

    public boolean getForDesign(){
        return _forDesign;
    }
    public void setForDesign(boolean value){
        _forDesign = value;
    }
    
    public boolean getIsSelectedInScenario(){
        return _isSelectedInScenario;
    }
    public void setIsSelectedInScenario(boolean value){
        _isSelectedInScenario = value;
    }
    
    public BMPItem getDesignItem() throws CloneNotSupportedException{
        if (!getForDesign())
                {
                    if (_designItem == null)
                    {
                        _designItem = (BMPItem) this.clone();
                        _designItem.setForDesign(true);
                    }
                }
                return _designItem; 
    }
    public void setDesignItem(BMPItem value){
        _designItem = value;
        if(_designItem != null) 
            _isSelectedInScenario = true;
    }
    
    public BMPType getType() {
        return _type;
    }
    
    public boolean IsCropBMP() {
        return _isCropBMP; 
    }
    
    public boolean IsTillage() {
        return _isTillage; 
    }
    
    public int getFeatureIndex(){
        return _featureIndex;
    }
    
    public int getID(){
        return _id;
    }
    
    public boolean getExisting() {
        return _existing; 
    }
    
    public String getIDColumnName() {
        return _column_name_id; 
    }
    
    public String getCost_String() {
        return String.valueOf(getCost()); 
    }
    
    public String getAnnualCost_String() { // !! virtual
        return String.valueOf(getAnnualCost()); 
    }
    
    public abstract double getCost();
    
    public abstract double getAnnualCost();
    
    protected void readData() throws SQLException { // !! virtual
        _id = getColumnValue_Int(getIDColumnName());
        _existing = getColumnValue_Int(_column_name_existing) > 0;
    }
    
    public abstract String InsertSQL();
    
    public abstract String InsertSQL_Economic(int year);
    
    public void ResetParameter() throws CloneNotSupportedException{
        _designItem = (BMPItem) this.clone();
        _designItem.setDesignItem(null);
        _designItem.setForDesign(true);
    }
    
    public static boolean IsCropBMP(BMPType type){
        if (type == BMPType.Small_Dam || type == BMPType.Holding_Pond || type == BMPType.Grazing)
            return false;
        return true;
    }

    public static boolean IsTillage(BMPType type){
        if (type == BMPType.Tillage_Field || type == BMPType.Tillage_Farm || type == BMPType.Tillage_Subbasin)
            return true;
        return false;
    }

    public static String bmpType2String(BMPType type){
        switch (type)
        {
            case Small_Dam: return "Small Dam";
            case Holding_Pond: return "Holding Pond";
            case Grazing: return "Grazing";
            case Tillage_Subbasin:
            case Tillage_Field:
            case Tillage_Farm:
                return "Tillage";
            case Forage_Subbasin:
            case Forage_Field:
            case Forage_Farm:
                return "Forage Conversion";
            default:
                return "";
        }
    }
    
    private static Map<BMPType, BMPItem> fromDatasetAndScnearioDesign(Map<BMPType, BMPItem> itemsFromDataset, Project project, Scenario scenario) throws SQLException{
        if (itemsFromDataset.keySet().size() <= 0) return null;

        BMPType type = BMPType.Small_Dam;
        for (BMPItem item : itemsFromDataset.values())
        {
            type = item.getType();
            break;
        }

        Map<BMPType, BMPItem> designItems = FromScenarioDesign(type,project,scenario);
        if (designItems.keySet().size() <= 0) return itemsFromDataset;

        for (BMPItem item : itemsFromDataset.values())
        {
            BMPItem dItem = null;
            if (designItems.containsKey(item.getID()))
            {
                dItem = designItems.get(item.getID());
                item.setDesignItem(dItem);
            }
        }
        return itemsFromDataset;
    }
    
    public static Map<BMPType, BMPItem> FromScenarioDesign(BMPType type, Project project,Scenario scenario) throws SQLException{
        String tableName = ScenarioDesign.GetBMPTableName(type);
        String sql = "select * from " + tableName;
        return FromScenarioDesign(Query.GetDataTable(sql, scenario.GetScenarioDB()), type, project, scenario);
    }
    
    private static Map<BMPType, BMPItem> FromScenarioDesign(
        ResultSet rs, BMPType type, Project project, Scenario scenario) throws SQLException{            
        Map<BMPType, BMPItem> items = new HashMap();
        while (rs.next()){
            BMPItem item = addOneBMPItem(type, rs, -1, project, scenario, items, true);
            if (item.getFeatureIndex() > -1) items.put(type, item);
        }
            
        return items;
    }
    
    private static BMPItem addOneBMPItem(BMPType type, ResultSet r, int feaIndex, Project project, Scenario scenario,Map<BMPType, BMPItem> items, boolean isForDesign) throws SQLException{
        BMPItem item = null;
        switch (type)
        {          
            case Small_Dam:
                item = (BMPItem) new BMPSmallDam(r,feaIndex,project,scenario);
                break;
            case Holding_Pond:
                item = (BMPItem) new BMPHoldingPond(r, feaIndex, project, scenario);
                break;
            case Grazing:
                item = (BMPItem) new BMPGrazing(r, feaIndex, project, scenario);
                break;
            case Tillage_Field:
                item = (BMPItem) new BMPFieldTillage(r, feaIndex, project, scenario);
                break;
            case Tillage_Farm:
                item = (BMPItem) new BMPFarmTillage(r, feaIndex, project, scenario);
                break;
            case Tillage_Subbasin:
                item = (BMPItem) new BMPSubbasinTillage(r, feaIndex, project, scenario);
                break;
            case Forage_Field:
                item = (BMPItem) new BMPFieldForage(r, feaIndex, project, scenario);
                break;
            case Forage_Farm:
                item = (BMPItem) new BMPFarmForage(r, feaIndex, project, scenario);
                break;
            case Forage_Subbasin:
                item = (BMPItem) new BMPSubbasinForage(r, feaIndex, project, scenario);
                break;
        }

        feaIndex++;
        if (item.getID() <= 0) return (BMPItem) new BMPSmallDam(r,-2,project,scenario);
        if (type == BMPType.Forage_Field ||
            type == BMPType.Tillage_Field)
            if (item.getID() >= 600) return (BMPItem) new BMPSmallDam(r,-2,project,scenario);

        item.setForDesign(isForDesign);
        if(!items.containsKey(item.getID()))
            return item;
        return (BMPItem) new BMPSmallDam(r,-2,project,scenario);
    }
    
    public static Map<BMPType, BMPItem> FromDataset(ResultSet fs, BMPType type, Project project, Scenario scenario) throws SQLException{
        Map<BMPType, BMPItem> items = new HashMap<BMPType, BMPItem>();
        int feaIndex = 0;
        while (fs.next())
        {
            BMPItem item = addOneBMPItem(type, fs, feaIndex, project, scenario, items, false);
            if(item.getFeatureIndex() > -1) items.put(type, item);
            feaIndex++;
        }    
        return items;
    }   
    
    
    public static Map<BMPType, BMPItem> FromFeatureLayer(ResultSet rs, BMPType type,Project project, Scenario scenario) throws SQLException{
        Map<BMPType, BMPItem> layerItems = FromDataset(rs, type, project, scenario);

        Map<BMPType, BMPItem> allItems = fromDatasetAndScnearioDesign(layerItems, project, scenario);

        return allItems;
    }  
}
