/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Shao
 */
public class ResultDisplayFrame extends JFrame implements ActionListener,ChangeListener{
    private ResultDisplayMap displayMap = new ResultDisplayMap();
    private ResultDisplayChart displayChart = new ResultDisplayChart();
    private JPanel displayControl = this.BuildResultsPanel();
    private JSplitPane mapnChart = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JSplitPane resultDisplay = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    
    private Scenario mainScenario;
    private Scenario compareScenario = null;
    private Project project;
    private ResultDisplayResultLevelType resultLevelType;
    private ResultDisplayScenarioCompareType compareType;
    private BMPType bmpType;
    private ResultDisplayResultType resultType;
    private SWATResultColumnType SWATType;
    private ResultDisplayTillageForageEconomicResultType economicType;
    private int startYear;
    private int endYear;    
    private int featureID = 1;
    
    private List<ResultDataPair> chartResults;
    private Map<Integer, Double> fieldResults;
    private Map<Integer, Double> farmResults;
    private Map<Integer, Double> subbasinResults;
    private Map<Integer, Double> smallDamResults;
    private Map<Integer, Double> holdingPondResults;
    private Map<Integer, Double> gzgResults;
    private Map<Integer, Double> gzgSubResults;
    
    private static final Font f = new Font("Sans_Serif", Font.PLAIN, 12);
    
    public ResultDisplayFrame(Project p, Scenario value) throws Exception{
        if (value == null) return;
        this.SetMainScenario(value, false);
        this.SetCompareType(ResultDisplayScenarioCompareType.NoCompare, false);
        this.SetBMPType(BMPType.Tillage_Field, false);
        this.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
        this.SetEndYear(GetMainScenario().GetEndYear(), false);
        this.SetStartYear(GetMainScenario().GetStartYear(), false);
        this.SetResultLevel(ResultDisplayResultLevelType.OnSite, false);
        this.SetResultType(ResultDisplayResultType.SWAT, false);
        this.SetSWATType(SWATResultColumnType.sediment, false);
        this.SetProject(p);
        
        mapnChart.add(displayMap);
        mapnChart.add(displayChart);
        resultDisplay.add(GetDisplayControl());      
        resultDisplay.add(mapnChart);
        
        displayMap.setSize(200,500);
        
        this.add(resultDisplay);
        updateMap();
        updateChart();
    }
    
    public ResultDisplayMap GetDisplayMap(){
        return displayMap;
    }    
    public ResultDisplayChart GetDisplayChart(){
        return displayChart;
    }    
    public JPanel GetDisplayControl(){
        if (displayControl == null) displayControl = this.BuildResultsPanel();
        return displayControl;
    }
    
    public Scenario GetMainScenario(){
        if (mainScenario == null) return new Scenario();
        return mainScenario;
    }
    public void SetMainScenario(Scenario value, boolean updateFlag) throws Exception{
        if(value == null) return;
        mainScenario = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }        
    }
    
    public Scenario GetCompareScenario(){
        return compareScenario;
    }
    public void SetCompareScenario(Scenario value, boolean updateFlag) throws Exception{
        if(value == null) return;
        compareScenario = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }      
    }
    
    public Project GetProject(){
        return project;
    }
    public void SetProject(Project value){
        project = value;
    }
    
    public ResultDisplayResultLevelType GetResultLevel(){
        return resultLevelType;
    }
    public void SetResultLevel(ResultDisplayResultLevelType value, boolean updateFlag) throws Exception{
        resultLevelType = value;
        if (updateFlag){
            updateMap();
            updateChart();
            if (resultLevelType == ResultDisplayResultLevelType.OffSite)
                updateChartYear();
        }      
    }
    
    public ResultDisplayScenarioCompareType GetCompareType(){
        return compareType;
    }
    public void SetCompareType(ResultDisplayScenarioCompareType value, boolean updateFlag) throws Exception{
        compareType = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }
    }
    
    public int GetFeatureID(){
        return featureID;
    }
    public void SetFeatureID(int value, boolean updateFlag) throws Exception{
        featureID = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }      
    }
    
    
    public BMPType GetBMPType(){
        return bmpType;
    }
    public void SetBMPType(BMPType value, boolean updateFlag) throws Exception{
        bmpType = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }      
    }
    
    public ResultDisplayResultType GetResultType(){
        return resultType;
    }
    public void SetResultType(ResultDisplayResultType value, boolean updateFlag) throws Exception{
        resultType = value;
        if (updateFlag){            
            updateMap();
            updateChart();
        }      
    }
    
    public SWATResultColumnType GetSWATType(){
        return SWATType;
    }
    public void SetSWATType(SWATResultColumnType value, boolean updateFlag) throws Exception{
        SWATType = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }      
    }
    
    public ResultDisplayTillageForageEconomicResultType GetEconomicType(){
        return economicType;
    }
    public void SetEconomicType(ResultDisplayTillageForageEconomicResultType value, boolean updateFlag) throws Exception{
        economicType = value;
        if (updateFlag){
            updateMap();
            updateChart();
        }      
    }
    
    public int GetStartYear(){
        return startYear;
    }
    public void SetStartYear(int value, boolean updateFlag) throws Exception{
        startYear = value;
        if (updateFlag){
            updateMap();
            updateChartYear();
        }      
    }
    
    public int GetEndYear(){
        return endYear;
    }
    public void SetEndYear(int value, boolean updateFlag) throws Exception{
        endYear = value;
        if (updateFlag){
            updateMap();
            updateChartYear();
        }      
    }  
        
    private void updateMap() throws Exception{
        if(true) return;
        //updateLayer(BMPType.Small_Dam);
        //updateLayer(BMPType.Holding_Pond);
        updateLayer(BMPType.Grazing);
        updateLayer(BMPType.Grazing_Subbasin);
        switch (GetBMPType())
        {
            case Tillage_Field:
            case Forage_Field:
                updateLayer(BMPType.Tillage_Field);
                break;
            case Tillage_Farm:
            case Forage_Farm:
                updateLayer(BMPType.Tillage_Farm);
            case Tillage_Subbasin:
            case Forage_Subbasin:
                updateLayer(BMPType.Tillage_Subbasin);
        }
    }
    
    private void updateLayer(BMPType type) throws Exception {
        List<Integer> ids = new ArrayList();
        switch (type)
        {
            case Small_Dam:
                ids = null;
                break;
            case Holding_Pond:
                ids = null;
                break;
            case Grazing:
                ids = Spatial.GetGrazings();
                break;
            case Grazing_Subbasin:
                ids = Spatial.GetGrazingSubs();
                break;
            case Tillage_Field:
            case Forage_Field:
                ids = Spatial.GetFields();
                break;
            case Tillage_Farm:
            case Forage_Farm:
                ids = Spatial.GetFarms();
                break;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                ids = Spatial.GetSubbasins();
                break;
        }
        Map<Integer, Double> mapResults = new HashMap();
        for (int id : ids){
            Double result = GetMainScenario().GetResult().GetAverageResult(
                    GetProject(), GetMainScenario(), id, GetCompareType(),
                    GetCompareScenario(), GetResultLevel(), GetBMPType(), GetResultType(),
                    GetSWATType(), GetEconomicType(), GetStartYear(), GetEndYear());
            mapResults.put(id, result);
        }
        switch (type)
        {
            case Small_Dam:
                smallDamResults = new HashMap();
                smallDamResults = mapResults;
                break;
            case Holding_Pond:
                holdingPondResults = new HashMap();
                holdingPondResults = mapResults;
                break;
            case Grazing:
                holdingPondResults = new HashMap();
                holdingPondResults = mapResults;
                break;
            case Grazing_Subbasin:
                holdingPondResults = new HashMap();
                holdingPondResults = mapResults;
                break;
            case Tillage_Field:
            case Forage_Field:
                fieldResults = new HashMap();
                fieldResults = mapResults;
                break;
            case Tillage_Farm:
            case Forage_Farm:
                farmResults = new HashMap();
                farmResults = mapResults;
                break;
            case Tillage_Subbasin:
            case Forage_Subbasin:
                subbasinResults = new HashMap();
                subbasinResults = mapResults;
                break;
        }
        
        Map<Integer, Interval> intervals = Query.GetQuantileIntervals(
                Arrays.asList(mapResults.values().toArray(new Double[mapResults.size()])), 5);
        
        for (int id : mapResults.keySet())
            System.out.println(String.format("Layer %s: ID #%d result is %f, "
                    + "and belongs to interval #%d", type.toString(), id, mapResults.get(id), 
                    Query.CheckInterval(intervals, mapResults.get(id))));
    }
    
    /**
     * This method update the chart based user setting.
     * @throws Exception 
     */
    private void updateChart() throws Exception{
        chartResults = GetMainScenario().GetResult().GetResults(
                    GetProject(), GetMainScenario(), GetFeatureID(), GetCompareType(),
                    GetCompareScenario(), GetResultLevel(), GetBMPType(), GetResultType(),
                    GetSWATType(), GetEconomicType(), GetStartYear(), GetEndYear());
        displayChart.DrawChart(chartResults, getChartTitle(), getDataTitle(), -99.0, false);                
        mapnChart.remove(displayChart);
        mapnChart.add(displayChart);
    }
    
    /**
     * This method update the chart based on the newly specified period.
     * @throws Exception 
     */
    private void updateChartYear() throws Exception{
        List<ResultDataPair> newChartResults = new ArrayList();
        int max = chartResults.get(0).getYear();
        int min = chartResults.get(0).getYear();
        for (ResultDataPair r : chartResults){
            if (r.getYear() > max) max = r.getYear();
            if (r.getYear() < min) min = r.getYear();
        }
        
        // If the old chart results don't include the set year period, calculat
        // the chart result again.
        if (min > GetStartYear() || max < GetEndYear()) {
            updateChart();
            return;
        }       
        
        // If the old chart results include the set year period, no calculation 
        // is needed.
        for (int i = 0; i < chartResults.size(); i++){
            if (chartResults.get(i).getYear() < GetStartYear() || 
                    chartResults.get(i).getYear() > GetEndYear()) {
                continue;
            }
            newChartResults.add(chartResults.get(i));
        }
        displayChart.DrawChart(newChartResults, getChartTitle(), 
                getDataTitle(), -99.0, false);
        mapnChart.remove(displayChart);
        mapnChart.add(displayChart);
    }
    
    /**
     * Currently, no chart title to display
     * @return 
     */
    private String getChartTitle(){        
        return "";
    }
    
    /** 
    * !! Shao.H This method update the legend unit
    *   (1) for SWAT result on site
    *       a. for water
    *           i. small dam and holding pond: unit as "m^3"
    *          ii. others: unit as "mm"
    *       b. for sediment
    *           i. small dam and holding pond: unit as "t"
    *          ii. others: unit as "t/ha"
    *       c. for others
    *           i. small dam and holding pond: unit as "kg"
    *          ii. others: unit as "kg/ha"
    *   (2) for SWAT result off site
    *       a. for water: unit as "m^3"
    *       b. for sediment: unit as "t"
    *       c. for others: unit as "kg"
    *   (3) for economic result
    *       a. for small dam and holding pond: all type units are "$"
    *       b. for other BMP type
    *           i. for on site: Yield unit as "kg/ha", others unit as "$/ha"
    *          ii. for off site grazing: Yield unit as "kg/ha", others unit as "$/ha"
    *         iii. for off site other BMP: Yield unit as "kg", others unit as "$"
    *   (4) for integrated result
    *       a. for water: units as "m^3/$1000"
    *       b. for sediment: units as "t/$1000"
    *       c. for others: units as "kg/$1000"
    * @author Shao
    */
    private String getDataTitle(){
        //get lengend item first
        //if (layer.LegendItems.Count() == 0) return;
        //ILegendItem item =  layer.LegendItems.ElementAt(0);

        ResultDisplayTillageForageEconomicResultType economicType = GetEconomicType();
        if (GetBMPType() == BMPType.Small_Dam || GetBMPType() == BMPType.Holding_Pond 
                || GetBMPType() == BMPType.Grazing)
            economicType = ResultDisplayTillageForageEconomicResultType.Cost;

        if (GetResultType() == ResultDisplayResultType.SWAT)
            return resultNameUnit_SWAT(GetResultLevel(), GetBMPType(), GetSWATType());
        else if (GetResultType() == ResultDisplayResultType.Economic)
            return resultNameUnit_Economic(GetResultLevel(), GetBMPType(), economicType);
        else
            return resultNameUnit_Integrated(GetBMPType(), GetSWATType(), economicType);
    }
    
    /** 
    * !! Shao.H This method update the SWAT result legend unit, see introduction
    * of updateLayerResultColumnName() for more detail
    * @author Shao
    */
    private static String resultNameUnit_SWAT(ResultDisplayResultLevelType resultLevelType,
        BMPType type, SWATResultColumnType swatResultType){
        String name = swatResultType.toString();
        if (swatResultType == SWATResultColumnType.water)
            name = "Water yield";
        if (swatResultType == SWATResultColumnType.sediment)
            name = "Sediment yield";

        String unit = "";
        if (resultLevelType == ResultDisplayResultLevelType.OnSite)
        {
            switch (swatResultType)
            {
                case water:
                    if (type == BMPType.Small_Dam ||
                        type == BMPType.Holding_Pond)
                        unit = "m^3";
                    else
                        unit = "mm";
                    break;
                case sediment:
                    if (type == BMPType.Small_Dam ||
                        type == BMPType.Holding_Pond)
                        unit = "t";
                    else
                        unit = "t/ha";
                    break;
                default:
                    if (type == BMPType.Small_Dam ||
                        type == BMPType.Holding_Pond)
                        unit = "kg";
                    else
                        unit = "kg/ha";
                    break;
            }
        }
        else
        {
            if (swatResultType == SWATResultColumnType.water) unit = "m^3";
            else if (swatResultType == SWATResultColumnType.sediment) unit = "t";
            else unit = "kg";
        }

        return String.format("%s(%s)",name,unit);
    }

    /** 
    * !! Shao.H This method update the economic result legend unit, see introduction
    * of updateLayerResultColumnName() for more detail
    * @author Shao
    */
    private static String resultNameUnit_Economic(
        ResultDisplayResultLevelType resultLevelType,
        BMPType type, ResultDisplayTillageForageEconomicResultType economicResultType){
        if (!BMPItem.IsCropBMP(type))
            economicResultType = ResultDisplayTillageForageEconomicResultType.Cost;

        String name = economicResultType.toString();
        String unit = "";
        switch (type)
        {
            case Small_Dam:
            case Holding_Pond:
                unit = "$";
                break;
            default:
                if (resultLevelType == ResultDisplayResultLevelType.OffSite &&
                    BMPItem.IsCropBMP(type))
                {
                    if (economicResultType != ResultDisplayTillageForageEconomicResultType.Yield)
                        unit = "$";
                    else
                        unit = "kg";
                }
                else if(economicResultType == ResultDisplayTillageForageEconomicResultType.Yield)
                    unit = "kg/ha";
                else
                    unit = "$/ha";
                break;
        }
        return String.format("%s(%s)", name, unit);
    }

    /** 
    * !! Shao.H This method update the integrated result legend unit, see introduction
    * of updateLayerResultColumnName() for more detail
    * @author Shao
    */
    private static String resultNameUnit_Integrated(BMPType type, 
        SWATResultColumnType swatResultType,
        ResultDisplayTillageForageEconomicResultType economicResultType){
        if (BMPItem.IsCropBMP(type)) economicResultType = ResultDisplayTillageForageEconomicResultType.BMPCost;
        else economicResultType = ResultDisplayTillageForageEconomicResultType.Cost;

        String name = swatResultType.toString() + "/" + economicResultType.toString();
        if (swatResultType == SWATResultColumnType.water)
            name = "water yield" + "/" + economicResultType.toString();
        String unit = "";
        switch (swatResultType)
        {
            case water:
                unit = "m^3/$1000";
                break;
            case sediment:
                unit = "t/$1000";
                break;
            default:
                unit = "kg/$1000";
                break;
        }
        return String.format("%s(%s)", name, unit);
    }
    
    /** 
    * !! Shao.H This method export the selected layer data to CSV file
    *   (1) set the destination path and file name using getCSVDestination()
    *       a. for click from legend on each layer, path to "/map";
    *       b. for click from chart, path to "/chart/on_site";
    *       c. for click from Off-site chart, path to "/chart/off_site"
    *       d. default file name is (layer)_(result type)_(time range)(_"compare_to"_(compare scenario)).csv
    *           i. layer: "Small Dam" or "Holding Pond" or "Grazing" or "Field"
    *                     or "Farm" or "Subbasin"
    *          ii. result type: "swat" or "economic" or "integrated"
    *         iii. time range: "startYear_endYear"
    *          iv. compare scenario : compared scenario name (miss when in Don't
    *              Compare mode).
    *   (2) get data from map or chart
    *       a. for click from legend on each layer
    *           i. get id and result from the database corresponding to the layer;
    *          ii. set the export header as "(layer),(result type)"
    *       b. for click from chart
    *           i. get year and result from the chart;
    *          ii. set the export header as "(year),(yTitle of chart)"
    *       c. for click from off site chart
    *           i. get year and result from the chart;
    *          ii. set the export header as "(year),(yTitle of chart)"
    *   (3) export the result to the destination file with the first line of 
    *       header
    *   (4) if export successfully, give a popup to the user to ask whether open
    *       the file.
    * @author Shao
    */
    /*
    public static void Export(IFeatureLayer layer, int featureId){
        String destinationFile = getCSVDestination(layer, featureId);
        if (String.IsNullOrEmpty(destinationFile)) return;

        try
        {
            List<ResultDataPair> results = null;
            String header = "";
            if (layer != null)
            {
                if (featureId == -1) //map
                {
                    DataTable dt = layer.DataSet.DataTable;
                    int resultIndex = dt.Columns.IndexOf(RESULT_COLUMN);
                    int idIndex = dt.Columns.IndexOf("ID");
                    if (idIndex == -1) idIndex = dt.Columns.IndexOf("Id");
                    if (resultIndex == -1 || idIndex == -1) return;

                    results = new ArrayList<ResultDataPair>();
                    for (DataRow row : dt.Rows)
                    {
                        int id = int.Parse(row[idIndex].ToString());
                        if (id <= 0) continue;
                        double r = double.Parse(row[resultIndex].ToString());
                        results.add(new ResultDataPair(id, r));
                    }
                    header = layer.LegendText + "," + layer.LegendItems.ElementAt(0).LegendText;
                }
                else
                {
                    results = _chartData;
                    header = "year," + layer.LegendItems.ElementAt(0).LegendText;
                } 
            }
            else
            {
                results = _chartData;

                String yTitle = "";
                ResultDisplayTillageForageEconomicResultType economicType = _economicType;
                if (_resultType == ResultDisplayResultType.SWAT)
                    yTitle = ResultNameUnit_SWAT(_resultLevelType, BMPType.Holding_Pond, _swatType);
                else if (_resultType == ResultDisplayResultType.Economic)
                    yTitle = ResultNameUnit_Economic(_resultLevelType, _offsiteEconomicBMPType, _economicType);
                else
                    yTitle = ResultNameUnit_Integrated(BMPType.Tillage_Field, _swatType, _economicType);

                header = "year," + yTitle;
            }


            Export2CSV(destinationFile,header,results);

            if (System.Windows.Forms.MessageBox.Show(
                "Results are saved to " + destinationFile + "." +
                Environment.NewLine + Environment.NewLine + "Do you want to open it?",
                "WEBs Interface", System.Windows.Forms.MessageBoxButtons.YesNoCancel,
                 System.Windows.Forms.MessageBoxIcon.Question) == System.Windows.Forms.DialogResult.Yes)
                System.Diagnostics.Process.Start(destinationFile);
        }
        catch (Exception e)
        {
            System.Windows.Forms.MessageBox.Show("Result saving failed." + Environment.NewLine + e.Message, "WEBs Interface");
        }
    }
    */

    /** 
    * !! Shao.H This method gives the destination path and file name of the CSV 
    * file, see introduction of export() for more detail
    * @author Shao
    */
    /*
    private String getCSVDestination(IFeatureLayer layer, int id){
        System.IO.FileInfo info = new System.IO.FileInfo(_main_scenario.getDatabasePath());
        String csvFolder = info.DirectoryName + "/results_csv";

        if (layer != null) //on-site
        {
            if (id == -1)   //map
                csvFolder += "/map";
            else            //chart
                csvFolder += "/chart/on_site";
        }
        else //chart, off_site
            csvFolder += "/chart/off_site";

        if (!System.IO.Directory.Exists(csvFolder))
            System.IO.Directory.CreateDirectory(csvFolder);

        System.Windows.Forms.SaveFileDialog d = new System.Windows.Forms.SaveFileDialog();
        d.InitialDirectory = csvFolder;
        d.DefaultExt = "csv";
        d.Filter = "Comma-separated values file (*.csv)|*.csv";
        d.FileName = getDefaultCSVName(layer, id);
        d.OverwritePrompt = true;
        d.CheckPathExists = true;
        if (d.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            return d.FileName;
        return "";
    }
    */

    
    /** 
    * !! Shao.H This method gives the default of the exported CSV file, see 
    * introduction of export() for more detail
    * @author Shao
    */
    /*
    private String getDefaultCSVName(IFeatureLayer layer,int id){
        String resultType = "";
        if(_resultType == ResultDisplayResultType.SWAT)
            resultType = "swat_" + _swatType.toString();
        else if(_resultType == ResultDisplayResultType.Economic)
            resultType = "economic_" + _economicType.toString();
        else
            resultType = "integrated_" + _swatType.toString() + "_" + _economicType.toString();

        String timeRange = String.valueOf(_startYear);
        if (_startYear != _endYear)
            timeRange += "_" + String.valueOf(_endYear);            

        String compare = "";
        if (_compare_scenario != null)
            compare = "_compare_to_" + _compare_scenario.toString().Replace(":","_");

        if (layer != null) //on-site
        {
            if (id == -1)   //map
                return String.format("{0}_{1}_{2}{3}.csv",
                    layer.LegendText,
                    resultType,
                    timeRange,
                    compare);
            else            //chart
                return String.format("{0}_{3}_{1}{2}.csv",
                    layer.LegendText,
                    resultType,
                    compare,id);
        }
        else //chart, off_site
            return String.format("%s%s.csv",
                resultType,
                compare);            
    }
    */
    
    /*
    public static void Export2CSV(String fileName, String header, List<ResultDataPair> results) throws IOException{
        PrintWriter pb = new PrintWriter(new FileWriter(fileName));
        pb.println(header);
        for (ResultDataPair r : results)
        {
            pb.println(String.format("%d,%f",r.getYear(),r.getData()));
        }
        pb.println();
    }
    */
    
    // Results Components
    private JPanel bmpPanel;
    private JPanel gLevelPanel;
    private JPanel tfLevelPanel;
    private JRadioButton gRbGraze;
    private JRadioButton gRbSubbasin;
    private JRadioButton rbOnlySwat;
    private JRadioButton rbOnlyEcon;
    private JRadioButton rbIntegrate;
    private JComboBox jcbComp;
    private JComboBox jcbSwat;
    private JComboBox jcbEcon;
    private JComboBox jcbTillFor;
    private JCheckBox cbDams;
    private JCheckBox cbPonds;
    private JCheckBox cbGraze;
    private JCheckBox cbTillFor;
    private JSlider startSlide;
    private JSlider endSlide;   
    private JRadioButton tfRbField;
    private JRadioButton tfRbFarm;
    private JRadioButton tfRbSubbasin;
    private JRadioButton rbOnSite;
    private JRadioButton rbOffSite;
        
    public JPanel BuildResultsPanel() {
        JPanel resultFunctionsPanel = new JPanel(new GridBagLayout());

        JPanel comparePanel = createPanel(new JPanel(), "Scenario", true);
        JRadioButton rbComp = new JRadioButton("Compare with", false);
        JRadioButton rbCompNot = new JRadioButton("Don't Compare", true);
        
        rbComp.setActionCommand("compare");
        rbComp.addActionListener(this);
        rbCompNot.setActionCommand("dontCompare");
        rbCompNot.addActionListener(this);

        ButtonGroup compGroup = new ButtonGroup();
        compGroup.add(rbCompNot);
        compGroup.add(rbComp);

        GridBagConstraints gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        comparePanel.add(rbCompNot, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 1.0);
        comparePanel.add(rbComp, gbc);

        String[] compareList = {"Base Historic", "Base Conventional", "Scenarios With SWAT Results"};
        jcbComp = new JComboBox(compareList);
        jcbComp.setEnabled(false);
        jcbComp.setSelectedIndex(0);
        jcbComp.addActionListener(this);

        gbc = setGbc(new Insets(0, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 2, 1, 1, 1.0, 1.0);
        comparePanel.add(jcbComp, gbc);

        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 1.0, 0.0);
        resultFunctionsPanel.add(comparePanel, gbc);

        JPanel resultLevelPanel = createPanel(new JPanel(), "Result Level", true);
        rbOnSite = new JRadioButton("On-Site", true);
        rbOffSite = new JRadioButton("Off-Site (Outlet)", false);

        rbOnSite.setActionCommand("onSite");
        rbOnSite.addActionListener(this);
        rbOffSite.setActionCommand("offSite");
        rbOffSite.addActionListener(this);
        
        ButtonGroup resLvlGroup = new ButtonGroup();
        resLvlGroup.add(rbOnSite);
        resLvlGroup.add(rbOffSite);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 1.0);
        resultLevelPanel.add(rbOnSite, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 1, 1, 1, 1.0, 1.0);
        resultLevelPanel.add(rbOffSite, gbc);

        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 1.0, 0.0);
        resultFunctionsPanel.add(resultLevelPanel, gbc);

        bmpPanel = createPanel(new JPanel(), "BMP", true);
        cbDams = new JCheckBox("Small Dams", true);
        cbPonds = new JCheckBox("Holding Ponds", true);
        cbGraze = new JCheckBox("Grazing Areas", true);
        cbTillFor = new JCheckBox("Tillage & Forage", true);
        
        cbDams.setActionCommand("damsLayerOn");
        cbDams.addActionListener(this);
        cbPonds.setActionCommand("pondsLayerOn");
        cbPonds.addActionListener(this);
        cbGraze.setActionCommand("grazingLayerOn");
        cbGraze.addActionListener(this);
        cbTillFor.setActionCommand("tillForageLevelOn");
        cbTillFor.addActionListener(this);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 0.0);
        bmpPanel.add(cbDams, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 0.0);
        bmpPanel.add(cbPonds, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 2, 1, 1, 1.0, 0.0);
        bmpPanel.add(cbGraze, gbc);

        gLevelPanel = createPanel(new JPanel(), "Level", true);
        gRbGraze = new JRadioButton("Grazing Area", true);
        gRbSubbasin = new JRadioButton("Subbasin", false);

        gRbGraze.setActionCommand("grazingLevel");
        gRbGraze.addActionListener(this);
        gRbSubbasin.setActionCommand("subbasinLevel");
        gRbSubbasin.addActionListener(this);
        
        ButtonGroup gLevelGroup = new ButtonGroup();
        gLevelGroup.add(gRbGraze);
        gLevelGroup.add(gRbSubbasin);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        gLevelPanel.add(gRbGraze, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 0, 1, 1, 1.0, 1.0);
        gLevelPanel.add(gRbSubbasin, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 3, 1, 1, 1.0, 0.0);
        bmpPanel.add(gLevelPanel, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 4, 1, 1, 1.0, 0.0);
        bmpPanel.add(cbTillFor, gbc);

        tfLevelPanel = createPanel(new JPanel(), "Level", true);
        tfRbField = new JRadioButton("Field", true);
        tfRbFarm = new JRadioButton("Farm", false);
        tfRbSubbasin = new JRadioButton("Subbasin", false);
        
        tfRbField.setActionCommand("fieldLevelOn");
        tfRbField.addActionListener(this);
        tfRbFarm.setActionCommand("farmLevelOn");
        tfRbFarm.addActionListener(this);
        tfRbSubbasin.setActionCommand("subbasinLevelOn");
        tfRbSubbasin.addActionListener(this);
        
        ButtonGroup tfLevelGroup = new ButtonGroup();
        tfLevelGroup.add(tfRbField);
        tfLevelGroup.add(tfRbFarm);
        tfLevelGroup.add(tfRbSubbasin);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        tfLevelPanel.add(tfRbField, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 0, 1, 1, 1.0, 1.0);
        tfLevelPanel.add(tfRbFarm, gbc);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 2, 0, 1, 1, 1.0, 1.0);
        tfLevelPanel.add(tfRbSubbasin, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 5, 1, 1, 1.0, 0.0);
        bmpPanel.add(tfLevelPanel, gbc);

        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 2, 1, 1, 1.0, 0.0);
        resultFunctionsPanel.add(bmpPanel, gbc);

        JPanel resultPanel = createPanel(new JPanel(), "Result", true);
        rbOnlySwat = new JRadioButton("Only SWAT", true);
        rbOnlyEcon = new JRadioButton("Only Economic", false);
        rbIntegrate = new JRadioButton("Integrated", false);
        rbIntegrate.setEnabled(false);

        rbOnlySwat.setActionCommand("onlySwat");
        rbOnlySwat.addActionListener(this);
        rbOnlyEcon.setActionCommand("onlyEconomic");
        rbOnlyEcon.addActionListener(this);
        rbIntegrate.setActionCommand("integrated");
        rbIntegrate.addActionListener(this);
        
        ButtonGroup resultGroup = new ButtonGroup();
        resultGroup.add(rbOnlySwat);
        resultGroup.add(rbOnlyEcon);
        resultGroup.add(rbIntegrate);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        resultPanel.add(rbOnlySwat, gbc);

        String[] swatList = {"Water Yield", "Sediment Yield", "Particulate Phosphorus", "Dissolved Phosphorus",
            "Total Phosphorus", "Particulate Nitrogen", "Dissolved Nitrogen", "Total Nitrogen"};
        jcbSwat = new JComboBox(swatList);
        jcbSwat.setActionCommand("selectSwatResult");
        jcbSwat.setSelectedIndex(1);
        jcbSwat.addActionListener(this);

        gbc = setGbc(new Insets(4, 0, 4, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 1.0);
        resultPanel.add(jcbSwat, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 2, 1, 1, 1.0, 1.0);
        resultPanel.add(rbOnlyEcon, gbc);
        
        String[] econList = {"Small Dam", "Holding Ponds", "Grazing Area", "Tillage", "Forage"};
        jcbEcon = new JComboBox(econList);
        jcbEcon.setActionCommand("selectEconResult");
        jcbEcon.setSelectedIndex(0);
        jcbEcon.setEnabled(false);
        jcbEcon.addActionListener(this);

        gbc = setGbc(new Insets(4, 0, 4, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 3, 1, 1, 1.0, 1.0);
        resultPanel.add(jcbEcon, gbc);

        JPanel tillForPanel = createPanel(new JPanel(), "Tillage & Forage", true);
        String[] tillForList = {"Yield", "Revenue", "Crop Cost", "Crop Return"};
        jcbTillFor = new JComboBox(tillForList);
        jcbTillFor.setActionCommand("selectTillForageResult");
        jcbTillFor.setSelectedIndex(2);
        jcbTillFor.addActionListener(this);
        jcbTillFor.setEnabled(false);

        gbc = setGbc(new Insets(4, 0, 4, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        tillForPanel.add(jcbTillFor, gbc);

        gbc = setGbc(new Insets(4, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 4, 1, 1, 1.0, 1.0);
        resultPanel.add(tillForPanel, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 5, 1, 1, 1.0, 1.0);
        resultPanel.add(rbIntegrate, gbc);

        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 3, 1, 1, 1.0, 0.0);
        resultFunctionsPanel.add(resultPanel, gbc);

        JPanel timePanel = createPanel(new JPanel(), "Time", true);
        startSlide = new JSlider(JSlider.HORIZONTAL, 1991, 2010, 1991);
        endSlide = new JSlider(JSlider.HORIZONTAL, 1991, 2010, 2010);
        
        startSlide.setMinorTickSpacing(1);
        startSlide.setPaintTicks(true);
        endSlide.setMinorTickSpacing(1);
        endSlide.setPaintTicks(true);
        
        startSlide.addChangeListener(this);
        endSlide.addChangeListener(this);

        JLabel startLbl = new JLabel("Start Year");
        JLabel endLbl = new JLabel("End Year");

        gbc = setGbc(new Insets(8, 0, 8, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 0, 1, 1, 1.0, 1.0);
        timePanel.add(startLbl, gbc);

        gbc = setGbc(new Insets(8, 0, 8, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 0, 1, 1, 1.0, 1.0);
        timePanel.add(startSlide, gbc);

        gbc = setGbc(new Insets(8, 0, 8, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 1.0);
        timePanel.add(endLbl, gbc);

        gbc = setGbc(new Insets(8, 0, 8, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1, 1, 1, 1, 1.0, 1.0);
        timePanel.add(endSlide, gbc);

        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 4, 1, 1, 1.0, 1.0);
        resultFunctionsPanel.add(timePanel, gbc);

        return resultFunctionsPanel;
    }
    
    private JPanel createPanel(JPanel pnl, String s, Boolean v) {
        pnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), s, TitledBorder.LEFT, TitledBorder.TOP, f));
        pnl.setLayout(new GridBagLayout());
        pnl.setVisible(v);
        return pnl;
    }
    
    private GridBagConstraints setGbc(Insets i, int fill, int a, int xCoord, int yCoord, int wide, int high, double weighX, double weighY) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = i;
        g.fill = fill;
        g.anchor = a;
        g.gridx = xCoord;
        g.gridy = yCoord;
        g.gridwidth = wide;
        g.gridheight = high;
        g.weightx = weighX;
        g.weighty = weighY;
        return g;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case("dontCompare"):
                jcbComp.setEnabled(false);
                rbIntegrate.setEnabled(false);
                try {
                    SetCompareType(ResultDisplayScenarioCompareType.NoCompare, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("compare"):
                jcbComp.setEnabled(true);
                rbIntegrate.setEnabled(true);
                try {
                    SetCompareType(ResultDisplayScenarioCompareType.Compare, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("compareTo"):
                try {
                    SetCompareScenario(compareScenario, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("onSite"):
                bmpPanel.setEnabled(true);
                cbDams.setEnabled(true);
                cbPonds.setEnabled(true);
                cbGraze.setEnabled(true);
                cbTillFor.setEnabled(true);
                gLevelPanel.setEnabled(true);
                gRbGraze.setEnabled(true);
                gRbSubbasin.setEnabled(true);
                tfLevelPanel.setEnabled(true);
                tfRbField.setEnabled(true);
                tfRbFarm.setEnabled(true);
                tfRbSubbasin.setEnabled(true);
                jcbEcon.setEnabled(false); // H.Shao
                try {
                    SetResultLevel(ResultDisplayResultLevelType.OnSite, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("offSite"):
                bmpPanel.setEnabled(false);
                cbDams.setEnabled(false);
                cbPonds.setEnabled(false);
                cbGraze.setEnabled(false);
                cbTillFor.setEnabled(false);
                gLevelPanel.setEnabled(false);
                gRbGraze.setEnabled(false);
                gRbSubbasin.setEnabled(false);
                tfLevelPanel.setEnabled(false);
                tfRbField.setEnabled(false);
                tfRbFarm.setEnabled(false);
                tfRbSubbasin.setEnabled(false);
                if (rbOnlyEcon.isSelected()) {
                    jcbEcon.setEnabled(true);
                    jcbTillFor.setSelectedIndex(2);
                    jcbTillFor.setEnabled(false);
                }
                try {
                    SetResultLevel(ResultDisplayResultLevelType.OffSite, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("damsLayerOn"):
                if(cbDams.isSelected()) {
                    cbPonds.setEnabled(false);
                    cbGraze.setEnabled(false);
                }
                else {
                    cbPonds.setEnabled(true);
                }
                try {
                    SetBMPType(BMPType.Small_Dam, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("pondsLayerOn"):
                if(cbPonds.isSelected()) {
                    cbGraze.setEnabled(false);
                }
                else {
                    cbGraze.setEnabled(true);
                }
                try {
                    SetBMPType(BMPType.Holding_Pond, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("grazingLayerOn"):
                if(cbGraze.isSelected()) {
                    gLevelPanel.setEnabled(true);
                    gRbGraze.setEnabled(true);
                    gRbSubbasin.setEnabled(true);
                }
                else {
                    gLevelPanel.setEnabled(false);
                    gRbGraze.setEnabled(false);
                    gRbSubbasin.setEnabled(false);
                }
                break;
            case("grazingLevel"):
                try {
                    SetBMPType(BMPType.Grazing, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("subbasinLevel"):
                try {
                    SetBMPType(BMPType.Grazing_Subbasin, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("tillForageLevelOn"):
                if(cbTillFor.isSelected()) {
                    tfLevelPanel.setEnabled(true);
                    tfRbField.setEnabled(true);
                    tfRbFarm.setEnabled(true);
                    tfRbSubbasin.setEnabled(true);
                }
                else {
                    tfLevelPanel.setEnabled(false);
                    tfRbField.setEnabled(false);
                    tfRbFarm.setEnabled(false);
                    tfRbSubbasin.setEnabled(false);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("fieldLevelOn"):
                try {
                    SetBMPType(BMPType.Tillage_Field, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("farmLevelOn"):
                try {
                    SetBMPType(BMPType.Tillage_Farm, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("subbasinLevelOn"):
                try {
                    SetBMPType(BMPType.Tillage_Subbasin, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("onlySwat"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                try {
                    SetResultType(ResultDisplayResultType.SWAT, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("selectSwatResult"):
                String entSwat = jcbSwat.getSelectedItem().toString();
                try {
                    switch(entSwat) {
                        case("Water Yield"):
                            SetSWATType(SWATResultColumnType.water, true);
                            break;
                        case("Sediment Yield"):
                            SetSWATType(SWATResultColumnType.sediment, true);
                            break;
                        case("Particulate Phosphorus"):
                            SetSWATType(SWATResultColumnType.PP, true);
                            break;
                        case("Dissolved Phosphorus"):
                            SetSWATType(SWATResultColumnType.DP, true);
                            break;
                        case("Total Phosphorus"):
                            SetSWATType(SWATResultColumnType.TP, true);
                            break;
                        case("Particulate Nitrogen"):
                            SetSWATType(SWATResultColumnType.PN, true);
                            break;
                        case("Dissolved Nitrogen"):
                            SetSWATType(SWATResultColumnType.DN, true);
                            break;
                        case("Total Nitrogen"):
                            SetSWATType(SWATResultColumnType.TN, true);
                            break;                      
                        default:
                            break;
                    }
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("onlyEconomic"):
                jcbSwat.setEnabled(false);
                // jcbEcon.setEnabled(true); // H.Shao
                if (rbOffSite.isSelected()){
                    jcbEcon.setEnabled(true);
                    jcbTillFor.setSelectedIndex(2);
                    jcbTillFor.setEnabled(false);
                } else {
                    jcbEcon.setEnabled(false);
                    jcbTillFor.setSelectedIndex(2);
                    jcbTillFor.setEnabled(true);
                }
                
                try {
                    SetResultType(ResultDisplayResultType.Economic, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                WhiteboxGuiClone.wb.refreshSplitPaneFour();
                break;
            case("selectEconResult"):
                String entEcon = jcbEcon.getSelectedItem().toString();
                try {
                    switch(entEcon) {
                    case("Small Dam"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        SetBMPType(BMPType.Small_Dam,true);
                        break;
                    case("Holding Ponds"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        SetBMPType(BMPType.Holding_Pond,true);
                        break;
                    case("Grazing Area"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        SetBMPType(BMPType.Grazing,true);
                        break;
                    case("Tillage"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        if (tfRbField.isSelected()){
                            SetBMPType(BMPType.Tillage_Field,true);
                        } else if  (tfRbFarm.isSelected()) {
                            SetBMPType(BMPType.Tillage_Farm,true);
                        } else {
                            SetBMPType(BMPType.Tillage_Subbasin,true);
                        }
                        break;
                    case("Forage"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        if (tfRbField.isSelected()){
                            SetBMPType(BMPType.Forage_Field,true);
                        } else if  (tfRbFarm.isSelected()) {
                            SetBMPType(BMPType.Forage_Farm,true);
                        } else {
                            SetBMPType(BMPType.Forage_Subbasin,true);
                        }
                        break;
                    default:
                        break;
                    }
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("selectTillForageResult"):
                String entTF = jcbTillFor.getSelectedItem().toString();
                try {
                    switch(entTF) {
                    case("Yield"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Yield, true);
                        break;
                    case("Revenue"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Revenue, true);
                        break;
                    case("Crop Cost"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, true);
                        break;
                    case("Crop Return"):
                        SetEconomicType(ResultDisplayTillageForageEconomicResultType.net_return, true);
                        break;
                    default:
                        break;
                    }
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }                
                break;
            case("integrated"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                try {
                    SetResultType(ResultDisplayResultType.Integrated, true);
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("startYear"):
                if(startSlide.getValue() > endSlide.getValue()) {
                    startSlide.setValue(endSlide.getValue());
                    startSlide.updateUI();
                }
                try {
                    SetStartYear(startSlide.getValue(), true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("endYear"):
                if(endSlide.getValue() < startSlide.getValue()) {
                    endSlide.setValue(startSlide.getValue());
                    endSlide.updateUI();
                }
                try {
                    SetStartYear(endSlide.getValue(), true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {        
        JSlider slider = (JSlider) e.getSource();
        slider.setToolTipText(String.valueOf(slider.getValue()));
        if (slider.equals(startSlide)) {
            if (slider.getValue() > endSlide.getValue()) 
                slider.setValue(endSlide.getValue());
            if (!slider.getValueIsAdjusting()){
                try {
                    SetStartYear(slider.getValue(),true);
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        if (slider.equals(endSlide)) {
            if (slider.getValue() < startSlide.getValue()) 
                    slider.setValue(startSlide.getValue());
            if (!slider.getValueIsAdjusting()){
                try {
                    SetEndYear(slider.getValue(),true);
                    WhiteboxGuiClone.wb.refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
