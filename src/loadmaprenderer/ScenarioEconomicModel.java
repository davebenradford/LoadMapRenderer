/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class ScenarioEconomicModel extends ScenarioDatabaseStructure {
    public ScenarioEconomicModel(String scenarioDatabasePath){
        super(scenarioDatabasePath);        
    }
    private Scenario scenario = new Scenario();
    private Project project = new Project();      
    
    
    private static String SQLPrefixEconomicSmallDam =
                "insert into " + tableNameSmallDamEconomic
                + " (" + columnNameID + ","
                + columnNameYear + ","
                + columnNameCost + ") values (";

    private static String SQLPrefixEconomicHoldingPond =
            "insert into " + tableNameHoldingPondEconomic
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameCost + ") values (";

    private static String SQLPrefixEconomicGrazing =
            "insert into " + tableNameGrazingEconomic
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameCost + ") values (";

    private static String SQLPrefixEconomicGrazingSubbasin =
            "insert into " + tableNameGrazingEconomicSubbasin
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameCost + ") values (";

    private static String SQLPrefixEconomiccrop_field =
            "insert into " + tableNameCropEconomicfield
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameYield + ","
            + columnNameRevenue + ","
            + columnNameCost + ","
            + columnNameNetReturn + ") values (";

    private static String SQLPrefixEconomicCropFarm =
            "insert into " + tableNameCropEconomicfarm
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameYield + ","
            + columnNameRevenue + ","
            + columnNameCost + ","
            + columnNameNetReturn + ") values (";

    private static String SQLPrefixEconomicCropSubbasin =
            "insert into " + tableNameCropEconomicSubbasin
            + " (" + columnNameID + ","
            + columnNameYear + ","
            + columnNameYield + ","
            + columnNameRevenue + ","
            + columnNameCost + ","
            + columnNameNetReturn + ") values (";
    
    private static String idSelectFormat = "select distinct %s from %s order by %s";
    
    private static String FieldAreaTable = "field_area";
    private static String FarmAreaTable = "farm_area";
    private static String GrazingAreaTable = "grazing_area";
    private static String HRUAreaTable = "hru_area";
    private static String SubbasinAreaTable = "subbasin_area";
    
    private static Connection connSpatial = Query.OpenConnection(Project.GetSpatialDB());
    
    private static Map <Integer, Double> fieldIDnArea = Spatial.GetTableIDnArea(connSpatial, FieldAreaTable);
    private static Map <Integer, Double> farmIDnArea = Spatial.GetTableIDnArea(connSpatial, FarmAreaTable);
    private static Map <Integer, Double> grazingIDnArea = Spatial.GetTableIDnArea(connSpatial, GrazingAreaTable);
    private static Map <Integer, Double> hruIDnArea = Spatial.GetTableIDnArea(connSpatial, HRUAreaTable);
    private static Map <Integer, Double> subbasinIDnArea = Spatial.GetTableIDnArea(connSpatial, SubbasinAreaTable);    
    
    public void SetScenario(Scenario s){
        scenario = s;
    }
    
    public void RunEconomic() throws Exception {
        prepareTables();
        System.out.println("Created empty economic tables!");

        int startYear = scenario.GetStartYear();
        int endYear = scenario.GetEndYear();

        if(startYear <= 0 || endYear <=0)
            throw new Exception("Wrong year.");

        if (startYear > endYear)
            throw new Exception("Start year is larger than end year.");

        saveNonCropBMPsEconomic(project, scenario, startYear, endYear);

        saveCropBMPsEconomic(project, scenario, startYear, endYear);
    }
    
    private void saveCropBMPEconomic(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Field)
            saveCropBMPEconomicField(conn, tillages, forages, project, scenario, startYear, endYear);
        else if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Farm)
            saveCropBMPEconomicFarm(conn, tillages, forages, project, scenario, startYear, endYear);
        else if (scenario.GetCropBMPLevel() == BMPSelectionLevelType.Subbasin)
            saveCropBMPEconomicSubbasin(conn, tillages, forages, project, scenario, startYear, endYear);
        else //level == unknown
            saveCropBMPEconomicField(conn, tillages, forages, project, scenario, startYear, endYear);
    }

    private void saveCropBMPsEconomic(Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        BMPType tillType = BMPType.Tillage_Field;
        BMPType forageType = BMPType.Forage_Field;
        switch (scenario.GetCropBMPLevel()){
            case Field:
                tillType = BMPType.Tillage_Field;
                forageType = BMPType.Forage_Field;
                break;
            case Farm:
                tillType = BMPType.Tillage_Farm;
                forageType = BMPType.Forage_Farm;
                break;
            case Subbasin:
                tillType = BMPType.Tillage_Subbasin;
                forageType = BMPType.Forage_Subbasin;
                break;
        }
        
        List<Integer> tillages = ScenarioDesign.ReadDesign(project, scenario, tillType);
        List<Integer> forages = ScenarioDesign.ReadDesign(project, scenario, forageType);
        
        Connection conn = Query.OpenConnection(scenario.GetScenarioDB());

        saveCropBMPEconomic(conn, tillages, forages, project, scenario, startYear, endYear);
    }
    
        private void saveCropBMPEconomicField_Field(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception {
        StringBuilder sb = new StringBuilder();
        
        Map<Integer, Integer> fieldIDs = new HashMap();
        for (int id : fieldIDnArea.keySet()){
            fieldIDs.put(id, id);
        }
        
        for (int till : tillages)
        {
            fieldIDs.remove(till);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, till, scenario.GetTillageType(), startYear, endYear, SQLPrefixEconomiccrop_field));
        }
        for (int forage : forages)
        {
            fieldIDs.remove(forage);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, forage, TillageType.Forage, startYear, endYear, SQLPrefixEconomiccrop_field));
        }
        for (int id : fieldIDs.keySet())
        {
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, id, TillageType.Base, startYear, endYear, SQLPrefixEconomiccrop_field));
        }

        insert(conn,sb.toString());
    }
    
    private void saveCropBMPEconomicField_Farm(Connection conn, List<Integer> tillIds, List<Integer> ForageIds, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        
        StringBuilder sb = new StringBuilder();
        for (int farm : farmIDnArea.keySet())
        {
            if (farm <= 0) continue;

            //get corresponding fields for current farm
            Map<Integer, Double> fieldnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, farm);
            if (fieldnPercents.isEmpty()) continue;
            
            //get tillage type of each field in current farm
            Map<Integer, TillageType> field_tillageTypes = new HashMap<>();
            for (int field : fieldnPercents.keySet())
            {
                if (tillIds.contains(field)) field_tillageTypes.put(field, scenario.GetTillageType());
                else if (ForageIds.contains(field)) field_tillageTypes.put(field, TillageType.Forage);
                else field_tillageTypes.put(field, TillageType.Base);
            }

            //try to get value for each field
            for (int year = startYear; year <= endYear; year++)
            {
                Map<Integer, Double> field_yields = new HashMap();
                Map<Integer, Double> field_revenues = new HashMap();
                Map<Integer, Double> field_costs = new HashMap();
                Map<Integer, Double> field_netreturns = new HashMap();
                for (int field : fieldnPercents.keySet())
                {
                    CropEconomic fieldEconomic = getCropEconomic(conn,
                        BMPSelectionLevelType.Field, field, scenario.GetBMPScenerioBaseType(),
                        field_tillageTypes.get(field), null, year);
                    field_yields.put(field,fieldEconomic.getYield());
                    field_revenues.put(field,fieldEconomic.getRevenue());
                    field_costs.put(field,fieldEconomic.getEconomicCost());
                    field_netreturns.put(field,fieldEconomic.getNetReturn());
                }

                double yield = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Farm,
                    field_yields, farm, fieldnPercents, true);
                double revenue = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Farm,
                    field_revenues, farm, fieldnPercents, true);
                double cost = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Farm,
                    field_costs, farm, fieldnPercents, true);
                double netreturn = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Farm,
                    field_netreturns, farm, fieldnPercents, true);

                sb.append(String.format("%s%d,%d,%f,%f,%f,%f);",
                    SQLPrefixEconomicCropFarm, farm, year,
                    yield, revenue, cost, netreturn));
            }
        }
        insert(conn, sb.toString());
    }
    
    private void saveCropBMPEconomicField_Subbasin(Connection conn, List<Integer> tillIds, List<Integer> ForageIds, Project project, Scenario scenario, int startYear, int endYear) throws Exception{
        StringBuilder sb = new StringBuilder();
        for (int subbasin : subbasinIDnArea.keySet())
        {
            //get corresponding fields for current subbasin
            Map<Integer, Double> fieldnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Subbasin, ResultLevelType.Field, subbasin);
            if (fieldnPercents.isEmpty()) continue;
            
            //get tillage type of each field in current subbasin
            Map<Integer, TillageType> field_tillageTypes = new HashMap();
            for (int field : fieldnPercents.keySet())
            {
                if (tillIds.contains(field)) field_tillageTypes.put(field, scenario.GetTillageType());
                else if (ForageIds.contains(field)) field_tillageTypes.put(field, TillageType.Forage);
                else field_tillageTypes.put(field, TillageType.Base);
            }

            //try to get value for each field
            for (int year = startYear; year <= endYear; year++)
            {
                Map<Integer, Double> field_yields = new HashMap();
                Map<Integer, Double> field_revenues = new HashMap();
                Map<Integer, Double> field_costs = new HashMap();
                Map<Integer, Double> field_netreturns = new HashMap();
                for (int field : fieldnPercents.keySet())
                {
                    CropEconomic fieldEconomic = getCropEconomic(conn,
                        BMPSelectionLevelType.Field, field, scenario.GetBMPScenerioBaseType(),
                        field_tillageTypes.get(field), null, year);
                    field_yields.put(field, fieldEconomic.getYield());
                    field_revenues.put(field, fieldEconomic.getRevenue());
                    field_costs.put(field, fieldEconomic.getEconomicCost());
                    field_netreturns.put(field, fieldEconomic.getNetReturn());
                }

                double yield = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_yields, subbasin, fieldnPercents, true);
                double revenue = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_revenues, subbasin, fieldnPercents, true);
                double cost = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_costs, subbasin, fieldnPercents, true);
                double netreturn = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_netreturns, subbasin, fieldnPercents, true);

                sb.append(String.format("%s%d,%d,%f,%f,%f,%f);",
                    SQLPrefixEconomicCropSubbasin, subbasin, year,
                    yield, revenue, cost, netreturn));
            }
        }
        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicField(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception {
        System.out.println("Calculating field level economic results...");
        saveCropBMPEconomicField_Field(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Field level economic results finished!");
        System.out.println("Calculating farm level economic results...");
        saveCropBMPEconomicField_Farm(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Farm level economic results finished!");
        System.out.println("Calculating subbasin level economic results...");
        saveCropBMPEconomicField_Subbasin(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Subbasin level economic results finished!");
    }
    
       private void saveCropBMPEconomicFarm_Farm(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> farmIDs = new HashMap();
        for (int id : farmIDnArea.keySet()){
            farmIDs.put(id,id);
        }
        for (int till : tillages)
        {
            farmIDs.remove(till);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Farm, till, scenario.GetTillageType(), startYear, endYear, SQLPrefixEconomicCropFarm));
        }
        for (int forage : forages)
        {
            farmIDs.remove(forage);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Farm, forage, TillageType.Forage, startYear, endYear, SQLPrefixEconomicCropFarm));

        }
        for (int id : farmIDs.keySet())
        {
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                    BMPSelectionLevelType.Farm, id, TillageType.Base, startYear, endYear, SQLPrefixEconomicCropFarm));
        }

        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicFarm_Field(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws Exception{
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> fieldIDs = new HashMap();
        for (int id : fieldIDnArea.keySet()){
            fieldIDs.put(id, id);
        }
        
        Map<Integer, Double> tillIdnPercents = new HashMap();
        
        for (int till : tillages){
            Map<Integer, Double> tillIds_new = 
                    Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, till);
            if (tillIds_new.isEmpty()) continue;
            for (int tillField : tillIds_new.keySet()){
                if (!tillIdnPercents.containsKey(tillField))
                    tillIdnPercents.put(tillField, tillIds_new.get(tillField));
            }
        }
        
        
        for (int till : tillIdnPercents.keySet())
        {
            fieldIDs.remove(till);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, till, scenario.GetTillageType(), startYear, endYear, SQLPrefixEconomiccrop_field));
        }

        Map<Integer, Double> forageIdnPercents = new HashMap();
        
        for (int forage : forages){
            Map<Integer, Double> forageIds_new = 
                    Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, forage);
            if (forageIds_new.isEmpty()) continue;
            for (int forageField : forageIds_new.keySet()){
                if (!forageIdnPercents.containsKey(forageField))
                    forageIdnPercents.put(forageField, forageIds_new.get(forageField));
            }
        }
        for (int forage : forageIdnPercents.keySet())
        {
            fieldIDs.remove(forage);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, forage, TillageType.Forage, startYear, endYear, SQLPrefixEconomiccrop_field));
        }
        for (int id : fieldIDs.keySet())
        {
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Field, id, TillageType.Base, startYear, endYear, SQLPrefixEconomiccrop_field));
        }

        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicFarm_Subbasin(Connection conn, List<Integer> tillIds, List<Integer> ForageIds, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        StringBuilder sb = new StringBuilder();
        List<Integer> subbasins = new ArrayList();
        List<Integer> tillFieldIDs = new ArrayList();
        List<Integer> ForageFieldIDs = new ArrayList();
        
        for (int id : subbasinIDnArea.keySet()){
            subbasins.add(id);
        }
        
        for (int id : tillIds){
            for (int idField : Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, id).keySet()){
                if (!tillFieldIDs.contains(idField)) tillFieldIDs.add(idField);
            };
        }
        for (int id : ForageIds){
            for (int idField : Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, id).keySet()){
                if (!ForageFieldIDs.contains(idField)) ForageFieldIDs.add(idField);
            };
        }

        for (int subbasin : subbasins)
        {
            Map<Integer, Double> fieldnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Subbasin, ResultLevelType.Field, subbasin);
            if (fieldnPercents == null || fieldnPercents.isEmpty()) continue;

            //get tillage type of each field in current subbasin
            Map<Integer, TillageType> field_tillageTypes = new HashMap();
            for (int field : fieldnPercents.keySet())
            {
                if (tillFieldIDs.contains(field)) field_tillageTypes.put(field, scenario.GetTillageType());
                else if (ForageFieldIDs.contains(field)) field_tillageTypes.put(field, TillageType.Forage);
                else field_tillageTypes.put(field, TillageType.Base);
            }

            //try to get value for each field
            for (int year = startYear; year <= endYear; year++)
            {
                Map<Integer, Double> field_yields = new HashMap();
                Map<Integer, Double> field_revenues = new HashMap();
                Map<Integer, Double> field_costs = new HashMap();
                Map<Integer, Double> field_netreturns = new HashMap();
                for (int field : fieldnPercents.keySet())
                {
                    CropEconomic fieldEconomic = getCropEconomic(conn,
                        BMPSelectionLevelType.Field, field, scenario.GetBMPScenerioBaseType(),
                        field_tillageTypes.get(field), null, year);
                    field_yields.put(field,fieldEconomic.getYield());
                    field_revenues.put(field,fieldEconomic.getRevenue());
                    field_costs.put(field,fieldEconomic.getEconomicCost());
                    field_netreturns.put(field,fieldEconomic.getNetReturn());
                }

                double yield = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_yields, subbasin, fieldnPercents, true);
                double revenue = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_revenues, subbasin, fieldnPercents, true);
                double cost = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_costs, subbasin, fieldnPercents, true);
                double netreturn = Spatial.ConvertResult(ResultLevelType.Field, ResultLevelType.Subbasin,
                    field_netreturns, subbasin, fieldnPercents, true);

                sb.append(String.format("%s%d,%d,%f,%f,%f,%f);",
                    SQLPrefixEconomicCropSubbasin, subbasin, year,
                    yield, revenue, cost, netreturn));
            }
        }

        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicFarm(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws Exception{
        System.out.println("Calculating field level economic results...");
        saveCropBMPEconomicFarm_Field(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Field level economic results finished!");
        System.out.println("Calculating farm level economic results...");
        saveCropBMPEconomicFarm_Farm(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Farm level economic results finished!");
        System.out.println("Calculating subbasin level economic results...");
        saveCropBMPEconomicFarm_Subbasin(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Subbasin level economic results finished!");
    }

    private void saveCropBMPEconomicSubbasin_Subbasin(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        StringBuilder sb = new StringBuilder();
        Map<Integer, Integer> subbasinIDs = new HashMap();
        
        for (int id : subbasinIDnArea.keySet()){
            subbasinIDs.put(id, id);
        }
        
        for (int till : tillages)
        {
            subbasinIDs.remove(till);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
               BMPSelectionLevelType.Subbasin, till, scenario.GetTillageType(), startYear, endYear, SQLPrefixEconomicCropSubbasin));
        }
        for (int forage : forages)
        {
            subbasinIDs.remove(forage);
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                BMPSelectionLevelType.Subbasin, forage, TillageType.Forage, startYear, endYear, SQLPrefixEconomicCropSubbasin));
        }
        for (int id : subbasinIDs.keySet())
        {
            sb.append(getCropBMPEconomicSQL(conn, scenario,
                    BMPSelectionLevelType.Subbasin, id, TillageType.Base, startYear, endYear, SQLPrefixEconomicCropSubbasin));
        }

        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicSubbasin_FieldFarm(Connection conn, List<Integer> till_Subbasins, List<Integer> forage_Subbasins, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        StringBuilder sb = new StringBuilder();
        Map<Integer, Map<Integer, CropEconomic>> fieldEconomicOutput = new HashMap<Integer, Map<Integer, CropEconomic>>() {};

        for (int field : fieldIDnArea.keySet())
        {
            if (field <= 0 || field >= 600) continue;

            //the field is split to different part by subbasins
            Map<Integer, Double> subnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Field, ResultLevelType.Subbasin, field);
            if (subnPercents.isEmpty()) continue;
            Map<Integer, TillageType> field_tillageTypes = new HashMap();
            for (int sub : subnPercents.keySet())
            {
                if (till_Subbasins.contains(sub)) field_tillageTypes.put(sub, scenario.GetTillageType());
                else if (forage_Subbasins.contains(sub)) field_tillageTypes.put(sub, TillageType.Forage);
                else field_tillageTypes.put(sub, TillageType.Base);
            }

            fieldEconomicOutput.put(field, new HashMap<Integer, CropEconomic>());

            //try to get value for each field
            for (int year = startYear; year <= endYear; year++)
            {
                double yield = 0.0;
                double revenue = 0.0;
                double cost = 0.0;
                double netreturn = 0.0;
                double totalArea = 0.0;

                for (int sub : subnPercents.keySet())
                {
                    CropEconomic fieldEconomic = getCropEconomic(conn,
                        BMPSelectionLevelType.Field, field, scenario.GetBMPScenerioBaseType(),
                        field_tillageTypes.get(sub), null, year);
                    yield += fieldEconomic.getYield() * subbasinIDnArea.get(sub) * subnPercents.get(sub);
                    revenue += fieldEconomic.getRevenue() * subbasinIDnArea.get(sub) * subnPercents.get(sub);
                    cost += fieldEconomic.getEconomicCost() * subbasinIDnArea.get(sub) * subnPercents.get(sub);
                    netreturn += fieldEconomic.getNetReturn() * subbasinIDnArea.get(sub) * subnPercents.get(sub);
                    totalArea += subbasinIDnArea.get(sub) * subnPercents.get(sub);
                }

                sb.append(String.format("%s%d,%d,%f,%f,%f,%f);",
                    SQLPrefixEconomiccrop_field, field, year,
                    yield / totalArea, revenue / totalArea, cost / totalArea, netreturn / totalArea));

                fieldEconomicOutput.get(field).put(year, new CropEconomic(yield / totalArea, revenue / totalArea, cost / totalArea, netreturn / totalArea));
            }
        }
        
        //for farm
        for (int farm : farmIDnArea.keySet())
        {
            if (farm <= 0) continue;

            Map<Integer, Double> farmFieldnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, ResultLevelType.Farm, ResultLevelType.Field, farm);
            if (farmFieldnPercents.isEmpty()) continue;
            
            for (int year = startYear; year <= endYear; year++)
            {
                Map<Integer, Double> farmField_yields = new HashMap();
                Map<Integer, Double> farmField_revenues = new HashMap();
                Map<Integer, Double> farmField_costs = new HashMap();
                Map<Integer, Double> farmField_netreturns = new HashMap();
                for (int field : farmFieldnPercents.keySet())
                {
                    CropEconomic eco = fieldEconomicOutput.get(field).get(year);
                    farmField_yields.put(field, eco.getYield());
                    farmField_revenues.put(field, eco.getRevenue());
                    farmField_costs.put(field, eco.getEconomicCost());
                    farmField_netreturns.put(field, eco.getNetReturn());
                }

                double yield = Spatial.ConvertResult(ResultLevelType.Field, 
                        ResultLevelType.Farm, farmField_yields, farm, 
                        farmFieldnPercents, true);
                double revenue = Spatial.ConvertResult(ResultLevelType.Field, 
                        ResultLevelType.Farm, farmField_revenues, farm, 
                        farmFieldnPercents, true);
                double cost = Spatial.ConvertResult(ResultLevelType.Field, 
                        ResultLevelType.Farm, farmField_costs, farm, 
                        farmFieldnPercents, true);
                double netreturn = Spatial.ConvertResult(ResultLevelType.Field, 
                        ResultLevelType.Farm, farmField_netreturns, farm, 
                        farmFieldnPercents, true);

                sb.append(String.format("%s%d,%d,%f,%f,%f,%f);",
                    SQLPrefixEconomicCropFarm, farm, year,
                    yield, revenue, cost, netreturn));
            }
        }
        insert(conn, sb.toString());
    }

    private void saveCropBMPEconomicSubbasin(Connection conn, List<Integer> tillages, List<Integer> forages, Project project, Scenario scenario, int startYear, int endYear) throws SQLException, Exception{
        System.out.println("Calculating subbasin level economic results...");
        saveCropBMPEconomicSubbasin_Subbasin(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Field level economic results finished!");
        System.out.println("Calculating farm and field level economic results...");
        saveCropBMPEconomicSubbasin_FieldFarm(conn, tillages, forages, project, scenario, startYear, endYear);
        System.out.println("Farm and field level economic results finished!");
    }

    private String getCropBMPEconomicSQL(Connection conn,Scenario scenario,
        BMPSelectionLevelType level, int id, TillageType tillType,
        int startYear, int endYear, String prefix) throws Exception{
        if (level == BMPSelectionLevelType.Field && (id <= 0 || id >= 600))
            return "";
        if (id <= 0) return "";

        StringBuilder sb = new StringBuilder();
        for (int year = startYear; year <= endYear; year++)
        {
            CropEconomic eco = getCropEconomic(conn, level, id, scenario.GetBMPScenerioBaseType(), tillType, null, year);
            sb.append(String.format("%s%d,%d,%s);",
                prefix, id, year, eco.InsertSQL("", -1, -1)));
        }
        return sb.toString();
    }
    

    
    private void prepareTables() throws SQLException{
        updateTables(scenario.GetScenarioDB());
        System.out.println("Cleared economic related tables!");
        prepareTable(BMPType.Small_Dam);
        prepareTable(BMPType.Holding_Pond);
        prepareTable(BMPType.Grazing);
        prepareTable(BMPType.Tillage_Field);
        prepareTable(BMPType.Forage_Field);
    }
    
    private void updateTables(String path) throws SQLException{
        Connection conn = Query.OpenConnection(path);
        String drop = "drop table if exists " + tableNameSmallDamEconomic + ";"
                   +  "drop table if exists " + tableNameHoldingPondEconomic + ";"
                   +  "drop table if exists " + tableNameGrazingEconomic + ";"
                   +     "drop table if exists " + tableNameGrazingEconomicSubbasin + ";"
                   +     "drop table if exists " + tableNameCropEconomicfield + ";"
                   +     "drop table if exists " + tableNameCropEconomicfarm + ";"
                   +     "drop table if exists " + tableNameCropEconomicSubbasin + ";";
        String[] sqls = drop.split(";");                   
            
        try {
            conn.setAutoCommit(false);
            for(String s : sqls) {   
                conn.prepareStatement(s).executeUpdate();                    
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            conn.commit();
        }
    }

    private void prepareTable(BMPType type){
        AccessColumn _id = new AccessColumn();
        _id.setColumnName(columnNameID);_id.setType(Integer.TYPE);
        AccessColumn _economic_year = new AccessColumn();
        _economic_year.setColumnName(columnNameYear);_economic_year.setType(Integer.TYPE);
        AccessColumn _economic_cost = new AccessColumn();
        _economic_cost.setColumnName(columnNameCost);_economic_cost.setType(Double.TYPE);
        
        switch (type)
        {
            case Small_Dam:
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameSmallDamEconomic,
                    _id, _economic_year, _economic_cost);
                break;
            case Holding_Pond:
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameHoldingPondEconomic,
                        _id, _economic_year, _economic_cost);
                break;
            case Grazing:
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameGrazingEconomic,
                        _id, _economic_year, _economic_cost);
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameGrazingEconomicSubbasin,
                        _id, _economic_year, _economic_cost);
                break;
            case Tillage_Farm:
            case Tillage_Field:
            case Tillage_Subbasin:
                AccessColumn _economic_yield = new AccessColumn();
                _economic_yield.setColumnName(columnNameYield);_economic_yield.setType(Double.TYPE);
                AccessColumn _economic_revenue = new AccessColumn();
                _economic_revenue.setColumnName(columnNameRevenue);_economic_revenue.setType(Double.TYPE);
                AccessColumn _economic_netreturn = new AccessColumn();
                _economic_netreturn.setColumnName(columnNameNetReturn);_economic_netreturn.setType(Double.TYPE);

                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicfield,
                    _id, _economic_year, _economic_yield,
                    _economic_revenue, _economic_cost, _economic_netreturn);
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicfarm,
                    _id, _economic_year, _economic_yield,
                    _economic_revenue, _economic_cost, _economic_netreturn);
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicSubbasin,
                    _id, _economic_year, _economic_yield,
                    _economic_revenue, _economic_cost, _economic_netreturn);
                break;
            case Forage_Field:
            case Forage_Farm:
            case Forage_Subbasin:
                AccessColumn _economic_yield_forage = new AccessColumn();
                _economic_yield_forage.setColumnName(columnNameYield);_economic_yield_forage.setType(Double.TYPE);
                AccessColumn _economic_revenue_forage = new AccessColumn();
                _economic_revenue_forage.setColumnName(columnNameRevenue);_economic_revenue_forage.setType(Double.TYPE);
                AccessColumn _economic_netreturn_forage = new AccessColumn();
                _economic_netreturn_forage.setColumnName(columnNameNetReturn);_economic_netreturn_forage.setType(Double.TYPE);
                
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicfield,
                    _id, _economic_year, _economic_yield_forage,
                    _economic_revenue_forage, _economic_cost, _economic_netreturn_forage);
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicfarm,
                    _id, _economic_year, _economic_yield_forage,
                    _economic_revenue_forage, _economic_cost, _economic_netreturn_forage);
                Query.PrepareTable(scenario.GetScenarioDB(), tableNameCropEconomicSubbasin,
                    _id, _economic_year, _economic_yield_forage,
                    _economic_revenue_forage, _economic_cost, _economic_netreturn_forage);
                break;
        }
    }
    
    private void saveNonCropBMPsEconomic(Project project, Scenario scenario, int startYear, int endYear) throws Exception{
        Connection conn = Query.OpenConnection(scenario.GetScenarioDB());
        saveNonCropBMPEconomic(conn, BMPType.Small_Dam, project, 
                ScenarioDesign.ReadNonCropDesign(scenario, BMPType.Small_Dam), startYear, endYear);
        System.out.println("Small dam economic results finished!");
        saveNonCropBMPEconomic(conn, BMPType.Holding_Pond, project, 
                ScenarioDesign.ReadNonCropDesign(scenario, BMPType.Holding_Pond), startYear, endYear);
        System.out.println("Holding pond economic results finished!");
        saveNonCropBMPEconomic(conn, BMPType.Grazing, project, 
                ScenarioDesign.ReadNonCropDesign(scenario, BMPType.Grazing), startYear, endYear);
        System.out.println("Grazing and grazing subbasin economic results finished!");
    }
    
    private void saveNonCropBMPEconomic(Connection conn, BMPType type, 
            Project project, List<BMPItem> Items, int startYear, int endYear) 
            throws SQLException, Exception{
        StringBuilder sb = new StringBuilder();

        for(BMPItem item : Items){
            for (int year = startYear; year <= endYear; year++)
            {
                switch (type)
                {
                    case Small_Dam:
                        sb.append(SQLPrefixEconomicSmallDam + item.InsertSQL_Economic(year));
                        break;
                    case Holding_Pond:
                        sb.append(SQLPrefixEconomicHoldingPond + item.InsertSQL_Economic(year));
                        break;
                    case Grazing:
                        sb.append(SQLPrefixEconomicGrazing + item.InsertSQL_Economic(year));
                        break;
                }
            }
        }            

        insert(conn, sb.toString());

        if (type == BMPType.Grazing)
            saveGrazingSubbasinEconomic(conn, Items, type, project, startYear, endYear);
    }
    
    private void saveGrazingSubbasinEconomic(Connection conn, List<BMPItem> items, BMPType type, Project project, int startYear, int endYear) throws SQLException, CloneNotSupportedException, Exception{
        if (type != BMPType.Grazing || items.size() == 0) return;

        StringBuilder sb = new StringBuilder();
        List<Integer> subs = new ArrayList();
        Map<Integer, Integer> ID2Index = new HashMap();
        int index = 0;
        for (BMPItem item : items){
            List<Integer> subs_item = Spatial.GetRelationIDs(
                    ResultLevelType.Grazing, ResultLevelType.Subbasin, 
                    item.getID(), true);
            for (int sub : subs_item){
                if (!subs.contains(sub)) subs.add(sub);
            }
            ID2Index.put(item.getID(), index);
            index++;
        }
        
        for (int sub : subs)
        {
            //try to get corresponding grazings for current subbasin
            Map<Integer, Double> subGrazingnPercents = 
                    Spatial.ConvertShapeIDnPercents(connSpatial,
                ResultLevelType.Subbasin, ResultLevelType.Grazing, sub);
            if (subGrazingnPercents.isEmpty()) continue;
            
            Map<Integer,Double> subGrazingResults = new HashMap<>();
            for (int grazing : subGrazingnPercents.keySet())
            {
                if (!ID2Index.containsKey(grazing)) {
                    subGrazingResults.put(grazing, 0.0);
                } else{
                    subGrazingResults.put(grazing,                            
                        ((BMPGrazing) items.get(ID2Index.get(grazing))).getUnitCost());
                } 
            }

            //try to get result for current subbasin
            double subResult = Spatial.ConvertResult(ResultLevelType.Grazing, 
                    ResultLevelType.Subbasin, subGrazingResults, sub, 
                    subGrazingnPercents, true); //unit is $/ha

            //generate SQL
            for (int year = startYear; year <= endYear; year++)
            {
                sb.append(String.format("%s%d,%d,%f);",
                    SQLPrefixEconomicGrazingSubbasin,
                    sub, year, subResult));
            }
        }
        
        insert(conn, sb.toString());
    }
    
    protected void insert(Connection conn, String sql) throws SQLException {
        if (sql == null || sql.isEmpty()) {    
        }
        else {
            String[] sqls = sql.split(";");                   
            
            try {
                conn.setAutoCommit(false);
                for(String s : sqls) {   
                    conn.prepareStatement(s).executeUpdate();                    
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                conn.commit();
            }
        }
    }
    
    private CropEconomic getCropEconomic(Connection conn, BMPSelectionLevelType level, 
            int id, BMPScenerioBaseType baseType, TillageType tillType,
            List<Integer> constrainFields, int year) throws Exception{
        if (level == BMPSelectionLevelType.Field)
        {
            if(constrainFields != null && !constrainFields.contains(id))
                return new CropEconomic();
            if (id <= 0 || id >= 600) return new CropEconomic(); //don't try to get non-crop field

            ResultSet rs = null;
            
            rs = getCorrespondingEconomicDataTable(baseType, tillType, 
                    String.format("id = %d and year = %d", id, year));
            double yield = rs.getDouble("yield");
            double revenue = rs.getDouble("revenue");
            double cost = rs.getDouble("cost");
            double netreturn = rs.getDouble("netreturn");

            return new CropEconomic(yield, revenue, cost, netreturn);
        }
        else if (level == BMPSelectionLevelType.Farm || level == BMPSelectionLevelType.Subbasin)
        {
            ResultLevelType shape1_type = ResultLevelType.Farm;
            if (level == BMPSelectionLevelType.Subbasin) shape1_type = ResultLevelType.Subbasin;

            Map<Integer, Double> fieldnPercents = Spatial.ConvertShapeIDnPercents(connSpatial, shape1_type, ResultLevelType.Field, id);
            if (fieldnPercents.isEmpty()) return new CropEconomic();
            
            Map<Integer, Double> field_yields = new HashMap();
            Map<Integer, Double> field_revenues = new HashMap();
            Map<Integer, Double> field_costs = new HashMap();
            Map<Integer, Double> field_netreturns = new HashMap();
            for (int field : fieldnPercents.keySet())
            {                    
                CropEconomic fieldEconomic = getCropEconomic(conn, 
                        BMPSelectionLevelType.Field, field, baseType, 
                        tillType,constrainFields,year);
                field_yields.put(field, fieldEconomic.getYield());
                field_revenues.put(field, fieldEconomic.getRevenue());
                field_costs.put(field, fieldEconomic.getEconomicCost());
                field_netreturns.put(field, fieldEconomic.getNetReturn());
            }

            double yield = Spatial.ConvertResult(ResultLevelType.Field, shape1_type, 
                    field_yields, id, fieldnPercents, true);
            double revenue = Spatial.ConvertResult(ResultLevelType.Field, shape1_type, 
                    field_revenues, id, fieldnPercents, true);
            double cost = Spatial.ConvertResult(ResultLevelType.Field, shape1_type, 
                    field_costs, id, fieldnPercents, true);
            double netreturn = Spatial.ConvertResult(ResultLevelType.Field, shape1_type, 
                    field_netreturns, id, fieldnPercents, true);
            return new CropEconomic(yield, revenue, cost, netreturn);
        }
        else
            throw new Exception("Wrong Type." + System.getProperty("line.separator") + Thread.currentThread());
    }

    private ResultSet getCorrespondingEconomicDataTable(BMPScenerioBaseType baseType, 
            TillageType tillType, String filter) throws SQLException{
        String economicTableName = "";
        if (baseType == BMPScenerioBaseType.Historic)
        {
            if (tillType == TillageType.Base)
                economicTableName = "yield_historic";
            else if (tillType == TillageType.Conventional)
                economicTableName = "yield_historic_conventional";
            else if (tillType == TillageType.ZERO)
                economicTableName = "yield_historic_zero";
            else
                economicTableName = "yield_historic_forage";
        }
        else
        {
            if (tillType == TillageType.Base || tillType == TillageType.Conventional)
                economicTableName = "yield_conventional";
            else if (tillType == TillageType.ZERO)
                economicTableName = "yield_conventional_zero";
            else
                economicTableName = "yield_conventional_forage";
        }
        
        String sql = "";
        if (!filter.isEmpty()){
            sql =
            String.format("select field as id,year,yield, " +
            "revenue, cost, netreturn " +
            "from %s" + " WHERE %s",
            economicTableName, filter); //just get unit result, i.e. kg/ha and $/ha, don't calculate kg and $ any more
        } else {
            sql =
            String.format("select field as id,year,yield, " +
            "revenue, cost, netreturn " +
            "from %s",
            economicTableName); //just get unit result, i.e. kg/ha and $/ha, don't calculate kg and $ any more
        }     

        return connSpatial.createStatement().executeQuery(sql);        
    }  
}
