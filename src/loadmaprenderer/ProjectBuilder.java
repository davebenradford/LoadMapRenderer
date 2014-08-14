package loadmaprenderer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import static loadmaprenderer.WhiteboxGuiClone.logger;

public class ProjectBuilder {
    private long modified;
    private String spatialDirectory;
    private String applicationDirectory;
    private String watershedDirectory;
    private String projectDirectory;
    private String txtInOutDirectory;
    private String scenarioDirectory;
    private String baseDirectory;
    private String userDirectory;
    
    /**
     * 
     * @param args 
     */
    
    public static void main(String args[]) {
        ProjectBuilder pb = new ProjectBuilder("test");
    }
    
    /**
     * 
     * @param filename 
     */
    
    public ProjectBuilder(String filename) {
        String sep = File.separator;
        try {
            applicationDirectory = java.net.URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            applicationDirectory += getClass().getName().replace('.', File.separatorChar);
            applicationDirectory = new File(applicationDirectory).getParent() + sep;
            projectDirectory = applicationDirectory + sep + "Projects" + sep + filename;
            scenarioDirectory = projectDirectory + sep + "Scenarios" + sep;
            watershedDirectory = applicationDirectory + "resources" + sep + "STC" + sep + "Data" + sep;
            spatialDirectory = watershedDirectory + "Spatial" + sep;
            txtInOutDirectory = watershedDirectory + "txtinout" + sep;
            baseDirectory = scenarioDirectory + "Base Scenarios" + sep;
            userDirectory = scenarioDirectory + "User Scenarios" + sep;
            File projFile = new File(projectDirectory);
            if(!projFile.exists()) {
                projFile.mkdirs();               
            }
            File[] files = {new File(scenarioDirectory), new File(baseDirectory),
                            new File(userDirectory)};
            for(File f: files) {
                if(!f.exists()) {
                    f.mkdirs();
                }
            }
            PrintWriter pw = new PrintWriter(projFile + sep + filename + ".wpj");
            pw.println("Project Name: " + filename);
            pw.println("Project Location: " + projectDirectory);
            pw.println("Spatial Location: " + spatialDirectory);
            pw.println("SWAT Location: " + txtInOutDirectory);
            pw.println("Scenario Location: " + scenarioDirectory);
            pw.println("Base Scenario Folder: " + baseDirectory);
            pw.println("User Scenario Folder: " + userDirectory);
            Date date = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("E yyy.MM.dd 'at' hh:mm:ss a zzz");
            pw.println("Date Created: " + ft.format(date));
            pw.println("Last Modified: " + modified);
            pw.println("\nDBF FILES\n");
            pw.println("boundary.shp");
            pw.println("stream.shp");
            pw.println("subbasin.shp");
            pw.println("small_dam.shp");
            pw.println("cattle_yard.shp");
            pw.println("grazing.shp");
            pw.println("farm2010.shp");
            pw.println("land2010_by_land_id.shp");
            pw.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGuiClone.ProjectBuilder", e);
        }
    }
}
