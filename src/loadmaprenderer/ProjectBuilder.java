package loadmaprenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import static loadmaprenderer.WhiteboxGuiClone.logger;

public class ProjectBuilder {
    private String spatialDirectory;
    private String txtInOutDirectory;
    private String scenarioDirectory;
    private String baseDirectory;
    private String userDirectory;
    private String createdOn;
    private String modified;
    
    /**
     * 
     * @param filename: String containing the name of the Project File.
     *                  Project Files use the ".wpj" file extension.
     * @param projectDirectory: String containing the root path to the project
     *                          Directory.
     * @param watershedDirectory: String containing the path to the STC directory.
     * @param hasBase: Boolean representing whether the project has base scenarios
     *                 linked to it.
     */
    
    //, boolean hasBase
    
    public ProjectBuilder(String filename, String projectDirectory, String watershedDirectory) {
        String sep = File.separator;
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E yyy.MM.dd 'at' hh:mm:ss a zzz");
        try {
            scenarioDirectory = projectDirectory + sep + filename + sep + "Scenarios" + sep;
            spatialDirectory = watershedDirectory + sep + "Spatial" + sep;
            txtInOutDirectory = watershedDirectory + sep + "txtinout" + sep;
            baseDirectory = scenarioDirectory + "Base Scenarios" + sep;
            userDirectory = scenarioDirectory + "User Scenarios" + sep;
            File projFile = new File(projectDirectory + sep + filename);
            if(!projFile.exists()) {
                projFile.mkdirs();
                createdOn = "Project Created: " + ft.format(date);
                modified = "Last Modified: ";
            }
            else {
                BufferedReader br =  new BufferedReader(new FileReader(projFile + sep + projFile.getName() + ".wpj"));
                String temp;
                while((temp = br.readLine()) != null)  {
                    if(temp.contains("Project Created:")) {
                        createdOn = temp;
                        modified = "Last Modified: " + ft.format(date);
                    }
                }
            }
            File[] files = {new File(scenarioDirectory), new File(baseDirectory),
                            new File(userDirectory)};
            for(File f: files) {
                if(!f.exists()) {
                    f.mkdirs();
                }
            }
            PrintWriter pw = new PrintWriter(projFile + sep + filename + ".wpj");
            pw.println("\nPROJECT FILE\n");
            pw.println("Project Name: " + filename);
            pw.println("Project Location: " + projectDirectory);
            pw.println("Spatial Location: " + spatialDirectory);
            pw.println("SWAT Location: " + txtInOutDirectory);
            pw.println("Scenario Location: " + scenarioDirectory);
            pw.println("Base Scenario Folder: " + baseDirectory);
            pw.println("User Scenario Folder: " + userDirectory);
            pw.println(createdOn);
            pw.println(modified);
            pw.println("\nDBF FILES\n");
            pw.println("boundary.shp");
            pw.println("stream.shp");
            pw.println("subbasin.shp");
            pw.println("small_dam.shp");
            pw.println("cattle_yard.shp");
            pw.println("grazing.shp");
            pw.println("farm2010.shp");
            pw.println("land2010_by_land_id.shp");
            //if(hasBase) {
            //    scenarioWriter(pw);
           // }
            pw.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGuiClone.ProjectBuilder", e);
        }
    }
    
    private void scenarioWriter(PrintWriter pw) {
        pw.println("\nSCENARIO DATA\n");
        pw.println("Scenario Name: ");
        pw.println("Scenario Description: ");
        pw.println("Scenario Path: ");
        pw.println("Scenario Created: ");
        pw.println("Last Modified Created: ");
        pw.println("Last Economic Run: ");
        pw.println("Last SWAT Run: ");
        pw.println("Scenario ID: ");
        pw.println("Scenario Type: ");
        pw.println("Crop BMP Level: ");
    }
}
