/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Shao
 */
public class ScenarioSWATModel extends ScenarioResult {
    public ScenarioSWATModel(String scenarioDatabasePath){
        super(scenarioDatabasePath);
    }
    private static String tableNameHRU = "hru";
    private static String tableNameSubbasin = "sub";
    private static String tableNameReach = "rch";
    private static String tableNameReservoir = "rsv";
    private static String tableNamePointSource = "rec";
    private static String tableNameField = "fld";
    private static String tableNameFarm = "frm";
    private static String tableNameGrazing = "gzg";
    private static String tableNameGrazingSubbasin = "gzg_sub";
    
    private static String columnNameID = "id";
    private static String columnNameYear = "year";

    private static String columns =
                        columnNameID + "," +
                        columnNameYear + "," +
                        SWATResultColumnType.water.toString() + "," +
                        SWATResultColumnType.sediment.toString() + "," +
                        SWATResultColumnType.PP.toString() + "," +
                        SWATResultColumnType.DP.toString() + "," +
                        SWATResultColumnType.TP.toString() + "," +
                        SWATResultColumnType.PN.toString() + "," +
                        SWATResultColumnType.DN.toString() + "," +
                        SWATResultColumnType.TN.toString();

    private static String SQLPrefixField =
                "insert into " + tableNameField
                + " (" + columns + ") values (";
    private static String SQLPrefixFarm =
                "insert into " + tableNameFarm
                + " (" + columns + ") values (";
    private static String SQLPrefixGrazing =
                "insert into " + tableNameGrazing
                + " (" + columns + ") values (";
    private static String SQLPrefixGrazingSubbasin =
                "insert into " + tableNameGrazingSubbasin
                + " (" + columns + ") values (";
    
    public void RunSWAT(Scenario scenario) throws Exception{
        String sep = System.getProperty("line.separator");
        Connection conn = Query.OpenConnection(this.GetDatabasePath());
        if (JOptionPane.showConfirmDialog(null, 
                "Running of the SWAT will take about 2 minutes.\n\n"
                        + "Press YES to start!", "SWAT model", 
                        JOptionPane.YES_NO_OPTION) 
                == JOptionPane.NO_OPTION) return;
        
        // Clear the table first
        updateTables(conn);
        
        // Mark the start of the SWAT model
        long startTime = System.currentTimeMillis();
        
        // Write the webs.webs file based on Scenario design
        if (scenario.getScenarioDesign().GenerateWEBsText(scenario)) {
            System.out.println("Link file (webs.webs) created successfully!");
        } else {
            return;
        }
        
        StringBuilder InputText = new StringBuilder();
        StringBuilder ErrText = new StringBuilder(); 
        File SWATExecutable = new File(Project.getSWATExecutive());
        try {
            
            ProcessBuilder SWATBuilder = new ProcessBuilder(SWATExecutable.getAbsolutePath(), "-i", "input", "-o", "output");
            SWATBuilder.directory(new File(Project.getSWATExecutive()).getParentFile().getAbsoluteFile());
            
            Process SWATProcess = SWATBuilder.start();
            System.out.print("SWAT model is running...\n");
            
            //Print the response of the SWAT model
            BufferedReader stdInput = new BufferedReader(new 
                InputStreamReader(SWATProcess.getInputStream()));
            BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(SWATProcess.getErrorStream()));
            String in;
            while ((in = stdInput.readLine()) != null) {
                InputText.append(in + sep);
            }
            
            String err;
            while ((err = stdError.readLine()) != null) {
                ErrText.append(err + sep);
            }            
            
            int result = SWATProcess.waitFor();            
            if (result == 0){
                System.out.println("SWAT model ran successfully!");
                // post SWAT processes
                postSWATProcess();

                long endTime = System.currentTimeMillis();
                int diffTotalSec = Math.round((endTime - startTime)/1000);
                int diffSec = diffTotalSec % 60;
                int diffMin = (int) Math.floor(diffTotalSec / 60);
                InputText.append(sep + "Run SWAT process finished successfully!\n");
                InputText.append("Total time use: " + String.valueOf(diffMin)
                        + " minutes " + String.valueOf(diffSec) + " seconds.");
                JOptionPane.showMessageDialog(null, InputText, "", JOptionPane.INFORMATION_MESSAGE);
            }
            else{
                ErrText.append("SWAT run has FAILED!\n "
                        + "Please check the input folder:\n"
                        + SWATExecutable.getParent() + sep);
                System.out.print(ErrText.toString() + sep);
                JOptionPane.showMessageDialog(null, ErrText, "", JOptionPane.INFORMATION_MESSAGE);
            }
                        
        } catch (IOException | InterruptedException e) {
            System.out.print(e.toString());
        } 
    }
    
    private void updateTables(Connection conn) throws SQLException{          
        String drop = "drop table if exists fld;" +
                        "drop table if exists frm;" +
                        "drop table if exists gzg;" +
                        "drop table if exists gzg_sub;" +
                        "drop table if exists hru;" +
                        "drop table if exists rch;" +
                        "drop table if exists rec;" +
                        "drop table if exists rsv;" +
                        "drop table if exists sub;";
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
    
    private void postSWATProcess() throws Exception{
        // Creating tables        
        prepareTables();
        System.out.println("Field, farm, grazing sub, and grazing tables are created.");
        System.out.println("Calculating results...");

        // Field results
        createFieldResult();
        System.out.println("Field results created!");

        // Farm results
        createFarmResult();
        System.out.println("Farm results created!");

        // Grazing sub results
        createGrazingSubbasinResult();
        System.out.println("Grazing sub results created!");
        
        // Grazing results
        createGrazingResult();
        System.out.println("Grazing results created!");
    }
    
    private void createFieldResult() throws Exception {
        //get hru swat result
        ResultSet hruResult = getCorrepondingSWATResultTable(SWATResultType.hru, "", true);
        Connection conn = Query.OpenConnection(Project.getSpatialDB());
        
        if (!hruResult.next()) return;
        
        if (hruIDnArea == null) hruIDnArea = Spatial.GetTableIDnArea(conn, HRUAreaTable);
        List<Integer> fields = Spatial.GetRelationIDs(ResultLevelType.HRU, 
                ResultLevelType.Field, -1, false);

        //get all years
        int startYear = GetStartYear();
        int endYear = GetEndYear();   

        //
        FieldSWATResults swatResults = new FieldSWATResults();
        for (int field : fields){
            Map<Integer, Double> HRUnPercents = Spatial.ConvertShapeIDnPercents(conn, 
                    ResultLevelType.Field, ResultLevelType.HRU, field);

            if (HRUnPercents.isEmpty())
            {
                System.out.println("No corresponding hrus for field " + String.valueOf(field));
                continue;
            }

            for (int year = startYear; year <= endYear; year++)
            {
                List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();//8 SWAT result columns
                for (int hru : HRUnPercents.keySet())
                {
                    String sql = getSQL(hru, year, year);
                    ResultSet rows;
                    rows = Query.ComputeRS(hruResult, sql);
                    if (rows.next()){
                        for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water);
                                i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                        {        
                            double value = rows.getDouble(i + 3);
                            if (results.size() <= i)
                                results.add(new HashMap<Integer, Double>());
                            results.get(i).put(hru,value);
                        }
                    }
                }


                for(int i=0; i < results.size(); i++)
                {
                    double value = Spatial.ConvertResult(ResultLevelType.HRU,
                        ResultLevelType.Field, results.get(i), field, HRUnPercents, true);

                    swatResults.Add(field, year, SWATResultColumnType.getEnum(i), value);
                }                                  
            }
        }            

        //recalucate field results using weights
        reCalculateFieldResults(swatResults); // !! ref

        //create SQL and insert to database
        insert(hruResult, swatResults.SQLInsert(SQLPrefixField));
    }
    
    private void reCalculateFieldResults(FieldSWATResults r) throws SQLException { // !! ref
        FieldWeights fieldWeights = new FieldWeights(Project.getSpatialDB());

        r.ReCalculateWithWeight(fieldWeights);
    }
    
    private void createFarmResult() throws Exception {
        //get field swat result
        ResultSet fieldResult = getCorrepondingSWATResultTable(SWATResultType.field, "", true);
        Connection conn = Query.OpenConnection(Project.getSpatialDB());
        
        if (!fieldResult.next()) return;

        //get all years
        int startYear = GetStartYear();
        int endYear = GetEndYear();   

        StringBuilder sb = new StringBuilder();

        if (fieldIDnArea == null) fieldIDnArea = Spatial.GetTableIDnArea(conn, FieldAreaTable);
        List<Integer> farms = Spatial.GetRelationIDs(ResultLevelType.Field, 
                ResultLevelType.Farm, -1, false);
        
        for (int farm : farms)
        {
            Map<Integer, Double> FieldnPercents = Spatial.ConvertShapeIDnPercents(conn, 
                    ResultLevelType.Farm, ResultLevelType.Field, farm);

            for (int year = startYear; year <= endYear; year++)
            {
                List<Map<Integer, Double>> results = new ArrayList<Map<Integer, Double>>();//8 SWAT result columns
                for (int field : FieldnPercents.keySet())
                {
                    String sql = getSQL(field, year, year);
                    ResultSet rows = Query.ComputeRS(fieldResult, sql);
                    if (rows.next()){
                        for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water);
                                i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                        {        
                            double value = rows.getDouble(i + 3);
                            if (results.size() <= i)
                                results.add(new HashMap<Integer, Double>());
                            results.get(i).put(field,value);
                        }
                    }
                    
                }

                sb.append(SQLPrefixFarm + String.valueOf(farm) + "," + String.valueOf(year));
                
                for(int i=0; i < results.size(); i++)
                {
                    double value = Spatial.ConvertResult(ResultLevelType.Field,
                        ResultLevelType.Farm, results.get(i), farm, FieldnPercents, true);

                    sb.append("," + String.valueOf(value));
                }
                sb.append(");");
            }
        }
        
        insert(fieldResult,sb.toString());
    }
    
    private void createGrazingSubbasinResult() throws Exception  {
        //get field swat result
        ResultSet hruResult = getCorrepondingSWATResultTable(SWATResultType.hru, "", true);
        Connection conn = Query.OpenConnection(Project.getSpatialDB());
        
        if (!hruResult.next()) return;
        
        //get all years
        int startYear = GetStartYear();
        int endYear = GetEndYear();   

        StringBuilder sb = new StringBuilder();

        if (fieldIDnArea == null) fieldIDnArea = Spatial.GetTableIDnArea(conn, FieldAreaTable);
        List<Integer> subs = Spatial.GetRelationIDs(ResultLevelType.Subbasin, 
                ResultLevelType.Grazing, -1, true);
        
        for (int sub : subs)
        {        
            List<Integer> hrus = Spatial.GetGrazingHRU(conn, sub);
            if (hrus.isEmpty()) continue;

            for (int year = startYear; year <= endYear; year++) //each year
            {
                sb.append(SQLPrefixGrazingSubbasin + String.valueOf(sub) + "," + String.valueOf(year));

                for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water); i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                {
                    List<Double> hruResults = new ArrayList<>();
                    for (int hru : hrus)
                    {
                        String sql = getSQL(hru, year, year);
                        ResultSet rows = Query.ComputeRS(hruResult, sql);
                        if (rows.next()){
                            double result = rows.getDouble(i + 3);
                            hruResults.add(result);
                        }
                    }
                    double value = getSubbasinGrazingSWATResult(conn, hrus, hruResults);
                    sb.append("," + String.valueOf(value));
                }
                sb.append(");");
            }
        }
        insert(hruResult, sb.toString());
    }
    
    private double getSubbasinGrazingSWATResult(Connection conn, List<Integer> hrus, List<Double> hruResults) throws Exception {
        Map<Integer, Double> HRUnPercents = new HashMap();
        if (conn != null)
        {
            try
            {
                Statement stmt = conn.createStatement();
                //try to create the table
                String sql = "select HRUSWATIndex, percent from hru_subbasin";
                ResultSet rs = stmt.executeQuery(sql);                
                while (rs.next()){
                    HRUnPercents.put(rs.getInt("HRUSWATIndex"),rs.getDouble("percent"));
                }
            }
            catch (SQLException e) {throw e;}
        }

        double result = 0.0;
        double totalAreaPercent = 0.0;
        for (int i = 0; i < hrus.size(); i++)
        {
            double percent = HRUnPercents.get(hrus.get(i));
            totalAreaPercent += percent;
            result += percent * hruResults.get(i);
        }

        return result / totalAreaPercent;
    }
    
    private void createGrazingResult() throws Exception {
        //get field swat result
        ResultSet subbasinGrazingResult = getCorrepondingSWATResultTable(SWATResultType.grazing_subbasin, "", true);
        Connection conn = Query.OpenConnection(Project.getSpatialDB());
        
        if (!subbasinGrazingResult.next()) return;

        //get all years
        int startYear = GetStartYear();
        int endYear = GetEndYear();   

        StringBuilder sb = new StringBuilder();

        if (subbasinIDnArea == null) subbasinIDnArea = Spatial.GetTableIDnArea(conn, SubbasinAreaTable);
        List<Integer> grazings = Spatial.GetRelationIDs(ResultLevelType.Subbasin, 
                ResultLevelType.Grazing, -1, false);
        
        for (int grazing : grazings)
        {
            Map<Integer, Double> grazingSubnPercents = Spatial.ConvertShapeIDnPercents(
                    conn, ResultLevelType.Grazing, ResultLevelType.Subbasin, grazing);

            for (int year = startYear; year <= endYear; year++) //each year
            {
                sb.append(SQLPrefixGrazing + String.valueOf(grazing) + "," + String.valueOf(year));

                for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water); i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                {
                    double finalResult = 0.0;
                    for (int sub : grazingSubnPercents.keySet())
                    {
                        String sql = getSQL(sub, year, year);
                        ResultSet rows = Query.ComputeRS(subbasinGrazingResult, sql);
                        if (rows.next()){
                            double result = rows.getDouble(i + 3);
                            finalResult += result * grazingSubnPercents.get(sub);
                        }                        
                    }
                    sb.append("," + String.valueOf(finalResult));
                }
                sb.append(");");
            }
        }
        insert(subbasinGrazingResult, sb.toString());
    }
    
    private void prepareTables() {          
        List<AccessColumn> results = new ArrayList<>();
        AccessColumn ac = new AccessColumn();
        ac.setColumnName(columnNameID);ac.setType(Integer.TYPE);
        results.add(ac);
        ac = new AccessColumn();
        ac.setColumnName(columnNameYear);ac.setType(Integer.TYPE);
        results.add(ac);
        for (SWATResultColumnType i : SWATResultColumnType.values())
        {
            ac = new AccessColumn();
            ac.setColumnName(i.toString());ac.setType(Double.TYPE);  
            results.add(ac);
        }           
        IAccessColumn[] columns = new IAccessColumn[results.size()];
        columns = results.toArray(columns);
        Query.PrepareTable(this.GetDatabasePath(), tableNameField, columns);
        Query.PrepareTable(this.GetDatabasePath(), tableNameFarm, columns);
        Query.PrepareTable(this.GetDatabasePath(), tableNameGrazing, columns);
        Query.PrepareTable(this.GetDatabasePath(), tableNameGrazingSubbasin, columns);  
    }  
    
    private ResultSet resultHru;
    private ResultSet resultSub;
    private ResultSet resultRch;
    private ResultSet resultRsv;
    private ResultSet resultRec;
    private ResultSet resultField;
    private ResultSet resultFarm;
    private ResultSet resultGrazing;
    private ResultSet resultGrazingSubbasin;
    private ResultSet resultRchEntire;
    
    private static Connection conn_spatial = Query.OpenConnection(Project.getSpatialDB());
    
    private static String FieldAreaTable = "field_area";
    private static String FarmAreaTable = "farm_area";
    private static String GrazingAreaTable = "grazing_area";
    private static String HRUAreaTable = "hru_area";
    private static String SubbasinAreaTable = "subbasin_area"; 
    
    private static Map <Integer, Double> fieldIDnArea = Spatial.GetTableIDnArea(conn_spatial, FieldAreaTable);
    private static Map <Integer, Double> farmIDnArea = Spatial.GetTableIDnArea(conn_spatial, FarmAreaTable);
    private static Map <Integer, Double> grazingIDnArea = Spatial.GetTableIDnArea(conn_spatial, GrazingAreaTable);
    private static Map <Integer, Double> hruIDnArea = Spatial.GetTableIDnArea(conn_spatial, HRUAreaTable);
    private static Map <Integer, Double> subbasinIDnArea = Spatial.GetTableIDnArea(conn_spatial, SubbasinAreaTable);    
       
    
    protected void insert(ResultSet rs, String sql) throws SQLException {
        if (sql == null) {    
        }
        else {
            Connection conn = rs.getStatement().getConnection();            
            String[] sqls = sql.split(";");                   
            
            try {
                conn.setAutoCommit(false);
                for(String s : sqls) {   
                    conn.prepareStatement(s).executeUpdate();                    
                }
                rs.close();
            } catch (SQLException e) {
                throw e;
            } finally {
                conn.commit();
            }
        }
    }
    
    @Override
    public void Update() {
        resultHru = null;
        resultSub = null;
        resultRch = null;
        resultRsv = null;
        resultRec = null;
        resultField = null;
        resultFarm = null;
        resultGrazing = null;
        resultGrazingSubbasin = null;     
        offSiteResults = null;
    }
    
    private ResultSet getCorrepondingSWATResultTable(SWATResultType resultType, 
            String filter, boolean closeFirst) throws SQLException{
        ResultSet rs = null;
        if (closeFirst){
                switch (resultType)
            {
                case hru:
                    resultHru = null;
                    break;
                case pointsource:
                    resultRec = null;
                    break;
                case reach:
                    resultRch = null;
                    break;
                case reservoir:
                    resultRsv = null;
                    break;
                case subbasin:
                    resultSub = null;
                    break;
                case field:
                    resultField = null;
                    break;
                case farm:
                    resultFarm = null;
                    break;
                case grazing:
                    resultGrazing = null;
                    break;
                case grazing_subbasin:
                    resultGrazingSubbasin = null;
                    break;
            }
        }
        switch (resultType)
        {
            case hru:
                rs = resultHru;
                break;
            case pointsource:
                rs = resultRec;
                break;
            case reach:
                rs = resultRch;
                break;
            case reservoir:
                rs = resultRsv;
                break;
            case subbasin:
                rs = resultSub;
                break;
            case field:
                rs = resultField;
                break;
            case farm:
                rs = resultFarm;
                break;
            case grazing:
                rs = resultGrazing;
                break;
            case grazing_subbasin:
                rs = resultGrazingSubbasin;
                break;
        }

        if (rs != null) return rs;

        if (rs == null)
        {
            rs = Query.GetDataTable("select * from " + getCorrespondingSWATResultTableName(resultType),
                     this.GetDatabasePath(), filter);
            switch (resultType)
            {
                case hru:
                    resultHru = rs;
                    break;
                case pointsource:
                    resultRec = rs;
                    break;
                case reach:
                    {
                        rs = Query.GetDataTable("select * from " + getCorrespondingSWATResultTableName(resultType) +                             
                             " where id = " + String.valueOf(computeValueFromResultSet(rs,"max(id)",""))
                             , this.GetDatabasePath(), filter);

                        resultRch = rs;
                        break;
                    }
                case reservoir:
                    resultRsv = rs;
                    break;
                case subbasin:
                    resultSub = rs;
                    break;
                case field:
                    resultField = rs;
                    break;
                case farm:
                    resultFarm = rs;
                    break;
                case grazing_subbasin:
                    resultGrazingSubbasin = rs;
                    break;
                case grazing:
                    resultGrazing = rs;
                    break;
            }
        }
        return rs;
    } 
    
    protected double computeValueFromResultSet(ResultSet rs, String expression, String filter) throws SQLException {
        Statement stmt = rs.getStatement();
        String tableName = rs.getMetaData().getTableName(1);
        String sql = "SELECT " + expression + " FROM " + tableName;
        if (!filter.isEmpty()) sql += " WHERE " + filter;        
        ResultSet rs_new = stmt.executeQuery(sql);
        if (!rs_new.next()) return 0.0;
        return rs_new.getDouble(expression);
    }
    
    private String getCorrespondingSWATResultTableName(SWATResultType resultType) {
        switch (resultType)
        {
            case hru:
                return tableNameHRU;
            case pointsource:
                return tableNamePointSource;
            case reach:
                return tableNameReach;
            case reservoir:
                return tableNameReservoir;
            case subbasin:
                return tableNameSubbasin;
            case field:
                return tableNameField;
            case farm:
                return tableNameFarm;
            case grazing:
                return tableNameGrazing;
            case grazing_subbasin:
                return tableNameGrazingSubbasin;
        }
        return "";
    }
    
    protected String getSQL(
            int id,                 //object id
            int startYear,     //start year
            int endYear)       //end year
    {
        String filter = ""; 
        if(id >= 0)
            filter = String.format("id = %d", id);
        if (startYear > -1 && endYear > -1)
        {
            if (filter.length() > 0) filter += " and ";
            if(startYear == endYear)
                filter += String.format("year = %d", startYear);
            else
                filter += String.format("year >= %d and year <= %d", startYear, endYear);
        }
        return filter;
    }   
    
    @Override
    protected void getOffSiteResults(Project project){
        if (offSiteResults == null || offSiteResults.size() == 0)
        {
            try {
                if (offSiteResults == null) offSiteResults = new HashMap<BMPType, Map<String, List<ResultDataPair>>>();
                offSiteResults.put(BMPType.Small_Dams, new HashMap<String, List<ResultDataPair>>());
                for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water); i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                    offSiteResults.get(BMPType.Small_Dams).put(SWATResultColumnType.getEnum(i).toString(), new ArrayList<ResultDataPair>());
                ResultSet rs = getCorrepondingSWATResultTable(SWATResultType.reach, "", false);
                while (rs.next())
                {
                    int year = rs.getInt("Year");
                    for (int i = SWATResultColumnType.getInt(SWATResultColumnType.water); i <= SWATResultColumnType.getInt(SWATResultColumnType.TN); i++)
                    {
                        double value = rs.getDouble(i + 2);                            
                        if (i == SWATResultColumnType.getInt(SWATResultColumnType.water))
                            value *= 31536000.0;  //m^3/s --> m^3
                        offSiteResults.get(BMPType.Small_Dams).get(SWATResultColumnType.getEnum(i).toString()).add(new ResultDataPair(year, value));
                    }
                }
                
                offSiteResults.put(BMPType.Holding_Ponds, offSiteResults.get(BMPType.Small_Dams));
                offSiteResults.put(BMPType.Grazing, offSiteResults.get(BMPType.Small_Dams));
                offSiteResults.put(BMPType.Tillage_Field, offSiteResults.get(BMPType.Small_Dams));
            } catch (SQLException ex) {
                Logger.getLogger(ScenarioSWATModel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }        
    
    public ResultSet GetReachResult(Project project) {
        if(resultRchEntire == null)
            resultRchEntire = 
                Query.GetDataTable("select * from " + tableNameReach, GetDatabasePath());
        return resultRchEntire;
    }
    
    @Override
    protected ResultSet getStartEndYearTable(){
        return getCorrepondingResultTable(null, BMPType.Tillage_Subbasin,"", true);
    }
    
    @Override
    protected ResultSet getCorrepondingResultTable(Project project, BMPType type,
            String filter, boolean closeFirst){
        SWATResultType resultType = SWATResultType.reservoir; //for small dam

        if (type == BMPType.Holding_Ponds)
            resultType = SWATResultType.pointsource;
        else if (type == BMPType.Grazing)
            resultType = SWATResultType.grazing;
        //return createSWATGrazingResult(project); //based on Yongbo's memo "Display grazing management results with STC_IM"
        else if (type == BMPType.Tillage_Field || type == BMPType.Forage_Field)
            resultType = SWATResultType.field;
        else if (type == BMPType.Tillage_Farm || type == BMPType.Forage_Farm)
            resultType = SWATResultType.farm;
        else if (type == BMPType.Tillage_Subbasin || type == BMPType.Forage_Subbasin)
            resultType = SWATResultType.subbasin;

        try {   
            return getCorrepondingSWATResultTable(resultType, "", closeFirst);
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioSWATModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
