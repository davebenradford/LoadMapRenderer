/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.io.File;
import java.sql.SQLException;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Shao
 */
public class test_SWATnEconomic {
    public static void main(String[] args) throws SQLException, Exception {
        String fieldScenarioDB = "C:\\Users\\Shao\\Documents\\WEBsInterface\\test\\ScenarioTest\\"
                + "WEBs_Scenario_1_field.db3";
        String farmScenarioDB = "C:\\Users\\Shao\\Documents\\WEBsInterface\\test\\ScenarioTest\\"
                + "WEBs_Scenario_2_farm.db3";
        String subbasinScenarioDB = "C:\\Users\\Shao\\Documents\\WEBsInterface\\test\\ScenarioTest\\"
                + "WEBs_Scenario_3_subbasin.db3";
        String baseScenarioDB = "C:\\Users\\Shao\\Documents\\WEBsInterface\\test\\ScenarioTest\\"
                + "WEBs_Scenario_Conventional.db3";
        File database = new File(fieldScenarioDB);
        if(!database.exists()) database.createNewFile();
        System.out.println("Is the database exist: " + database.exists());
        
        Project project = new Project();        
        project.addScenario(new Scenario("", "", baseScenarioDB,
                ScenarioType.Base_Conventional, BMPScenerioBaseType.Conventional, 
                BMPSelectionLevelType.Field, ""));
        project.addScenario(new Scenario("", "", fieldScenarioDB,
                ScenarioType.Normal, BMPScenerioBaseType.Conventional, 
                BMPSelectionLevelType.Field, ""));
        
        
        /*
        Integer[] tillIDs = new Integer[]{1,2,3,4,5,6,7,8,9,10};
        Integer[] forageIDs = new Integer[]{11,12,13,14,15,16,17,18,19,20};
        Integer[] pondIDs = new Integer[]{10,13};
        Integer[] smalDamIDs = new Integer[]{1,2,3,4,5,6,7,8,9,10};
        Integer[] grazingIDs = new Integer[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
        
        scenario.SetBMPSelector(BMPType.Small_Dam, Arrays.asList(smalDamIDs));
        scenario.SetBMPSelector(BMPType.Holding_Pond, Arrays.asList(pondIDs));
        scenario.SetBMPSelector(BMPType.Grazing, Arrays.asList(grazingIDs));
        scenario.SetBMPSelector(BMPType.Tillage_Field, Arrays.asList(tillIDs));
        scenario.SetBMPSelector(BMPType.Forage_Field, Arrays.asList(forageIDs));
        
        //scenario.SaveDesign(project, true, true);
        
        
        System.out.println("--------------Start economic model!-------------");
        mainScenario.RunEconomic();
        System.out.println("----------Economic simulation finished!---------");
        System.out.println("----------------Start SWAT model!---------------");
        mainScenario.RunSWAT();
        System.out.println("------------SWAT simulation finished!-----------");
        
        
        List<Double> values = Arrays.asList(new Double[]{1.1,0.2,3.3,4.5,2.7,11.2,20.0,
        3.2,4.3,2.5,10.6,50.3,88.9});
        System.out.println(values.toString());
        values.sort(null);
        Map<Integer, Interval> intervals = Query.GetQuantileIntervals(values, 3);
        */
        
        // Set the UI looks like the user's system look and feel
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        ResultDisplayFrame displayFrame = new ResultDisplayFrame(project, project.GetCurrentScenario());
        displayFrame.setVisible(true);
        displayFrame.setSize(1400,800);
        displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        
    }
    
    /*
    private void printScenario(Scenario scenario){
        File file = new File("C:\\Users\\Shao\\Documents\\WEBsInterface\\test\\ScenarioTest\\txt.txt");

        if (file.exists()) {
            file.delete();
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter out = null;
        try {
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw, true);

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(Scenario.class, new MapInfoSerializer());
            Gson gson = gsonBuilder.create();

            String json = gson.toJson(openMaps.get(selectedMapAndLayer[0]));
            out.println(json);
        } catch (java.io.IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.saveMap", e);
            //System.err.println("Error: " + e.getMessage());
        } catch (Exception e) { //Catch exception if any
            logger.log(Level.SEVERE, "WhiteboxGui.saveMap", e);
            //System.err.println("Error: " + e.getMessage());
        } finally {
            if (out != null || bw != null) {
                out.flush();
                out.close();
            }

        }
    }
    */
    
    
}
