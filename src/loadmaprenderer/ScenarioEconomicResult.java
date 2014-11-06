/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Shao
 */
public class ScenarioEconomicResult extends ScenarioResult {

    public ScenarioEconomicResult(String scenarioDatabasePath) {
        super(scenarioDatabasePath);
    }
    
    private ResultSet resultSmallDam;
    private ResultSet resultHoldingPond;
    private ResultSet resultGrazing;
    private ResultSet resultGrazingSubbasin;
    private ResultSet resultCropField;
    private ResultSet resultCropFarm;
    private ResultSet resultCropSubbasin;
    private List<ResultDataPair> offSiteResultsTotal; //total cost

    @Override
    protected ResultSet getStartEndYearTable() {
        ResultSet rs = getCorrepondingResultTable(null, BMPType.Small_Dams, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Holding_Ponds, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Grazing, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Tillage_Field, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Tillage_Farm, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Tillage_Subbasin, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Forage_Field, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Forage_Farm, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = getCorrepondingResultTable(null, BMPType.Forage_Subbasin, "", true);
        try {
            if (rs.next()) return rs;
        } catch (SQLException ex) {
            Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public void Update() {
        resultSmallDam = null;
        resultHoldingPond = null;
        resultGrazing = null;
        resultCropField = null;
        resultCropFarm = null;
        resultCropSubbasin = null;
        resultGrazingSubbasin = null;
        offSiteResults = null;
        offSiteResultsTotal = null;
    }

    @Override
    protected ResultSet getCorrepondingResultTable(Project project, BMPType type, 
            String filter, boolean closeFirst) {
        ResultSet rs = null;
        
        if (closeFirst){
            switch (type)
            {
            case Small_Dams:
                resultSmallDam = null;
                break;
            case Holding_Ponds:
                resultHoldingPond = null;
                break;
            case Grazing:
                resultGrazing = null;
                break;
            case Grazing_Subbasin:
                resultGrazingSubbasin = null;
                break;
            case Tillage_Field:
            case Forage_Field:
                resultCropField = null;
                break;
            case Tillage_Farm:
            case Forage_Farm:
                resultCropFarm = null;
                break;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                resultCropSubbasin = null;
                break;
            }
        }
        switch (type)
        {
            case Small_Dams:
                rs = resultSmallDam;
                break;
            case Holding_Ponds:
                rs = resultHoldingPond;
                break;
            case Grazing:
                rs = resultGrazing;
                break;
            case Grazing_Subbasin:
                rs = resultGrazingSubbasin;
                break;
            case Tillage_Field:
            case Forage_Field:
                rs = resultCropField;
                break;
            case Tillage_Farm:
            case Forage_Farm:
                rs = resultCropFarm;
                break;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                rs = resultCropSubbasin;
                break;
        }

        if (rs != null) return rs;

        if (rs == null)
        {
            rs = Query.GetDataTable(
              "select * from " + ScenarioDatabaseStructure.BMPEconomicResultTableName(type), this.GetDatabasePath(), filter);

            switch (type)
            {
                case Small_Dams:
                    resultSmallDam = rs;
                    break;
                case Holding_Ponds:
                     resultHoldingPond = rs;
                    break;
                case Grazing:
                     resultGrazing = rs;
                    break;
                case Grazing_Subbasin:
                    resultGrazingSubbasin = rs;
                    break;
                case Tillage_Field:
                case Forage_Field:
                     resultCropField = rs;
                    break;
                case Tillage_Farm:
                case Forage_Farm:
                     resultCropFarm = rs;
                    break;
                case Tillage_Subbasin:
                case Forage_Subbasin:
                     resultCropSubbasin = rs;
                    break;
            }
        }

        return rs;
    }

    @Override
    protected void getOffSiteResults(Project project) {
        if (offSiteResults == null || offSiteResults.size() == 0)
            {
                createOffResults();
            try {
                calculateOffResults(project);
            } catch (Exception ex) {
                Logger.getLogger(ScenarioEconomicResult.class.getName()).log(Level.SEVERE, null, ex);
            }
            }    
    }
    
    private void createOffResults() {
        if (offSiteResults == null) offSiteResults = new HashMap<BMPType, Map<String, List<ResultDataPair>>>();
        createOffResults(BMPType.Small_Dams);
        createOffResults(BMPType.Holding_Ponds);
        createOffResults(BMPType.Grazing);
        createOffResults(BMPType.Tillage_Field);
    }
    
    private void createOffResults(BMPType type) {
        offSiteResults.put(type, new HashMap<String, List<ResultDataPair>>());
        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds ||
            type == BMPType.Grazing)
            offSiteResults.get(type).put("Cost", new ArrayList<ResultDataPair>());
        else
        {
            for (ResultDisplayTillageForageEconomicResultType i : ResultDisplayTillageForageEconomicResultType.values())
                offSiteResults.get(type).put(i.toString(), new ArrayList<ResultDataPair>());
        }
    }
    
    private void calculateOffResults(Project project) throws Exception {
        calculateOffResults(project,BMPType.Small_Dams);
        calculateOffResults(project, BMPType.Holding_Ponds);
        calculateOffResults(project, BMPType.Grazing);
        calculateOffResults(project, BMPType.Tillage_Field);
    }
    
    private void calculateOffResults(Project project,BMPType type) throws Exception {
        int startYear = StartYear();
        int endYear = EndYear();

        for(int year=startYear;year<=endYear;year++)
        {
            if (type == BMPType.Small_Dams ||
                type == BMPType.Holding_Ponds ||
                type == BMPType.Grazing)
                offSiteResults.get(type).get("Cost").add(
                    new ResultDataPair(year, getTotalCost(project,type, year, ResultDisplayTillageForageEconomicResultType.Cost)));
            else
            {
                offSiteResults.get(type).get(ResultDisplayTillageForageEconomicResultType.Yield.toString()).add(
                        new ResultDataPair(year, getTotalCost(project, type, year, ResultDisplayTillageForageEconomicResultType.Yield)));

                offSiteResults.get(type).get(ResultDisplayTillageForageEconomicResultType.Revenue.toString()).add(
                        new ResultDataPair(year, getTotalCost(project, type, year, ResultDisplayTillageForageEconomicResultType.Revenue)));

                offSiteResults.get(type).get(ResultDisplayTillageForageEconomicResultType.Cost.toString()).add(
                        new ResultDataPair(year, getTotalCost(project, type, year, ResultDisplayTillageForageEconomicResultType.Cost)));

                offSiteResults.get(type).get(ResultDisplayTillageForageEconomicResultType.net_return.toString()).add(
                        new ResultDataPair(year, getTotalCost(project, type, year, ResultDisplayTillageForageEconomicResultType.net_return)));
            }
        }
    }
    
    public List<ResultDataPair> GetOffSiteResults_TotalCost(Project project) throws Exception {
        if (offSiteResultsTotal != null) return offSiteResultsTotal;

        getOffSiteResults(project);

        offSiteResultsTotal = new ArrayList<ResultDataPair>();
        for (int year = StartYear(); year <= EndYear(); year++) //each year
        {
            double totalCost = 0.0;
            for (BMPType type : offSiteResults.keySet())
            {
                Map<String,List<ResultDataPair>> results = offSiteResults.get(type);
                if (type == BMPType.Small_Dams || type == BMPType.Holding_Ponds)
                    totalCost += results.get("Cost").get(year - StartYear()).getData(); //$
                else if (type == BMPType.Grazing || type == BMPType.Grazing_Subbasin)
                    totalCost += results.get("Cost").get(year - StartYear()).getData() * Spatial.GetShapeArea(BMPType.Grazing); //$/ha * ha
                else
                    totalCost += results.get("NetReturn").get(year - StartYear()).getData();// *project.Spatial.GetTotalArea(); //$/ha * ha, economic result is changed to $ from $/ha, 2012-12-22
            }
            offSiteResultsTotal.add(new ResultDataPair(year,totalCost));
        }
        return offSiteResultsTotal;
    }
    
    private double getTotalCost(Project project, BMPType type, int year, ResultDisplayTillageForageEconomicResultType columnType) throws Exception{
        ResultSet rs = getCorrepondingResultTable(null, type, "", true);
        if (type == BMPType.Small_Dams ||
            type == BMPType.Holding_Ponds)    //$
            return computeValueFromResultSet(rs, "sum(cost)", "year=" + String.valueOf(year));
        else                                //$/ha
        {
            if (columnType == ResultDisplayTillageForageEconomicResultType.BMPCost)
                throw new Exception("Don't support crop BMP Cost");

            ResultSet rs1 = getCorrepondingResultTable(null, type, " where year = " + String.valueOf(year), true);
            double totalCost = 0.0;
            double totalArea = 0.0;
            while (rs1.next())
            {
                RowItem item = new RowItem(rs1);
                int id = item.getColumnValue_Int(ScenarioDatabaseStructure.columnNameID);
                double cost = item.getColumnValue_Double(columnType.toString());

                double area = Spatial.GetShapeArea(type, id);
                totalCost += area * cost; //add all cost together based on unit cost
                totalArea += area;
            }
            //change unit from $/ha to $, 2012-12-22
            if (totalArea > 0) return totalCost; //return totalCost / totalArea;
            return 0.0;
        }
    }

}
