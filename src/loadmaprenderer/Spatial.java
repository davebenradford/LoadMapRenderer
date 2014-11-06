/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public class Spatial {
    private static Connection conn_spatial = Query.OpenConnection(Project.getSpatialDB());
    
    private static String FieldAreaTable = "field_area";
    private static String FarmAreaTable = "farm_area";
    private static String GrazingAreaTable = "grazing_area";
    private static String HRUAreaTable = "hru_area";
    private static String SubbasinAreaTable = "subbasin_area"; 
    
    private static Map <Integer, Double> fieldIDnArea;
    private static Map <Integer, Double> farmIDnArea;
    private static Map <Integer, Double> grazingIDnArea;
    private static Map <Integer, Double> hruIDnArea;
    private static Map <Integer, Double> subbasinIDnArea;    
    
    public static Map<Integer, Double> GetTableIDnArea(Connection conn, String tableName){
        if (conn != null)
        {
            try
            {
                Statement stmt = conn.createStatement();
                //try to create the table
                String sql = "select distinct id, area from " + tableName;
                ResultSet rs = stmt.executeQuery(sql);
                Map<Integer, Double> IDnAreas = new HashMap();
                while (rs.next()){
                    IDnAreas.put(rs.getInt(1),rs.getDouble(2));
                }
                return IDnAreas;
            }
            catch (SQLException e) { return null; }
        }
        return new HashMap();
    }
    
    public static List<Integer> GetRelationIDs(
            ResultLevelType inShape, ResultLevelType outShape, 
            int inShapeID, boolean isGzg_sub){
        String colName;
        if (inShapeID < 0) inShapeID = -1;
        if (outShape == ResultLevelType.Grazing){
            colName = isGzg_sub ? inShape.toString() : outShape.toString();
        }
        else  {
            colName = outShape == ResultLevelType.HRU ? 
                "HRUSWATIndex" : outShape.toString();
        }
        String tableName;
        if (inShape != ResultLevelType.Grazing){
            tableName = inShape.toString() + "_" + outShape.toString();
        }else{
            tableName = outShape.toString() + "_" + inShape.toString();
        }
        if (conn_spatial != null)
        {
            try
            {
                Statement stmt = conn_spatial.createStatement();
                //try to create the table
                String sql;
                if (inShapeID == -1) {
                    sql = "select distinct " + colName + " from " + tableName;
                } else {
                    sql = String.format("select distinct %s from %s where %s = %d",
                            colName, tableName, inShape.toString(), inShapeID);
                }
                
                ResultSet rs_new = stmt.executeQuery(sql);
                List<Integer> IDs = new ArrayList();
                while (rs_new.next()){
                    IDs.add(rs_new.getInt(colName));
                }

                return IDs;
            }
            catch (SQLException e) { return null; }
        }
        return new ArrayList();
    }
    
    public static List<Integer> GetRelationIDs(
            ResultLevelType inShape, ResultLevelType outShape, 
            List<Integer> inShapeIDs, boolean isGzg_sub){
        List<Integer> idsOUT = new ArrayList();
        for (int id : inShapeIDs){
            List<Integer> idsIN = GetRelationIDs(inShape, outShape, id, isGzg_sub);
            if (idsIN.isEmpty() || idsIN == null) continue;
            for (int idIN : idsIN){
                if (!idsOUT.contains(idIN)) idsOUT.add(idIN);
            }
        }
        return idsOUT;
    }
    
    public static List<Integer> GetGrazingHRU(Connection conn, int sub){
        if (conn != null)
        {
            try
            {
                Statement stmt = conn.createStatement();
                //try to create the table
                String sql = "select HRUSWATIndex from subbasin_grazing_hru "
                        + "where subbasin = " + String.valueOf(sub);
                ResultSet rs = stmt.executeQuery(sql);
                List<Integer> IDs = new ArrayList();
                while (rs.next()){
                    IDs.add(rs.getInt("HRUSWATIndex"));
                }
                return IDs;
            }
            catch (SQLException e) { return null; }
        }
        return new ArrayList();
    }
    
    public static List<Integer> GetGrazingHRU(Connection conn, List<Integer> subs){
        List<Integer> HRUs = new ArrayList();
        for (int sub : subs){
            List<Integer> HRUs_sub = GetGrazingHRU(conn, sub);
            for (int HRU : HRUs_sub){
                if (!HRUs.contains(HRU)) HRUs.add(HRU);
            }
        }
        return HRUs;
    }
    
    
    public static Map<Integer, Double> ConvertShapeIDnPercents(Connection conn, 
            ResultLevelType inShape, ResultLevelType outShape, int inShapeID){
        String colName = outShape == ResultLevelType.HRU ? 
                "HRUSWATIndex" : outShape.toString();
        String tableName;
        if (inShape != ResultLevelType.Grazing){
            tableName = inShape.toString() + "_" + outShape.toString();
        }else{
            tableName = outShape.toString() + "_" + inShape.toString();
        }
        if (conn != null)
        {
            try
            {
                Statement stmt = conn.createStatement();
                //try to create the table
                String sql = "select " + colName + ", percent from " + tableName + 
                        " where " + inShape.toString() + " = " + 
                        String.valueOf(inShapeID);
                ResultSet rs = stmt.executeQuery(sql);
                Map<Integer, Double> IDnPercents = new HashMap();
                while (rs.next()){
                    IDnPercents.put(rs.getInt(colName), rs.getDouble("percent"));
                }
                return IDnPercents;
            }
            catch (SQLException e) { return null; }
        }
        return new HashMap();
    }
    
    public static double ConvertResult(ResultLevelType inShape, 
            ResultLevelType outShape, Map<Integer, Double> inResults, int outShapeID,
            Map<Integer,Double> inShapeIDnPercents, boolean isUnitValue){
        if (inResults.size() != inShapeIDnPercents.size())
        {
            System.out.println(
                "Different number of values and polygons" + System.getProperty("line.separator") +
                Thread.currentThread().getStackTrace());
            return 0.0;
        }

        double v = 0.0;
        double totalArea = 0.0;
        for (int ID : inResults.keySet())
        {
            double percent = inShapeIDnPercents.get(ID);
            if (isUnitValue)
            {
                double area;
                try{
                    area = GetShapeArea(inShape, ID);
                } catch (NullPointerException e){
                    throw e;
                }
                
                if (area <= 0)
                    System.out.println("Wrong Area, ID " + String.valueOf(ID));
                else
                {
                    v += inResults.get(ID) * percent * area;
                    totalArea += area * percent;
                }
            }
            else
                v += inResults.get(ID) * percent;
        }
        if (isUnitValue)
        {
            if (totalArea > 0)
                v /= totalArea;
            else
                System.out.println("Wrong Area, area should be larger than 0." +
                    System.getProperty("line.separator") +
                    Thread.currentThread().getStackTrace().toString());
        }
        return v;              
    }
    
    public static double GetShapeArea(ResultLevelType shapeType, int ID){
        switch (shapeType)
        {
            case HRU:
                if (hruIDnArea == null) 
                    hruIDnArea = GetTableIDnArea(conn_spatial, HRUAreaTable);
                return hruIDnArea.get(ID);
            case Field:
                if (fieldIDnArea == null) 
                    fieldIDnArea = GetTableIDnArea(conn_spatial, FieldAreaTable);
                return fieldIDnArea.get(ID);
            case Farm:
                if (farmIDnArea == null) 
                    farmIDnArea = GetTableIDnArea(conn_spatial, FarmAreaTable);
                return farmIDnArea.get(ID);
            case Grazing:
                if (grazingIDnArea == null) 
                    grazingIDnArea = GetTableIDnArea(conn_spatial, GrazingAreaTable);
                return grazingIDnArea.get(ID);
            case Subbasin:
                if (subbasinIDnArea == null) 
                    subbasinIDnArea = GetTableIDnArea(conn_spatial, SubbasinAreaTable);
                return subbasinIDnArea.get(ID);
        }
        return 0.0;
    }
    
    public static double GetShapeArea(BMPType type, int ID){
        ResultLevelType shapeType = ResultLevelType.Field;
        switch (type)
        {
            case Small_Dams:
            case Holding_Ponds:
            case Grazing_Subbasin:
                return 0.0;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                shapeType = ResultLevelType.Subbasin;
                break;
            case Tillage_Farm:
            case Forage_Farm:
                shapeType = ResultLevelType.Farm;
                break;
            case Forage_Field:
            case Tillage_Field:
                shapeType = ResultLevelType.Field;
                break;
            case Grazing:
                shapeType = ResultLevelType.Grazing;
                break;
        }
        return GetShapeArea(shapeType, ID);
    }
    
    public static double GetShapeArea(ResultLevelType shapeType){
        Collection<Double> values = new HashSet();
        Double sumValue = 0.0;
        switch (shapeType)
        {
            case HRU:
                if (hruIDnArea == null) 
                    hruIDnArea = GetTableIDnArea(conn_spatial, HRUAreaTable);
                values = hruIDnArea.values();
                break;
            case Field:
                if (fieldIDnArea == null) 
                    fieldIDnArea = GetTableIDnArea(conn_spatial, FieldAreaTable);
                values = fieldIDnArea.values();
                break;
            case Farm:
                if (farmIDnArea == null) 
                    farmIDnArea = GetTableIDnArea(conn_spatial, FarmAreaTable);
                values = farmIDnArea.values();
                break;
            case Grazing:
                if (grazingIDnArea == null) 
                    grazingIDnArea = GetTableIDnArea(conn_spatial, GrazingAreaTable);
                values = grazingIDnArea.values();
                break;
            case Subbasin:
                if (subbasinIDnArea == null) 
                    subbasinIDnArea = GetTableIDnArea(conn_spatial, SubbasinAreaTable);
                values = subbasinIDnArea.values();
        }
        for (double value : values) {
            if (value > 0) {
                sumValue += value;
            } else {
                System.out.println("Area error. Less than zero!");
            }
        }
        return sumValue;
    }
    
    public static double GetShapeArea(BMPType type){
        ResultLevelType shapeType = ResultLevelType.Field;
        switch (type)
        {
            case Small_Dams:
            case Holding_Ponds:
            case Grazing_Subbasin:
                return 0.0;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                shapeType = ResultLevelType.Subbasin;
                break;
            case Tillage_Farm:
            case Forage_Farm:
                shapeType = ResultLevelType.Farm;
                break;
            case Forage_Field:
            case Tillage_Field:
                shapeType = ResultLevelType.Field;
                break;
            case Grazing:
                shapeType = ResultLevelType.Grazing;
                break;
        }
        return GetShapeArea(shapeType);
    }
    
    public static List<Integer> GetFields(){
        if (fieldIDnArea == null) 
                    fieldIDnArea = GetTableIDnArea(conn_spatial, FieldAreaTable);
        List<Integer> ids = new ArrayList();
        for (int id : fieldIDnArea.keySet()) ids.add(id);
        return ids;
    }
    
    public static List<Integer> GetHRUs(){
        if (hruIDnArea == null) 
                    hruIDnArea = GetTableIDnArea(conn_spatial, HRUAreaTable);
        List<Integer> ids = new ArrayList();
        for (int id : hruIDnArea.keySet()) ids.add(id);
        return ids;
    }
    
    public static List<Integer> GetFarms(){
        if (farmIDnArea == null) 
                    farmIDnArea = GetTableIDnArea(conn_spatial, FarmAreaTable);
        List<Integer> ids = new ArrayList();
        for (int id : farmIDnArea.keySet()) ids.add(id);
        return ids;
    }
    
    public static List<Integer> GetSubbasins(){
        if (subbasinIDnArea == null) 
                    subbasinIDnArea = GetTableIDnArea(conn_spatial, SubbasinAreaTable);
        List<Integer> ids = new ArrayList();
        for (int id : subbasinIDnArea.keySet()) ids.add(id);
        return ids;
    }
    
    public static List<Integer> GetGrazings(){
        if (grazingIDnArea == null) 
                    grazingIDnArea = GetTableIDnArea(conn_spatial, GrazingAreaTable);
        List<Integer> ids = new ArrayList();
        for (int id : grazingIDnArea.keySet()) ids.add(id);
        return ids;
    }
    
    public static List<Integer> GetGrazingSubs(){
        if (conn_spatial != null)
        {
            try
            {
                Statement stmt = conn_spatial.createStatement();
                //try to create the table
                String sql = "select distinct subbasin from subbasin_grazing";
                ResultSet rs = stmt.executeQuery(sql);
                List<Integer> ids = new ArrayList();
                while (rs.next()){
                    ids.add(rs.getInt(1));
                }
                return ids;
            }
            catch (SQLException e) {return new ArrayList();}
        }
        return new ArrayList();
    }
    
    public static List<Integer> GetField(List<BMPItem> items) throws Exception{
        if (items.isEmpty()) return new ArrayList<Integer>();

        BMPType type = items.get(0).getType();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds ||
            type == BMPType.Grazing)
            return new ArrayList<Integer>();

        List<Integer> originalIDs = new ArrayList<Integer>();
        for (BMPItem item : items)
            originalIDs.add(item.getID());

        switch (type)
        {
            case Tillage_Farm:
            case Forage_Farm:
                return GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, originalIDs, false);
            case Tillage_Field:
            case Forage_Field:
                return originalIDs;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return GetRelationIDs(ResultLevelType.Subbasin, 
                        ResultLevelType.Field, originalIDs, false);
            default:
                return new ArrayList<Integer>();
        }
    }

    public static List<Integer> GetHRU(List<BMPItem> items) throws Exception{
        if (items.isEmpty()) return new ArrayList<Integer>();

        BMPType type = items.get(0).getType();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds)
            return new ArrayList<Integer>();

        List<Integer> originalIDs = new ArrayList<Integer>();
        for (BMPItem item : items)
            originalIDs.add(item.getID());

        switch (type)
        {
            case Tillage_Farm:
            case Forage_Farm:
                List<Integer> fieldIDs = GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, originalIDs, false);
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.HRU, fieldIDs, false);
            case Tillage_Field:
            case Forage_Field:
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.HRU, originalIDs, false);
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return GetRelationIDs(ResultLevelType.Subbasin, 
                        ResultLevelType.HRU, originalIDs, false);                
            case Grazing:
                return GetGrazingHRU(conn_spatial, GetSubbasin(originalIDs, type));
            default:
                return new ArrayList<Integer>();
        }
    }

    public static List<Integer> GetSubbasin(List<BMPItem> items) throws Exception{
        if (items.isEmpty()) return new ArrayList<Integer>();

        BMPType type = items.get(0).getType();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds)
            return new ArrayList<Integer>();

        List<Integer> originalIDs = new ArrayList<Integer>();
        for (BMPItem item : items)
            originalIDs.add(item.getID());

        switch (type)
        {
            case Grazing:
                return GetRelationIDs(ResultLevelType.Grazing, 
                        ResultLevelType.Subbasin, originalIDs, false);
            case Tillage_Farm:
            case Forage_Farm:
                List<Integer> fieldIDs = GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, originalIDs, false);
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.Subbasin, fieldIDs, false);
            case Tillage_Field:
            case Forage_Field:
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.Subbasin, originalIDs, false);
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return originalIDs;
            default:
                return new ArrayList<Integer>();
        }
    }
    
    public static List<Integer> GetField(List<Integer> ids, BMPType type) throws Exception{
        if (ids.isEmpty()) return new ArrayList<Integer>();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds ||
            type == BMPType.Grazing)
            return new ArrayList<Integer>();

        switch (type)
        {
            case Tillage_Farm:
            case Forage_Farm:
                return GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, ids, false);
            case Tillage_Field:
            case Forage_Field:
                return ids;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return GetRelationIDs(ResultLevelType.Subbasin, 
                        ResultLevelType.Field, ids, false);
            default:
                return new ArrayList<Integer>();
        }
    }

    public static List<Integer> GetHRU(List<Integer> ids, BMPType type) throws Exception{
        if (ids.isEmpty()) return new ArrayList<Integer>();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds)
            return new ArrayList<Integer>();

        switch (type)
        {
            case Tillage_Farm:
            case Forage_Farm:
                List<Integer> fieldIDs = GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, ids, false);
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.HRU, fieldIDs, false);
            case Tillage_Field:
            case Forage_Field:
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.HRU, ids, false);
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return GetRelationIDs(ResultLevelType.Subbasin, 
                        ResultLevelType.HRU, ids, false);
            case Grazing:
                return GetGrazingHRU(conn_spatial, GetSubbasin(ids, type));
            default:
                return new ArrayList<Integer>();
        }
    }

    public static List<Integer> GetSubbasin(List<Integer> ids, BMPType type) throws Exception{
        if (ids.isEmpty()) return new ArrayList<Integer>();

        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds)
            return new ArrayList<Integer>();

        switch (type)
        {
            case Grazing:
                return GetRelationIDs(ResultLevelType.Grazing, 
                        ResultLevelType.Subbasin, ids, false);
            case Tillage_Farm:
            case Forage_Farm:
                List<Integer> fieldIDs = GetRelationIDs(ResultLevelType.Farm, 
                        ResultLevelType.Field, ids, false);
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.Subbasin, fieldIDs, false);
            case Tillage_Field:
            case Forage_Field:
                return GetRelationIDs(ResultLevelType.Field, 
                        ResultLevelType.Subbasin, ids, false);
            case Tillage_Subbasin:
            case Forage_Subbasin:
                return ids;
            default:
                return new ArrayList<Integer>();
        }
    }
}
