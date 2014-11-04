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
import java.util.List;
import java.util.Map;

/**
 *
 * @author Shao
 */
public abstract class ScenarioResult extends SQLiteDatabase{
    public ScenarioResult(String scenarioDatabasePath){
        super(scenarioDatabasePath);
    }
    
    protected double computeValueFromResultSet(ResultSet rs, String expression, String filter) throws SQLException {        
        String tableName;
        ResultSet rs_new;
        try{
            tableName = rs.getMetaData().getTableName(1);
            String sql = "SELECT " + expression + " FROM " + tableName;
            if (!filter.isEmpty()) sql += " WHERE " + filter; 
            rs_new = rs.getStatement().executeQuery(sql);
        } catch (Exception e) {
            rs_new = null;
        }
        
        if (!rs_new.next()) return 0.0;
        return rs_new.getDouble(expression);
    }
    
    protected int startYear = -1;
    protected int endYear = -1;
    
    private static Connection conn_spatial = Query.OpenConnection(Project.GetSpatialDB());
    
    protected abstract ResultSet getStartEndYearTable();
    
    protected void getStartEndYear() throws Exception {
        if (startYear > 0 && endYear > 0) return;
        try
        {
            ResultSet rsStart = getStartEndYearTable();
            if (rsStart != null)
                startYear = (int) Math.round(computeValueFromResultSet(rsStart, "min(year)", ""));//int.Parse(dt.Compute("min(year)", "").ToString());
            ResultSet rsEnd = getStartEndYearTable();
            if (rsEnd != null)
                endYear = (int) Math.round(computeValueFromResultSet(rsEnd, "max(year)", ""));//int.Parse(dt.Compute("max(year)", "").ToString());
        }  catch (SQLException e) {  }

        if (startYear <= 0 || endYear <= 0)
            throw new Exception("Simulation year is not correct.");
    }
    
    public int StartYear() throws Exception {
        getStartEndYear();
        return startYear;
    }

    public int EndYear() throws Exception {
        getStartEndYear();
        return endYear;
    }   
    
    public abstract void Update();
    
    protected abstract ResultSet getCorrepondingResultTable(Project project, 
            BMPType type, String filter, boolean closeFirst);
    
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
    
    public double GetAverageResult(  // !!virtual class
            Project project,
            BMPType type,           //used to get corresponding table
            String resultColumn,    //used to get corresponding column
            int id,                 //object id
            int startYear,     //start year
            int endYear,       //end year
            Scenario compareScenario,
            boolean reservoirGotoReach) throws Exception
    {
        ResultSet rs = getCorrepondingResultTable(project,type,"", true);

        if (rs == null)
            throw new Exception("Data of " + type.toString() + " is null.");

        String filter = getSQL(id, startYear, endYear);

        double value = 0.0;
        try
        {
            if (type == BMPType.Small_Dam && (compareScenario != null || reservoirGotoReach) && this.getClass() == ScenarioSWATModel.class)
                reservoirGotoReach = true;

            if (Query.ComputeRS(rs, filter).next())
                value = computeValueFromResultSet(rs, String.format("Avg(%s)", resultColumn), filter);
            else if (reservoirGotoReach) //no small dam, compare with others, SWAT result
            {
                //try to find out flow of corresponding reach
                ScenarioSWATModel SWAT = (ScenarioSWATModel) this;
                rs = SWAT.GetReachResult(project);//rech result table
                if (Query.ComputeRS(rs, filter).next())
                    value = computeValueFromResultSet(rs, String.format("Avg(%s)", resultColumn), filter);                    
            }
        }
        catch (SQLException e){
            System.err.println("Query error." + filter);
            //return 0.0;
        }            

        double compareValue = 0.0;
        if (compareScenario != null)
            compareValue = compareScenario.GetResult().GetAverageResult(
                project, type, resultColumn, id, startYear, endYear, null, reservoirGotoReach);

        value -= compareValue;

        if (type == BMPType.Small_Dam && resultColumn.equals(SWATResultColumnType.water))
            value *= 31536000.0;  //m^3/s --> m^3

        System.err.println(
            String.format("id = %d, result = %s, value = %f",id,resultColumn,value));
        return value;
    }
    
    private double getDifference(double scenario1, double scenario2) {
        return scenario1 - scenario2;
    }
    
    protected List<ResultDataPair> getDifference(List<ResultDataPair> scenario1, List<ResultDataPair> scenario2) {
        if (scenario2.size() == 0) return scenario1;
        List<ResultDataPair> results = new ArrayList<ResultDataPair>();
        if (scenario1.size() == 0)  {
            for (ResultDataPair r : scenario2)
                results.add(new ResultDataPair(r.getYear(), getDifference(0.0, r.getData())));
        }
        else  {
            for (int i = 0; i < scenario1.size(); i++)
                results.add(
                    new ResultDataPair(
                        scenario1.get(i).getYear(), getDifference(scenario1.get(i).getData(), scenario2.get(i).getData())));
        }
        return results;
    }
    
    public List<ResultDataPair> GetResults( //!! virtual
            Project project,
            BMPType type,           //used to get corresponding table
            String resultColumn,    //used to get corresponding column
            int id,                 //object id, if id = -1, return off-site result.
            int startYear,     //start year
            int endYear,       //end year
            Scenario compareScenario,
            boolean reservoirGotoReach) throws Exception
    {        
        if (id == -1)
            return getOffSiteResults(project,type, resultColumn, startYear, endYear,compareScenario);

        ResultSet rs = getCorrepondingResultTable(project, type, "", true);

        if (rs == null)
            throw new Exception("Data of " + type.toString() + " is null.");

        String filter = getSQL(id, startYear, endYear);
        
        int colIndex = rs.findColumn(resultColumn);
        int yearIndex = rs.findColumn("Year");
        ResultSet rows = Query.ComputeRS(rs, filter); // !!
        List<ResultDataPair> results = new ArrayList<ResultDataPair>();

        if (type == BMPType.Small_Dam && (compareScenario != null || reservoirGotoReach) && this instanceof ScenarioSWATModel)
            reservoirGotoReach = true;

        
        if (reservoirGotoReach) //no small dam, compare with others, SWAT result
        {
            //try to find out flow of corresponding reach
            ScenarioSWATModel SWAT = (ScenarioSWATModel) this;
                rs = SWAT.GetReachResult(project);//rech result table
            rows = Query.ComputeRS(rs, filter);  // !!
            while (rows.next())
                results.add(
                    new ResultDataPair(
                        rows.getInt(yearIndex), //year is the second column
                        rows.getDouble(colIndex)));
            reservoirGotoReach = true;
        } else {
            while (rows.next())
                results.add(
                    new ResultDataPair(
                        rows.getInt(yearIndex), //year is the second column
                        rows.getDouble(colIndex)));
        }

        if (compareScenario != null)             
            results =
                getDifference(
                results,
                compareScenario.GetResult().GetResults(project, type, resultColumn, id, startYear, endYear, null, reservoirGotoReach));

        if (type == BMPType.Small_Dam && resultColumn.equals(SWATResultColumnType.water))
        {
            for (ResultDataPair p : results)
                p.setData(p.getData()* 31536000.0);  //m^3/s --> m^3
        }

        return results;
    }
    
    /// <summary>
    /// off site results
    /// first key is the bmp type
    /// second key is the column type
    /// </summary>
    protected Map<BMPType, Map<String, List<ResultDataPair>>> offSiteResults;

    /// <summary>
    /// used to create off-site results _off_site_results
    /// </summary>
    protected abstract void getOffSiteResults(Project project);

    protected List<ResultDataPair> getOffSiteResults( // !! virtual
        Project project,
        BMPType type,           //used to get corresponding table
        String resultColumn,    //used to get corresponding column
        int startYear,     //start year
        int endYear,       //end year
        Scenario compareScenario) throws Exception
    {
        compareScenario = null;
        startYear = -1;
        endYear = -1;
        //create off site results first
        getOffSiteResults(project);

        //read values
        Map<String, List<ResultDataPair>> existingTypeResults = null;
        if (offSiteResults.containsKey(type))
        {
            existingTypeResults = offSiteResults.get(type);
            List<ResultDataPair> existingResults = null;
            if (existingTypeResults.containsKey(resultColumn))
            {
                existingResults = existingTypeResults.get(resultColumn);
                if (startYear < 0 && endYear < 0) return existingResults;
                if (startYear == StartYear() && endYear == EndYear()) return existingResults;

                List<ResultDataPair> result = new ArrayList<ResultDataPair>();
                for (ResultDataPair p : existingResults)
                    if (p.getYear() >= startYear && p.getYear() <= endYear) result.add(p);

                if (compareScenario != null)
                    result =
                        getDifference(
                        result,
                        compareScenario.GetResult().getOffSiteResults(
                        project,type, resultColumn, startYear, endYear, null));

                return result;
            }
            else 
                throw new Exception("Wrong result column: " + resultColumn);                
        }
        else
            throw new Exception("Wrong result type: " + type.toString());
    }
    
    protected boolean _hasResult = false;
    
    public boolean GetHasResult(){
        return _hasResult;
    }
    public void SetHasResult(boolean hasResult){
        _hasResult = hasResult;
    }
    
}
