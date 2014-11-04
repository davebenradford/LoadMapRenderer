/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class ScenarioDesign extends ScenarioDatabaseStructure {

    public ScenarioDesign(String scenarioDatabasePath) {
        super(scenarioDatabasePath);
    }
    
    private boolean hasSmallDam = false;
    private boolean hasHoldingPond = false;
    private boolean hasGrazing = false;
    private boolean hasTillage = false;
    private boolean hasForageConversion = false;    
    
    private static Connection connSpatial = Query.OpenConnection(Project.GetSpatialDB());
    
    public boolean GetHasSmallDam(){
        return hasSmallDam;
    }
    public void SetHasSmallDam(boolean value){
        hasSmallDam = value;
    }
    
    public boolean GetHasGrazing(){
        return hasGrazing;
    }
    public void SetHasGrazing(boolean value){
        hasGrazing = value;
    }
    
    public boolean GetHasHoldingPond(){
        return hasHoldingPond;
    }
    public void SetHasHoldingPond(boolean value){
        hasHoldingPond = value;
    }
    
    public boolean GetHasTillage(){
        return hasTillage;
    }
    public void SetHasTillage(boolean value){
        hasTillage = value;
    }
    
    public boolean GetHasForageConversion(){
        return hasForageConversion;
    }
    public void SetHasForageConversion(boolean value){
        hasForageConversion = value;
    }
    
    
    public void PrepareTables() {
            prepareTable(BMPType.Small_Dam);
            prepareTable(BMPType.Holding_Pond);
            prepareTable(BMPType.Grazing);
            prepareTable(BMPType.Tillage_Field);
            prepareTable(BMPType.Forage_Field);
        }

    private void prepareTable(BMPType type){
        AccessColumn _id = new AccessColumn();_id.setColumnName(columnNameID);_id.setType(Integer.TYPE);
        AccessColumn _economic_year = new AccessColumn(); 
        _economic_year.setColumnName(columnNameYear); _economic_year.setType(Integer.TYPE);
        AccessColumn _economic_cost = new AccessColumn();
        _economic_cost.setColumnName(columnNameCost);_economic_cost.setType(Double.TYPE);

        switch (type)
        {
            case Small_Dam:
                AccessColumn ac_dam_1 = new AccessColumn();
                ac_dam_1.setColumnName(columnNameSmallDamEmbankment);ac_dam_1.setType(Double.TYPE);
                AccessColumn ac_dam_2 = new AccessColumn();
                ac_dam_2.setColumnName(columnNameSmallDamLifetime);ac_dam_2.setType(Integer.TYPE);
                Query.PrepareTable(GetDatabasePath(), tableNameSmallDam, _id, ac_dam_1, ac_dam_2);
                hasSmallDam = false;
                break;
            case Holding_Pond:
                AccessColumn ac_pond_1 = new AccessColumn();
                ac_pond_1.setColumnName(columnNameHoldingPondHRU);ac_pond_1.setType(Integer.TYPE);
                AccessColumn ac_pond_2 = new AccessColumn();
                ac_pond_2.setColumnName(columnNameHoldingPondCattles);ac_pond_2.setType(Double.TYPE);
                AccessColumn ac_pond_3 = new AccessColumn();
                ac_pond_3.setColumnName(columnNameHoldingPondClayLiner);ac_pond_3.setType(Integer.TYPE);
                AccessColumn ac_pond_4 = new AccessColumn();
                ac_pond_4.setColumnName(columnNameHoldingPondPlasticLn);ac_pond_4.setType(Integer.TYPE);
                AccessColumn ac_pond_5 = new AccessColumn();
                ac_pond_5.setColumnName(columnNameHoldingPondWireFence);ac_pond_5.setType(Integer.TYPE);
                AccessColumn ac_pond_6 = new AccessColumn();
                ac_pond_6.setColumnName(columnNameHoldingPondDistance);ac_pond_6.setType(Double.TYPE);
                AccessColumn ac_pond_7 = new AccessColumn();
                ac_pond_7.setColumnName(columnNameHoldingPondTrenching);ac_pond_7.setType(Double.TYPE);
                AccessColumn ac_pond_8 = new AccessColumn();
                ac_pond_8.setColumnName(columnNameHoldingPondPondYrs);ac_pond_8.setType(Integer.TYPE);
                AccessColumn ac_pond_9 = new AccessColumn();
                ac_pond_9.setColumnName(columnNameHoldingPondAnnualCost);ac_pond_9.setType(Double.TYPE);
                AccessColumn ac_pond_10 = new AccessColumn();
                ac_pond_10.setColumnName(columnNameHoldingPondMaintenance);ac_pond_10.setType(Double.TYPE);
                AccessColumn ac_pond_11 = new AccessColumn();
                ac_pond_11.setColumnName(columnNameHoldingPondTotalCost);ac_pond_11.setType(Double.TYPE);                
                Query.PrepareTable(GetDatabasePath(), tableNameHoldingPond, _id,ac_pond_1,
                        ac_pond_2,
                        ac_pond_3,
                        ac_pond_4,
                        ac_pond_5,
                        ac_pond_6,
                        ac_pond_7,
                        ac_pond_8,
                        _economic_cost,
                        ac_pond_9,
                        ac_pond_10,
                        ac_pond_11);
                hasHoldingPond = false;
                break;
            case Grazing:
                AccessColumn ac_grazing_1 = new AccessColumn();
                ac_grazing_1.setColumnName(columnNameGrazingArea);ac_grazing_1.setType(Double.TYPE);
                AccessColumn ac_grazing_2 = new AccessColumn();
                ac_grazing_2.setColumnName(columnNameGrazingUnitCost);ac_grazing_2.setType(Double.TYPE);
                Query.PrepareTable(GetDatabasePath(), tableNameGrazing,
                        _id,
                        ac_grazing_1,
                        ac_grazing_2,
                        _economic_cost);
                Query.PrepareTable(GetDatabasePath(), tableNameGrazingHRU, _id);
                hasGrazing = false;
                break;
            case Tillage_Farm:
            case Tillage_Field:
            case Tillage_Subbasin:
                AccessColumn ac_tillage_1 = new AccessColumn();
                ac_tillage_1.setColumnName(columnNameTillageType);ac_tillage_1.setType(Double.TYPE);
                Query.PrepareTable(GetDatabasePath(), tableNameTillage, _id);
                Query.PrepareTable(GetDatabasePath(), tableNameTillageHRU, _id,
                    ac_tillage_1);
                hasTillage = false;
                break;
            case Forage_Field:
            case Forage_Farm:
            case Forage_Subbasin:
                Query.PrepareTable(GetDatabasePath(), tableNameForage, _id);
                Query.PrepareTable(GetDatabasePath(), tableNameForageHRU, _id);
                hasForageConversion = false;
                break;
        }
    }
    
    private static String SQLPrefixsmalldam =
                "insert into " + tableNameSmallDam
                + " (" + columnNameID + ","
                + columnNameSmallDamEmbankment + ","
                + columnNameSmallDamLifetime + ") values (";

    private static String SQLPrefixHoldingPond =
            "insert into " + tableNameHoldingPond
            + " (" + columnNameID + ","
            + columnNameHoldingPondHRU + ","
            + columnNameHoldingPondCattles + ","
            + columnNameHoldingPondClayLiner + ","
            + columnNameHoldingPondPlasticLn + ","
            + columnNameHoldingPondWireFence + ","
            + columnNameHoldingPondDistance + ","
            + columnNameHoldingPondTrenching + ","
            + columnNameHoldingPondPondYrs + ","
            + columnNameCost + ","
            + columnNameHoldingPondAnnualCost + ","
            + columnNameHoldingPondMaintenance + ","
            + columnNameHoldingPondTotalCost + ") values (";

    private static String SQLPrefixGrazing =
            "insert into " + tableNameGrazing
            + " (" + columnNameID + ","
            + columnNameGrazingArea + ","
            + columnNameGrazingUnitCost + ","
            + columnNameCost + ") values (";
    
    private static String SQLPrefixsmalldamID =
                "insert into " + tableNameSmallDam
                + " (" + columnNameID + ") values (";

    private static String SQLPrefixHoldingPondID =
            "insert into " + tableNameHoldingPond
            + " (" + columnNameID + ") values (";

    private static String SQLPrefixGrazingID =
            "insert into " + tableNameGrazing
            + " (" + columnNameID + ") values (";

    private static String SQLPrefixGrazingHRU =
            "insert into " + tableNameGrazingHRU
            + " (" + columnNameID + ") values (";

    private static String SQLPrefixtillage =
            "insert into " + tableNameTillage
            + " (" + columnNameID + ") values (";

    private static String SQLPrefixtillageHRU =
            "insert into " + tableNameTillageHRU
            + " (" + columnNameID + ","
            + columnNameTillageType + ") values (";

    private static String SQLPrefixforage =
            "insert into " + tableNameForage
            + " (" + columnNameID + ") values (";

    private static String SQLPrefixforageHRU =
            "insert into " + tableNameForageHRU
            + " (" + columnNameID + ") values (";
    
    private void 
        saveCropBMPDesign(Connection conn, List<BMPItem> cropBMPs,Scenario scenario) throws SQLException, Exception{
        if (cropBMPs.size() == 0) return;
        if (!cropBMPs.get(0).IsCropBMP()) return;

        StringBuilder sb = new StringBuilder();

        //all the selected items
        boolean isTillage = cropBMPs.get(0).IsTillage();
        for (BMPItem item : cropBMPs)
        {
            if (item.IsTillage())
                sb.append(SQLPrefixtillage);
            else
                sb.append(SQLPrefixforage);
            sb.append(item.getID());
            sb.append(");");
        }

        //selected hrus
        List<Integer> hrus = Spatial.GetHRU(cropBMPs);
        for (int hru : hrus)
        {
            if (isTillage)
                sb.append(SQLPrefixtillageHRU);
            else
                sb.append(SQLPrefixforageHRU);

            sb.append(hru);

            if (isTillage)
            {
                sb.append(",");
                if (scenario.GetTillageType() == TillageType.Conventional)
                    sb.append(56);  //Mouldboard Plow	 MLDBGE10
                else
                    sb.append(4); //Minimal till	 MINIMALTILL
            }
            sb.append(");");
        }

        insert(conn, sb.toString());

        if (isTillage)
            hasTillage = true;
        else
            hasForageConversion = true;
    }

    private void saveCropBMPDesign(Connection conn, List<Integer> ids,BMPType type,Scenario scenario) throws Exception{
        if (ids.isEmpty()) return;
        if (!BMPItem.IsCropBMP(type)) return;

        StringBuilder sb = new StringBuilder();

        //all the selected items
        boolean isTillage = BMPItem.IsTillage(type);
        for (int id : ids)
        {
            if (isTillage)
                sb.append(SQLPrefixtillage);
            else
                sb.append(SQLPrefixforage);
            sb.append(id);
            sb.append(");");
        }

        //selected hrus           
        List<Integer> fields = null;
        if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Field)
            fields = ids;
        else if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Farm)
            fields = Spatial.GetRelationIDs(ResultLevelType.Farm, ResultLevelType.Field, ids, false);
        else if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Subbasin)
            fields = Spatial.GetRelationIDs(ResultLevelType.Subbasin, ResultLevelType.Field, ids, false);
        else
            throw new Exception("Unknown Level.");

        //fields --> HRUs
        List<Integer> hrus = Spatial.GetRelationIDs(ResultLevelType.Field, ResultLevelType.HRU, fields, false);

        for (int hru : hrus)
        {
            if (isTillage)
                sb.append(SQLPrefixtillageHRU);
            else
                sb.append(SQLPrefixforageHRU);

            sb.append(hru);

            if (isTillage)
            {
                sb.append(",");

                if (scenario.GetTillageType() == TillageType.Conventional)
                    sb.append(56);  //Mouldboard Plow	 MLDBGE10
                else
                    sb.append(4); //Minimal till	 MINIMALTILL
            }
            sb.append(");");
        }

        insert(conn, sb.toString());

        if (isTillage)
            hasTillage = true;
        else
            hasForageConversion = true;
    }

    private void saveSmallDamHoldingPondGrazingBMPItems(Connection conn, List<BMPItem> items, BMPType type) throws SQLException, Exception{
        if (BMPItem.IsCropBMP(type) || items.size() == 0) return;

        StringBuilder sb = new StringBuilder();
        for (BMPItem item : items)
        {
            switch (type)
            {
                case Small_Dam:
                    sb.append(SQLPrefixsmalldam + item.InsertSQL());
                    break;
                case Holding_Pond:
                    sb.append(SQLPrefixHoldingPond + item.InsertSQL());
                    break;
                case Grazing:
                    sb.append(SQLPrefixGrazing + item.InsertSQL());
                    break;                    
            }
        }

        //get correponding hrus from grazing
        if (type == BMPType.Grazing){
            List<Integer> hrus = Spatial.GetHRU(items);
            for (int hru : hrus){
                sb.append(SQLPrefixGrazing + String.valueOf(hru) + ");");
            }
        }

        insert(conn, sb.toString());

        //set status
        if (type == BMPType.Small_Dam) hasSmallDam = true;
        if (type == BMPType.Holding_Pond) hasHoldingPond = true;
        if (type == BMPType.Grazing) hasGrazing = true;
    }

    private void saveSmallDamHoldingPondGrazingIDs(Connection conn, List<Integer> ids, BMPType type) throws SQLException, Exception{
        if (BMPItem.IsCropBMP(type) || ids.size() == 0) return;

        StringBuilder sb = new StringBuilder();
        for (int id : ids)
        {
            switch (type)
            {
                case Small_Dam:
                    sb.append(SQLPrefixsmalldamID + String.valueOf(id) + ");");
                    break;
                case Holding_Pond:
                    sb.append(SQLPrefixHoldingPondID + String.valueOf(id) + ");");
                    break;
                case Grazing:
                    sb.append(SQLPrefixGrazingID + String.valueOf(id) + ");");
                    break;
            }
        }

        //get correponding hrus from grazing
        if (type == BMPType.Grazing){
            List<Integer> hrus = Spatial.GetHRU(ids, type);
            for (int hru : hrus){
                sb.append(SQLPrefixGrazingHRU + String.valueOf(hru) + ");");
            }
        }

        insert(conn, sb.toString());

        //set status
        if (type == BMPType.Small_Dam) hasSmallDam = true;
        if (type == BMPType.Holding_Pond) hasHoldingPond = true;
        if (type == BMPType.Grazing) hasGrazing = true;
    }

    public void SaveDesignBMPItems(Connection conn, List<BMPItem> items, BMPType type, Project project, Scenario scenario) throws SQLException, Exception{
        //clear current data
        prepareTable(type);

        //do nothing for small dam,holding pond, grazing when no design is done
        if (items.isEmpty()) return;

        //save design information and economic result of small dam, holding pond and grazing
        saveSmallDamHoldingPondGrazingBMPItems(conn, items, type);

        //save tillage or forage conversion
        saveCropBMPDesign(conn, items, scenario);
    }

    public void SaveDesignIDs(Connection conn,List<Integer> ids, BMPType type, Project project, Scenario scenario) throws Exception{
        //clear current data
        prepareTable(type);

        //do nothing for small dam,holding pond, grazing when no design is done
        if (ids.isEmpty()) return;

        //save design information and economic result of small dam, holding pond and grazing
        saveSmallDamHoldingPondGrazingIDs(conn, ids, type);

        //save tillage or forage conversion
        saveCropBMPDesign(conn, ids, type,scenario);
    }

    public static List<BMPItem> ReadNonCropDesign(Scenario scenario, BMPType type) throws Exception{
        List<BMPItem> results = new ArrayList<>();

        if(BMPItem.IsCropBMP(type))
            throw new Exception("Function ReadNonCropDesign is just used for non-crop BMP.");

        ResultSet rs = Query.GetDataTable(
            "select * from " + BMPDesignTableName(type),
            scenario.GetScenarioDB());

        while(rs.next())
        {
            switch(type)
            {
                case Small_Dam:
                    results.add(new BMPSmallDam(rs,-1,null,null));
                    break;
                case Holding_Pond:
                    results.add(new BMPHoldingPond(rs,-1,null,null));
                    break;
                case Grazing:
                    results.add(new BMPGrazing(rs,-1,null,null));
                    break;
            }
        }
        return results;
    }

    public void Update(){
        design = null;
    }

    private static Map<BMPType, List<Integer>> design;

    
    public static List<Integer> ReadDesign(Project p, Scenario s, BMPType type) throws Exception{          
        //first try to read from design
        List<Integer> results = null;
        if (design != null && design.containsKey(type)) {
            results = design.get(type);
            return results;
        }

        if (design == null) design = new HashMap<BMPType, List<Integer>>();

        results = new ArrayList<Integer>();
        if (BMPItem.IsCropBMP(type) && s.GetCropBMPLevel() == BMPSelectionLevelType.Unknown) 
        {
            design.put(type, results);
            return results;
        }
        if (BMPItem.IsCropBMP(type) && !type.toString().toLowerCase().contains(s.GetCropBMPLevel().toString().toLowerCase()))
        {
            //first read design in the design level
            BMPType typeDesign = BMPType.Tillage_Farm;
            if (s.GetCropBMPLevel() == BMPSelectionLevelType.Field)
                if (BMPItem.IsTillage(type)) typeDesign = BMPType.Tillage_Field;
                else typeDesign = BMPType.Forage_Field;
            else if (s.GetCropBMPLevel() == BMPSelectionLevelType.Farm)
                if (BMPItem.IsTillage(type)) typeDesign = BMPType.Tillage_Farm;
                else typeDesign = BMPType.Forage_Farm;
            else if (s.GetCropBMPLevel() == BMPSelectionLevelType.Subbasin)
                if (BMPItem.IsTillage(type)) typeDesign = BMPType.Tillage_Subbasin;
                else typeDesign = BMPType.Forage_Subbasin;

            List<Integer> designs = ReadDesign(p, s, typeDesign);

            //convert to current level
            ResultLevelType designLevel = ResultLevelType.valueOf(s.GetCropBMPLevel().toString());
            ResultLevelType currentLevel = ResultLevelType.valueOf(type.toString().split("_")[1]);

            for (int design : designs){
                for (int i : Spatial.ConvertShapeIDnPercents(connSpatial,
                        currentLevel,designLevel, design).keySet()){
                    if(!results.contains(i)) results.add(i);
                }
            }
            
            design.put(type, results);
            return results;
        }

        ResultSet rs = Query.GetDataTable(
            "select * from " + BMPDesignTableName(type),
            s.GetScenarioDB());
        while(rs.next())
        {
            RowItem item = new RowItem(rs);
            results.add(item.getColumnValue_Int(columnNameID));
        }
        design.put(type, results);
        return results;
    }    

    public boolean IsInDesign(Project p, Scenario s, BMPType type, int id) throws Exception{
        return ReadDesign(p, s, type).contains(id);
    }

    private static String idSelectFormat = "select distinct %s from %s order by %s";

    public static boolean GenerateWEBsText(Scenario s) throws IOException, SQLException, ClassNotFoundException{
        StringBuilder sb = new StringBuilder();
        
        Connection conn = Query.OpenConnection(s.GetScenarioDB());
        
        String sep = System.getProperty("line.separator");
        //some information
        sb.append("Input file generated by WEBsInterface, University of Guelph" + sep);

        //scenario database
        sb.append("Scenario Database" + sep);
        sb.append(s.GetScenarioDB() + sep);

        //base scenario
        sb.append("Base Scenario" + sep);
        sb.append(s.GetBMPScenerioBaseType() == BMPScenerioBaseType.Conventional ? "1" : "0");
        sb.append(sep);

        //small dam   
        sb.append("Small Dam (*.RES)" + sep);
        sb.append(getSelectedIDs(conn, "small_dams", "ID") + sep);

        //holding pond
        sb.append("Holding Pond" + sep);
        sb.append("Point Source IDs (*.DAT)" + sep);
        sb.append(getSelectedIDs(conn, "holding_ponds", "ID") + sep); //all point source ids
        sb.append("Corresponding HRUs (*.HRU)" + sep);
        sb.append(getSelectedIDs(conn, "holding_ponds", "HRU") + sep); //all hru ids

        //Grazing
        sb.append("Grazing (*.MGT)" + sep);
        sb.append(getSelectedIDs(conn, "grazing_hrus", "ID") + sep);

        //Tillage
        sb.append("Tillage" + sep);
        sb.append("Tillage Type & CNOP" + sep);
        sb.append(s.GetTillageType() == TillageType.Conventional ? "56" : "4"); //bmp tillage type
        sb.append(s.GetTillageType() == TillageType.Conventional ? " 1.1" : " 1.02"); //bmp tillage type
        sb.append(sep);
        sb.append("HRUs (*.MGT)" + sep);
        sb.append(getSelectedIDs(conn, "tillage_hrus", "ID") + sep);

        //Forage Conversion
        sb.append("Forage Conversion (*.MGT)" + sep);
        sb.append(getSelectedIDs(conn, "forage_hrus", "ID"));
        
        try {
            //create an print writer for writing to a file
            PrintWriter out = new PrintWriter(new FileWriter(Project.GetWEBs()));

            //output to the webs.webs
            out.println(sb.toString());

            //close the file (VERY IMPORTANT!)
            out.close();
            
            return true;
        } catch(IOException e) {
            System.out.println("Error during printing WEBS.WEBS");
        }
        return false;
    }
    
    private static String getSelectedIDs(Connection conn, String tableName, String columnName) throws SQLException, ClassNotFoundException{
        String query = String.format(idSelectFormat, columnName, tableName, columnName);
        
        //Build the data table;
        ResultSet rs = null;

        //Configure the data access service;
        try{
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
        }  catch (SQLException e) {
            
        }
        
        if (!rs.next()) {
            return "0" + System.getProperty("line.separator") + "";
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            StringBuilder sb_IDs = new StringBuilder();
            int size = 0;
            sb_IDs.append(rs.getString(1));
            size++;
            while(rs.next())
            {
                sb_IDs.append(" ");
                sb_IDs.append(rs.getString(1));
                size++;
            }
            
            sb.append(String.valueOf(size) + System.getProperty("line.separator") +
                    sb_IDs.toString());
            return sb.toString();            
        }
    }
    
    public static String GetBMPTableName(BMPType type){
        switch (type)
        {
            case Small_Dam:
                return tableNameSmallDam;
            case Holding_Pond:
                return tableNameHoldingPond;
            case Grazing:
                return tableNameGrazing;
            case Tillage_Farm:
            case Tillage_Field:
            case Tillage_Subbasin:
                return tableNameTillage;
            case Forage_Farm:
            case Forage_Field:
            case Forage_Subbasin:
                return tableNameForage;
            default:
                return "";
        }
    }
}
