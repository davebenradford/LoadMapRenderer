/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Shao
 */
public class ScenarioIntegratedResult extends ScenarioResult{

    public ScenarioIntegratedResult(String scenarioDbPath) {
        super(scenarioDbPath);
    }

    private ScenarioSWATModel swatResult;
    private ScenarioEconomicResult economicResult;
    private double averageResult;
    
    public ScenarioSWATModel GetSWATResult(){
        if (swatResult == null)
            swatResult = new ScenarioSWATModel(this.GetDatabasePath());
        return swatResult;
    }
    public void SetSWATResult(ScenarioSWATModel value){
        swatResult = value;
    }
    
    public ScenarioEconomicResult GetEconomicResult(){
        if (economicResult == null)
            economicResult = new ScenarioEconomicResult(this.GetDatabasePath());
        return economicResult;
    }
    public void SetEconomicResult(ScenarioEconomicResult value){
        economicResult = value;
    }
    
    public double GetAverage(){
        return averageResult;
    }
    public void SetAverage(double value){
        averageResult = value;
    }    
    
    @Override
    protected ResultSet getStartEndYearTable() {
        return null;
    }
    
    @Override
    protected void getStartEndYear() throws Exception{
        startYear = GetSWATResult().StartYear();
        endYear = GetSWATResult().EndYear();
    }

    @Override
    public void Update() {
        GetSWATResult().Update();
        GetEconomicResult().Update();
    }
    
    private double getIntegratedResult(double swat, double economic, boolean swatWaterUnitConversion){
        if (Math.abs(economic) > 1)
        {
            double convert = 1.0;
            if (swatWaterUnitConversion) convert = 10.0; //mm.ha/$1000 --> m^3/$1000
            return swat / economic * 1000.0 * convert;
        }
        else
            return 0.0;    
    }
    
    private List<ResultDataPair> getIntegratedResult(List<ResultDataPair> swat, List<ResultDataPair> economic, boolean swatWaterUnitConversion){ // !! out
        List<ResultDataPair> results = new ArrayList<ResultDataPair>();
        double totalSWAT = 0.0;
        double totalEconomic = 0.0;
        for (int i = 0; i < swat.size(); i++)
        {
            if (swat.size() == economic.size()){
                results.add(
                    new ResultDataPair(swat.get(i).getYear(), getIntegratedResult(swat.get(i).getData(), economic.get(i).getData(), swatWaterUnitConversion)));
                totalSWAT += swat.get(i).getData();
                totalEconomic += economic.get(i).getData();
            }                
            else
                results.add(new ResultDataPair(swat.get(i).getYear(), 0.0));
        }
        if (swat.size() == economic.size())
            SetAverage(getIntegratedResult(totalSWAT, totalEconomic, swatWaterUnitConversion));
        else
            SetAverage(0.0);
        return results;
    }    
    
    private double getIntegratedResult_Average(List<ResultDataPair> swat, List<ResultDataPair> economic, boolean swatWaterUnitConversion){  // !! out average
        double average = 0.0;
        double totalSWAT = 0.0;
        double totalEconomic = 0.0;
        for (int i = 0; i < swat.size(); i++)
        {
            if (swat.size() == economic.size())
            {                
                totalSWAT += swat.get(i).getData();
                totalEconomic += economic.get(i).getData();
            }
        }

        if (swat.size() == economic.size())
            average = getIntegratedResult(totalSWAT, totalEconomic, swatWaterUnitConversion);
        else
            average = 0.0;

        return average;
    }

    @Override
    public double GetAverageResult(Project project, BMPType type, String resultColumn, int id, int startYear, int endYear, Scenario compareScenario, boolean reservoirGotoReach) throws Exception{
        SWATResultColumnType swatColumnType = asSWATResultColumnType(resultColumn);
        ResultDisplayTillageForageEconomicResultType economicColumnType = asResultDisplayTillageForageEconomicResultType(resultColumn);
        if (swatColumnType != null)
            return GetSWATResult().GetAverageResult(project, type, swatColumnType.toString(), id, startYear, endYear, compareScenario, reservoirGotoReach);
        if (economicColumnType != null)
            return GetEconomicResult().GetAverageResult(project, type, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);
        throw new Exception("Wrong result column!");

    }
    
    public double GetAverageResult(Project project, BMPType type,
            SWATResultColumnType swatColumnType,
            ResultDisplayTillageForageEconomicResultType economicColumnType, 
            int id, int startYear, int endYear, Scenario compareScenario) throws Exception{
        double swat = GetSWATResult().GetAverageResult(project, type, swatColumnType.toString(), id, startYear, endYear, compareScenario, false);
        if (type == BMPType.Small_Dams || type == BMPType.Holding_Ponds || type == BMPType.Grazing)
            economicColumnType = ResultDisplayTillageForageEconomicResultType.Cost;
        double economic = GetEconomicResult().GetAverageResult(project, type, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);

        return getIntegratedResult(swat, economic,
            type != BMPType.Small_Dams && type != BMPType.Holding_Ponds && swatColumnType == SWATResultColumnType.water);
    }

    public double GetAverageResult_Integrated(
        Project project, Scenario mainScenario, Scenario compareScenario, BMPType bmpType, SWATResultColumnType swatColumnType,
        int id, int startYear, int endYear) throws Exception{
        if (project == null)
            throw new Exception("Proejct is null.");

        if (compareScenario == null)
            throw new Exception("on-site intergrated results must have a compare scenario.");

        String economicResultColumn = ResultDisplayTillageForageEconomicResultType.net_return.toString();
        if(!BMPItem.IsCropBMP(bmpType)) 
            economicResultColumn = ResultDisplayTillageForageEconomicResultType.Cost.toString();


        if(isCropBMPResultsZero(project,mainScenario,compareScenario,bmpType,id)) return 0.0;

        //double swat = swatResult.GetAverageResult(project, bmpType, swatColumnType.ToString(), id,startYear,endYear);
        double bmpCost = GetEconomicResult().GetAverageResult(project, bmpType,
            economicResultColumn, id, startYear, endYear, null, false);

        //double swat_compare = compareScenario.Result.SWATResult.GetAverageResult(project, bmpType, swatColumnType.ToString(), id, startYear, endYear);
        double bmpCost_compare = compareScenario.getResult().GetAverageResult(project, bmpType,
            economicResultColumn, id, startYear, endYear, null, false);

        double swat_reduction = //swat_compare - swat;
            -GetSWATResult().GetAverageResult(project, bmpType, swatColumnType.toString(), id, startYear, endYear, compareScenario, false);
        double bmpCost_different = bmpCost - bmpCost_compare;

        return getIntegratedResult(swat_reduction, bmpCost_different,
            bmpType != BMPType.Small_Dams && bmpType != BMPType.Holding_Ponds && swatColumnType == SWATResultColumnType.water);
    }

    private boolean isCropBMPResultsZero(Project project, Scenario mainScenario, Scenario compareScenario, BMPType bmpType, int id) throws Exception{
        //just for crop bmps
        if (compareScenario != null && id >= 0 && BMPItem.IsCropBMP(bmpType))    //see if the id is selected to do in current and compare BMPs
        {
            //if (bmpType == BMPType.Tillage_Field && (id <= 0 || id >= 600)) return true;
            if (bmpType == BMPType.Tillage_Farm && id <= 0) return true;

            //no tillage and forage bmps
            if (compareScenario.getScenarioType() == ScenarioType.Normal && compareScenario.getCropBMPLevel() == BMPSelectionLevelType.Unknown)
                return true;

            //have tillage or forage bmps
            //and don't have tillage and forage bmps on current id
            BMPType forageType = BMPType.Forage_Field;
            if (bmpType == BMPType.Tillage_Farm) forageType = BMPType.Forage_Farm;
            else if (bmpType == BMPType.Tillage_Subbasin) forageType = BMPType.Forage_Subbasin;

            if (!mainScenario.getScenarioDesign().IsInDesign(mainScenario, bmpType, id) &&
                !compareScenario.getScenarioDesign().IsInDesign(mainScenario, bmpType, id) &&
                !mainScenario.getScenarioDesign().IsInDesign(mainScenario, forageType, id) &&
                !compareScenario.getScenarioDesign().IsInDesign(mainScenario, forageType, id))
            {
                return true;
            }
        }
        return false;
    }

    public double GetAverageResult(Project project, Scenario mainScenario,
        int id,
        ResultDisplayScenarioCompareType compareType, Scenario compareScenario, //if compare with other scenario
        ResultDisplayResultLevelType resultLevelType,                           //on-site or off-site
        BMPType bmpType,                                                        //bmp type
        ResultDisplayResultType resultType,                                     //swat, economic or integrated
        SWATResultColumnType swatColumnType,                                    //swat columns
        ResultDisplayTillageForageEconomicResultType economicColumnType,        //economic columns
        int startYear, int endYear) throws Exception{                                             //time period
        //debug information
        System.out.println(
            String.format("GetAverageResult -- id: %d, compare type: %s, result level type: %s, bmp type: %s, result type: %s, swat type: %s, economic type: %s, start year: %d, end year: %d",
            id,compareType,resultLevelType,bmpType,resultType,swatColumnType,economicColumnType,startYear, endYear));
        
        //set compareScenario to null if NoCompare is selected
        if (compareType == ResultDisplayScenarioCompareType.NoCompare) 
            compareScenario = null;

        //try to get compare scenario
        if (compareType == ResultDisplayScenarioCompareType.Compare && compareScenario == null)
        {
            compareScenario = mainScenario.getBaseScenario(project);
            if (compareScenario == null)
                throw new Exception("Please create base scenario first.");
        }

        //for off-site result, the id is unuseful
        if (resultLevelType == ResultDisplayResultLevelType.OffSite)
        {
            id = -1;
            if (bmpType == BMPType.Tillage_Farm || bmpType == BMPType.Tillage_Subbasin)
                bmpType = BMPType.Tillage_Field;
        }

        //get results
        //if the current id is not selected in design, the coparison result is all 0
        //2012-12-24
        if (isCropBMPResultsZero(project, mainScenario, compareScenario, bmpType, id)) return 0.0;
        else
        {
            if (resultType == ResultDisplayResultType.SWAT)             //swat results
                return GetSWATResult().GetAverageResult(project, bmpType, swatColumnType.toString(), id, startYear, endYear,compareScenario, false);
            else if (resultType == ResultDisplayResultType.Economic)    //economic results
            {
                if (bmpType == BMPType.Small_Dams || bmpType == BMPType.Holding_Ponds || bmpType == BMPType.Grazing)
                    economicColumnType = ResultDisplayTillageForageEconomicResultType.Cost;
                return GetEconomicResult().GetAverageResult(project, bmpType, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);
            }
            else
                //unit should be considered here
                return GetAverageResult(project, bmpType, swatColumnType, economicColumnType, id, startYear, endYear, compareScenario);
        }
    }

    
    @Override
    protected ResultSet getCorrepondingResultTable(Project project, BMPType type,
            String filter, boolean closeFirst) {
        return null;
    }

    
    @Override
    public List<ResultDataPair> GetResults(Project project, BMPType type, String resultColumn, int id, int startYear, int endYear, Scenario compareScenario, boolean reservoirGotoReach) throws Exception{
        //do nothing
        //throw new Exception("Please don't call this function.");
        SWATResultColumnType swatColumnType = asSWATResultColumnType(resultColumn);
        ResultDisplayTillageForageEconomicResultType economicColumnType = asResultDisplayTillageForageEconomicResultType(resultColumn);
        if (swatColumnType != null) return GetSWATResult().GetResults(project, type, swatColumnType.toString(), id, startYear, endYear, compareScenario, reservoirGotoReach);
        if (economicColumnType != null) return GetEconomicResult().GetResults(project, type, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);
        throw new Exception("Wrong result column!");
    }

    public List<ResultDataPair> GetResults(Project project, BMPType type,
            SWATResultColumnType swatColumnType,
            ResultDisplayTillageForageEconomicResultType economicColumnType, 
            int id, int startYear, int endYear, Scenario compareScenario) throws Exception{            
            List<ResultDataPair> swat = GetSWATResult().GetResults(project, type, swatColumnType.toString(), id, startYear, endYear, compareScenario, false);
            if (type == BMPType.Small_Dams || type == BMPType.Holding_Ponds || type == BMPType.Grazing)
                economicColumnType = ResultDisplayTillageForageEconomicResultType.Cost;
            List<ResultDataPair> economic = GetEconomicResult().GetResults(project, type, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);

            //if economic is empty, means no bmp
            if (swat.size() != economic.size())
                throw new Exception("Number of results of SWAT and economic should be same.");

            return getIntegratedResult(swat, economic,
                type != BMPType.Small_Dams && type != BMPType.Holding_Ponds && swatColumnType == SWATResultColumnType.water);
        }
    
    public List<ResultDataPair> GetResults(Project project, Scenario mainScenario,
            int id,
            ResultDisplayScenarioCompareType compareType, Scenario compareScenario, //if compare with other scenario
            ResultDisplayResultLevelType resultLevelType,                           //on-site or off-site
            BMPType bmpType,                                                        //bmp type
            ResultDisplayResultType resultType,                                     //swat, economic or integrated
            SWATResultColumnType swatColumnType,                                    //swat columns
            ResultDisplayTillageForageEconomicResultType economicColumnType,        //economic columns
            int startYear, int endYear) throws Exception{                                             //time period
        //debug information
        System.out.println(
            String.format("GetResults -- id: %d, compare type: %s, result level type: %s, bmp type: %s, result type: %s, swat type: %s, economic type: %s, start year: %d, end year: %d",
            id, compareType, resultLevelType, bmpType, resultType, swatColumnType, economicColumnType, startYear, endYear));


        //try to get compare scenario
        if (compareType == ResultDisplayScenarioCompareType.Compare && compareScenario == null)
        {
            compareScenario = mainScenario.getBaseScenario(project);
            if (compareScenario == null)
                throw new Exception("Please create base scenario first.");
        }

        //for off-site result, the id is unuseful
        if (resultLevelType == ResultDisplayResultLevelType.OffSite)
        {
            id = -1;
            if (bmpType == BMPType.Tillage_Farm || bmpType == BMPType.Tillage_Subbasin
            || bmpType == BMPType.Forage_Farm || bmpType == BMPType.Forage_Subbasin
            || bmpType == BMPType.Forage_Field)
                bmpType = BMPType.Tillage_Field;

            if (resultType == ResultDisplayResultType.SWAT)
                return GetOffSiteResults_SWAT(project,mainScenario, compareScenario, swatColumnType);
            else if (resultType == ResultDisplayResultType.Economic)
                return GetOffSiteResults_Economic(project, compareScenario, bmpType, economicColumnType);
            else
            {                
                return GetOffSiteResults_Intergrated(project, mainScenario, compareScenario, swatColumnType);
            }
        }

        //change column type if necessary
        if (bmpType == BMPType.Small_Dams || bmpType == BMPType.Holding_Ponds || bmpType == BMPType.Grazing)
            economicColumnType = ResultDisplayTillageForageEconomicResultType.Cost;

        //get on-site results
        //if the current id is not selected in design, the coparison result is all 0
        //2012-12-24
        if (isCropBMPResultsZero(project, mainScenario, compareScenario, bmpType, id))             
            return getZeroResults();
        else
        {
            if (resultType == ResultDisplayResultType.SWAT)             //swat results~                
                return GetSWATResult().GetResults(project, bmpType, swatColumnType.toString(), id, startYear, endYear, compareScenario, false);
            else if (resultType == ResultDisplayResultType.Economic)    //economic results
                return GetEconomicResult().GetResults(project, bmpType, economicColumnType.toString(), id, startYear, endYear, compareScenario, false);
            else
            {
                return GetResults_Integrated(project,mainScenario, compareScenario, bmpType, swatColumnType, id, startYear, endYear);
            }
        }
    }
    
    private static List<ResultDataPair> getZeroResults() {
        List<ResultDataPair> _zeroResults = new ArrayList<ResultDataPair>();
        _zeroResults.add(new ResultDataPair(0, 0.0));
        return _zeroResults;
    }
    
    public List<ResultDataPair> GetResults_Integrated(
    Project project, Scenario mainScenario, Scenario compareScenario, BMPType bmpType, SWATResultColumnType swatColumnType,
    int id, int startYear, int endYear) throws Exception { // !! out
        if (project == null)
            throw new Exception("Proejct is null.");

        if (compareScenario == null)
            throw new Exception("on-site intergrated results must have a compare scenario.");

        if (isCropBMPResultsZero(project, mainScenario, compareScenario, bmpType, id))
        {
            return getZeroResults();
        }

        String economicResultColumn = ResultDisplayTillageForageEconomicResultType.net_return.toString();
        if (!BMPItem.IsCropBMP(bmpType))
            economicResultColumn = ResultDisplayTillageForageEconomicResultType.Cost.toString();

        //List<ResultDataPair> swat = swatResult.GetResults(project, bmpType, swatColumnType.ToString(), id, startYear, endYear);
        List<ResultDataPair> bmpCost = GetEconomicResult().GetResults(project, bmpType,
            economicResultColumn, id, startYear, endYear, null, false);

        //List<ResultDataPair> swat_compare = compareScenario.Result.SWATResult.GetResults(project, bmpType, swatColumnType.ToString(), id, startYear, endYear);
        List<ResultDataPair> bmpCost_compare = compareScenario.getResult().GetResults(project, bmpType,
            economicResultColumn, id, startYear, endYear, null, false);

        List<ResultDataPair> swat_reduction = //this.getDifference(swat_compare,swat);
            GetSWATResult().GetResults(project, bmpType, swatColumnType.toString(), id, startYear, endYear, compareScenario, false);
        for (ResultDataPair r : swat_reduction)
            r.setData(-r.getData());
        List<ResultDataPair> bmpCost_different = this.getDifference(bmpCost,bmpCost_compare);

        return getIntegratedResult(swat_reduction, bmpCost_different,
            bmpType != BMPType.Small_Dams && bmpType != BMPType.Holding_Ponds && swatColumnType == SWATResultColumnType.water);   // !! out
    }
    
    private void debugOutputChartData(String name, List<ResultDataPair> data) {
        System.out.println("-----------------");
        System.out.println(name);
        for (ResultDataPair r : data)
        {
            System.out.println(String.format("%d,%f",r.getYear(),r.getData()));
        }
        System.out.println("-----------------");
    }
    
    @Override
    protected void getOffSiteResults(Project project) {
        // do nothing;
    }
    
    @Override
    protected List<ResultDataPair> getOffSiteResults(
        Project project,BMPType type, String resultColumn, 
        int startYear, int endYear, Scenario compareScenario) throws Exception{
        startYear = -1;
        endYear = -1;
        compareScenario = null;
        //do nothing
        throw new Exception("Don't call this function."); 
    }
    
    public List<ResultDataPair> GetOffSiteResults_SWAT(
           Project project, Scenario mainScenario, Scenario compareScenario, SWATResultColumnType swatColumnType) throws Exception{
        List<ResultDataPair> swat = this.GetSWATResult().GetResults(project, BMPType.Small_Dams, swatColumnType.toString(), -1, -1, -1, null, false);

        if (compareScenario == null) return swat;

        List<ResultDataPair> swat_base = compareScenario.getResult().GetSWATResult().GetResults(project, BMPType.Small_Dams, swatColumnType.toString(),  -1, -1, -1, null, false);

        List<ResultDataPair> difference = getDifference(swat, swat_base);

        //try to get all selected field difference, 2012-12-24
        List<ResultDataPair> results = new ArrayList<ResultDataPair>();
        List<Integer> fields = Spatial.GetFields();
        for (int field : fields)
        {
            List<ResultDataPair> oneFieldResults = 
                        mainScenario.getResult().GetResults(
                        project, mainScenario, field, ResultDisplayScenarioCompareType.Compare,
                        compareScenario, ResultDisplayResultLevelType.OnSite, BMPType.Tillage_Field
                        , ResultDisplayResultType.SWAT, swatColumnType, ResultDisplayTillageForageEconomicResultType.Cost, this.StartYear(), this.EndYear());
            if (oneFieldResults.size() == 1 && oneFieldResults.get(0).getData() == 0.0) continue;

            //get field area
            double area = Spatial.GetShapeArea(ResultLevelType.Field, field);
            for (ResultDataPair r : oneFieldResults)
                r.setData(r.getData()*area);     //multiple area to change kg/ha to kg

            //add results together
            results = addResults(results,oneFieldResults);
        }

        if (results.size() == 0) return difference;
        if (difference.size() != results.size()) return difference;
        for (int i = 0; i < difference.size(); i++)
            if (Math.abs(difference.get(i).getData()) > Math.abs(results.get(i).getData()))
                difference.get(i).setData(results.get(i).getData());       //if reach difference is bigger than all fields difference, use fields difference, 2012-12-24
        return difference;
    }
     
    private List<ResultDataPair> addResults(List<ResultDataPair> results1, List<ResultDataPair> results2){
        if (results1.size() != 0 && results1.size() != results2.size()) return results1;
        if (results1.size() == 0 && results2.size() == 1) return results1;
        if (results1.size() == 0)
        {
            for (ResultDataPair r : results2)
                results1.add(r);
            return results1;
        }
        for (int i = 0; i < results1.size(); i++)
            results1.get(i).setData(results1.get(i).getData() + results2.get(i).getData());
        return results1;
    }
     
    public List<ResultDataPair> GetOffSiteResults_Economic(
        Project project, Scenario compareScenario, BMPType type,
        ResultDisplayTillageForageEconomicResultType economicResult) throws Exception{
        if (!BMPItem.IsCropBMP(type))
            economicResult = ResultDisplayTillageForageEconomicResultType.Cost;

        List<ResultDataPair> eco = 
            this.GetEconomicResult().GetResults(project, type, economicResult.toString(), -1, -1, -1, null, false);

        if (compareScenario == null) return eco;

        List<ResultDataPair> eco_base =
            compareScenario.getResult().GetEconomicResult().GetResults(project, type, economicResult.toString(), -1, -1, -1, null, false);
        return getDifference(eco, eco_base);
    }


    public List<ResultDataPair> GetOffSiteResults_Intergrated(
        Project project, Scenario mainScenario, Scenario compareScenario, SWATResultColumnType swatColumnType) throws Exception // !! out
    {
        if (project == null)
            throw new Exception("Proejct is null.");

        if (compareScenario == null)
            throw new Exception("Off-site intergrated results must have a compare scenario.");

        //List<ResultDataPair> swat = swatResult.GetResults(project, BMPType.Small_Dams, swatColumnType.ToString(), -1);
        List<ResultDataPair> totalCost = this.GetEconomicResult().GetOffSiteResults_TotalCost(project);

        //List<ResultDataPair> swat_base = compareScenario.Result.SWATResult.GetResults(project, BMPType.Small_Dams, swatColumnType.ToString(), -1);
        List<ResultDataPair> totalpCost_base = compareScenario.getResult().GetEconomicResult().GetOffSiteResults_TotalCost(project);

        List<ResultDataPair> swat_reduction = //getDifference(swat_base, swat);
            GetOffSiteResults_SWAT(project, mainScenario, compareScenario, swatColumnType);
        for (ResultDataPair r : swat_reduction)
            r.setData(-r.getData());

        List<ResultDataPair> totalCost_different = getDifference(totalCost, totalpCost_base);

        return getIntegratedResult(swat_reduction, totalCost_different, false); // !! out
    }
    
    protected boolean hasResult = false;
    
    public boolean GetHasResult(){
        return hasResult;
    }
    public void SetHasResult(boolean value){
        hasResult = value;
    }
}
