/*
 *  Copyright (C) 2011-2014 John Lindsay
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package loadmaprenderer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.script.*;
import javax.swing.*;
import javax.swing.JToolBar.Separator;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nl.knaw.dans.common.dbflib.CorruptedTableException;
import nl.knaw.dans.common.dbflib.Table;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import rastercalculator.RasterCalculator;
import whitebox.cartographic.*;
import whitebox.cartographic.MapArea;
import whitebox.cartographic.MapInfo;
import whitebox.geospatialfiles.RasterLayerInfo;
import whitebox.geospatialfiles.ShapeFile;
import whitebox.geospatialfiles.VectorLayerInfo;
import whitebox.geospatialfiles.shapefile.ShapeFileRecord;
import whitebox.geospatialfiles.shapefile.ShapeTypeDimension;
import whitebox.geospatialfiles.shapefile.attributes.AttributeTable;
import whitebox.geospatialfiles.shapefile.attributes.DBFException;
import whitebox.geospatialfiles.shapefile.attributes.DBFField;
import whitebox.interfaces.*;
import whitebox.interfaces.InteropPlugin.InteropPluginType;
import whitebox.interfaces.MapLayer.MapLayerType;
import whitebox.internationalization.WhiteboxInternationalizationTools;
import whitebox.plugins.ReturnedDataEvent;
import whitebox.serialization.MapInfoDeserializer;
import whitebox.serialization.MapInfoSerializer;
import whitebox.structures.BoundingBox;
import whitebox.structures.ExtensionFileFilter;
import whitebox.structures.InteroperableGeospatialDataFormat;
import whitebox.structures.MenuExtension;
import whitebox.ui.ComboBoxProperty;
import whitebox.ui.ShapefileDatabaseRecordEntry;
import whitebox.ui.SupportedLanguageChooser;
import whitebox.utilities.FileUtilities;
import whitebox.utilities.StringUtilities;
import whiteboxgis.*;
import whiteboxgis.user_interfaces.AboutWhitebox;
import whiteboxgis.user_interfaces.FeatureSelectionPanel;
import whiteboxgis.user_interfaces.IconTreeNode;
import whiteboxgis.user_interfaces.LayersPopupMenu;
import whiteboxgis.user_interfaces.PaletteManager;
import whiteboxgis.user_interfaces.RecentMenu;
import whiteboxgis.user_interfaces.Scripter;
import whiteboxgis.user_interfaces.SettingsDialog;
import whiteboxgis.user_interfaces.ToolDialog;
import whiteboxgis.user_interfaces.TreeNodeRenderer;
import whiteboxgis.user_interfaces.ViewCodeDialog;
import whiteboxgis.user_interfaces.ViewTextDialog;

/**
 * For Whitebox GAT 3.2.1.
 *
 * @author Dr. John Lindsay <jlindsay@uoguelph.ca>
 *
 * For WEBs Interface
 * @author David Radford <radfordd@uoguelph.ca>
 *
 * All code used with permission from Dr. John Lindsay in accordance with the
 * GNU Public License. <http://www.gnu.org/licenses/>.
 */
@SuppressWarnings("unchecked")
public class WhiteboxGuiClone extends JFrame implements ThreadListener, ActionListener, WhiteboxPluginHost, Communicator, ChangeListener{

    public static final Logger logger = Logger.getLogger(WhiteboxGuiClone.class.getPackage().getName());
    private static PluginService pluginService = null;
    private StatusBar status;

    // common variables
    private static final String versionName = "3.2 'Iguazu'";
    public static final String versionNumber = "3.2.1";
    public static String currentVersionNumber;
    private String skipVersionNumber = versionNumber;
    private ArrayList<PluginInfo> plugInfo = null;
    private String applicationDirectory;
    private String resourcesDirectory;
    private String graphicsDirectory;
    private String pluginDirectory;
    private String helpDirectory;
    private String workingDirectory;
    private String paletteDirectory;
    private String logDirectory;

    public String wbProjectDirectory;
    private String watershedDirectory;
    private String spatialDirectory;
    private String txtInOutDirectory;
    private boolean hasBase = false;

    private String defaultQuantPalette;
    private String defaultQualPalette;
    private String pathSep;
    private String toolboxFile;
    private String propsFile;
    private String userName;
    private int splitterLoc1 = 250;

    //Tab Index for Drawing Area
    private int ptbTabsIndex = 0;
    private int mtbTabsIndex = 0;
    
    private AttributesFileViewer afv;

    //BMP Map Values
    private boolean damMap = false;
    private int openDam = -1;
    private MapInfo miDam;
    private int[] damData = null;
    private boolean pondMap = false;
    private int openPond = -1;
    private MapInfo miPond;
    private int[] pondData = null;
    private boolean grazeMap = false;
    private int openGraze = -1;
    private MapInfo miGraze;
    private int[] grazeData = null;
    private boolean tillMap = false;
    private int openTill = -1;
    private MapInfo miTill;
    private int[] tillData = null;
    private boolean forageMap = false;
    private int openForage = -1;
    private MapInfo miForage;
    private int[] forageData = null;
    private int openResults = -1;
    private MapInfo miResults;
    private boolean resultsMap = false;
    private int itemIndex;
    public String currentShapeFile;
    public String[] resultsShapeFiles = new String[4];
    public int[] currentData;
    public int userScenarioIndex = -1;
    public int userScenarioLimit = 100;
    public ScenarioBuilder currentScenario;
    public ScenarioBuilder baseHistoric;
    public ScenarioBuilder baseConventional;
    public ScenarioBuilder[] userScenarios = new ScenarioBuilder[userScenarioLimit];
    private VectorLayerInfo vli;
    private ResultDisplayFrame rdf;
    private Project project;
    private boolean noChart = true;
    private boolean resultsOn = false;

    // Whitebox Tabs
    private int qlTabsIndex = 0;
    private int[] selectedMapAndLayer = new int[3];
    private boolean linkAllOpenMaps = false;
    private boolean hideAlignToolbar = true;

    // Gui items.
    private JList allTools;
    private JList recentTools;
    private JList mostUsedTools;
    private JSplitPane splitPane;
    private JSplitPane splitPane2;
    private JSplitPane splitPane3;
    private JSplitPane splitPane4;

    // WEBs Components
    public JLabel projLocation;
    public JLabel spatLocation;
    public JLabel swatLocation;
    public JTextArea scenDescFld;
    public JTextField projNameFld;
    public JTextField scenNameFld;
    public JButton saveProj;
    private Separator projSep;
    private Separator scenSep;
    private JButton dams;
    private JButton ponds;
    private JButton grazing;
    private JButton tillage;
    private JButton forage;
    private JButton newScen;
    private JButton editScen;
    private JButton delScen;
    public JButton btnSaveSc;
    public JButton btnSaveAsSc;
    private JRadioButton rbConvTill;
    private JRadioButton rbZeroTill;
    private JRadioButton rbOnSite;
    private JRadioButton rbOffSite;
    private JRadioButton tfRbField;
    private JRadioButton tfRbFarm;
    private JRadioButton tfRbSubbasin;
    
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

    // WEBs Panels
    public JPanel webs;
    public JPanel tableView;
    public JPanel projPanel;
    private JPanel scenPanel;
    private JPanel basicScen;
    private JPanel bmpSection;
    private JPanel controlSection;

    private JTabbedPane tabs = new JTabbedPane();
    private JTree tree = null;
    private JTabbedPane qlTabs = null;
    private JTabbedPane ptb = null;

    private JTabbedPane mtb = new JTabbedPane();

    private MapRenderingTool drawingArea = new MapRenderingTool();
    private ArrayList<MapInfo> openMaps = new ArrayList<>();
    private int activeMap = 0;
    private int numOpenMaps = 1;

    private JPopupMenu mapsPopup = null;
    private JPopupMenu mapAreaPopup = null;

    private JToggleButton pan = null;
    private JToggleButton zoomIntoBox = null;
    private JToggleButton zoomOut = null;
    private JToggleButton select = null;
    private JToggleButton selectFeature = null;
    private JButton paletteManager;
    private JToggleButton modifyPixelVals = null;
    private JToggleButton distanceToolButton = null;
    private JToggleButton editVectorButton = null;
    private JToggleButton digitizeNewFeatureButton = null;

    private JButton deleteFeatureButton = null;
    private JButton deleteLastNodeInFeatureButton = null;
    private JCheckBoxMenuItem modifyPixels = null;
    private JCheckBoxMenuItem zoomMenuItem = null;
    private JCheckBoxMenuItem zoomOutMenuItem = null;
    private JCheckBoxMenuItem panMenuItem = null;
    private JCheckBoxMenuItem selectMenuItem = null;
    private JCheckBoxMenuItem selectFeatureMenuItem = null;
    private JCheckBoxMenuItem distanceToolMenuItem = null;
    private JCheckBoxMenuItem editVectorMenuItem = null;
    private JCheckBoxMenuItem digitizeNewFeatureMenuItem = null;
    private JMenuItem deleteLastNodeInFeatureMenuItem = null;
    private JMenuItem deleteFeatureMenuItem = null;
    private JCheckBoxMenuItem linkMap = null;
    private JCheckBoxMenuItem wordWrap = null;
    private JTextField searchText = new JTextField();
    private HashMap<String, ImageIcon> icons = new HashMap<>();
    private HashMap<String, Font> fonts = new HashMap<>();
    private JTextField scaleText = new JTextField();
    private PageFormat defaultPageFormat = new PageFormat();
    private Font defaultFont = null;

    private static final Font f = new Font("Sans_Serif", Font.PLAIN, 12);

    private int numberOfRecentItemsToStore = 5;
    private RecentMenu recentDirectoriesMenu = new RecentMenu();
    private RecentMenu recentFilesMenu = new RecentMenu();
    private RecentMenu recentFilesMenu2 = new RecentMenu();
    private RecentMenu recentFilesPopupMenu = new RecentMenu();
    private RecentMenu recentMapsMenu = new RecentMenu();
    private Color backgroundColour = new Color(225, 245, 255);
    private CartographicToolbar ctb;
    private double defaultMapMargin = 0.0;
    private ArrayList<WhiteboxAnnouncement> announcements = new ArrayList<>();
    private int announcementNumber = 0;
//    private Locale currentLocale;
    private ResourceBundle bundle;
    private ResourceBundle pluginBundle;
    private ResourceBundle messages;
    private String language = "en";
    private String country = "CA";
    private boolean checkForUpdates = true;
    private boolean receiveAnnouncements = true;
    private String updateDownloadArtifact = "";
    public static WhiteboxGuiClone wb;

    public static void main(String[] args) {
        try {
            setLookAndFeel("systemLAF");
            if (System.getProperty("os.name").contains("Mac")) {
                System.setProperty("apple.awt.brushMetalLook", "true");
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Whitebox GAT");
                System.setProperty("com.apple.mrj.application.growbox.intrudes", "true");
                System.setProperty("Xdock:name", "Whitebox");
                System.setProperty("apple.awt.fileDialogForDirectories", "true");

                System.setProperty("apple.awt.textantialiasing", "true");

                System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
            }
            wb = new WhiteboxGuiClone();
            wb.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            wb.setVisible(true);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "WhiteboxGuiClone.Main", e);
        }
    }
    private String retFile;
    private boolean flag = true;

    private void findFile(File dir, String fileName) {
        if (flag) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    findFile(files[i], fileName);
                } else if (files[i].getName().equals(fileName)) {
                    retFile = files[i].toString();
                    flag = false;
                    break;
                }
            }
        }
    }

    private static void setLookAndFeel(String lafName) {
        try {

            if (lafName.equals("systemLAF")) {
                lafName = getSystemLookAndFeelName();
            }

            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lafName.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.setLookAndFeel", e);
        }
    }

    private static String getSystemLookAndFeelName() {
        try {
            String className = UIManager.getSystemLookAndFeelClassName();
            String name = null;
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (className.equals(info.getClassName())) {
                    name = info.getName();
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            return name;
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException e) {
            logger.log(Level.WARNING, "WhiteboxGui.getSystemLookAndFeelName", e);
            return null;
        }
    }

    public WhiteboxGuiClone() throws InvocationTargetException {
        super("Whitebox GAT " + versionName);
        this.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        try {
            // initialize the pathSep and GraphicsDirectory variables
            pathSep = File.separator;

            applicationDirectory = java.net.URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            if (applicationDirectory.endsWith(".exe") || applicationDirectory.endsWith(".jar")) {
                applicationDirectory = new File(applicationDirectory).getParent();
            } else {
                // Add the path to the class files
                applicationDirectory += getClass().getName().replace('.', File.separatorChar);

                // Step one level up as we are only interested in the
                // directory containing the class files
                applicationDirectory = new File(applicationDirectory).getParent();
//                applicationDirectory = new File(applicationDirectory).getParent();
//                applicationDirectory = new File(applicationDirectory).getParent();
//                applicationDirectory = new File(applicationDirectory).getParent();
            }
            resourcesDirectory = applicationDirectory + pathSep + "resources" + pathSep;
            graphicsDirectory = resourcesDirectory + "Images" + pathSep;
            
            wbProjectDirectory = applicationDirectory + pathSep + "Projects";
            watershedDirectory = resourcesDirectory + "STC";
            spatialDirectory = resourcesDirectory + "STC" + pathSep + "Data" + pathSep + "Spatial" + pathSep;
            txtInOutDirectory = resourcesDirectory + "STC" + pathSep + "Data" + pathSep + "txtinout" + pathSep;
            
            helpDirectory = resourcesDirectory + "Help" + pathSep;
            pluginDirectory = resourcesDirectory + "plugins" + pathSep;
            toolboxFile = resourcesDirectory + "toolbox.xml";
            propsFile = resourcesDirectory + "app.config";
            workingDirectory = resourcesDirectory + "samples" + pathSep;
            paletteDirectory = resourcesDirectory + "palettes" + pathSep;
            logDirectory = applicationDirectory + pathSep + "logs" + pathSep;
            File ld = new File(logDirectory);
            if (!ld.exists()) {
                ld.mkdirs();
            }
            //logDirectory = resourcesDirectory + "logs" + pathSep;

            // set up the logger
            int limit = 1000000; // 1 Mb
            int numLogFiles = 3;
            FileHandler fh = new FileHandler(logDirectory + "WhiteboxLog%g_%u.xml", limit, numLogFiles, true);
            fh.setFormatter(new XMLFormatter());
            logger.addHandler(fh);

            this.getApplicationProperties();

            WhiteboxInternationalizationTools.setLocale(language, country);
            bundle = WhiteboxInternationalizationTools.getGuiLabelsBundle(); //ResourceBundle.getBundle("whiteboxgis.i18n.GuiLabelsBundle", currentLocale);
            messages = WhiteboxInternationalizationTools.getMessagesBundle(); //ResourceBundle.getBundle("whiteboxgis.i18n.messages", currentLocale);
            pluginBundle = WhiteboxInternationalizationTools.getPluginsBundle();
            this.loadPlugins();

            boolean newInstall = checkForNewInstallation();

            // create the gui
            status = new StatusBar(this);

            findFile(new File(applicationDirectory + pathSep), "wbGAT.png");
            if (retFile != null) {
                this.setIconImage(new ImageIcon(retFile).getImage());
            }

            drawingArea.setPrintResolution(printResolution);

            callSplashScreen();

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // set the application icon
            String imgLocation = graphicsDirectory + "wbGAT.png";
            setIconImage(Toolkit.getDefaultToolkit().getImage(imgLocation));

            String[] fontName = {"Serif", "SanSerif", "Monospaced", "Dialog", "DialogInput"};

            fonts.put("root", new Font(fontName[1], Font.PLAIN, 12));
            fonts.put("activeMap", new Font(fontName[1], Font.BOLD, 12));
            fonts.put("inactiveMap", new Font(fontName[1], Font.PLAIN, 12));
            fonts.put("inactiveLayer", new Font(fontName[1], Font.PLAIN, 12));
            fonts.put("activeLayer", new Font(fontName[1], Font.BOLD, 12));
            icons.put("root", new ImageIcon(graphicsDirectory + "map.png", ""));
            icons.put("activeMap", new ImageIcon(graphicsDirectory + "map.png", ""));
            icons.put("inactiveMap", new ImageIcon(graphicsDirectory + "map.png", ""));
            icons.put("activeLayer", new ImageIcon(graphicsDirectory + "rgb.png", ""));
            icons.put("inactiveLayer", new ImageIcon(graphicsDirectory + "rgb.png", ""));

            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });

            if (defaultQuantPalette.equals("")) {
                defaultQuantPalette = "spectrum.pal";
            }
            if (defaultQualPalette.equals("")) {
                defaultQualPalette = "qual.pal";
            }

            this.createGui();

            if (newInstall) {
                refreshToolUsage();
                recentDirectoriesMenu.removeAllMenuItems();
                recentFilesMenu.removeAllMenuItems();
                recentFilesMenu2.removeAllMenuItems();
                recentFilesPopupMenu.removeAllMenuItems();
                recentMapsMenu.removeAllMenuItems();
            }

            checkVersionIsUpToDate();

            // Issue a warning if Whitebox is being run on 32-bit JRE on a 64-bit machine, at least on Windows
            if (System.getProperty("os.name").toLowerCase().contains("Windows")) {
                if (!System.getProperty("sun.arch.data.model").contains("32")
                        && (System.getenv("ProgramFiles(x86)") != null)) {
                    String str = "WARNGING: Whitebox is running on a 32-bit Java runtime which could lead to out-of-memory errors \n"
                            + "when handling large files. It is advisable that you uninstall the 32-bit version of Java and \n"
                            + "download and install the 64-bit version. This will allow Whitebox to take fuller advantage of \n"
                            + "the RAM resources of your computer.";
                    returnData(str);
                    status.setMessage(str);
                }
            }

            pan();

        } catch (IOException | SecurityException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.constructor", e);
        }
    }

    public boolean isVersionUpToDate() {
        try {
            String currentVersionName = "";
            currentVersionNumber = "";
            String downloadLocation = "";

            //make a URL to a known source
            String baseUrl = "http://www.uoguelph.ca/~hydrogeo/Whitebox/VersionInfo.xml";
            URL url = new URL(baseUrl);

            //open a connection to that source
            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

            //trying to retrieve data from the source. If there
            //is no connection, this line will fail
            Object objData = urlConnect.getContent();

            InputStream inputStream = (InputStream) urlConnect.getContent();
            DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
            docbf.setNamespaceAware(true);
            DocumentBuilder docbuilder = docbf.newDocumentBuilder();
            Document document = docbuilder.parse(inputStream, baseUrl);

            document.getDocumentElement().normalize();
            Element docElement = document.getDocumentElement();

            Element el;
            NodeList nl = docElement.getElementsByTagName("VersionName");
            if (nl.getLength() > 0) {
                el = (Element) nl.item(0);
                currentVersionName = el.getFirstChild().getNodeValue().replace("\"", "");
            }

            nl = docElement.getElementsByTagName("VersionNumber");
            if (nl.getLength() > 0) {
                el = (Element) nl.item(0);
                currentVersionNumber = el.getFirstChild().getNodeValue().replace("\"", "");
            }

            nl = docElement.getElementsByTagName("DownloadLocation");
            if (nl.getLength() > 0) {
                el = (Element) nl.item(0);
                downloadLocation = el.getFirstChild().getNodeValue().replace("\"", "");
            }

            nl = docElement.getElementsByTagName("DownloadArtifact");
            if (nl.getLength() > 0) {
                el = (Element) nl.item(0);
                updateDownloadArtifact = el.getFirstChild().getNodeValue().replace("\"", "");
            }

            if (currentVersionName.isEmpty()
                    || currentVersionNumber.isEmpty()
                    || downloadLocation.isEmpty()) {
                return true;
            }

            if (Integer.parseInt(versionNumber.replace(".", ""))
                    < Integer.parseInt(currentVersionNumber.replace(".", ""))) {
                return false;
            }
            return true;
        } catch (UnknownHostException e) {
            logger.log(Level.WARNING, "WhiteboxGui.checkVersionIsUpToDate", e);
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.checkVersionIsUpToDate", e);
            return true;
        } catch (NumberFormatException | ParserConfigurationException | DOMException | SAXException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.checkVersionIsUpToDate", e);
            return true;
        }
    }

    private boolean checkVersionIsUpToDate() {
        // Throwing this on the EDT to allow the window to pop up faster
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (checkForUpdates || receiveAnnouncements) {
                        String currentVersionName = "";
                        currentVersionNumber = "";
                        String downloadLocation = "";

                        //make a URL to a known source
                        String baseUrl = "http://www.uoguelph.ca/~hydrogeo/Whitebox/VersionInfo.xml";
                        URL url = new URL(baseUrl);

                        //open a connection to that source
                        HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

                        //trying to retrieve data from the source. If there
                        //is no connection, this line will fail
                        Object objData = urlConnect.getContent();

                        InputStream inputStream = (InputStream) urlConnect.getContent();
                        DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
                        docbf.setNamespaceAware(true);
                        DocumentBuilder docbuilder = docbf.newDocumentBuilder();
                        Document document = docbuilder.parse(inputStream, baseUrl);

                        document.getDocumentElement().normalize();
                        Element docElement = document.getDocumentElement();

                        Element el;
                        NodeList nl = docElement.getElementsByTagName("VersionName");
                        if (nl.getLength() > 0) {
                            el = (Element) nl.item(0);
                            currentVersionName = el.getFirstChild().getNodeValue().replace("\"", "");
                        }

                        nl = docElement.getElementsByTagName("VersionNumber");
                        if (nl.getLength() > 0) {
                            el = (Element) nl.item(0);
                            currentVersionNumber = el.getFirstChild().getNodeValue().replace("\"", "");
                        }

                        nl = docElement.getElementsByTagName("DownloadLocation");
                        if (nl.getLength() > 0) {
                            el = (Element) nl.item(0);
                            downloadLocation = el.getFirstChild().getNodeValue().replace("\"", "");
                        }

                        nl = docElement.getElementsByTagName("DownloadArtifact");
                        if (nl.getLength() > 0) {
                            el = (Element) nl.item(0);
                            updateDownloadArtifact = el.getFirstChild().getNodeValue().replace("\"", "");
                        }

                        if (receiveAnnouncements) {
                            // read the announcement data, if any
                            nl = docElement.getElementsByTagName("Announcements");
                            if (nl != null && nl.getLength() > 0) {
                                el = (Element) nl.item(0);
                                int thisAnnouncementNumber = Integer.parseInt(el.getAttribute("number"));
                                if (thisAnnouncementNumber > announcementNumber) {
                                    NodeList nl2 = el.getElementsByTagName("Announcement");
                                    if (nl2.getLength() > 0) {
                                        for (int i = 0; i < nl2.getLength(); i++) {
                                            Element el2 = (Element) nl2.item(i);
                                            String date = getTextValue(el2, "Date");
                                            String title = getTextValue(el2, "Title");
                                            String message = getTextValue(el2, "Message");
                                            if (!message.replace("\n", "").isEmpty()) {
                                                WhiteboxAnnouncement wba
                                                        = new WhiteboxAnnouncement(message, title, date);
                                                announcements.add(wba);
                                            }
                                        }
                                    }
                                    announcementNumber = thisAnnouncementNumber;
                                }
                            }

                            if (announcements.size() > 0) {
                                displayAnnouncements();
                            }

                        }

                        if (currentVersionName.isEmpty()
                                || currentVersionNumber.isEmpty()
                                || downloadLocation.isEmpty()) {
                            return;
                        }

                        if (checkForUpdates) {
                            if (Integer.parseInt(versionNumber.replace(".", ""))
                                    < Integer.parseInt(currentVersionNumber.replace(".", ""))
                                    && Integer.parseInt(skipVersionNumber.replace(".", ""))
                                    < Integer.parseInt(currentVersionNumber.replace(".", ""))) {
                                //Custom button text
                                Object[] options = {"Yes, proceed to download site", "Not now", "Don't ask again"};
                                int n = JOptionPane.showOptionDialog(null,
                                        "A newer version is available. "
                                        + "Would you like to download Whitebox "
                                        + currentVersionName + " (" + currentVersionNumber
                                        + ")?" + "\nYou are currently using Whitebox " + versionName
                                        + " (" + versionNumber + ").",
                                        "Whitebox Version",
                                        JOptionPane.YES_NO_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        options,
                                        options[0]);

                                if (n == 0) {
                                    Desktop d = Desktop.getDesktop();
                                    d.browse(new URI(downloadLocation));
                                } else if (n == 2) {
                                    skipVersionNumber = currentVersionNumber;
                                }
                            }
                        }
                    }
                } catch (UnknownHostException e) {
                    logger.log(Level.WARNING, "WhiteboxGui.checkVersionIsUpToDate", e);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "WhiteboxGui.checkVersionIsUpToDate", e);
                } catch (HeadlessException | NumberFormatException | URISyntaxException | ParserConfigurationException | DOMException | SAXException e) {
                    logger.log(Level.SEVERE, "WhiteboxGui.checkVersionIsUpToDate", e);
                }
            }
        });
        return true;
    }

    private String getTextValue(Element ele, String tagName) {
        String textVal = "";
        try {
            NodeList nl = ele.getElementsByTagName(tagName);
            if (nl != null && nl.getLength() > 0) {
                Element el = (Element) nl.item(0);
                textVal = el.getFirstChild().getNodeValue().replace("\"", "");
            }
        } catch (DOMException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.getTextValue", e);
        }
        return textVal;
    }

    private boolean checkForNewInstallation() {
        if (userName == null || !userName.equals(System.getProperty("user.name"))) {

            userName = System.getProperty("user.name");

            final JDialog dialog = new JDialog(this, "", true);

            Box mainBox = Box.createVerticalBox();
            mainBox.add(Box.createVerticalStrut(15));

            Box hbox1 = Box.createHorizontalBox();
            hbox1.add(Box.createHorizontalGlue());
            String message = messages.getString("Welcome") + " Whitebox " + userName + ".";
            JLabel welcomeLabel = new JLabel(message);
            hbox1.add(welcomeLabel);
            hbox1.add(Box.createHorizontalGlue());
            mainBox.add(hbox1);

            mainBox.add(Box.createVerticalStrut(15));

            Box hbox2 = Box.createHorizontalBox();
            hbox2.add(Box.createHorizontalStrut(15));
            hbox2.add(new JLabel("Please select your preferred language..."));
            hbox2.add(Box.createHorizontalGlue());
            mainBox.add(hbox2);

            mainBox.add(Box.createVerticalStrut(5));

            Box hbox3 = Box.createHorizontalBox();
            hbox3.add(Box.createHorizontalStrut(15));
            ComboBoxProperty languageChooser
                    = SupportedLanguageChooser.getLanguageChooser(this, true);
            languageChooser.setName("languageChooser");
            hbox3.add(languageChooser);
            hbox3.add(Box.createHorizontalStrut(15));
            mainBox.add(hbox3);

            mainBox.add(Box.createVerticalStrut(15));

            Box btnBox = Box.createHorizontalBox();
            btnBox.add(Box.createHorizontalGlue());
            JButton ok = new JButton(bundle.getString("OK"));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                }
            });
            btnBox.add(ok);

            btnBox.add(Box.createHorizontalGlue());

            mainBox.add(btnBox);

            mainBox.add(Box.createVerticalStrut(15));

            Container contentPane = dialog.getContentPane();
            contentPane.add(mainBox, BorderLayout.CENTER);
            dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            dialog.pack();

            // centers on screen
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

            return true;
        }
        return false;
    }

    private ArrayList<InteroperableGeospatialDataFormat> interopGeospatialDataFormat;

    private void loadPlugins() {
        pluginService = PluginServiceFactory.createPluginService(pluginDirectory);
        pluginService.initPlugins();
        plugInfo = pluginService.getPluginList();
        interopGeospatialDataFormat = pluginService.getInteroperableDataFormats();

        loadScripts();
    }

    private void loadScripts() {
        ArrayList<String> pythonScripts = FileUtilities.findAllFilesWithExtension(resourcesDirectory, ".py", true);
        ArrayList<String> groovyScripts = FileUtilities.findAllFilesWithExtension(resourcesDirectory, ".groovy", true);
        ArrayList<String> jsScripts = FileUtilities.findAllFilesWithExtension(resourcesDirectory, ".js", true);
        for (String str : pythonScripts) {
            try {
                // Open the file
                FileInputStream fstream = new FileInputStream(str);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;

                //Read File Line By Line
                boolean containsName = false;
                boolean containsDescriptiveName = false;
                boolean containsDescription = false;
                boolean containsToolboxes = false;
                String name = "";
                String descriptiveName = "";
                String description = "";
                String[] toolboxes = null;
                boolean containsExtensions = false;
                boolean containsFileTypeName = false;
                boolean containsIsRasterFormat = false;
                boolean containsPluginType = false;
                String[] extensions = null;
                String fileTypeName = "";
                boolean isRasterFormat = false;
                InteropPluginType pluginType = InteropPluginType.importPlugin;

                boolean containsParentMenu = false;
                boolean containsMenuLabel = false;
                boolean containsKeyStroke = false;
                String parentMenu = "";
                String menuLabel = "";
                char keyStroke = 0;

                while ((strLine = br.readLine()) != null
                        && (!containsName || !containsDescriptiveName
                        || !containsDescription || !containsToolboxes
                        || !containsPluginType)) {
                    if (!strLine.startsWith("#")) {
                        if (strLine.startsWith("name = \"") && name.isEmpty()
                                && !strLine.toLowerCase().contains("descriptivename")
                                && !strLine.toLowerCase().contains("filetypename")) {
                            containsName = true;
                            // now retreive the name
                            String[] str2 = strLine.split("=");
                            name = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.startsWith("descriptiveName = \"")) {
                            containsDescriptiveName = true;
                            String[] str2 = strLine.split("=");
                            descriptiveName = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.startsWith("description = \"")) {
                            containsDescription = true;
                            String[] str2 = strLine.split("=");
                            description = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.startsWith("toolboxes = [\"")) {
                            containsToolboxes = true;
                            String[] str2 = strLine.split("=");
                            toolboxes = str2[str2.length - 1].replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").trim().split(",");
                            for (int i = 0; i < toolboxes.length; i++) {
                                toolboxes[i] = toolboxes[i].trim();
                            }
                        } else if (strLine.startsWith("extensions = [")) {
                            containsExtensions = true;
                            String[] str2 = strLine.split("=");
                            extensions = str2[str2.length - 1].replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").trim().split(",");
                            for (int i = 0; i < extensions.length; i++) {
                                extensions[i] = extensions[i].trim();
                            }
                        } else if (strLine.startsWith("fileTypeName = \"")) {
                            containsFileTypeName = true;
                            String[] str2 = strLine.split("=");
                            fileTypeName = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.startsWith("isRasterFormat = ")) {
                            containsIsRasterFormat = true;
                            String[] str2 = strLine.split("=");
                            isRasterFormat = Boolean.parseBoolean(str2[str2.length - 1].replace("\"", "").replace("\'", "").trim());
                        } else if (strLine.startsWith("interopPluginType = InteropPluginType")) {
                            containsPluginType = true;
                            if (strLine.toLowerCase().contains("import")) {
                                pluginType = InteropPluginType.importPlugin;
                            } else {
                                pluginType = InteropPluginType.exportPlugin;
                            }
                        } else if (strLine.toLowerCase().contains("parentmenu = \"")) {
                            containsParentMenu = true;
                            String[] str2 = strLine.split("=");
                            parentMenu = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("menulabel = \"")) {
                            containsMenuLabel = true;
                            String[] str2 = strLine.split("=");
                            menuLabel = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("acceleratorkey = \"")) {
                            containsKeyStroke = true;
                            String[] str2 = strLine.split("=");
                            String str3 = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                            keyStroke = str3.charAt(0);
                        }
                    }
                }

                //Close the input stream
                br.close();

                if (containsName && containsDescriptiveName
                        && containsDescription && containsToolboxes) {
                    // it's a plugin!
                    if (pluginBundle.containsKey(name)) {
                        descriptiveName = pluginBundle.getString(name);
                    }
                    PluginInfo pi = new PluginInfo(name, descriptiveName,
                            description, toolboxes, PluginInfo.SORT_MODE_NAMES);
                    pi.setScript(true);
                    pi.setScriptFile(str);
                    plugInfo.add(pi);
                }

                if (containsExtensions && containsFileTypeName && containsIsRasterFormat
                        && containsPluginType) {
                    interopGeospatialDataFormat.add(new InteroperableGeospatialDataFormat(fileTypeName,
                            extensions, name, isRasterFormat, pluginType));
                }

                if (containsParentMenu && containsMenuLabel) {
                    if (!parentMenu.isEmpty()) {
                        MenuExtension me;
                        if (parentMenu.toLowerCase().contains("file")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.FILE, str);
                        } else if (parentMenu.toLowerCase().contains("layers")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.LAYERS, str);
                        } else if (parentMenu.toLowerCase().contains("view")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.VIEW, str);
                        } else if (parentMenu.toLowerCase().contains("carto")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.CARTOGRAPHIC, str);
                        } else if (parentMenu.toLowerCase().contains("tools")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.TOOLS, str);
                        } else if (parentMenu.toLowerCase().contains("help")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.HELP, str);
                        } else {
                            logger.log(Level.SEVERE, "WhiteboxGui.loadScripts", "Error adding menu extension");
                            me = null;
                        }
                        if (containsKeyStroke) {
                            me.setAcceleratorKeyStroke(keyStroke);
                        }
                        menuExtensions.add(me);
                    }
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "WhiteboxGui.loadScripts", ioe);
            }
        }

        for (String str : jsScripts) {
            try {
                // Open the file
                FileInputStream fstream = new FileInputStream(str);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;

                //Read File Line By Line
                boolean containsName = false;
                boolean containsDescriptiveName = false;
                boolean containsDescription = false;
                boolean containsToolboxes = false;
                String name = "";
                String descriptiveName = "";
                String description = "";
                String[] toolboxes = null;
                while ((strLine = br.readLine()) != null
                        && (!containsName || !containsDescriptiveName
                        || !containsDescription || !containsToolboxes)) {
                    if (strLine.contains("name = \"") && name.isEmpty()
                            && !strLine.toLowerCase().contains("descriptivename")) {
                        containsName = true;
                        // now retreive the name
                        String[] str2 = strLine.split("=");
                        name = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                    } else if (strLine.toLowerCase().contains("descriptivename = \"")) {
                        containsDescriptiveName = true;
                        String[] str2 = strLine.split("=");
                        descriptiveName = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                    } else if (strLine.toLowerCase().contains("description = \"")) {
                        containsDescription = true;
                        String[] str2 = strLine.split("=");
                        description = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                    } else if (strLine.toLowerCase().contains("toolboxes = [\"")) {
                        containsToolboxes = true;
                        String[] str2 = strLine.split("=");
                        toolboxes = str2[str2.length - 1].replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").trim().split(",");
                        for (int i = 0; i < toolboxes.length; i++) {
                            toolboxes[i] = toolboxes[i].trim();
                        }
                    }
                }

                //Close the input stream
                br.close();

                if (containsName && containsDescriptiveName
                        && containsDescription && containsToolboxes) {
                    // it's a plugin!
                    if (pluginBundle.containsKey(name)) {
                        descriptiveName = pluginBundle.getString(name);
                    }
                    PluginInfo pi = new PluginInfo(name, descriptiveName,
                            description, toolboxes, PluginInfo.SORT_MODE_NAMES);
                    pi.setScript(true);
                    pi.setScriptFile(str);
                    plugInfo.add(pi);
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "WhiteboxGui.loadScripts", ioe);
            }
        }

        for (String str : groovyScripts) {
            try {
                // Open the file
                FileInputStream fstream = new FileInputStream(str);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

                String strLine;

                //Read File Line By Line
                boolean containsName = false;
                boolean containsDescriptiveName = false;
                boolean containsDescription = false;
                boolean containsToolboxes = false;
                String name = "";
                String descriptiveName = "";
                String description = "";
                String[] toolboxes = null;

                boolean containsExtensions = false;
                boolean containsFileTypeName = false;
                boolean containsIsRasterFormat = false;
                boolean containsPluginType = false;
                String[] extensions = null;
                String fileTypeName = "";
                boolean isRasterFormat = false;
                InteropPluginType pluginType = InteropPluginType.importPlugin;

                boolean containsParentMenu = false;
                boolean containsMenuLabel = false;
                boolean containsKeyStroke = false;
                String parentMenu = "";
                String menuLabel = "";
                char keyStroke = 0;

                while ((strLine = br.readLine()) != null
                        && (!containsName || !containsDescriptiveName
                        || !containsDescription || !containsToolboxes
                        || !containsExtensions || !containsFileTypeName
                        || !containsIsRasterFormat || !containsPluginType
                        || containsParentMenu || !containsMenuLabel)) {
                    if (!strLine.startsWith("//")) {
                        if (strLine.contains("name = \"") && name.isEmpty()
                                && !strLine.toLowerCase().contains("descriptivename")
                                && !strLine.toLowerCase().contains("filetypename")) {
                            containsName = true;
                            // now retreive the name
                            String[] str2 = strLine.split("=");
                            name = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("descriptivename = \"")) {
                            containsDescriptiveName = true;
                            String[] str2 = strLine.split("=");
                            descriptiveName = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("description = \"")) {
                            containsDescription = true;
                            String[] str2 = strLine.split("=");
                            description = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("toolboxes = [\"")) {
                            containsToolboxes = true;
                            String[] str2 = strLine.split("=");
                            toolboxes = str2[str2.length - 1].replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").trim().split(",");
                            for (int i = 0; i < toolboxes.length; i++) {
                                toolboxes[i] = toolboxes[i].trim();
                            }
                        } else if (strLine.toLowerCase().contains("extensions = [")) {
                            containsExtensions = true;
                            String[] str2 = strLine.split("=");
                            extensions = str2[str2.length - 1].replace("\"", "").replace("\'", "").replace("[", "").replace("]", "").trim().split(",");
                            for (int i = 0; i < extensions.length; i++) {
                                extensions[i] = extensions[i].trim();
                            }
                        } else if (strLine.toLowerCase().contains("filetypename = \"")) {
                            containsFileTypeName = true;
                            String[] str2 = strLine.split("=");
                            fileTypeName = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("israsterformat = ")) {
                            containsIsRasterFormat = true;
                            String[] str2 = strLine.split("=");
                            isRasterFormat = Boolean.parseBoolean(str2[str2.length - 1].replace("\"", "").replace("\'", "").trim());
                        } else if (strLine.toLowerCase().contains("interopplugintype = interopplugintype")) {
                            containsPluginType = true;
                            if (strLine.toLowerCase().contains("import")) {
                                pluginType = InteropPluginType.importPlugin;
                            } else {
                                pluginType = InteropPluginType.exportPlugin;
                            }
                        } else if (strLine.toLowerCase().contains("parentmenu = \"")) {
                            containsParentMenu = true;
                            String[] str2 = strLine.split("=");
                            parentMenu = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("menulabel = \"")) {
                            containsMenuLabel = true;
                            String[] str2 = strLine.split("=");
                            menuLabel = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                        } else if (strLine.toLowerCase().contains("acceleratorkey = \"")) {
                            containsKeyStroke = true;
                            String[] str2 = strLine.split("=");
                            String str3 = str2[str2.length - 1].replace("\"", "").replace("\'", "").trim();
                            keyStroke = str3.charAt(0);
                        }
                    }
                }

                //Close the input stream
                br.close();

                if (containsName && containsDescriptiveName
                        && containsDescription && containsToolboxes) {
                    // it's a plugin!
                    if (pluginBundle.containsKey(name)) {
                        descriptiveName = pluginBundle.getString(name);
                    }
                    PluginInfo pi = new PluginInfo(name, descriptiveName,
                            description, toolboxes, PluginInfo.SORT_MODE_NAMES);
                    pi.setScript(true);
                    pi.setScriptFile(str);
                    plugInfo.add(pi);
                }

                if (containsExtensions && containsFileTypeName && containsIsRasterFormat
                        && containsPluginType) {
                    interopGeospatialDataFormat.add(new InteroperableGeospatialDataFormat(fileTypeName,
                            extensions, name, isRasterFormat, pluginType));
                }

                if (containsParentMenu && containsMenuLabel) {
                    if (!parentMenu.isEmpty()) {
                        MenuExtension me;
                        if (parentMenu.toLowerCase().contains("file")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.FILE, str);
                        } else if (parentMenu.toLowerCase().contains("layers")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.LAYERS, str);
                        } else if (parentMenu.toLowerCase().contains("view")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.VIEW, str);
                        } else if (parentMenu.toLowerCase().contains("carto")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.CARTOGRAPHIC, str);
                        } else if (parentMenu.toLowerCase().contains("tools")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.TOOLS, str);
                        } else if (parentMenu.toLowerCase().contains("help")) {
                            me = new MenuExtension(menuLabel, MenuExtension.ParentMenu.HELP, str);
                        } else {
                            logger.log(Level.SEVERE, "WhiteboxGui.loadScripts", "Error adding menu extension");
                            me = null;
                        }
                        if (containsKeyStroke) {
                            me.setAcceleratorKeyStroke(keyStroke);
                        }
                        menuExtensions.add(me);
                    }
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "WhiteboxGui.loadScripts", ioe);
            }
        }
    }
    private ArrayList<WhiteboxPlugin> activePlugs = new ArrayList<>();

    @Override
    public List returnPluginList() {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < plugInfo.size(); i++) {
            ret.add(plugInfo.get(i).getName());
        }
        Collections.sort(ret);
        return ret;
    }

    public boolean isPluginAScript(String pluginName) {
        boolean isScript = false;
        for (int i = 0; i < plugInfo.size(); i++) {
            PluginInfo pi = plugInfo.get(i);
            if (pi.getDescriptiveName().equals(pluginName)
                    || pi.getName().equals(pluginName)) {
                pi.setLastUsedToNow();
                pi.incrementNumTimesUsed();
                if (pi.isScript()) {
                    isScript = true;
                }
                break;
            }
        }
        return isScript;
    }

    @Override
    public void runPlugin(String pluginName, String[] args, boolean runOnDedicatedThread) {
        try {
            if (!runOnDedicatedThread) { // run on current thread
                boolean isScript = false;
                String scriptFile = null;
                for (int i = 0; i < plugInfo.size(); i++) {
                    PluginInfo pi = plugInfo.get(i);
                    if (pi.getDescriptiveName().equals(pluginName)
                            || pi.getName().equals(pluginName)) {
                        pi.setLastUsedToNow();
                        pi.incrementNumTimesUsed();
                        if (pi.isScript()) {
                            isScript = true;
                            scriptFile = pi.getScriptFile();
                        }
                        break;
                    }
                }

                if (!isScript) {
                    requestForOperationCancel = false;
                    WhiteboxPlugin plug = pluginService.getPlugin(pluginName, StandardPluginService.SIMPLE_NAME);
                    if (plug == null) {
                        throw new Exception("Plugin not located.");
                    }
                    plug.setPluginHost(this);
                    plug.setArgs(args);
                    plug.run();
                } else {
                    // what is the scripting language?
                    if (scriptFile == null) {
                        return; // can't find scriptFile
                    }

                    String myScriptingLanguage;
                    if (scriptFile.toLowerCase().endsWith(".py")) {
                        myScriptingLanguage = "python";
                    } else if (scriptFile.toLowerCase().endsWith(".groovy")) {
                        myScriptingLanguage = "groovy";
                    } else if (scriptFile.toLowerCase().endsWith(".js")) {
                        myScriptingLanguage = "javascript";
                    } else {
                        showFeedback("Unsupported script type.");
                        return;
                    }

                    // has the scripting engine been initialized for the current script language.
                    //if (engine == null || !myScriptingLanguage.equals(scriptingLanguage)) {
                    scriptingLanguage = myScriptingLanguage;
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    final ScriptEngine newEngine = mgr.getEngineByName(scriptingLanguage);
                    //}

//                    PrintWriter out = new PrintWriter(new TextAreaWriter(textArea));
//                    newEngine.getContext().setWriter(out);
                    if (scriptingLanguage.equals("python")) {
                        newEngine.put("__file__", scriptFile);
                    }
                    requestForOperationCancel = false;
                    newEngine.put("pluginHost", (WhiteboxPluginHost) this);
                    newEngine.put("args", args);

                    // run the script
//                    PrintWriter errOut = new PrintWriter(new TextAreaWriter(textArea));
                    try {
                        // read the contents of the file
                        final String scriptContents = new String(Files.readAllBytes(Paths.get(scriptFile)));

                        Object result = newEngine.eval(scriptContents);
                    } catch (IOException | ScriptException e) {
                        showFeedback(e.getMessage());
//                        errOut.append(e.getMessage() + "\n");
                    }
                }

            } else { // run on a dedicated thread
                runPlugin(pluginName, args);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.runPlugin", e);
        }
    }

    public void isPluginReturnDataSuppressed(boolean value) {
        multiOperationsuppressReturnedData = value;
    }

    private boolean suppressReturnedData;
    private boolean multiOperationsuppressReturnedData = false;

    @Override
    public void runPlugin(String pluginName, String[] args, boolean runOnDedicatedThread,
            boolean suppressReturnedData) {
        try {
            this.suppressReturnedData = suppressReturnedData;
            runPlugin(pluginName, args, runOnDedicatedThread);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.runPlugin", e);
        }
    }

    @Override
    public void runPlugin(String pluginName, String[] args) {
        try {
            boolean isScript = false;
            String scriptFile = null;
            for (int i = 0; i < plugInfo.size(); i++) {
                PluginInfo pi = plugInfo.get(i);
                if (pi.getDescriptiveName().equals(pluginName)
                        || pi.getName().equals(pluginName)) {
                    pi.setLastUsedToNow();
                    pi.incrementNumTimesUsed();
                    if (pi.isScript()) {
                        isScript = true;
                        scriptFile = pi.getScriptFile();
                    }
                    break;
                }
            }

            if (!isScript) {
                requestForOperationCancel = false;
                WhiteboxPlugin plug = pluginService.getPlugin(pluginName, StandardPluginService.SIMPLE_NAME);
                if (plug == null) {
                    throw new Exception("Plugin not located.");
                }
                plug.setPluginHost(this);
                plug.setArgs(args);
                activePlugs.add(plug);
                if (plug instanceof NotifyingThread) {
                    NotifyingThread t = (NotifyingThread) (plug);
                    t.addListener(this);
                }
                new Thread(plug).start();
            } else {
                // what is the scripting language?
                if (scriptFile == null) {
                    return; // can't find scriptFile
                }

                String myScriptingLanguage;
                if (scriptFile.toLowerCase().endsWith(".py")) {
                    myScriptingLanguage = "python";
                } else if (scriptFile.toLowerCase().endsWith(".groovy")) {
                    myScriptingLanguage = "groovy";
                } else if (scriptFile.toLowerCase().endsWith(".js")) {
                    myScriptingLanguage = "javascript";
                } else {
                    showFeedback("Unsupported script type.");
                    return;
                }

                // has the scripting engine been initialized for the current script language.
                if (engine == null || !myScriptingLanguage.equals(scriptingLanguage)) {
                    scriptingLanguage = myScriptingLanguage;
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    engine = mgr.getEngineByName(scriptingLanguage);
                }
                if (scriptingLanguage.equals("python")) {
                    engine.put("__file__", scriptFile);
                }
                requestForOperationCancel = false;
                engine.put("pluginHost", (WhiteboxPluginHost) this);
                engine.put("args", args);

                // run the script
                // read the contents of the file
                final String scriptContents = new String(Files.readAllBytes(Paths.get(scriptFile)));
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            engine.eval(scriptContents);
                        } catch (ScriptException e) {
                            System.out.println(e.getStackTrace());
                        }
                    }
                };
                final Thread t = new Thread(r);
                t.start();

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.runPlugin", e);
        }
    }

    private void executeScriptFile(String scriptFile, String[] args, boolean runOnDedicatedThread) {
        try {
            // what is the scripting language?
            if (scriptFile == null) {
                return; // can't find scriptFile
            }

            String myScriptingLanguage;
            if (scriptFile.toLowerCase().endsWith(".py")) {
                myScriptingLanguage = "python";
            } else if (scriptFile.toLowerCase().endsWith(".groovy")) {
                myScriptingLanguage = "groovy";
            } else if (scriptFile.toLowerCase().endsWith(".js")) {
                myScriptingLanguage = "javascript";
            } else {
                showFeedback("Unsupported script type.");
                return;
            }

            // has the scripting engine been initialized for the current script language.
            if (engine == null || !myScriptingLanguage.equals(scriptingLanguage)) {
                scriptingLanguage = myScriptingLanguage;
                ScriptEngineManager mgr = new ScriptEngineManager();
                engine = mgr.getEngineByName(scriptingLanguage);
            }

            if (scriptingLanguage.equals("python")) {
                engine.put("__file__", scriptFile);
            }
            requestForOperationCancel = false;
            engine.put("pluginHost", (WhiteboxPluginHost) this);
            engine.put("args", args);

            // run the script
            // read the contents of the file
            final String scriptContents = new String(Files.readAllBytes(Paths.get(scriptFile)));

            if (runOnDedicatedThread) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            engine.eval(scriptContents);
                        } catch (ScriptException e) {
                            logger.log(Level.SEVERE, "WhiteboxGui.executeScriptFile", e);
                        }
                    }
                };
                final Thread t = new Thread(r);
                t.start();
            } else {
                engine.eval(scriptContents);
            }
        } catch (IOException | ScriptException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.executeScriptFile", e);
        }
    }

    private boolean automaticallyDisplayReturns = true;

    @Override
    public void returnData(Object ret) {
        try {
            if (!suppressReturnedData && !multiOperationsuppressReturnedData) {

                // this is where all of the data returned by plugins is handled.
                if (ret instanceof String) {
                    String retStr = ret.toString();
                    if (retStr.endsWith(".dep") && retStr.contains(pathSep)) {
                        if (automaticallyDisplayReturns) {
                            addLayer(retStr);
                        }
                    } else if (retStr.endsWith(".shp") && retStr.contains(pathSep)) {
                        if (automaticallyDisplayReturns) {
                            addLayer(retStr);
                        }
                    } else if (retStr.toLowerCase().endsWith(".dbf") && retStr.contains(pathSep)) {
                        AttributesFileViewer afv = new AttributesFileViewer(this, false, retStr.replace(".dbf", ".shp"));
                        int height = 500;
                        afv.setSize((int) (height * 1.61803399), height); // golden ratio.
                        afv.setVisible(true);
                    } else if (retStr.endsWith(".html") && retStr.contains(pathSep)) {
                        // display this markup in a webbrowser component
                        try {
                            JFrame frame = new HTMLViewer(this, retStr);
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.setSize(600, 600);
                            frame.setVisible(true);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "WhiteboxGui.returnData", e);
                        }
                    } else if (retStr.contains("<html") && retStr.contains("</html>")) {
                        // display this markup in a webbrowser component
                        try {
                            JFrame frame = new HTMLViewer(this, retStr);
                            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frame.setSize(600, 600);
                            frame.setVisible(true);
                        } catch (Exception e) {
                            logger.log(Level.SEVERE, "WhiteboxGui.returnData", e);
                        }
                    } else if (retStr.toLowerCase().startsWith("newmap")) {
                        String mapName = "NewMap";
                        if (retStr.contains(":")) {
                            String[] val = retStr.split(":");
                            mapName = val[val.length - 1].trim();
                        }
                        newMap(mapName);
                    } else {
                        viewTextDialog(retStr);
                    }
                } else if (ret instanceof JPanel) {
                    // Create a dialog and place it in that. Then display the dialog.
                    JDialog dialog = new JDialog(this, "");
                    Container contentPane = dialog.getContentPane();
                    JPanel panel = (JPanel) ret;
                    contentPane.add(panel, BorderLayout.CENTER);
                    if (panel.getPreferredSize().height > 100) {
                        dialog.setPreferredSize(panel.getPreferredSize());
                    } else {
                        dialog.setPreferredSize(new Dimension(500, 500));
                    }
                    if (panel.getName() != null) {
                        dialog.setTitle(panel.getName());
                    }
                    dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    dialog.pack();
                    dialog.setLocationRelativeTo(null);
                    dialog.setVisible(true);
                } else if (ret instanceof MapLayer) {
                    if (automaticallyDisplayReturns) {
                        addLayer((MapLayer) ret);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.returnData", e);
        } finally {
            suppressReturnedData = false;
            fireReturnedDataEvent(new ReturnedDataEvent(this, ret));
        }
    }

    private void viewTextDialog(String text) {
        ViewTextDialog vtd = new ViewTextDialog(this, false);
        if (!text.isEmpty()) {
            vtd.setText(text);
        }
        vtd.setVisible(true);
    }

    private void viewHtmlDialog(String text) {
        try {
            JFrame frame = new HTMLViewer(this, text);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(600, 600);
            frame.setVisible(true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.returnData", e);
        }
    }

    // Create the listener list
    protected javax.swing.event.EventListenerList returnedDataListenerList
            = new javax.swing.event.EventListenerList();

    // This methods allows classes to register for ReturnedDataEvents
    public void addReturnedDataEventListener(ReturnedDataListener listener) {
        returnedDataListenerList.add(ReturnedDataListener.class, listener);
    }

    // This methods allows classes to unregister for ReturnedDataEvents
    public void removeReturnedDataEventListener(ReturnedDataListener listener) {
        returnedDataListenerList.remove(ReturnedDataListener.class, listener);
    }

    // This private class is used to fire ReturnedDataEvents
    void fireReturnedDataEvent(ReturnedDataEvent evt) {
        Object[] listeners = returnedDataListenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ReturnedDataListener.class) {
                ((ReturnedDataListener) listeners[i + 1]).dataReturned(evt);
            }
        }
    }

    private boolean isToolAScript(String pluginName) {
        boolean isScript = false;
        for (int i = 0; i < plugInfo.size(); i++) {
            PluginInfo pi = plugInfo.get(i);
            if (pi.getDescriptiveName().equals(pluginName)
                    || pi.getName().equals(pluginName)) {
                pi.setLastUsedToNow();
                pi.incrementNumTimesUsed();
                if (pi.isScript()) {
                    isScript = true;
                }
                break;
            }
        }
        return isScript;
    }

    private String getScriptFile(String pluginName) {
        String scriptFile = null;
        for (int i = 0; i < plugInfo.size(); i++) {
            PluginInfo pi = plugInfo.get(i);
            if (pi.getDescriptiveName().equals(pluginName)
                    || pi.getName().equals(pluginName)) {
                pi.setLastUsedToNow();
                pi.incrementNumTimesUsed();
                if (pi.isScript()) {
                    scriptFile = pi.getScriptFile();
                }
                break;
            }
        }
        return scriptFile;
    }

    @Override
    public void launchDialog(String pluginName) {
        // update the tools lists
        populateToolTabs();

        boolean isScript = false;
        String scriptFile = null;
        for (int i = 0; i < plugInfo.size(); i++) {
            PluginInfo pi = plugInfo.get(i);
            if (pi.getDescriptiveName().equals(pluginName)
                    || pi.getName().equals(pluginName)) {
                pi.setLastUsedToNow();
                pi.incrementNumTimesUsed();
                if (pi.isScript()) {
                    isScript = true;
                    scriptFile = pi.getScriptFile();
                }
                break;
            }
        }

        if (!isScript) {

            WhiteboxPlugin plug = pluginService.getPlugin(pluginName, StandardPluginService.DESCRIPTIVE_NAME);

            // does this plugin provide it's own dialog?
            boolean pluginProvidesDialog = false;
            String parameterFile = FileUtilities.findFileInDirectory(new File(resourcesDirectory + "plugins" + pathSep), plug.getName() + ".xml");
            File file = new File(parameterFile);

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);
                doc.getDocumentElement().normalize();
                Node topNode = doc.getFirstChild();
                Element docElement = doc.getDocumentElement();

                NodeList nl = docElement.getElementsByTagName("DialogComponent");
                String componentType;
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {

                        Element el = (Element) nl.item(i);
                        componentType = el.getAttribute("type");

                        if (componentType.equals("CustomDialogProvidedByPlugin")) {
                            pluginProvidesDialog = true;
                            break;
                        }
                    }
                }
            } catch (ParserConfigurationException | SAXException | IOException e) {
                logger.log(Level.SEVERE, "WhiteboxGui.launchDialog", e);
            }

            if (pluginProvidesDialog) {
                String[] args = {""};
                runPlugin(plug.getName(), args);
            } else {
                // use the xml-based dialog provided in the Dialog folder.
                String helpFile = helpDirectory + plug.getName() + ".html";
                String descriptiveName;
                if (pluginBundle.containsKey(plug.getName())) {
                    descriptiveName = pluginBundle.getString(plug.getName());
                } else {
                    descriptiveName = plug.getDescriptiveName();
                }
                ToolDialog dlg = new ToolDialog(this, false, plug.getName(), descriptiveName, helpFile);
                dlg.setSize(800, 400);
                dlg.setVisible(true);
            }
        } else {
            // what is the scripting language?
            if (scriptFile == null) {
                return; // can't find scriptFile
            }

            String myScriptingLanguage;
            if (scriptFile.toLowerCase().endsWith(".py")) {
                myScriptingLanguage = "python";
            } else if (scriptFile.toLowerCase().endsWith(".groovy")) {
                myScriptingLanguage = "groovy";
            } else if (scriptFile.toLowerCase().endsWith(".js")) {
                myScriptingLanguage = "javascript";
            } else {
                showFeedback("Unsupported script type.");
                return;
            }

            // has the scripting engine been initialized for the current script language.
            if (engine == null || !myScriptingLanguage.equals(scriptingLanguage)) {
                scriptingLanguage = myScriptingLanguage;
                ScriptEngineManager mgr = new ScriptEngineManager();
                engine = mgr.getEngineByName(scriptingLanguage);
            }
            if (scriptingLanguage.equals("python")) {
                engine.put("__file__", scriptFile);
            }
            requestForOperationCancel = false;
            engine.put("pluginHost", (WhiteboxPluginHost) this);
            engine.put("args", new String[0]);
            // run the script
            try {
                // read the contents of the file
                String scriptContents = new String(Files.readAllBytes(Paths.get(scriptFile)));

                Object result = engine.eval(scriptContents);
            } catch (IOException | ScriptException e) {
                logger.log(Level.SEVERE, "WhiteboxGui.launchDialog", e);
            }
        }
    }
    private ScriptEngine engine;
    private String scriptingLanguage = "python";

    @Override
    public MapLayer getActiveMapLayer() {
        int mapNum;
        int mapAreaNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is no mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return null;
            }
        }

        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return null;
        }
        return ma.getActiveLayer();
    }

    @Override
    public ArrayList<MapLayer> getAllMapLayers() {
        ArrayList<MapLayer> ret = new ArrayList<>();
        for (MapInfo mi : openMaps) {
            for (MapArea ma : mi.getMapAreas()) {
                for (MapLayer ml : ma.getLayersList()) {
                    ret.add(ml);
                }
            }
        }
        return ret;
    }

    public final class TextAreaWriter extends Writer {

        private final JTextArea textArea;

        public TextAreaWriter(final JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            textArea.append(new String(cbuf, off, len));
        }
    }
    private boolean requestForOperationCancel = false;

    /**
     * Used to communicate a request to cancel an operation
     */
    @Override
    public boolean isRequestForOperationCancelSet() {
        return requestForOperationCancel;
    }

    /**
     * Used to ensure that there is no active cancel operation request
     */
    @Override
    public void resetRequestForOperationCancel() {
        requestForOperationCancel = false;
    }

    @Override
    public void cancelOperation() {
        requestForOperationCancel = true;

        Iterator<WhiteboxPlugin> iterator = activePlugs.iterator();
        while (iterator.hasNext()) {
            WhiteboxPlugin plugin = iterator.next();
            plugin.setCancelOp(true);
        }
        activePlugs.clear();
    }

    @Override
    public void pluginComplete() {
        // remove inactive plugins from activePlugs.
        Iterator<WhiteboxPlugin> iterator = activePlugs.iterator();
        ArrayList<WhiteboxPlugin> toRemove = new ArrayList<>();
        while (iterator.hasNext()) {
            WhiteboxPlugin plugin = iterator.next();
            if (!plugin.isActive()) {
                toRemove.add(plugin);
            }
        }
        activePlugs.removeAll(toRemove);
    }

    @Override
    public void refreshMap(boolean updateLayers) {
        try {
            drawingArea.repaint();
            if (updateLayers) {
                legendEntries.clear();
                updateLayersTab();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.refreshMap", e);
        }
    }

    @Override
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        recentDirectoriesMenu.addMenuItem(workingDirectory);
    }

    @Override
    public String getApplicationDirectory() {
        return applicationDirectory;
    }

    @Override
    public void setApplicationDirectory(String applicationDirectory) {
        this.applicationDirectory = applicationDirectory;
    }

    @Override
    public String getLogDirectory() {
        return logDirectory;
    }

    @Override
    public String getHelpDirectory() {
        return helpDirectory;
    }

    @Override
    public String getResourcesDirectory() {
        return resourcesDirectory;
    }

    /**
     * Used to retrieve all of the files currently displayed in the active map.
     *
     * @return String[] of file names of displayed raster and vector files.
     */
    @Override
    public String[] getCurrentlyDisplayedFiles() {

        ArrayList<String> displayedLayers = new ArrayList<>();
        for (MapInfo m : openMaps) {
            for (MapArea ma : m.getMapAreas()) {
                ArrayList<MapLayer> myLayers = ma.getLayersList(); //activeMapArea.getLayersList();
                int i = 0;
                for (MapLayer maplayer : myLayers) {
                    if (maplayer.getLayerType() == MapLayer.MapLayerType.RASTER) {
                        RasterLayerInfo raster = (RasterLayerInfo) maplayer;
                        displayedLayers.add(raster.getHeaderFile());
                    } else if (maplayer.getLayerType() == MapLayer.MapLayerType.VECTOR) {
                        VectorLayerInfo vector = (VectorLayerInfo) maplayer;
                        displayedLayers.add(vector.getFileName());
                    }
                    i++;
                }
            }
        }
        String[] ret = new String[displayedLayers.size()];
        ret = displayedLayers.toArray(ret);
        return ret;
    }

    private void getApplicationProperties() {
        // see if the app.config file exists
        File propertiesFile = new File(propsFile);
        if (propertiesFile.exists()) {
            Properties props = new Properties();

            try {
                FileInputStream in = new FileInputStream(propsFile);
                props.load(in);
                workingDirectory = props.getProperty("workingDirectory");
                File wd = new File(workingDirectory);
                if (!wd.exists()) {
                    workingDirectory = resourcesDirectory + "samples";
                }

                splitterLoc1 = Integer.parseInt(props.getProperty("splitterLoc1"));
                ptbTabsIndex = Integer.parseInt(props.getProperty("ptbTabsIndex"));
                mtbTabsIndex = Integer.parseInt(props.getProperty("mtbTabsIndex"));
                qlTabsIndex = Integer.parseInt(props.getProperty("qlTabsIndex"));
                defaultQuantPalette = props.getProperty("defaultQuantPalette");
                defaultQualPalette = props.getProperty("defaultQualPalette");
                defaultQuantPalette = defaultQuantPalette.replace(".plt", ".pal"); // just in case the old style palette is specified.
                defaultQualPalette = defaultQualPalette.replace(".plt", ".pal");
                userName = props.getProperty("userName");
                defaultPageFormat.setOrientation(Integer.parseInt(props.getProperty("defaultPageOrientation")));
                double width = Float.parseFloat(props.getProperty("defaultPageWidth"));
                double height = Float.parseFloat(props.getProperty("defaultPageHeight"));
                Paper paper = defaultPageFormat.getPaper();
                paper.setSize(width, height);
                defaultPageFormat.setPaper(paper);
                if (props.containsKey("printResolution")) {
                    printResolution = Integer.parseInt(props.getProperty("printResolution"));
                }
                if (props.containsKey("hideAlignToolbar")) {
                    hideAlignToolbar = Boolean.parseBoolean(props.getProperty("hideAlignToolbar"));
                }
                if (props.containsKey("defaultMapMargin")) {
                    defaultMapMargin = Double.parseDouble(props.getProperty("defaultMapMargin"));
                }
                String[] FONTS = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                if (props.containsKey("defaultFont")) {
                    String fontName = props.getProperty("defaultFont");
                    for (String fnt : FONTS) {
                        if (fnt.equals(fontName)) {
                            defaultFont = new Font(fnt, Font.PLAIN, 11);
                            break;
                        }
                    }
                }
                // See if the defaultFont was found. If not, see if Arial is available.
                if (defaultFont == null) {
                    String fontName = "Arial";
                    for (String fnt : FONTS) {
                        if (fnt.toLowerCase().equals(fontName)) {
                            defaultFont = new Font(fnt, Font.PLAIN, 11);
                            break;
                        }
                    }
                    if (defaultFont == null) { // if arial is not available go with the java SanSerif default.
                        defaultFont = new Font("SanSerif", Font.PLAIN, 11);
                    }
                }

                if (props.containsKey("numberOfRecentItemsToStore")) {
                    numberOfRecentItemsToStore
                            = Integer.parseInt(props.getProperty("numberOfRecentItemsToStore"));
                    recentFilesMenu.setNumItemsToStore(numberOfRecentItemsToStore);
                    recentFilesMenu2.setNumItemsToStore(numberOfRecentItemsToStore);
                    recentFilesPopupMenu.setNumItemsToStore(numberOfRecentItemsToStore);
                    recentMapsMenu.setNumItemsToStore(numberOfRecentItemsToStore);
                    recentDirectoriesMenu.setNumItemsToStore(numberOfRecentItemsToStore);
                }

                // retrieve the recent data layers info
                if (props.containsKey("recentDataLayers")) {
                    String[] recentDataLayers = props.getProperty("recentDataLayers").split(",");
                    for (int i = recentDataLayers.length - 1; i >= 0; i--) { // add them in reverse order
                        recentFilesMenu.addMenuItem(recentDataLayers[i]);
                        recentFilesMenu2.addMenuItem(recentDataLayers[i]);
                        recentFilesPopupMenu.addMenuItem(recentDataLayers[i]);
                    }
                }

                // retrieve the recent maps info
                if (props.containsKey("recentMaps")) {
                    String[] recentMaps = props.getProperty("recentMaps").split(",");
                    for (int i = recentMaps.length - 1; i >= 0; i--) { // add them in reverse order
                        recentMapsMenu.addMenuItem(recentMaps[i]);
                    }
                }

                // retrieve the recent workingDirectories info
                if (props.containsKey("recentWorkingDirectories")) {
                    String[] recentDirectories = props.getProperty("recentWorkingDirectories").split(",");
                    for (int i = recentDirectories.length - 1; i >= 0; i--) { // add them in reverse order
                        recentDirectoriesMenu.addMenuItem(recentDirectories[i]);
                    }
                }

                // retrieve the skipVersionNumber
                if (props.containsKey("skipVersionNumber")) {
                    skipVersionNumber = props.getProperty("skipVersionNumber");
                }

                // retrieve the announcementNumber
                if (props.containsKey("announcementNumber")) {
                    announcementNumber = Integer.parseInt(props.getProperty("announcementNumber"));
                }

                // retrieve the langauge setting
                if (props.containsKey("language")) {
                    language = props.getProperty("language");
                }

                // retrieve the country setting
                if (props.containsKey("country")) {
                    country = props.getProperty("country");
                }

                // receive announcements
                if (props.containsKey("receiveAnnouncements")) {
                    receiveAnnouncements = Boolean.parseBoolean(props.getProperty("receiveAnnouncements"));
                }

                // check for updates
                if (props.containsKey("checkForUpdates")) {
                    checkForUpdates = Boolean.parseBoolean(props.getProperty("checkForUpdates"));
                }

                // check for scroll zoom direction
                if (props.containsKey("scrollZoomDirection")) {
                    int i = Integer.parseInt(props.getProperty("scrollZoomDirection"));
                    if (i == 0) {
                        setScrollZoomDirection(MapRenderingTool.ScrollZoomDirection.NORMAL);
                    } else {
                        setScrollZoomDirection(MapRenderingTool.ScrollZoomDirection.REVERSE);
                    }
                }

                // retrieve the plugin usage information
                String[] pluginNames = props.getProperty("pluginNames").split(",");
                String[] pluginUsage = props.getProperty("pluginUsage").split(",");
                String[] pluginLastUse = props.getProperty("toolLastUse").split(",");
                String plugName;
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
                Date lastUsed = null;
//                for (int i = 0; i < plugInfo.size(); i++) {
//                    plugName = plugInfo.get(i).getName();
//                    for (int j = 0; j < pluginNames.length; j++) {
//                        if (pluginNames[j].equals(plugName)) {
//                            try {
//                                lastUsed = df.parse(pluginLastUse[j]);
//                            } catch (ParseException e) {
//                            }
//                            plugInfo.get(i).setLastUsed(lastUsed);
//                            plugInfo.get(i).setNumTimesUsed(Integer.parseInt(pluginUsage[j]));
//                        }
//                    }
//                }

            } catch (IOException e) {
                logger.log(Level.SEVERE, "WhiteboxGui.getApplicationProperties", e);
            }

        } else {
            setWorkingDirectory(resourcesDirectory + "samples");
            splitterLoc1 = 250;
//            splitterToolboxLoc = 250;
            ptbTabsIndex = 0;
            qlTabsIndex = 0;
            defaultQuantPalette = "spectrum.pal";
            defaultQualPalette = "qual.pal";

//            int k = 0;
        }
    }

    private void setApplicationProperties() {
        // see if the app.config file exists
        if (!(new File(propsFile).exists())) {
            return;
        }
        Properties props = new Properties();
        props.setProperty("workingDirectory", workingDirectory);
        props.setProperty("splitterLoc1", Integer.toString(splitPane.getDividerLocation() - 2));
        props.setProperty("splitterToolboxLoc", Integer.toString(splitPane2.getDividerLocation() - 2)); //qlTabs.getSize().height));
        props.setProperty("ptbTabsIndex", Integer.toString(ptb.getSelectedIndex()));
        props.setProperty("mtbTabsIndex", Integer.toString(mtb.getSelectedIndex()));
        props.setProperty("qlTabsIndex", Integer.toString(qlTabs.getSelectedIndex()));
        props.setProperty("defaultQuantPalette", defaultQuantPalette);
        props.setProperty("defaultQualPalette", defaultQualPalette);
        props.setProperty("userName", System.getProperty("user.name"));
        props.setProperty("defaultPageOrientation", Integer.toString(defaultPageFormat.getOrientation()));
        props.setProperty("defaultPageHeight", Double.toString(defaultPageFormat.getPaper().getHeight()));
        props.setProperty("defaultPageWidth", Double.toString(defaultPageFormat.getPaper().getWidth()));
        props.setProperty("printResolution", Integer.toString(getPrintResolution()));
        props.setProperty("hideAlignToolbar", Boolean.toString(hideAlignToolbar));
        //props.setProperty("defaultFont", defaultFont.getName());
        props.setProperty("numberOfRecentItemsToStore", Integer.toString(numberOfRecentItemsToStore));
        props.setProperty("defaultMapMargin", Double.toString(defaultMapMargin));
        props.setProperty("skipVersionNumber", skipVersionNumber);
        props.setProperty("announcementNumber", Integer.toString(announcementNumber));
        props.setProperty("language", language);
        props.setProperty("country", country);
        props.setProperty("receiveAnnouncements", Boolean.toString(receiveAnnouncements));
        props.setProperty("checkForUpdates", Boolean.toString(checkForUpdates));

        if (scrollZoomDir == MapRenderingTool.ScrollZoomDirection.NORMAL) {
            props.setProperty("scrollZoomDirection", "0");
        } else {
            props.setProperty("scrollZoomDirection", "1");
        }

        // set the recent data layers
        String recentDataLayers = "";
        List<String> layersList = recentFilesMenu.getList();
        for (String str : layersList) {
            if (!recentDataLayers.isEmpty()) {
                recentDataLayers += "," + str;
            } else {
                recentDataLayers += str;
            }
        }
        props.setProperty("recentDataLayers", recentDataLayers);

        // set the recent maps info
        String recentMaps = "";
        List<String> mapList = recentMapsMenu.getList();
        for (String str : mapList) {
            if (!recentMaps.isEmpty()) {
                recentMaps += "," + str;
            } else {
                recentMaps += str;
            }
        }
        props.setProperty("recentMaps", recentMaps);

        // set the recent working directories info
        String recentDirectories = "";
        List<String> directoriesList = recentDirectoriesMenu.getList();
        for (String str : directoriesList) {
            if (!recentDirectories.isEmpty()) {
                recentDirectories += "," + str;
            } else {
                recentDirectories += str;
            }
        }
        props.setProperty("recentWorkingDirectories", recentDirectories);

        // set the tool usage properties
        // first sort plugInfo alphabetacally.
        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_NAMES);
        }
        Collections.sort(plugInfo);

        // create a string of tool names
        String toolNames = plugInfo.get(0).getName();
        for (int i = 1; i < plugInfo.size(); i++) {
            toolNames += "," + plugInfo.get(i).getName();
        }
        props.setProperty("pluginNames", toolNames);

        // create a string of tool usage
        String toolUsage = Integer.toString(plugInfo.get(0).getNumTimesUsed());
        for (int i = 1; i < plugInfo.size(); i++) {
            toolUsage += "," + Integer.toString(plugInfo.get(i).getNumTimesUsed());
        }
        props.setProperty("pluginUsage", toolUsage);

        // create a string of tool last used dates
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        String toolLastUse = df.format(plugInfo.get(0).getLastUsed());
        for (int i = 1; i < plugInfo.size(); i++) {
            toolLastUse += "," + df.format(plugInfo.get(i).getLastUsed());
        }
        props.setProperty("toolLastUse", toolLastUse);

        try {
            try (FileOutputStream out = new FileOutputStream(propsFile)) {
                props.store(out, "--No comments--");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.setApplicationProperties", e);
        }

    }

    private void showToolDescription(String pluginName) {
        for (PluginInfo pi : plugInfo) {
            if (pi.getDescriptiveName().equals(pluginName)) {
                status.setMessage(pi.getDescription());
                break;
            }
        }
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

    private JPanel createPanel(JPanel pnl, String s, Boolean v) {
        pnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), s, TitledBorder.LEFT, TitledBorder.TOP, f));
        pnl.setLayout(new GridBagLayout());
        pnl.setVisible(v);
        return pnl;
    }

    private JButton createTopPanelButton(JButton btn, String s, String ac, Border brd, Boolean en, ActionListener al, MouseListener m) {
        btn.setFont(f);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(80, 64));
        btn.setBorder(brd);
        btn.setContentAreaFilled(false);
        btn.setToolTipText(s);
        btn.setEnabled(en);
        btn.setFocusable(false);
        btn.addActionListener(al);
        btn.setActionCommand(ac);
        btn.addMouseListener(m);
        return btn;
    }

    private JButton createScenarioPanelButton(JButton btn, String s, String ac, ActionListener al, MouseListener m) {
        btn.setFont(f);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(84, 64));
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setToolTipText(s);
        btn.setEnabled(true);
        btn.setFocusable(false);
        btn.addActionListener(al);
        btn.setActionCommand(ac);
        btn.addMouseListener(m);
        return btn;
    }

    private JPanel buildProjectTreePanel() {
        JPanel projTreePanel = new JPanel(new GridBagLayout());
        projTreePanel.setBorder(BorderFactory.createLoweredBevelBorder());
        projTreePanel.setBackground(Color.WHITE);
        projTreePanel.setToolTipText("This is the Project Tree Panel.");

        DefaultMutableTreeNode projNode = new DefaultMutableTreeNode("Project");
        DefaultMutableTreeNode scenNode = new DefaultMutableTreeNode("Base Scenario"); // Generic Node without having an Object Name? May be necessary.
        JTree tree = new JTree(projNode);
        scenNode.add(new DefaultMutableTreeNode("Small Dams"));
        scenNode.add(new DefaultMutableTreeNode("Holding Ponds"));
        scenNode.add(new DefaultMutableTreeNode("Grazing Areas"));
        scenNode.add(new DefaultMutableTreeNode("Tillage Areas"));
        scenNode.add(new DefaultMutableTreeNode("Foraging Areas"));
        scenNode.add(new DefaultMutableTreeNode("Results"));
        projNode.add(scenNode);

        JPanel treePanel = new JPanel(new GridBagLayout());
        treePanel.setPreferredSize(new Dimension(projTreePanel.getWidth(), projTreePanel.getHeight()));
        treePanel.setBackground(Color.WHITE);
        treePanel.setVisible(true);
        GridBagConstraints gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.2, 0.0);
        treePanel.add(tree, gbc);

        JLabel filler = new JLabel();
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.BOTH, GridBagConstraints.WEST, 1, 0, 2, 2, 0.8, 0.9);
        treePanel.add(filler, gbc);
        projTreePanel.add(treePanel, gbc);
        projTreePanel.setVisible(true);
        return projTreePanel;
    }

    private JPanel buildResultsPanel() {
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
        cbPonds.setEnabled(false);
        cbGraze.setActionCommand("grazingLayerOn");
        cbGraze.addActionListener(this);
        cbGraze.setEnabled(false);
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
        jcbSwat.setSelectedIndex(1);
        jcbSwat.addActionListener(this);

        gbc = setGbc(new Insets(4, 0, 4, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 1, 1, 1, 1.0, 1.0);
        resultPanel.add(jcbSwat, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 2, 1, 1, 1.0, 1.0);
        resultPanel.add(rbOnlyEcon, gbc);
        
        String[] econList = {"Small Dam", "Holding Ponds", "Grazing Area", "Tillage", "Forage"};
        jcbEcon = new JComboBox(econList);
        jcbEcon.setSelectedIndex(0);
        jcbEcon.setEnabled(false);
        jcbEcon.addActionListener(this);

        gbc = setGbc(new Insets(4, 0, 4, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 0, 3, 1, 1, 1.0, 1.0);
        resultPanel.add(jcbEcon, gbc);

        JPanel tillForPanel = createPanel(new JPanel(), "Tillage & Forage", true);
        String[] tillForList = {"Yield", "Revenue", "Crop Cost", "Crop Return"};
        jcbTillFor = new JComboBox(tillForList);
        jcbTillFor.setSelectedIndex(0);
        jcbTillFor.addActionListener(this);

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

    private void buildNewProjectPanel() {
        projPanel = new JPanel(new GridBagLayout());
        projPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        Border b = BorderFactory.createLoweredBevelBorder();
        JPanel basicProj = createPanel(new JPanel(), "Basic Information", true);

        JLabel projNameLbl = new JLabel("Watershed Name", SwingConstants.RIGHT);
        GridBagConstraints gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.0, 0.0);
        basicProj.add(projNameLbl, gbc);

        projNameFld = new JTextField("", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 0, 1, 1, 0.75, 0.0);
        basicProj.add(projNameFld, gbc);

        JLabel wsNameLbl = new JLabel("Watershed Name", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 0.0, 0.0);
        basicProj.add(wsNameLbl, gbc);

        JTextField wsNameFld = new JTextField("STC", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 1, 1, 1, 0.75, 0.0);
        basicProj.add(wsNameFld, gbc);

        JLabel projLocLbl = new JLabel("Project File", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 0), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 2, 1, 1, 0.05, 0.0);
        basicProj.add(projLocLbl, gbc);

        projLocation = new JLabel("C:\\", SwingConstants.LEFT);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 2, 1, 1, 0.75, 0.0);
        basicProj.add(projLocation, gbc);

        gbc = setGbc(new Insets(0, 4, 0, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1, 0, 1, 1, 0.75, 0.0);
        projPanel.add(basicProj, gbc);

        JPanel spatial = createPanel(new JPanel(), "Spatial Data", true);

        JLabel spatLocLbl = new JLabel("Spatial Data Folder", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 1, 1, 1, 0.05, 0.0);
        spatial.add(spatLocLbl, gbc);

        spatLocation = new JLabel("C:\\", SwingConstants.LEFT);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 1, 1, 0.75, 0.0);
        spatial.add(spatLocation, gbc);

        JButton spatLocBtn = createTopPanelButton(new JButton("folder_16x16.png"), "Show the Spatial Data Folder in Explorer.", "empty", b, true, null, null);
        spatLocBtn.setVisible(false);
        spatLocBtn.setPreferredSize(new Dimension(32, 32));
        gbc = setGbc(new Insets(0, 0, 0, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 3, 1, 1, 3, 0.0, 0.0);
        spatial.add(spatLocBtn, gbc);

        JLabel smDamLbl = new JLabel("Small Dam", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 2, 1, 1, 0.05, 0.0);
        spatial.add(smDamLbl, gbc);

        JTextField smDamFld = new JTextField("small_dam", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 2, 1, 1, 0.75, 0.0);
        spatial.add(smDamFld, gbc);

        JLabel pondLbl = new JLabel("Holding Pond", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 3, 1, 1, 0.05, 0.0);
        spatial.add(pondLbl, gbc);

        JTextField pondFld = new JTextField("cattle_yard", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 3, 1, 1, 0.75, 0.0);
        spatial.add(pondFld, gbc);

        JLabel grazeLbl = new JLabel("Grazing", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 4, 1, 1, 0.05, 0.0);
        spatial.add(grazeLbl, gbc);

        JTextField grazeFld = new JTextField("grazing", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 4, 1, 1, 0.75, 0.0);
        spatial.add(grazeFld, gbc);

        JLabel fieldLbl = new JLabel("Field", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 5, 1, 1, 0.05, 0.0);
        spatial.add(fieldLbl, gbc);

        JTextField fieldFld = new JTextField("land2010_by_land_id", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 5, 1, 1, 0.75, 0.0);
        spatial.add(fieldFld, gbc);

        JLabel farmLbl = new JLabel("Farm", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 24, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 6, 1, 1, 0.05, 0.0);
        spatial.add(farmLbl, gbc);

        JTextField farmFld = new JTextField("farm2010", 40);
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 6, 1, 1, 0.75, 1.0);
        spatial.add(farmFld, gbc);

        gbc = setGbc(new Insets(0, 4, 0, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1, 1, 1, 1, 0.75, 0.0);
        projPanel.add(spatial, gbc);

        JPanel swat = createPanel(new JPanel(), "SWAT Data", true);

        JLabel swatLocLbl = new JLabel("SWAT Data Folder", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(16, 16, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 1, 1, 1, 0.05, 0.0);
        swat.add(swatLocLbl, gbc);

        swatLocation = new JLabel("C:\\", SwingConstants.LEFT);
        gbc = setGbc(new Insets(16, 16, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 1, 1, 0.75, 0.0);
        swat.add(swatLocation, gbc);

        JButton swatLocBtn = createTopPanelButton(new JButton("folder_16x16.png"), "Show the SWAT Data Folder in Explorer.", "empty", b, true, null, null);
        swatLocBtn.setVisible(false);
        swatLocBtn.setPreferredSize(new Dimension(32, 32));
        gbc = setGbc(new Insets(0, 0, 0, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 3, 1, 1, 1, 0.0, 0.0);
        swat.add(swatLocBtn, gbc);

        gbc = setGbc(new Insets(0, 4, 0, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, 1, 2, 1, 1, 0.75, 0.0);
        projPanel.add(swat, gbc);

        JButton scenButton = createScenarioPanelButton(new JButton(), "Builds the Base Historical and Conventional Scenarios.", "baseScenario", this, null);
        scenButton.setLayout(new BorderLayout());
        scenButton.setMargin(new Insets(2, 2, 2, 2));
        scenButton.setPreferredSize(new Dimension(72, 54));
        JLabel top = new JLabel(" Create  ", SwingConstants.CENTER);
        top.setVerticalAlignment(SwingConstants.BOTTOM);
        JLabel mid = new JLabel("  Base   ", SwingConstants.CENTER);
        mid.setVerticalAlignment(SwingConstants.CENTER);
        JLabel bot = new JLabel("Scenarios", SwingConstants.CENTER);
        bot.setVerticalAlignment(SwingConstants.TOP);
        scenButton.add(BorderLayout.NORTH, top);
        scenButton.add(BorderLayout.CENTER, mid);
        scenButton.add(BorderLayout.SOUTH, bot);

        gbc = setGbc(new Insets(16, 32, 16, 16), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 3, 0, 0, 0.0, 0.25);
        scenButton.setContentAreaFilled(true);
        scenButton.setVisible(true);
        projPanel.add(scenButton, gbc);
    }

    private void buildScenarioPanel() {
        scenPanel = new JPanel(new GridBagLayout());
        scenPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        basicScen = createPanel(new JPanel(), "Basic Information", true);

        JLabel scenNameLbl = new JLabel("Scenario Name", SwingConstants.RIGHT);
        GridBagConstraints gbc = setGbc(new Insets(16, 70, 16, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 1, 1, 1, 0.2, 0.0);
        basicScen.add(scenNameLbl, gbc);

        scenNameFld = new JTextField("PLACEHOLDER", 82);
        gbc = setGbc(new Insets(0, 4, 0, 4), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 1, 1, 1, 0.8, 0.0);
        basicScen.add(scenNameFld, gbc);

        JLabel filler = new JLabel();
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.BOTH, GridBagConstraints.WEST, 2, 1, 1, 2, 1.0, 0.0);
        basicScen.add(filler, gbc);
        
        JLabel scenDescLbl = new JLabel("Scenario Description", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(0, 46, 16, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 2, 1, 1, 0.2, 0.0);
        basicScen.add(scenDescLbl, gbc);

        JTextArea scenDescFld = new JTextArea("Description PLACEHOLDER", 3, 60);
        scenDescFld.setFont(f);
        scenDescFld.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        gbc = setGbc(new Insets(0, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 2, 1, 1, 0.8, 0.0);
        basicScen.add(scenDescFld, gbc);
        
        JLabel tillageTypeLbl = new JLabel("Tillage Type:", SwingConstants.RIGHT);
        gbc = setGbc(new Insets(0, 46, 16, 0), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 3, 1, 1, 0.2, 0.0);
        basicScen.add(tillageTypeLbl, gbc);
        
        rbConvTill = new JRadioButton("Conventional Tillage");
        rbZeroTill = new JRadioButton("Zero Tillage");
        
        ButtonGroup tillGroup = new ButtonGroup();
        tillGroup.add(rbConvTill);
        tillGroup.add(rbZeroTill);
        
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 3, 1, 1, 1.0, 0.0);
        basicScen.add(rbConvTill, gbc);
        
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 2, 3, 1, 1, 0.0, 0.0);
        basicScen.add(rbZeroTill, gbc);
        
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 1.0, 0.0);
        scenPanel.add(basicScen, gbc);

        bmpSection = createPanel(new JPanel(), "BMPs", true);

        JButton btnDamSc = createScenarioPanelButton(new JButton("Small Dam", new ImageIcon(graphicsDirectory + "draw_polygon_curves_32x32.png")),
                "Select Small Dams", "damsBmp", this, null); //new damsListener()
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 0, 1, 1, 0.0, 0.0);
        bmpSection.add(btnDamSc, gbc);

        JButton btnPondSc = createScenarioPanelButton(new JButton("Holding Pond", new ImageIcon(graphicsDirectory + "button_32x32.png")),
                "Select Holding Ponds", "pondsBmp", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 0, 1, 1, 0.0, 0.0);
        bmpSection.add(btnPondSc, gbc);

        JButton btnGrazeSc = createScenarioPanelButton(new JButton("Grazing", new ImageIcon(graphicsDirectory + "cow_head_32x32.png")),
                "Select Grazing Areas", "grazingBmp", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 2, 0, 1, 1, 0.0, 0.0);
        bmpSection.add(btnGrazeSc, gbc);

        JButton btnTillSc = createScenarioPanelButton(new JButton("Tillage", new ImageIcon(graphicsDirectory + "tractor_32x32.png")),
                "Select Tillage Areas", "tillageBmp", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 3, 0, 1, 1, 0.0, 0.0);
        bmpSection.add(btnTillSc, gbc);

        JButton btnForageSc = createScenarioPanelButton(new JButton("Forage", new ImageIcon(graphicsDirectory + "grass_32x32.png")),
                "Select Foraging Areas", "forageBmp", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 4, 0, 1, 1, 1.0, 0.0);
        bmpSection.add(btnForageSc, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 1.0, 0.0);
        scenPanel.add(bmpSection, gbc);

        controlSection = createPanel(new JPanel(), "Controls", true);

        btnSaveSc = createScenarioPanelButton(new JButton("Save", new ImageIcon(graphicsDirectory + "picture_save_32x32.png")), "Save Scenario", "saveScenario", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 0, 0, 1, 1, 0.0, 0.0);
        controlSection.add(btnSaveSc, gbc);

        btnSaveAsSc = createScenarioPanelButton(new JButton("Save As", new ImageIcon(graphicsDirectory + "page_save_32x32.png")), "Save Scenario As", "empty", null, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 1, 0, 1, 1, 0.0, 0.0);
        controlSection.add(btnSaveAsSc, gbc);

        JButton btnEconSc = createScenarioPanelButton(new JButton("Economic", new ImageIcon(graphicsDirectory + "cash_stack_32x32.png")), "Run Economic Model", "runEconomic", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 2, 0, 1, 1, 0.0, 0.0);
        controlSection.add(btnEconSc, gbc);

        JButton btnSwatSc = createScenarioPanelButton(new JButton("SWAT", new ImageIcon(graphicsDirectory + "weather_snow_32x32.png")), "Run SWAT Model", "runSwat", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 3, 0, 1, 1, 0.0, 0.0);
        controlSection.add(btnSwatSc, gbc);

        JButton btnResultSc = createScenarioPanelButton(new JButton("Results", new ImageIcon(graphicsDirectory + "3d_glasses_32x32.png")), "View Scenario Results", "results", this, null);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.WEST, 4, 0, 1, 1, 1.0, 0.0);
        controlSection.add(btnResultSc, gbc);

        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 2, 1, 1, 1.0, 0.25);
        scenPanel.add(controlSection, gbc);
        scenPanel.validate();
    }
    
    public void updateSelectedFeaturesList(VectorLayerInfo vli, String shapeName) throws Exception {
        try {
            ArrayList<Integer> selected = vli.getSelectedFeatureNumbers();
            ShapeFile sf = new ShapeFile(shapeName);
            AttributeTable aTable = sf.getAttributeTable();
            if(resultsOn) {
                for(int i = 0; i < selected.size(); i++) {
                    double valueX = (double) aTable.getValue(selected.get(i), "ID");
                    rdf.SetFeatureID((int) Math.round(valueX), false);
                    if(shapeName.contains("grazing")) {
                        rdf.SetBMPType(BMPType.Grazing, true);
                    }
                    else if(shapeName.contains("pond")) {
                        rdf.SetBMPType(BMPType.Holding_Pond, true);
                    }
                    else if(shapeName.contains("dam")) {
                        rdf.SetBMPType(BMPType.Small_Dam, true);
                    }
                    else if(shapeName.contains("land2010")) {
                        rdf.SetBMPType(BMPType.Tillage_Field, true);
                    }
                    else if(shapeName.contains("farm2010")) {
                        rdf.SetBMPType(BMPType.Tillage_Farm, true);
                    }
                    else if(shapeName.contains("basin")) {
                        rdf.SetBMPType(BMPType.Tillage_Subbasin, true);
                    }
                }
                refreshSplitPaneFour();
            }
            else {
                currentData = new int[selected.size()];
                itemIndex = 0;
                for(int i = 0; i < selected.size(); i++) {
                    if(selected.get(i) - 1 < 363) {
                        double valueX = (double) aTable.getValue(selected.get(i) - 1, "ID");
                        currentData[itemIndex] = (int) Math.round(valueX);
                        itemIndex++;
                    }
                }
                if(activeMap == openDam) {
                    damData = currentData;
                }
                else if(activeMap == openPond) {
                    pondData = currentData;
                }
                else if(activeMap == openGraze) {
                    grazeData = currentData;
                }
                else if(activeMap == openTill) {
                    tillData = currentData;
                }
                else if(activeMap == openForage) {
                    forageData = currentData;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setActiveMapLayer(int v){
        vli.setOverlayNumber(v);
    }
    
    private void displayBMPArrayData() {
        System.out.println("\n***** TEST PRINTING OF BMP DATA ARRAYS *****\n");
        System.out.println("*** SMALL DAM BMP ARRAY DATA ***\n");
        if(damData != null) {
            for(int i = 0; i < damData.length; i++) {
                System.out.println(damData[i] + ": SMALL DAM DATA AT INDEX " + i);
            }
        }
        System.out.println("\n*** HOLDING POND BMP ARRAY DATA ***\n");
        if(pondData != null) {
            for(int i = 0; i < pondData.length; i++) {
                System.out.println(pondData[i] + ": HOLDING POND DATA AT INDEX " + i);
            }
        }
        System.out.println("\n*** GRAZING AREA BMP ARRAY DATA ***\n");
        if(grazeData != null) {
            for(int i = 0; i < grazeData.length; i++) {
                System.out.println(grazeData[i] + ": GRAZING AREA DATA AT INDEX " + i);
            }
        }
        System.out.println("\n*** TILLAGE MAP BMP ARRAY DATA ***\n");
        if(tillData != null) {
            for(int i = 0; i < tillData.length; i++) {
                System.out.println(tillData[i] + ": TILLAGE DATA AT INDEX " + i);
            }
        }
        System.out.println("\n*** FORAGE MAP BMP ARRAY DATA ***\n");
        if(forageData != null) {
            for(int i = 0; i < forageData.length; i++) {
                System.out.println(forageData[i] + ": FORAGE DATA AT INDEX " + i);
            }
        }
    }
    
    public void newScenario(String name, boolean base, boolean scenarioType, String spatialDir, String projectDir) {
        try {
            userScenarioIndex++;
            userScenarios[userScenarioIndex] = new ScenarioBuilder(name, false, scenarioType, spatialDir, projectDir);
            currentScenario = userScenarios[userScenarioIndex];
            scenNameFld.setText(name);
            wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            wb.getGlassPane().setVisible(true);
            //scenDescFld.setText(descArea.getText());
            wb.validate();
            wb.repaint();
            try {
                project.addScenario(new Scenario("", "", wbProjectDirectory + "User Scenarios" + pathSep + currentScenario.getName(),
                    ScenarioType.Normal, BMPScenerioBaseType.Conventional,
                    BMPSelectionLevelType.Field, ""));
            } catch (Exception ex) {
                Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
            }
            btnSaveSc.setEnabled(true);
            btnSaveAsSc.setEnabled(true);
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void refreshSplitPaneFour() {
        if(noChart) {
            try {
                rdf = new ResultDisplayFrame(project, project.GetCurrentScenario());
                tabs.insertTab("Results", null, rdf.GetDisplayControl(), "", 2);
                noChart = false;
            } catch (Exception ex) {
                Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        splitPane4.removeAll();
        splitPane4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawingArea, rdf.GetDisplayChart());
        splitPane4.setResizeWeight(0.80);
        splitPane4.setPreferredSize(new Dimension(100, 100));
        splitPane4.setOneTouchExpandable(false);
        splitPane4.setDividerSize(3);
        splitPane4.setDividerLocation(0.80);
        mtb.remove(1);
        mtb.insertTab("Drawing Area", null, splitPane4, "", 1);
        mtb.setSelectedIndex(1);
    }
    
    private void mapRefreshThing(String shapefile, HashMap hash) {
        try {
            ShapeFile shp = new ShapeFile(shapefile);
            AttributeTable table = shp.getAttributeTable();
            DBFField newField = new DBFField();
            String fieldName = "ResultsCat";
            newField.setName(fieldName);
            table.addField(newField);
            DBFField category = table.getField(table.getFieldCount() - 1);
            ShapeFileRecord shapeRec;
            System.out.println(category.getName() + ": CATEGORY NAME");
            
            for(int i = 0; i < table.getNumberOfRecords(); i++) {
                table.setValue(i, fieldName, hash.get(i));
            }
            
        } catch (IOException ex) {
            Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    private void createGui() throws InvocationTargetException {
        try {
            if (System.getProperty("os.name").contains("Mac")) {

                try {
                    Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
                    Class params[] = new Class[2];
                    params[0] = Window.class;
                    params[1] = Boolean.TYPE;
                    Method method = util.getMethod("setWindowCanFullScreen", params);
                    method.invoke(util, this, true);
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                    logger.log(Level.SEVERE, "WhiteboxGui.createGui", e);
                }
                UIManager.put("apple.awt.brushMetalLook", Boolean.TRUE);
            }
            // add the menubar and toolbar
            createMenu();
            createPopupMenus();
            createToolbar();
            ctb = new CartographicToolbar(this, false);
            ctb.setOrientation(SwingConstants.VERTICAL);
            //this.getContentPane().add(ctb, BorderLayout.EAST);

            MapInfo mapinfo = new MapInfo(bundle.getString("Map") + "1");
            mapinfo.setMapName(bundle.getString("Map") + "1");
            mapinfo.setPageFormat(defaultPageFormat);
            mapinfo.setWorkingDirectory(workingDirectory);
            mapinfo.setDefaultFont(defaultFont);
            mapinfo.setMargin(defaultMapMargin);
            mapinfo.setPageVisible(false);

            MapArea ma = new MapArea(bundle.getString("MapArea").replace(" ", "") + "1");
            ma.setUpperLeftX(-32768);
            ma.setUpperLeftY(-32768);
            ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
            mapinfo.addNewCartographicElement(ma);

            openMaps.add(mapinfo);
            activeMap = 0;
            drawingArea.setMapInfo(mapinfo);
            drawingArea.setStatusBar(status);
            drawingArea.setScaleText(scaleText);
            drawingArea.setHost(this);

            ptb = createTabbedPane();
            ptb.setMaximumSize(new Dimension(150, 50));
            ptb.setPreferredSize(new Dimension(splitterLoc1, 50));
            ptb.setSelectedIndex(0);
            webs = new JPanel(new BorderLayout());
            tableView = new JPanel();
            buildNewProjectPanel();
            buildScenarioPanel();
            splitPane3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, webs, tableView);
            splitPane3.setResizeWeight(0.65);
            splitPane3.setPreferredSize(new Dimension(100, 100));
            splitPane3.setOneTouchExpandable(false);
            splitPane3.setDividerSize(3);
            splitPane4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, drawingArea, null);
            splitPane4.setResizeWeight(0.65);
            splitPane4.setPreferredSize(new Dimension(100, 100));
            splitPane4.setOneTouchExpandable(false);
            splitPane4.setDividerSize(3);
            mtb.insertTab("WEBs Interface", null, splitPane3, "", 0);
            mtb.insertTab("Drawing Area", null, splitPane4, "", 1);
            mtb.setPreferredSize(new Dimension(splitterLoc1, 50));
            mtb.setSelectedIndex(mtbTabsIndex);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, ptb, mtb); //splitPane3);
            splitPane.setResizeWeight(0);
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerSize(3);
            this.getContentPane().add(splitPane);

            // add the status bar
            this.getContentPane().add(status, java.awt.BorderLayout.SOUTH);
            this.setMinimumSize(new Dimension(700, 500));
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

            // set the message indicating the number of plugins that were located.
            status.setMessage(" " + plugInfo.size() + " plugins were located");

            splitPane2.setDividerLocation(0.75); //splitterToolboxLoc);

            closeMap();

            pack();
        } catch (SecurityException | IllegalArgumentException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.createGui", e);
        }
    }

    private void displayAnnouncements() {
        if (announcements.size() < 1) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        sb.append("<html>\n");
        sb.append("  <head>\n");
        sb.append("    <title>Whitebox Announcements");
        sb.append("</title>\n");
        sb.append("<style media=\"screen\" type=\"text/css\">\n"
                + "h1\n"
                + "{\n"
                + "font-family: Helvetica, Verdana, Geneva, Arial, sans-serif;\n"
                + "font-size: 12pt;\n"
                + "background-color: rgb(200,215,250); \n"
                + "font-weight: bold;\n"
                + "line-height: 20pt;\n"
                + "margin-left: 10px;\n"
                + "margin-right: 10px;\n"
                + "}\n"
                + "p\n"
                + "{\n"
                + "text-align: left;\n"
                + "color:black;\n"
                + "font-family:Verdana, Geneva, Arial, Helvetica, sans-serif;\n"
                + "font-size: 10pt;\n"
                + "background-color: transparent;\n"
                + "line-height: normal;\n"
                + "margin-left: 10px;\n"
                + "margin-right: 10px;\n"
                + "}\n"
                + "ul\n"
                + "{\n"
                + "list-style-type: square;\n"
                + "list-style-position: inside;\n"
                + "font-family:Verdana, Geneva, Arial, Helvetica, sans-serif;\n"
                + "font-size: 10pt;\n"
                + "margin-left: 10px;\n"
                + "margin-bottom: 0;\n"
                + "margin-top: 5px;\n"
                + "}"
                + "\n"
                + "</style>\n");
        sb.append("  </head>\n");
        sb.append("  <body><h1><b>Whitebox Announcements").append("</b></h1>\n");
        for (WhiteboxAnnouncement wba : announcements) {
            sb.append("    <p>");
            if (!wba.getTitle().isEmpty()) {
                sb.append("<b>").append(wba.getTitle()).append("</b><br>\n");
            }
            sb.append("      ").append(wba.getMessage()).append("\n");
            if (!wba.getDate().isEmpty()) {
                sb.append("<p>Date: ").append(wba.getDate());
            }
            sb.append("    </p>\n");
        }
        sb.append("  </body>\n");
        sb.append("</html>\n");

        JEditorPane pane = new JEditorPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setText(sb.toString());
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent r) {
                try {
                    if (r.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        Desktop d = Desktop.getDesktop();
                        String linkName = r.getURL().toString();
                        d.browse(new URI(linkName));
                    }
                } catch (URISyntaxException | IOException e) {
                    logger.log(Level.SEVERE, "WhiteboxGui.displayAnnouncement", e);
                }
            }
        });
        pane.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(pane);
        JDialog dialog = new JDialog(this, "");
        Container contentPane = dialog.getContentPane();
        contentPane.add(scroll, BorderLayout.CENTER);
        dialog.setPreferredSize(new Dimension(500, 500));
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
    }

    private JMenu fileMenu;
    private JMenu layersMenu;
    private JMenu viewMenu;
    private JMenu cartoMenu;
    private JMenu toolsMenu;
    private JMenu helpMenu;

    private void createMenu() {
        try {
            JMenuBar menubar = new JMenuBar();

            JMenuItem newMap = new JMenuItem(bundle.getString("NewMap"),
                    new ImageIcon(graphicsDirectory + "map.png"));
            JMenuItem openMap = new JMenuItem(bundle.getString("OpenMap"),
                    new ImageIcon(graphicsDirectory + "open.png"));
            JMenuItem saveMap = new JMenuItem(bundle.getString("SaveMap"),
                    new ImageIcon(graphicsDirectory + "SaveMap.png"));
            JMenuItem closeMap = new JMenuItem(bundle.getString("CloseMap"),
                    new ImageIcon(graphicsDirectory + "close.png"));

            JMenuItem close = new JMenuItem(bundle.getString("Close"));

            JMenuItem layerProperties = new JMenuItem(bundle.getString("LayerDisplayProperties"),
                    new ImageIcon(graphicsDirectory + "LayerProperties.png"));
            layerProperties.setActionCommand("layerProperties");
            layerProperties.addActionListener(this);

            JMenuItem rasterCalc = new JMenuItem(bundle.getString("RasterCalculator"),
                    new ImageIcon(graphicsDirectory + "RasterCalculator.png"));
            modifyPixels = new JCheckBoxMenuItem(bundle.getString("ModifyPixelValues"),
                    new ImageIcon(graphicsDirectory + "ModifyPixels.png"));
            JMenuItem paletteManagerMenu = new JMenuItem(bundle.getString("PaletteManager"),
                    new ImageIcon(graphicsDirectory + "paletteManager.png"));
            JMenuItem refreshTools = new JMenuItem(bundle.getString("RefreshToolUsage"));

            // File menu
            fileMenu = new JMenu(bundle.getString("File"));
            recentFilesMenu.setNumItemsToStore(numberOfRecentItemsToStore);
            recentFilesMenu.setText(bundle.getString("RecentDataLayers"));
            recentFilesMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addLayer(e.getActionCommand());
                }
            });
            //fileMenu.add(recentFilesMenu);

            recentMapsMenu.setNumItemsToStore(numberOfRecentItemsToStore);
            recentMapsMenu.setText(bundle.getString("RecentMaps"));
            recentMapsMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openMap(e.getActionCommand());
                }
            });
            //fileMenu.add(recentMapsMenu);

            recentDirectoriesMenu.setNumItemsToStore(numberOfRecentItemsToStore);
            recentDirectoriesMenu.setText(bundle.getString("RecentWorkingDirectories"));
            recentDirectoriesMenu.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setWorkingDirectory(e.getActionCommand());
                }
            });
            //fileMenu.add(recentDirectoriesMenu);

            if (System.getProperty("os.name").contains("Mac") == false) {
                fileMenu.addSeparator();
                fileMenu.add(close);
                close.setActionCommand("close");
                close.addActionListener(this);
            }

            //menubar.add(fileMenu);
            // Layers menu
            layersMenu = new JMenu(bundle.getString("Data_Layers"));
            recentFilesMenu2.setNumItemsToStore(numberOfRecentItemsToStore);
            recentFilesMenu2.setText(bundle.getString("RecentDataLayers"));
            recentFilesMenu2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addLayer(e.getActionCommand());
                }
            });
            //layersMenu.add(recentFilesMenu2);

            JMenuItem addLayers = new JMenuItem(bundle.getString("AddLayersToMap"),
                    new ImageIcon(graphicsDirectory + "AddLayer.png"));
            //layersMenu.add(addLayers);
            addLayers.addActionListener(this);
            addLayers.setActionCommand("addLayer");
            addLayers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            JMenuItem removeLayers = new JMenuItem(
                    bundle.getString("RemoveActiveLayerFromMap"),
                    new ImageIcon(graphicsDirectory + "RemoveLayer.png"));
            //layersMenu.add(removeLayers);
            removeLayers.addActionListener(this);
            removeLayers.setActionCommand("removeLayer");
            removeLayers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            JMenuItem removeAllLayers = new JMenuItem(
                    bundle.getString("RemoveAllLayers"));
            //layersMenu.add(removeAllLayers);
            removeAllLayers.addActionListener(this);
            removeAllLayers.setActionCommand("removeAllLayers");
            JMenuItem allLayersInvisible = new JMenuItem(
                    bundle.getString("HideAllLayers"));
            //layersMenu.add(allLayersInvisible);
            allLayersInvisible.addActionListener(this);
            allLayersInvisible.setActionCommand("allLayersInvisible");
            JMenuItem allLayersVisible = new JMenuItem(
                    bundle.getString("ShowAllLayers"));
            //layersMenu.add(allLayersVisible);
            allLayersVisible.addActionListener(this);
            allLayersVisible.setActionCommand("allLayersVisible");
            //layersMenu.addSeparator();
            JMenuItem raiseLayers = new JMenuItem(bundle.getString("RaiseLayer"),
                    new ImageIcon(graphicsDirectory + "PromoteLayer.png"));
            //layersMenu.add(raiseLayers);
            raiseLayers.addActionListener(this);
            raiseLayers.setActionCommand("raiseLayer");
            JMenuItem lowerLayers = new JMenuItem(bundle.getString("LowerLayer"),
                    new ImageIcon(graphicsDirectory + "DemoteLayer.png"));
            //layersMenu.add(lowerLayers);
            lowerLayers.addActionListener(this);
            lowerLayers.setActionCommand("lowerLayer");
            JMenuItem layerToTop = new JMenuItem(bundle.getString("LayerToTop"),
                    new ImageIcon(graphicsDirectory + "LayerToTop.png"));
            //layersMenu.add(layerToTop);
            layerToTop.addActionListener(this);
            layerToTop.setActionCommand("layerToTop");
            JMenuItem layerToBottom = new JMenuItem(bundle.getString("LayerToBottom"),
                    new ImageIcon(graphicsDirectory + "LayerToBottom.png"));
            //layersMenu.add(layerToBottom);
            layerToBottom.addActionListener(this);
            layerToBottom.setActionCommand("layerToBottom");

            //layersMenu.addSeparator();
            JMenuItem deselectAllFeatures = new JMenuItem(bundle.getString("clearAllSelectedFeatures"));
            deselectAllFeatures.addActionListener(this);
            deselectAllFeatures.setActionCommand("clearAllSelectedFeatures");
            //layersMenu.add(deselectAllFeatures);

            JMenuItem saveSelection = new JMenuItem(bundle.getString("saveSelection"));
            saveSelection.addActionListener(this);
            saveSelection.setActionCommand("saveSelection");
            //layersMenu.add(saveSelection);

            //layersMenu.addSeparator();
            JMenuItem layerProperties2 = new JMenuItem(bundle.getString("LayerDisplayProperties"),
                    new ImageIcon(graphicsDirectory + "LayerProperties.png"));
            layerProperties2.setActionCommand("layerProperties");
            layerProperties2.addActionListener(this);
            //layersMenu.add(layerProperties2);

            JMenuItem viewAttributeTable = new JMenuItem(bundle.getString("ViewAttributeTable"),
                    new ImageIcon(graphicsDirectory + "AttributeTable.png"));
            //layersMenu.add(viewAttributeTable);
            viewAttributeTable.addActionListener(this);
            viewAttributeTable.setActionCommand("viewAttributeTable");

            JMenuItem histoMenuItem = new JMenuItem(bundle.getString("ViewHistogram"));
            histoMenuItem.addActionListener(this);
            histoMenuItem.setActionCommand("viewHistogram");
            //layersMenu.add(histoMenuItem);

            //layersMenu.addSeparator();
            JMenuItem clipLayerToExtent = new JMenuItem(bundle.getString("ClipLayerToCurrentExtent"));
            clipLayerToExtent.addActionListener(this);
            clipLayerToExtent.setActionCommand("clipLayerToExtent");
            //layersMenu.add(clipLayerToExtent);

            //layersMenu.addSeparator();
            JMenuItem mi = new JMenuItem(bundle.getString("ExportLayer"));
            mi.addActionListener(this);
            mi.setActionCommand("exportLayer");
            //layersMenu.add(mi);

            //layersMenu.addSeparator();
            //menubar.add(layersMenu);
            // View menu
            viewMenu = new JMenu(bundle.getString("View"));

            selectMenuItem = new JCheckBoxMenuItem(bundle.getString("SelectMapElement"),
                    new ImageIcon(graphicsDirectory + "select.png"));
            viewMenu.add(selectMenuItem);
            selectMenuItem.addActionListener(this);
            selectMenuItem.setActionCommand("select");

            selectFeatureMenuItem = new JCheckBoxMenuItem(bundle.getString("SelectFeature"),
                    new ImageIcon(graphicsDirectory + "SelectFeature.png"));
            viewMenu.add(selectFeatureMenuItem);
            selectFeatureMenuItem.addActionListener(this);
            selectFeatureMenuItem.setActionCommand("selectFeature");

            panMenuItem = new JCheckBoxMenuItem(bundle.getString("Pan"),
                    new ImageIcon(graphicsDirectory + "Pan2.png"));
            viewMenu.add(panMenuItem);
            panMenuItem.addActionListener(this);
            panMenuItem.setActionCommand("pan");

            zoomMenuItem = new JCheckBoxMenuItem(bundle.getString("ZoomIn"),
                    new ImageIcon(graphicsDirectory + "ZoomIn.png"));
            viewMenu.add(zoomMenuItem);
            zoomMenuItem.addActionListener(this);
            zoomMenuItem.setActionCommand("zoomToBox");
            zoomMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            zoomOutMenuItem = new JCheckBoxMenuItem(bundle.getString("ZoomOut"),
                    new ImageIcon(graphicsDirectory + "ZoomOut.png"));
            viewMenu.add(zoomOutMenuItem);
            zoomOutMenuItem.addActionListener(this);
            zoomOutMenuItem.setActionCommand("zoomOut");
            zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            JMenuItem zoomToFullExtent = new JMenuItem(bundle.getString("ZoomMapAreaToFullExtent"),
                    new ImageIcon(graphicsDirectory + "Globe.png"));
            zoomToFullExtent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            viewMenu.add(zoomToFullExtent);
            zoomToFullExtent.addActionListener(this);
            zoomToFullExtent.setActionCommand("zoomToFullExtent");

            JMenuItem zoomToPage = new JMenuItem(bundle.getString("ZoomToPage"),
                    new ImageIcon(graphicsDirectory + "ZoomToPage.png"));
            viewMenu.add(zoomToPage);
            zoomToPage.addActionListener(this);
            zoomToPage.setActionCommand("zoomToPage");

            mi = new JMenuItem(bundle.getString("ZoomToSelection"));
            mi.addActionListener(this);
            mi.setActionCommand("zoomToSelection");
            viewMenu.add(mi);

            selectMenuItem.setState(false);
            selectFeatureMenuItem.setState(false);
            zoomMenuItem.setState(false);
            zoomOutMenuItem.setState(false);
            panMenuItem.setState(true);

            JMenuItem panLeft = new JMenuItem(bundle.getString("PanLeft"));
            viewMenu.add(panLeft);
            panLeft.addActionListener(this);
            panLeft.setActionCommand("panLeft");
            panLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            JMenuItem panRight = new JMenuItem(bundle.getString("PanRight"));
            viewMenu.add(panRight);
            panRight.addActionListener(this);
            panRight.setActionCommand("panRight");
            panRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            JMenuItem panUp = new JMenuItem(bundle.getString("PanUp"));
            viewMenu.add(panUp);
            panUp.addActionListener(this);
            panUp.setActionCommand("panUp");
            panUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            JMenuItem panDown = new JMenuItem(bundle.getString("PanDown"));
            viewMenu.add(panDown);
            panDown.addActionListener(this);
            panDown.setActionCommand("panDown");
            panDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

            JMenuItem previousExtent = new JMenuItem(bundle.getString("PreviousExtent"),
                    new ImageIcon(graphicsDirectory + "back.png"));
            viewMenu.add(previousExtent);
            previousExtent.addActionListener(this);
            previousExtent.setActionCommand("previousExtent");

            JMenuItem nextExtent = new JMenuItem(bundle.getString("NextExtent"),
                    new ImageIcon(graphicsDirectory + "forward.png"));
            viewMenu.add(nextExtent);
            nextExtent.addActionListener(this);
            nextExtent.setActionCommand("nextExtent");

            viewMenu.addSeparator();
            JMenuItem refresh = new JMenuItem(bundle.getString("RefreshMap"));
            viewMenu.add(refresh);
            refresh.addActionListener(this);
            refresh.setActionCommand("refreshMap");

            viewMenu.addSeparator();
            JMenuItem mapProperties = new JMenuItem(bundle.getString("MapProperties"));
            mapProperties.addActionListener(this);
            mapProperties.setActionCommand("mapProperties");
            viewMenu.add(mapProperties);
            viewMenu.add(layerProperties);
            JMenuItem viewHistogram = new JMenuItem(bundle.getString("ViewHistogram"));
            viewHistogram.setActionCommand("viewHistogram");
            viewHistogram.addActionListener(this);
            viewMenu.add(viewHistogram);

            JMenuItem options = new JMenuItem(bundle.getString("OptionsAndSettings"));
            viewMenu.add(options);
            options.addActionListener(this);
            options.setActionCommand("options");

            viewMenu.addSeparator();
            JMenuItem textViewer = new JMenuItem("Text Viewer");
            viewMenu.add(textViewer);
            textViewer.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    viewTextDialog("");
                }
            });

            JMenuItem htmlViewer = new JMenuItem("HTML Viewer");
            viewMenu.add(htmlViewer);
            htmlViewer.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    viewHtmlDialog("");
                }
            });

            //menubar.add(viewMenu);
            // Cartographic menu
            cartoMenu = new JMenu(bundle.getString("Cartographic"));

            cartoMenu.add(newMap);
            newMap.setActionCommand("newMap");
            newMap.addActionListener(this);
            newMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            cartoMenu.add(openMap);
            openMap.setActionCommand("openMap");
            openMap.addActionListener(this);
            openMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            cartoMenu.add(closeMap);
            closeMap.setActionCommand("closeMap");
            //closeMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            closeMap.addActionListener(this);
//          cartoMenu.add(saveMap);
            saveMap.addActionListener(this);
            saveMap.addActionListener(this);
            saveMap.setActionCommand("saveMap");
            saveMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            cartoMenu.add(saveMap);

            JMenuItem saveAsMap = new JMenuItem(bundle.getString("SaveAs") + "...");
            cartoMenu.add(saveAsMap);
            saveAsMap.addActionListener(this);
            saveAsMap.setActionCommand("saveMapAs");

            JMenuItem printMap = new JMenuItem(bundle.getString("PrintMap"), new ImageIcon(graphicsDirectory + "Print.png"));
            cartoMenu.add(printMap);
            printMap.addActionListener(this);
            printMap.setActionCommand("printMap");
            printMap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            JMenuItem exportMap = new JMenuItem(bundle.getString("ExportMapAsImage"));
            cartoMenu.add(exportMap);
            exportMap.addActionListener(this);
            exportMap.setActionCommand("exportMapAsImage");

            cartoMenu.addSeparator();

            JMenuItem insertTitle = new JMenuItem(bundle.getString("InsertMapTitle"));
            cartoMenu.add(insertTitle);
            insertTitle.addActionListener(this);
            insertTitle.setActionCommand("insertTitle");

            JMenuItem insertNorthArrow = new JMenuItem(bundle.getString("InsertNorthArrow"));
            cartoMenu.add(insertNorthArrow);
            insertNorthArrow.addActionListener(this);
            insertNorthArrow.setActionCommand("insertNorthArrow");

            JMenuItem insertScale = new JMenuItem(bundle.getString("InsertScale"));
            cartoMenu.add(insertScale);
            insertScale.addActionListener(this);
            insertScale.setActionCommand("insertScale");

            JMenuItem insertLegend = new JMenuItem(bundle.getString("InsertLegend"));
            cartoMenu.add(insertLegend);
            insertLegend.addActionListener(this);
            insertLegend.setActionCommand("insertLegend");

            JMenuItem insertNeatline = new JMenuItem(bundle.getString("InsertNeatline"));
            cartoMenu.add(insertNeatline);
            insertNeatline.addActionListener(this);
            insertNeatline.setActionCommand("insertNeatline");

            JMenuItem insertMapArea = new JMenuItem(bundle.getString("InsertMapArea"),
                    new ImageIcon(graphicsDirectory + "mapArea.png"));
            cartoMenu.add(insertMapArea);
            insertMapArea.addActionListener(this);
            insertMapArea.setActionCommand("insertMapArea");

            JMenuItem insertTextArea = new JMenuItem(bundle.getString("InsertTextArea"));
            cartoMenu.add(insertTextArea);
            insertTextArea.addActionListener(this);
            insertTextArea.setActionCommand("insertTextArea");

            JMenuItem insertImage = new JMenuItem(bundle.getString("InsertImage"));
            cartoMenu.add(insertImage);
            insertImage.addActionListener(this);
            insertImage.setActionCommand("insertImage");

            cartoMenu.addSeparator();

            JMenuItem pageProps = new JMenuItem(bundle.getString("PageProperties"),
                    new ImageIcon(graphicsDirectory + "page.png"));
            cartoMenu.add(pageProps);
            pageProps.addActionListener(this);
            pageProps.setActionCommand("pageProps");

            // align and distribute sub-menu
            cartoMenu.addSeparator();
            JMenu alignAndDistribute = new JMenu(bundle.getString("AlignAndDistribute"));
            cartoMenu.add(alignAndDistribute);

            JMenuItem alignRightMenu = new JMenuItem(bundle.getString("AlignRight"),
                    new ImageIcon(graphicsDirectory + "AlignRight.png"));
            alignRightMenu.addActionListener(this);
            alignRightMenu.setActionCommand("alignRight");
            alignAndDistribute.add(alignRightMenu);

            JMenuItem centerVerticalMenu = new JMenuItem(bundle.getString("CenterVertically"),
                    new ImageIcon(graphicsDirectory + "CenterVertical.png"));
            centerVerticalMenu.addActionListener(this);
            centerVerticalMenu.setActionCommand("centerVertical");
            alignAndDistribute.add(centerVerticalMenu);

            JMenuItem alignLeftMenu = new JMenuItem(bundle.getString("AlignLeft"),
                    new ImageIcon(graphicsDirectory + "AlignLeft.png"));
            alignLeftMenu.addActionListener(this);
            alignLeftMenu.setActionCommand("alignLeft");
            alignAndDistribute.add(alignLeftMenu);

            JMenuItem alignTopMenu = new JMenuItem(bundle.getString("AlignTop"),
                    new ImageIcon(graphicsDirectory + "AlignTop.png"));
            alignTopMenu.addActionListener(this);
            alignTopMenu.setActionCommand("alignTop");
            alignAndDistribute.add(alignTopMenu);

            JMenuItem centerHorizontalMenu = new JMenuItem(bundle.getString("CenterHorizontally"),
                    new ImageIcon(graphicsDirectory + "CenterHorizontal.png"));
            centerHorizontalMenu.addActionListener(this);
            centerHorizontalMenu.setActionCommand("centerHorizontal");
            alignAndDistribute.add(centerHorizontalMenu);

            JMenuItem alignBottomMenu = new JMenuItem(bundle.getString("AlignBottom"),
                    new ImageIcon(graphicsDirectory + "AlignBottom.png"));
            alignBottomMenu.addActionListener(this);
            alignBottomMenu.setActionCommand("alignBottom");
            alignAndDistribute.add(alignBottomMenu);

            alignAndDistribute.addSeparator();

            JMenuItem distributeVerticallyMenu = new JMenuItem(bundle.getString("DistributeVertically"),
                    new ImageIcon(graphicsDirectory + "DistributeVertically.png"));
            distributeVerticallyMenu.addActionListener(this);
            distributeVerticallyMenu.setActionCommand("distributeVertically");
            alignAndDistribute.add(distributeVerticallyMenu);

            JMenuItem distributeHorizontallyMenu = new JMenuItem(bundle.getString("DistributeHorizontally"),
                    new ImageIcon(graphicsDirectory + "DistributeHorizontally.png"));
            distributeHorizontallyMenu.addActionListener(this);
            distributeHorizontallyMenu.setActionCommand("distributeHorizontally");
            alignAndDistribute.add(distributeHorizontallyMenu);

            JMenuItem groupMenu = new JMenuItem(bundle.getString("GroupElements"),
                    new ImageIcon(graphicsDirectory + "GroupElements.png"));
            groupMenu.addActionListener(this);
            groupMenu.setActionCommand("groupElements");
            cartoMenu.add(groupMenu);

            JMenuItem ungroupMenu = new JMenuItem(bundle.getString("UngroupElements"),
                    new ImageIcon(graphicsDirectory + "UngroupElements.png"));
            ungroupMenu.addActionListener(this);
            ungroupMenu.setActionCommand("ungroupElements");
            cartoMenu.add(ungroupMenu);

            //menubar.add(cartoMenu);
            // Tools menu
            toolsMenu = new JMenu(bundle.getString("Tools"));

            toolsMenu.add(rasterCalc);
            rasterCalc.setActionCommand("rasterCalculator");
            rasterCalc.addActionListener(this);

            JMenuItem scripter = new JMenuItem(bundle.getString("Scripting"),
                    new ImageIcon(graphicsDirectory + "ScriptIcon2.png"));
            scripter.addActionListener(this);
            scripter.setActionCommand("scripter");
            toolsMenu.add(scripter);

            distanceToolMenuItem = new JCheckBoxMenuItem(bundle.getString("MeasureDistance"),
                    new ImageIcon(graphicsDirectory + "DistanceTool.png"));
            toolsMenu.add(distanceToolMenuItem);
            distanceToolMenuItem.addActionListener(this);
            distanceToolMenuItem.setActionCommand("distanceTool");

            toolsMenu.add(paletteManagerMenu);
            paletteManagerMenu.addActionListener(this);
            paletteManagerMenu.setActionCommand("paletteManager");

            toolsMenu.add(modifyPixels);
            modifyPixels.addActionListener(this);
            modifyPixels.setActionCommand("modifyPixels");

            toolsMenu.add(refreshTools);
            refreshTools.addActionListener(this);
            refreshTools.setActionCommand("refreshTools");

            JMenuItem newHelp = new JMenuItem(bundle.getString("CreateNewHelpEntry"));
            newHelp.addActionListener(this);
            newHelp.setActionCommand("newHelp");
            toolsMenu.add(newHelp);
            //menubar.add(toolsMenu);

            toolsMenu.addSeparator();

            JMenu editVectorMenu = new JMenu(bundle.getString("On-ScreenDigitizing"));

            editVectorMenuItem = new JCheckBoxMenuItem(bundle.getString("EditVector"),
                    new ImageIcon(graphicsDirectory + "Digitize.png"));
            editVectorMenu.add(editVectorMenuItem);
            editVectorMenuItem.addActionListener(this);
            editVectorMenuItem.setActionCommand("editVector");
            editVectorMenuItem.setEnabled(false);

            digitizeNewFeatureMenuItem = new JCheckBoxMenuItem(bundle.getString("DigitizeNewFeature"),
                    new ImageIcon(graphicsDirectory + "DigitizeNewFeature.png"));
            editVectorMenu.add(digitizeNewFeatureMenuItem);
            digitizeNewFeatureMenuItem.addActionListener(this);
            digitizeNewFeatureMenuItem.setActionCommand("digitizeNewFeature");
            digitizeNewFeatureMenuItem.setEnabled(false);

            deleteLastNodeInFeatureMenuItem = new JMenuItem(bundle.getString("DeleteLastNodeFeature"),
                    new ImageIcon(graphicsDirectory + "undo.png"));
            editVectorMenu.add(deleteLastNodeInFeatureMenuItem);
            deleteLastNodeInFeatureMenuItem.addActionListener(this);
            deleteLastNodeInFeatureMenuItem.setActionCommand("deleteLastNodeInFeature");
            deleteLastNodeInFeatureMenuItem.setEnabled(false);

            deleteFeatureMenuItem = new JMenuItem(bundle.getString("DeleteFeature"),
                    new ImageIcon(graphicsDirectory + "DeleteFeature.png"));
            editVectorMenu.add(deleteFeatureMenuItem);
            deleteFeatureMenuItem.addActionListener(this);
            deleteFeatureMenuItem.setActionCommand("deleteFeature");
            deleteFeatureMenuItem.setEnabled(false);

            toolsMenu.add(editVectorMenu);

            // Help menu
            helpMenu = new JMenu(bundle.getString("Help"));
            JMenuItem helpIndex = new JMenuItem(bundle.getString("Index"),
                    new ImageIcon(graphicsDirectory + "help.png"));
            helpMenu.add(helpIndex);
            helpIndex.setActionCommand("helpIndex");
            helpIndex.addActionListener(this);

            JMenuItem helpSearch = new JMenuItem(bundle.getString("Search"));
            helpSearch.setActionCommand("helpSearch");
            helpSearch.addActionListener(this);
            helpMenu.add(helpSearch);

            JMenuItem helpTutorials = new JMenuItem(bundle.getString("Tutorials"));
            helpMenu.add(helpTutorials);
            helpTutorials.setActionCommand("helpTutorials");
            helpTutorials.addActionListener(this);

            helpMenu.addSeparator();

            String[][] tutorialFiles = findTutorialFiles();
            if (tutorialFiles != null) {
                JMenuItem[] tutorialMenuItems = new JMenuItem[tutorialFiles.length];
                for (int a = 0; a < tutorialFiles.length; a++) {
                    tutorialMenuItems[a] = new JMenuItem(tutorialFiles[a][1]);
                    tutorialMenuItems[a].setActionCommand("tutorial_file:\t" + tutorialFiles[a][0]);
                    tutorialMenuItems[a].addActionListener(this);
                    helpMenu.add(tutorialMenuItems[a]);
                }
            }

            helpMenu.addSeparator();

            JMenuItem helpAbout = new JMenuItem(bundle.getString("About") + " Whitebox GAT");
            helpAbout.setActionCommand("helpAbout");
            helpAbout.addActionListener(this);
            helpMenu.add(helpAbout);

            helpMenu.addSeparator();

            JMenuItem helpReport = new JMenuItem(bundle.getString("HelpCompletenessReport"));
            helpReport.setActionCommand("helpReport");
            helpReport.addActionListener(this);
            helpMenu.add(helpReport);

            //menubar.add(helpMenu);
            this.setJMenuBar(menubar);

            loadMenuExtensions();

        } catch (HeadlessException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.createMenu", e);
        }
    }

    private ArrayList<MenuExtension> menuExtensions = new ArrayList<>();

    private void loadMenuExtensions() {
        for (MenuExtension me : menuExtensions) {
            final String fileName = me.getScriptFile();
            JMenuItem mi = new JMenuItem(me.getLabel());
            mi.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] args = new String[0];
                    executeScriptFile(fileName, args, false);
                }
            });
            if (me.getAcceleratorKeyStroke() != null) {
                mi.setAccelerator(me.getAcceleratorKeyStroke());
            }

            switch (me.getMenu()) {
                case FILE:
                    fileMenu.add(mi);
                    break;
                case LAYERS:
                    layersMenu.add(mi);
                    break;
                case VIEW:
                    viewMenu.add(mi);
                    break;
                case CARTOGRAPHIC:
                    cartoMenu.add(mi);
                    break;
                case TOOLS:
                    toolsMenu.add(mi);
                    break;
                case HELP:
                    helpMenu.add(mi);
                    break;
            }
        }

    }

    private String[][] findTutorialFiles() {
        String[][] tutorialFiles = null;
        try {
            ArrayList<String> allTutorialFiles
                    = FileUtilities.findAllFilesWithExtension(new File(helpDirectory + "tutorials" + pathSep), ".html", true);

            String[] tutorialFiles1 = new String[allTutorialFiles.size()];
            tutorialFiles1 = allTutorialFiles.toArray(tutorialFiles1);

            Arrays.sort(tutorialFiles1, new Comparator<String>() {
                @Override
                public int compare(String str1, String str2) {
                    return str1.toLowerCase().compareTo(str2.toLowerCase());
                }
            });
            tutorialFiles = new String[tutorialFiles1.length][2];
            for (int a = 0; a < tutorialFiles1.length; a++) {
                tutorialFiles[a][0] = tutorialFiles1[a];
                tutorialFiles[a][1] = Help.findTitle(tutorialFiles1[a]);
            }

            Arrays.sort(tutorialFiles, new Comparator<String[]>() {
                @Override
                public int compare(String[] str1, String[] str2) {
                    return str1[1].toLowerCase().compareTo(str2[1].toLowerCase());
                }
            });
        } catch (Exception e) {
            logger.log(Level.WARNING, "Help.findAvailableHelpFiles", e);
        }
        return tutorialFiles;
    }

    private void createPopupMenus() {

        // maps menu
        mapsPopup = new JPopupMenu();

        JMenuItem mi = new JMenuItem(bundle.getString("MapProperties"));
        mi.addActionListener(this);
        mi.setActionCommand("mapProperties");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("RenameMap"));
        mi.addActionListener(this);
        mi.setActionCommand("renameMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("SetAsActiveMap"));
        mi.addActionListener(this);
        mi.setActionCommand("setAsActiveMap");
        mapsPopup.add(mi);

        mapsPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("SaveMap"),
                new ImageIcon(graphicsDirectory + "SaveMap.png"));
        mi.addActionListener(this);
        mi.setActionCommand("saveMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("SaveAs") + "...");
        mi.addActionListener(this);
        mi.setActionCommand("saveMapAs");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("OpenMap"),
                new ImageIcon(graphicsDirectory + "open.png"));
        mi.addActionListener(this);
        mi.setActionCommand("openMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("AddNewMap"),
                new ImageIcon(graphicsDirectory + "map.png"));
        mi.addActionListener(this);
        mi.setActionCommand("newMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("PrintMap"),
                new ImageIcon(graphicsDirectory + "Print.png"));
        mi.addActionListener(this);
        mi.setActionCommand("printMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("ExportMapAsImage"));
        mi.addActionListener(this);
        mi.setActionCommand("exportMapAsImage");
        mapsPopup.add(mi);

        mapsPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("RefreshMap"));
        mi.addActionListener(this);
        mi.setActionCommand("refreshMap");
        mapsPopup.add(mi);

        mi = new JMenuItem(bundle.getString("ZoomToPage"),
                new ImageIcon(graphicsDirectory + "ZoomFullExtent3.png"));
        mi.addActionListener(this);
        mi.setActionCommand("zoomToPage");
        mapsPopup.add(mi);

        mapsPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("CloseMap"));
        mi.addActionListener(this);
        mi.setActionCommand("closeMap");
        mapsPopup.add(mi);

        mapsPopup.setOpaque(true);
        mapsPopup.setLightWeightPopupEnabled(true);

        // map area popup menu
        mapAreaPopup = new JPopupMenu();

        mi = new JMenuItem(bundle.getString("AddLayer"),
                new ImageIcon(graphicsDirectory + "AddLayer.png"));
        mi.addActionListener(this);
        mi.setActionCommand("addLayer");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("RemoveLayer"),
                new ImageIcon(graphicsDirectory + "RemoveLayer.png"));
        mi.addActionListener(this);
        mi.setActionCommand("removeLayer");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("RemoveAllLayers"));
        mi.addActionListener(this);
        mi.setActionCommand("removeAllLayers");
        mapAreaPopup.add(mi);

        recentFilesPopupMenu.setNumItemsToStore(numberOfRecentItemsToStore);
        recentFilesPopupMenu.setText(bundle.getString("AddRecentDataLayer"));
        recentFilesPopupMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addLayer(e.getActionCommand());
            }
        });
        mapAreaPopup.add(recentFilesPopupMenu);

        mapAreaPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("FitMapToPage"));
        mi.addActionListener(this);
        mi.setActionCommand("fitMapAreaToPage");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("FitToData"));
        mi.addActionListener(this);
        mi.setActionCommand("fitMapAreaToData");
        mapAreaPopup.add(mi);

        JCheckBoxMenuItem miCheck = new JCheckBoxMenuItem(
                bundle.getString("MaximizeScreenSize"));
        miCheck.addActionListener(this);
        miCheck.setActionCommand("maximizeMapAreaScreenSize");
        mapAreaPopup.add(miCheck);

        mi = new JMenuItem(bundle.getString("ZoomToActiveLayer"),
                new ImageIcon(graphicsDirectory + "ZoomToActiveLayer.png"));
        mi.addActionListener(this);
        mi.setActionCommand("zoomToLayer");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("ZoomToFullExtent"),
                new ImageIcon(graphicsDirectory + "Globe.png"));
        mi.addActionListener(this);
        mi.setActionCommand("zoomToFullExtent");
        mapAreaPopup.add(mi);

        mapAreaPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("ShowAllLayers"));
        mi.addActionListener(this);
        mi.setActionCommand("allLayersVisible");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("HideAllLayers"));
        mi.addActionListener(this);
        mi.setActionCommand("allLayersInvisible");
        mapAreaPopup.add(mi);

        mi = new JMenuItem(bundle.getString("ToggleVisibilityOfAllLayers"));
        mi.addActionListener(this);
        mi.setActionCommand("toggleAllLayerVisibility");
        mapAreaPopup.add(mi);

        mapAreaPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("ShowProperties"));
        mi.addActionListener(this);
        mi.setActionCommand("mapAreaProperties");
        mapAreaPopup.add(mi);

        mapAreaPopup.addSeparator();

        mi = new JMenuItem(bundle.getString("DeleteMapArea"));
        mi.addActionListener(this);
        mi.setActionCommand("deleteMapArea");
        mapAreaPopup.add(mi);
    }

    public void setCartoElementToolbarVisibility(boolean value) {
        ctb.setVisible(value);
    }

    private void createToolbar() {
        try {
            JToolBar toolbar = new JToolBar();
            JButton addLayer = makeToolBarButton("AddLayer.png", "addLayer",
                    bundle.getString("AddLayer"), "Add Layer");
            //toolbar.add(addLayer);
            JButton removeLayer = makeToolBarButton("RemoveLayer.png", "removeLayer",
                    bundle.getString("RemoveLayer"), "Remove Layer");
            //toolbar.add(removeLayer);
            JButton raiseLayer = makeToolBarButton("PromoteLayer.png", "raiseLayer",
                    bundle.getString("RaiseLayer"), "Raise Layer");
            //toolbar.add(raiseLayer);
            JButton lowerLayer = makeToolBarButton("DemoteLayer.png", "lowerLayer",
                    bundle.getString("LowerLayer"), "Lower Layer");
            //toolbar.add(lowerLayer);
            JButton layerToTop = makeToolBarButton("LayerToTop.png", "layerToTop",
                    bundle.getString("LayerToTop"), "Layer To Top");
            //toolbar.add(layerToTop);
            JButton layerToBottom = makeToolBarButton("LayerToBottom.png", "layerToBottom",
                    bundle.getString("LayerToBottom"), "Layer To Bottom");
            //toolbar.add(layerToBottom);

            JButton layerProps = makeToolBarButton("LayerProperties.png", "layerProperties",
                    bundle.getString("LayerDisplayProperties"), "Layer Properties");
            //toolbar.add(layerProps);

            JButton attributeTable = makeToolBarButton("AttributeTable.png", "viewAttributeTable",
                    bundle.getString("ViewAttributeTable"), "View Attribute Table");
            //toolbar.add(attributeTable);

            //toolbar.addSeparator();
            select = makeToggleToolBarButton("select.png", "select",
                    bundle.getString("SelectMapElement"), "Select");
            toolbar.add(select);
            selectFeature = makeToggleToolBarButton("SelectFeature.png", "selectFeature",
                    bundle.getString("SelectFeature"), "Select Feature");
            toolbar.add(selectFeature);
            // Feature selection should go here.
            pan = makeToggleToolBarButton("Pan2.png", "pan",
                    bundle.getString("Pan"), "Pan");
            toolbar.add(pan);
            zoomIntoBox = makeToggleToolBarButton("ZoomIn.png", "zoomToBox",
                    bundle.getString("ZoomIn"), "Zoom");
            toolbar.add(zoomIntoBox);
            zoomOut = makeToggleToolBarButton("ZoomOut.png", "zoomOut",
                    bundle.getString("ZoomOut"), "Zoom out");
            toolbar.add(zoomOut);
            JButton zoomToFullExtent = makeToolBarButton("Globe.png", "zoomToFullExtent",
                    bundle.getString("ZoomToFullExtent"), "Zoom To Full Extent");
            toolbar.add(zoomToFullExtent);
            JButton zoomToPage = makeToolBarButton("ZoomFullExtent3.png", "zoomToPage",
                    bundle.getString("ZoomToPage"), "Zoom To Page");
            toolbar.add(zoomToPage);

            ButtonGroup viewButtonGroup = new ButtonGroup();
            viewButtonGroup.add(select);
            viewButtonGroup.add(selectFeature);
            viewButtonGroup.add(pan);
            viewButtonGroup.add(zoomOut);
            viewButtonGroup.add(zoomIntoBox);
            //select.setSelected(true);
            //zoomIntoBox.setSelected(true);
            pan.setSelected(true);
            //openMaps.get(activeMap).deslectAllCartographicElements();
            //refreshMap(false);
            drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_ZOOM);
            selectMenuItem.setState(false);
            selectFeatureMenuItem.setState(false);
            zoomMenuItem.setState(true);
            zoomOutMenuItem.setState(false);
            panMenuItem.setState(false);

            JButton previousExtent = makeToolBarButton("back.png", "previousExtent",
                    bundle.getString("PreviousExtent"), "Prev Extent");
            toolbar.add(previousExtent);
            JButton nextExtent = makeToolBarButton("forward.png", "nextExtent",
                    bundle.getString("NextExtent"), "Next Extent");
            nextExtent.setActionCommand("nextExtent");
            toolbar.add(nextExtent);

            toolbar.addSeparator();

            JButton newMap = makeToolBarButton("map.png", "newMap",
                    bundle.getString("NewMap"), "New");

            //toolbar.add(newMap);
            JButton openMap = makeToolBarButton("open.png", "openMap",
                    bundle.getString("OpenMap"), "Open");
            //toolbar.add(openMap);

            JButton closeMap = makeToolBarButton("close.png", "closeMap",
                    bundle.getString("CloseMap"), "Close");
            //toolbar.add(closeMap);

            JButton saveMap = makeToolBarButton("SaveMap.png", "saveMap",
                    bundle.getString("SaveMap"), "Save");
            //toolbar.add(saveMap);
            JButton printMap = makeToolBarButton("Print.png", "printMap",
                    bundle.getString("PrintMap"), "Print");
            //toolbar.add(printMap);

            //toolbar.addSeparator();
            JButton rasterCalculator = makeToolBarButton("RasterCalculator.png", "rasterCalculator",
                    bundle.getString("RasterCalculator"), "Raster Calc");
            //toolbar.add(rasterCalculator);

            JButton scripter = makeToolBarButton("ScriptIcon2.png", "scripter",
                    bundle.getString("Scripting"), "Scripter");
            toolbar.add(scripter);

            distanceToolButton = makeToggleToolBarButton("DistanceTool.png", "distanceTool",
                    bundle.getString("MeasureDistance"), "Distance Tool");
            toolbar.add(distanceToolButton);

            paletteManager = makeToolBarButton("paletteManager.png", "paletteManager",
                    bundle.getString("PaletteManager"), "Palette Manager");
            toolbar.add(paletteManager);
            //paletteManager.setVisible(false);

            modifyPixelVals = makeToggleToolBarButton("ModifyPixels.png", "modifyPixels",
                    bundle.getString("ModifyPixelValues"), "Modify Pixels");
            //toolbar.add(modifyPixelVals);
            modifyPixelVals.setVisible(false);

            editVectorButton = makeToggleToolBarButton("Digitize.png", "editVector",
                    bundle.getString("EditVector"), "Edit Vector");
            editVectorButton.setEnabled(false);
            //toolbar.add(editVectorButton);

            digitizeNewFeatureButton = makeToggleToolBarButton("DigitizeNewFeature.png", "digitizeNewFeature",
                    bundle.getString("DigitizeNewFeature"), "Digitize New Feature");
            digitizeNewFeatureButton.setVisible(false);
            //toolbar.add(digitizeNewFeatureButton);

            deleteLastNodeInFeatureButton = makeToolBarButton("undo.png", "deleteLastNodeInFeature",
                    bundle.getString("DeleteLastNodeFeature"), "Delete Last Node In Feature");
            deleteLastNodeInFeatureButton.setVisible(false);
            //toolbar.add(deleteLastNodeInFeatureButton);

            deleteFeatureButton = makeToolBarButton("DeleteFeature.png", "deleteFeature",
                    bundle.getString("DeleteFeature"), "Delete Feature");
            deleteFeatureButton.setVisible(false);
            //toolbar.add(deleteFeatureButton);

            toolbar.addSeparator();
            JButton help = makeToolBarButton("Help.png", "helpIndex",
                    bundle.getString("Help"), "Help");
            toolbar.add(help);

            toolbar.addSeparator();

            JButton newProj = makeToolBarButton("document_empty_16x16.png", "newProject",
                    "New WEBs Project", "New WEBs Project");
            toolbar.add(newProj);

            JButton openProj = makeToolBarButton("folder_16x16.png", "newProject",
                    "Open WEBs Project", "Open WEBs Project");
            toolbar.add(openProj);

            saveProj = makeToolBarButton("save_as_16x16.png", "saveProject",
                    "Save WEBs Project", "Save WEBs Project");
            saveProj.setEnabled(false);
            toolbar.add(saveProj);

            projSep = new Separator();
            projSep.setVisible(false);
            toolbar.add(projSep);

            newScen = makeToolBarButton("add_16x16.png", "newScenario", "New WEBs Scenario", "New WEBs Scenario");
            newScen.setVisible(false);
            toolbar.add(newScen);

            editScen = makeToolBarButton("information_16x16.png", "editScenario", "Edit Current Scenario", "Edit Current Scenario");
            editScen.setVisible(false);
            toolbar.add(editScen);

            delScen = makeToolBarButton("delete_16x16.png", "deleteScenario", "Delete Current Scenario", "Delete Current Scenario");
            delScen.setVisible(false);
            toolbar.add(delScen);

            scenSep = new Separator();
            scenSep.setVisible(false);
            toolbar.add(scenSep);

            dams = makeToolBarButton("draw_polygon_curves_16x16.png", "damsBmp",
                    "Small Dams", "Small Dams");
            dams.setVisible(false);
            toolbar.add(dams);

            ponds = makeToolBarButton("button_16x16.png", "pondsBmp",
                    "Holding Ponds", "Holding Ponds");
            ponds.setVisible(false);
            toolbar.add(ponds);

            grazing = makeToolBarButton("cow_head_16x16.png", "grazingBmp",
                    "Grazing Area", "Grazing Area");
            grazing.setVisible(false);
            toolbar.add(grazing);

            tillage = makeToolBarButton("tractor_16x16.png", "tillageBmp",
                    "Tillage", "Tillage");
            tillage.setVisible(false);
            toolbar.add(tillage);

            forage = makeToolBarButton("grass_16x16.png", "forageBmp",
                    "Forage", "Forage");
            forage.setVisible(false);
            toolbar.add(forage);

            toolbar.add(Box.createHorizontalGlue());
            JPanel scalePanel = new JPanel();
            scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.X_AXIS));
            JLabel scaleLabel = new JLabel("1:");
            scalePanel.add(scaleLabel);
            scalePanel.add(scaleText);
            scalePanel.setOpaque(false);
            scaleText.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        updateMapScale();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }
            });
            scalePanel.setMinimumSize(new Dimension(120, 22));
            scalePanel.setPreferredSize(new Dimension(120, 22));
            scalePanel.setMaximumSize(new Dimension(120, 22));
            toolbar.add(scalePanel);
            toolbar.add(Box.createHorizontalStrut(15));

            this.getContentPane().add(toolbar, BorderLayout.PAGE_START);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.createToolbar", e);
            showFeedback(e.toString());
        }

    }

    private void updateMapScale() {
        try {
            String input = scaleText.getText().replace(",", "");
            double newScale = Double.parseDouble(input);
            MapArea mapArea = openMaps.get(activeMap).getActiveMapArea();
            mapArea.setScale(newScale);
            refreshMap(false);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "WhiteboxGui.updateMapScale", e);
        }
    }

    private JButton makeToolBarButton(String imageName, String actionCommand, String toolTipText, String altText) {
        //Look for the image.
        String imgLocation = graphicsDirectory + imageName;
        ImageIcon image = new ImageIcon(imgLocation, "");

        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setOpaque(false);
        button.setBorderPainted(false);
        try {
            button.setIcon(image);
        } catch (Exception e) {
            button.setText(altText);
            showFeedback(e.getMessage());
            logger.log(Level.WARNING, "WhiteboxGui.makeToolbarButton", e);
        }

        return button;
    }

    private JToggleButton makeToggleToolBarButton(String imageName, String actionCommand, String toolTipText, String altText) {
        //Look for the image.
        String imgLocation = graphicsDirectory + imageName;
        ImageIcon image = new ImageIcon(imgLocation, "");

        //Create and initialize the button.
        JToggleButton button = new JToggleButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);
        button.setOpaque(false);
        try {
            button.setIcon(image);
        } catch (Exception e) {
            showFeedback(e.getMessage());
            logger.log(Level.WARNING, "WhiteboxGui.makeToggleToolbarButton", e);
        }

        return button;
    }
    private JPanel layersPanel;
    private FeatureSelectionPanel featuresPanel;

    private JTabbedPane createTabbedPane() {
        try {
            JSplitPane wbTools = getToolbox();
            //tabs.insertTab(bundle.getString("Tools"), null, wbTools, "", 0);
            layersPanel = new JPanel(new BorderLayout());
            layersPanel.setBackground(Color.white);
            updateLayersTab();
            tabs.insertTab(bundle.getString("Layers"), null, layersPanel, "", 0);
            featuresPanel = new FeatureSelectionPanel(bundle, this);
            //tabs.insertTab(bundle.getString("Features"), null, featuresPanel, "", 1);

            JPanel infoPanel = new JPanel(new BorderLayout());
            infoPanel.add(buildProjectTreePanel());

            tabs.insertTab("Information", null, infoPanel, "", 1);

            JPanel resultsPanel = new JPanel(new BorderLayout());
            resultsPanel.add(buildResultsPanel());

            return tabs;
        } catch (Exception e) {
            showFeedback(e.toString());
            logger.log(Level.SEVERE, "WhiteboxGui.createTabbedPane", e);
            return null;
        }

    }
    ArrayList<LegendEntryPanelClone> legendEntries = new ArrayList<>();
    JScrollPane scrollView = new JScrollPane();

    private void updateLayersTab() {
        try {
            int pos = scrollView.getVerticalScrollBar().getValue();
            layersPanel.removeAll();
            if (legendEntries.size() <= 0) {
                getLegendEntries();
            } else {
                // how many legend entries should there be?
                int numLegendEntries = 0;
                for (MapInfo mi : openMaps) {
                    numLegendEntries++; // one for the map entry.
                    for (MapArea ma : mi.getMapAreas()) {
                        numLegendEntries++; // plus one for the mapArea
                        numLegendEntries += ma.getNumLayers();
                    }
                }
                if (numLegendEntries != legendEntries.size()) {
                    getLegendEntries();
                }
            }

            Box legendBox = Box.createVerticalBox();
            legendBox.add(Box.createVerticalStrut(5));

            JPanel legend = new JPanel();
            legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
            legend.setOpaque(true);

            // add the legend nodes
            for (LegendEntryPanelClone lep : legendEntries) {
                if (lep.getLegendEntryType() == 0) { // it's a map
                    if (lep.getMapNum() == activeMap) {
                        lep.setTitleFont(fonts.get("activeMap"));
                    } else {
                        lep.setTitleFont(fonts.get("inactiveMap"));

                        if (linkAllOpenMaps && (openMaps.get(lep.getMapNum()).getActiveMapArea().getCurrentExtent() != openMaps.get(activeMap).getActiveMapArea().getCurrentExtent())) {
                            openMaps.get(lep.getMapNum()).getActiveMapArea().setCurrentExtent(openMaps.get(activeMap).getActiveMapArea().getCurrentExtent());
                        }

                    }

                    if ((lep.getMapNum() == selectedMapAndLayer[0])
                            & (selectedMapAndLayer[1]) == -1
                            & (selectedMapAndLayer[2]) == -1) {
                        lep.setSelected(true);
                    } else {
                        lep.setSelected(false);
                    }

                    if (lep.getMapNum() == 0) {
                        legend.add(Box.createVerticalStrut(5));
                    } else {
                        legend.add(Box.createVerticalStrut(15));
                    }
                    legend.add(lep);

                } else if (lep.getLegendEntryType() == 2) { // it's a map area
                    if (lep.getMapArea() == openMaps.get(activeMap).getActiveMapArea().getElementNumber()
                            && lep.getMapNum() == activeMap) {
                        lep.setTitleFont(fonts.get("activeMap"));
                    } else {
                        lep.setTitleFont(fonts.get("inactiveMap"));
                    }
                    if ((lep.getMapArea() == selectedMapAndLayer[2])
                            & (selectedMapAndLayer[1]) == -1
                            && lep.getMapNum() == selectedMapAndLayer[0]) {
                        lep.setSelected(true);
                    } else {
                        lep.setSelected(false);
                    }
                    legend.add(Box.createVerticalStrut(8));
                    legend.add(lep);

                } else if (lep.getLegendEntryType() == 1) { // it's a layer
                    if ((lep.getMapNum() == selectedMapAndLayer[0])
                            && (lep.getLayerNum() == selectedMapAndLayer[1])
                            && (lep.getMapArea() == selectedMapAndLayer[2])) {
                        lep.setSelected(true);
                    } else {
                        lep.setSelected(false);
                    }
                    if (lep.getMapNum() == activeMap) {
                        if (lep.getMapArea() == openMaps.get(activeMap).getActiveMapArea().getElementNumber()) {
                            if (lep.getLayerNum() == openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber()) {
                                lep.setTitleFont(fonts.get("activeLayer"));
                                lep.setSelected(true);
                                if (openMaps.get(activeMap).getActiveMapArea().getActiveLayer() instanceof VectorLayerInfo) {
                                    VectorLayerInfo vli = (VectorLayerInfo) openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
                                    if (vli.isActivelyEdited()) {
                                        editVectorButton.setEnabled(true);
                                        editVectorMenuItem.setState(true);
                                        digitizeNewFeatureButton.setVisible(true);
                                        deleteFeatureButton.setVisible(true);
                                        deleteLastNodeInFeatureButton.setVisible(true);
                                        digitizeNewFeatureMenuItem.setEnabled(true);
                                        deleteFeatureMenuItem.setEnabled(true);
                                        deleteLastNodeInFeatureMenuItem.setEnabled(true);
                                    } else {
                                        editVectorButton.setEnabled(true);
                                        editVectorMenuItem.setEnabled(true);
                                        editVectorMenuItem.setState(false);
                                        digitizeNewFeatureButton.setVisible(false);
                                        drawingArea.setDigitizingNewFeature(false);
                                        deleteFeatureButton.setVisible(false);
                                        deleteLastNodeInFeatureButton.setVisible(false);
                                        digitizeNewFeatureMenuItem.setState(false);
                                        digitizeNewFeatureMenuItem.setEnabled(false);
                                        deleteFeatureMenuItem.setEnabled(false);
                                        deleteLastNodeInFeatureMenuItem.setEnabled(false);
                                    }
                                } else {
                                    editVectorButton.setEnabled(false);
                                    editVectorMenuItem.setEnabled(false);
                                    editVectorMenuItem.setState(false);
                                    digitizeNewFeatureButton.setVisible(false);
                                    drawingArea.setDigitizingNewFeature(false);
                                    deleteFeatureButton.setVisible(false);
                                    deleteLastNodeInFeatureButton.setVisible(false);
                                    digitizeNewFeatureMenuItem.setState(false);
                                    digitizeNewFeatureMenuItem.setEnabled(false);
                                    deleteFeatureMenuItem.setEnabled(false);
                                    deleteLastNodeInFeatureMenuItem.setEnabled(false);
                                }
                            } else {
                                lep.setTitleFont(fonts.get("inactiveLayer"));
                            }
                        } else {
                            lep.setTitleFont(fonts.get("inactiveLayer"));
                        }
                    } else {
                        lep.setTitleFont(fonts.get("inactiveLayer"));
                    }

//                JPanel layerBox = new JPanel();
//                layerBox.setLayout(new BoxLayout(layerBox, BoxLayout.X_AXIS));
//                layerBox.setMaximumSize(new Dimension(1000, 20));
                    Box layerBox = Box.createHorizontalBox();
                    layerBox.setOpaque(false);
                    layerBox.add(Box.createHorizontalStrut(10));
                    layerBox.add(lep);
                    layerBox.add(Box.createHorizontalGlue());
                    legend.add(Box.createVerticalStrut(5));
                    legend.add(lep);
                }
            }

            legend.add(Box.createVerticalGlue());
            legend.setBackground(Color.white);
            scrollView = new JScrollPane(legend);
            layersPanel.add(scrollView, BorderLayout.CENTER);
            layersPanel.validate();
            layersPanel.repaint();

            scrollView.getVerticalScrollBar().setValue(pos);
        } catch (Exception e) {
            System.out.println(e.toString());
            logger.log(Level.WARNING, "WhiteboxGui.updateLayersTab", e);
        }
    }

    private void getLegendEntries() {
        legendEntries.clear();
        // add the map nodes
        int i = 0;
        for (MapInfo mi : openMaps) {
            LegendEntryPanelClone legendMapEntry;
            if (i == activeMap) {
                legendMapEntry = new LegendEntryPanelClone(mi.getMapName(),
                        this, fonts.get("activeMap"), i, -1, -1, (i == selectedMapAndLayer[0]));
            } else {
                legendMapEntry = new LegendEntryPanelClone(mi.getMapName(),
                        this, fonts.get("inactiveMap"), i, -1, -1, (i == selectedMapAndLayer[0]));
            }
            legendEntries.add(legendMapEntry);

            for (MapArea mapArea : mi.getMapAreas()) {
                LegendEntryPanelClone legendMapAreaEntry;
//                legendMapAreaEntry = new LegendEntryPanelClone(mapArea, 
//                    this, fonts.get("inactiveMap"), i, mapArea.getElementNumber(), 
//                        -1, (mapArea.getElementNumber() == selectedMapAndLayer[2]));
                legendMapAreaEntry = new LegendEntryPanelClone(mapArea,
                        this, fonts.get("inactiveMap"), i, mapArea.getElementNumber(),
                        -1, (mapArea.getElementNumber() == selectedMapAndLayer[2]
                        & selectedMapAndLayer[1] == -1));
                legendEntries.add(legendMapAreaEntry);

                for (int j = mapArea.getNumLayers() - 1; j >= 0; j--) {
                    // add them to the tree in the order of their overlayNumber
                    MapLayer layer = mapArea.getLayer(j);
                    LegendEntryPanelClone legendLayer;
                    if (j == mapArea.getActiveLayerOverlayNumber()) {
                        legendLayer = new LegendEntryPanelClone(layer, this, fonts.get("activeLayer"),
                                i, mapArea.getElementNumber(), j, (j == selectedMapAndLayer[1]));
                        if (layer.getLayerType() == MapLayer.MapLayerType.VECTOR
                                && legendLayer.getMapArea() == openMaps.get(activeMap).getActiveMapArea().getElementNumber()
                                && legendLayer.getMapNum() == activeMap) {
                            // get the name of the shapefile
                            VectorLayerInfo vli = (VectorLayerInfo) layer;
                            String fileName = vli.getFileName();
                            // see if this is the current shapefile on the feature selection panel and
                            // if not, update it.
                            if (featuresPanel.getVectorLayerInfo() == null || !featuresPanel.getVectorLayerInfo().getFileName().equals(fileName)) {
                                featuresPanel.setVectorLayerInfo(vli);
                            }
                        }
                    } else {
                        legendLayer = new LegendEntryPanelClone(layer, this, fonts.get("inactiveLayer"),
                                i, mapArea.getElementNumber(), j, (j == selectedMapAndLayer[1]));
                    }
                    legendEntries.add(legendLayer);
                }
            }
            i++;

        }

    }

    public void layersTabMousePress(MouseEvent e, int mapNum, int mapArea, int layerNum) {
        // update the selected map and layer
        selectedMapAndLayer[0] = mapNum;
        selectedMapAndLayer[1] = layerNum;
        selectedMapAndLayer[2] = mapArea;

//        if (selectedMapAndLayer[0] != mapNum ||
//                selectedMapAndLayer[1] != layerNum ||
//                selectedMapAndLayer[2] != mapArea) {
//            selectedMapAndLayer[0] = mapNum;
//            selectedMapAndLayer[1] = layerNum;
//            selectedMapAndLayer[2] = mapArea;
//        } else if (e.getClickCount() != 2) {
//            selectedMapAndLayer[0] = -1;
//            selectedMapAndLayer[1] = -1;
//            selectedMapAndLayer[2] = -1;
//        }
        if (e.getButton() == 3 || e.isPopupTrigger()) {
            // is it a map?
            if (layerNum == -1 && mapArea == -1) { // it's a map
                mapsPopup.show((JComponent) e.getSource(), e.getX(), e.getY());
            } else if (layerNum == -1) { // it's a mapArea
                mapAreaPopup.show((JComponent) e.getSource(), e.getX(), e.getY());
            } else { // it's a layer
                // see if it's a raster or vector layer
                openMaps.get(mapNum).deslectAllCartographicElements();
                ArrayList<MapArea> mapAreas = openMaps.get(mapNum).getMapAreas();

                MapArea activeMapArea = null;
                for (MapArea ma : mapAreas) {
                    if (ma.getElementNumber() == mapArea) {
                        activeMapArea = ma;
                    }
                }
                if (activeMapArea != null) {
                    LayersPopupMenu layersPopup = new LayersPopupMenu(activeMapArea.getLayer(layerNum),
                            this, graphicsDirectory, bundle);
                    layersPopup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
            }
        } else { //if (e.getClickCount() == 1) {
            openMaps.get(activeMap).deslectAllCartographicElements();
            if (layerNum == -1) {
                setAsActiveMap();

            } else {
                setAsActiveLayer();
            }
            refreshMap(true);
        } //else if (e.getClickCount() == 1) {
//            updateLayersTab();
//        }
    }

    private JSplitPane getToolbox() {
        try {
            // create the tool treeview
            File file = new File(toolboxFile);

            if (!file.exists()) {
                showFeedback(messages.getString("MissingToolbox"));
                return null;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node topNode = doc.getFirstChild();
            DefaultMutableTreeNode result = populateTree(topNode);

            // This adds any tools that may be contain in the top-level and 
            // not in any toolbox.
            ArrayList<String> t = findToolsInToolbox("topmost");
            if (t.size() > 0) {
                for (String t1 : t) {
                    String[] toolDetails = t1.split(";");
                    IconTreeNode childTreeNode2 = new IconTreeNode(toolDetails[0]);
                    if (toolDetails[1].equals("false")) {
                        childTreeNode2.setIconName("tool");
                    } else {
                        childTreeNode2.setIconName("script");
                    }
                    result.add(childTreeNode2);
                }
            }

            tree = new JTree(result); //populateTree(topNode));

            MouseListener ml = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    String label;
                    if (selRow != -1) {
                        if (e.getClickCount() == 1) {
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                            if (n.getChildCount() == 0) {
                                label = selPath.getLastPathComponent().toString();
                                if (e.isPopupTrigger() || e.getButton() == 3) {
                                    if (isToolAScript(label)) {
                                        JPopupMenu pm = new JPopupMenu();
                                        final String scriptName = getScriptFile(label);
                                        JMenuItem mi = new JMenuItem(bundle.getString("EditScript"));
                                        mi.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                editScript(scriptName);
                                            }
                                        });
                                        //mi.setActionCommand("editScript");
                                        pm.add(mi);

                                        mi = new JMenuItem(bundle.getString("UpdateScript"));
                                        mi.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                String[] args = new String[1];
                                                args[0] = FileUtilities.getShortFileName(scriptName);
                                                String updateScript = resourcesDirectory + "plugins" + pathSep + "Scripts" + pathSep + "UpdateScriptFile.groovy";
                                                executeScriptFile(updateScript, args, false);
                                            }
                                        });
                                        pm.add(mi);

                                        pm.show((JComponent) e.getSource(), e.getX(), e.getY());

                                    }
                                } else {
                                    showToolDescription(label);
                                }
                            } else if (n.toString().equals(bundle.getString("topmost"))) {
                                // set the message indicating the number of plugins that were located.
                                status.setMessage(" " + plugInfo.size() + " " + messages.getString("PluginsWereLocated"));
                            }
                        } else if (e.getClickCount() == 2) {
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                            if (n.getChildCount() == 0) {
                                label = selPath.getLastPathComponent().toString();
                                launchDialog(label);
                            }
                        }
                    }
                }
            };
            tree.addMouseListener(ml);

            HashMap icons = new HashMap();
            icons.put("toolbox", new ImageIcon(graphicsDirectory + "opentools.png", ""));
            icons.put("tool", new ImageIcon(graphicsDirectory + "tool.png", ""));
            icons.put("script", new ImageIcon(graphicsDirectory + "ScriptIcon2.png", ""));

//            ImageIcon leafIcon = new ImageIcon(graphicsDirectory + "tool.png", "");
//            ImageIcon leafIconScript = new ImageIcon(graphicsDirectory + "ScriptIcon2.png", "");
//            ImageIcon stemIcon = new ImageIcon(graphicsDirectory + "opentools.png", "");
//            DefaultTreeCellRenderer renderer =
//                    new DefaultTreeCellRenderer();
//            renderer.setLeafIcon(leafIcon);
//            renderer.setClosedIcon(stemIcon);
//            renderer.setOpenIcon(stemIcon);
            tree.putClientProperty("JTree.icons", icons);
            tree.setCellRenderer(new TreeNodeRenderer()); //renderer);

            JScrollPane treeView = new JScrollPane(tree);

            // create the quick launch
            qlTabs = new JTabbedPane();

            MouseListener ml2 = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    JList theList = (JList) e.getSource();
                    String label = null;
                    int index = theList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        Object o = theList.getModel().getElementAt(index);
                        label = o.toString();
                    }
                    if (e.getClickCount() == 1) {
                        showToolDescription(label);
                    } else if (e.getClickCount() == 2) {
                        launchDialog(label);

                    }

                }
            };

            //DefaultListModel model = new DefaultListModel();
            allTools = new JList();
            recentTools = new JList();
            mostUsedTools = new JList();

            populateToolTabs();

            allTools.addMouseListener(ml2);
            recentTools.addMouseListener(ml2);
            mostUsedTools.addMouseListener(ml2);

            JScrollPane scroller1 = new JScrollPane(allTools);
            JScrollPane scroller2 = new JScrollPane(recentTools);
            JScrollPane scroller3 = new JScrollPane(mostUsedTools);

            JPanel allToolsPanel = new JPanel();
            allToolsPanel.setLayout(new BoxLayout(allToolsPanel, BoxLayout.Y_AXIS));
            Box box = Box.createHorizontalBox();
            box.add(Box.createHorizontalStrut(3));
            box.add(new JLabel(bundle.getString("Search") + ":"));
            box.add(Box.createHorizontalStrut(3));
            searchText.setMaximumSize(new Dimension(275, 22));
            box.setMaximumSize(new Dimension(275, 24));
            searchText.addActionListener(searchFieldListener);
            box.add(searchText);
            allToolsPanel.add(box);
            allToolsPanel.add(scroller1);

            qlTabs.insertTab(bundle.getString("All"), null, allToolsPanel, "", 0); // + plugInfo.size() + " tools", null, scroller1, "", 2);
            qlTabs.insertTab(bundle.getString("Most_Used"), null, scroller3, "", 1);
            qlTabs.insertTab(bundle.getString("Recent"), null, scroller2, "", 2);

            //qlTabs.setPreferredSize(new Dimension(200, splitterToolboxLoc));
            qlTabs.setSelectedIndex(qlTabsIndex);

            splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeView, qlTabs);
            splitPane2.setResizeWeight(1);
            //splitPane2.setDividerLocation(0.75); //splitterToolboxLoc);
            splitPane2.setOneTouchExpandable(true);

            return splitPane2;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.log(Level.SEVERE, "WhiteboxGui.getToolbox", e);
            showFeedback(e.toString());
            return null;
        }
    }

    private void editScript(String scriptName) {
        Scripter scripter = new Scripter(this, false, scriptName);
        scripter.openFile(scriptName);
        scripter.setVisible(true);
    }
    private ActionListener searchFieldListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            searchForWords();
        }
    };

    private void searchForWords() {
        try {
            DefaultListModel model = new DefaultListModel();
            String searchString = searchText.getText().toLowerCase();
            String descriptiveName, shortName, description;

            if (searchString == null || searchString.equals("")) {

                for (int i = 0; i < plugInfo.size(); i++) {
                    plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_NAMES);
                }
                Collections.sort(plugInfo);
                for (int i = 0; i < plugInfo.size(); i++) {
                    model.add(i, plugInfo.get(i).getDescriptiveName());
                }

            } else {

                // find quotations
                ArrayList<String> quotedStrings = new ArrayList<>();
                Pattern p = Pattern.compile("\"([^\"]*)\"");
                Matcher m = p.matcher(searchString);
                while (m.find()) {
                    quotedStrings.add(m.group(1));
                }

                // now remove all quotedStrings from the line
                for (int i = 0; i < quotedStrings.size(); i++) {
                    searchString = searchString.replace(quotedStrings.get(i), "");
                }

                searchString = searchString.replace("\"", "");

                int count = 0;
                boolean containsWord;

                searchString = searchString.replace("-", " ");
                searchString = searchString.replace(" the ", "");
                searchString = searchString.replace(" a ", "");
                searchString = searchString.replace(" of ", "");
                searchString = searchString.replace(" to ", "");
                searchString = searchString.replace(" and ", "");
                searchString = searchString.replace(" be ", "");
                searchString = searchString.replace(" in ", "");
                searchString = searchString.replace(" it ", "");

                String[] words = searchString.split(" ");
                for (int i = 0; i < plugInfo.size(); i++) {
                    plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_NAMES);
                }
                Collections.sort(plugInfo);
                for (int i = 0; i < plugInfo.size(); i++) {
                    descriptiveName = plugInfo.get(i).getDescriptiveName().toLowerCase().replace("-", " ");
                    shortName = plugInfo.get(i).getName().toLowerCase().replace("-", " ");
                    description = plugInfo.get(i).getDescription();
                    containsWord = false;

                    for (String word : words) {
                        if (descriptiveName.contains(word)) {
                            containsWord = true;
                        }
                        if (shortName.contains(word)) {
                            containsWord = true;
                        }
                        if (description.contains(" " + word + " ")) {
                            containsWord = true;
                        }
                    }

                    for (String word : quotedStrings) {
                        if (descriptiveName.contains(word)) {
                            containsWord = true;
                        }
                        if (shortName.contains(word)) {
                            containsWord = true;
                        }
                        if (description.contains(" " + word + " ")) {
                            containsWord = true;
                        }
                    }
                    if (containsWord) {
                        model.add(count, plugInfo.get(i).getDescriptiveName());
                        count++;
                    }

                }
            }
            allTools.setModel(model);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "WhiteboxGui.searchForWords", e);
        }
    }

    private DefaultMutableTreeNode populateTree(Node n) {
        ArrayList<String> t;
        Element e = (Element) n;
        String label = e.getAttribute("label");
        String toolboxName = e.getAttribute("name");
        //DefaultMutableTreeNode result;
        IconTreeNode result;
        if (bundle.containsKey(toolboxName)) {
            result = new IconTreeNode(bundle.getString(toolboxName));
        } else {
            result = new IconTreeNode(label);
        }
        result.setIconName("toolbox");

        NodeList nodeList = n.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) nodeList.item(i);
                label = childElement.getAttribute("label");
                toolboxName = childElement.getAttribute("name");
                IconTreeNode childTreeNode;
                if (bundle.containsKey(toolboxName)) {
                    childTreeNode = new IconTreeNode(bundle.getString(toolboxName));
                } else {
                    childTreeNode = new IconTreeNode(label);
                }
                childTreeNode.setIconName("toolbox");

                t = findToolsInToolbox(toolboxName);

                if (nodeList.item(i).getFirstChild() != null) {
                    NodeList childNodeList = nodeList.item(i).getChildNodes();
                    for (int j = 0; j < childNodeList.getLength(); j++) {
                        if (childNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            childTreeNode.add(populateTree(childNodeList.item(j)));
                        }
                    }

                }

                if (t.size() > 0) {
                    for (int k = 0; k < t.size(); k++) {
                        String[] toolDetails = t.get(k).split(";");
                        IconTreeNode childTreeNode2 = new IconTreeNode(toolDetails[0]);
                        if (toolDetails[1].equals("false")) {
                            childTreeNode2.setIconName("tool");
                        } else {
                            childTreeNode2.setIconName("script");
                        }
                        childTreeNode.add(childTreeNode2);
                    }
                } else if (nodeList.item(i).getFirstChild() == null) {
                    IconTreeNode childTreeNode2 = new IconTreeNode("No tools");
                    childTreeNode2.setIconName("tool");
                    childTreeNode.add(childTreeNode2);
                }

                result.add(childTreeNode);
            }
        }
        if (nodeList.getLength() == 0) {
            t = findToolsInToolbox(toolboxName);
            if (t.size() > 0) {
                for (int k = 0; k < t.size(); k++) {
                    String[] toolDetails = t.get(k).split(";");
                    IconTreeNode childTreeNode2 = new IconTreeNode(toolDetails[0]);
                    if (toolDetails[1].equals("false")) {
                        childTreeNode2.setIconName("tool");
                    } else {
                        childTreeNode2.setIconName("script");
                    }
                    result.add(childTreeNode2);
                }
            } else {
                IconTreeNode childTreeNode2 = new IconTreeNode("No tools");
                childTreeNode2.setIconName("tool");
                result.add(childTreeNode2);
            }
        }

//        toolboxName = "topmost";
//        t = findToolsInToolbox(toolboxName);
//        if (t.size() > 0) {
//            for (int k = 0; k < t.size(); k++) {
//                String[] toolDetails = t.get(k).split(";");
//                IconTreeNode childTreeNode2 = new IconTreeNode(toolDetails[0]);
//                if (toolDetails[1].equals("false")) {
//                    childTreeNode2.setIconName("tool");
//                } else {
//                    childTreeNode2.setIconName("script");
//                }
//                result.add(childTreeNode2);
//            }
//        }
        return result;

    }

    private ArrayList<String> findToolsInToolbox(String toolbox) {
        Iterator<WhiteboxPlugin> iterator = pluginService.getPlugins();
        ArrayList<String> plugs = new ArrayList<>();
        String plugName;
        String plugDescriptiveName;
//        while (iterator.hasNext()) {
//            WhiteboxPlugin plugin = iterator.next();
//            String[] tbox = plugin.getToolbox();
//            for (int i = 0; i < tbox.length; i++) {
//                if (tbox[i].equals(toolbox)) {
//                    plugName = plugin.getName();
//                    if (pluginBundle.containsKey(plugName)) {
//                        plugDescriptiveName = pluginBundle.getString(plugName);
//                    } else {
//                        plugDescriptiveName = plugin.getDescriptiveName();
//                    }
//                    plugs.add(plugDescriptiveName);
//                }
//            }
//
//        }
        for (PluginInfo pi : plugInfo) {
            String[] tbox = pi.getToolboxes();
            for (int i = 0; i < tbox.length; i++) {
                if (tbox[i].equals(toolbox)) {
                    plugName = pi.getName();
                    if (pluginBundle.containsKey(plugName)) {
                        plugDescriptiveName = pluginBundle.getString(plugName);
                    } else {
                        plugDescriptiveName = pi.getDescriptiveName();
                    }
                    plugs.add(plugDescriptiveName + ";" + Boolean.toString(pi.isScript()));
                }
            }
        }
        Collections.sort(plugs, new SortIgnoreCase());
        return plugs;
    }

    @Override
    public ResourceBundle getGuiLabelsBundle() {
        return bundle;
    }

    @Override
    public ResourceBundle getMessageBundle() {
        return messages;
    }

    @Override
    public String getLanguageCountryCode() {
        return language + "_" + country;
    }

    @Override
    public void setLanguageCountryCode(String code) {
        String[] str = code.split("_");
        if (str.length != 2) {
            showFeedback("Language-Country code improperly formated");
            return;
        }
        language = str[0];
        country = str[1];

        //currentLocale = new Locale(language, country);
        WhiteboxInternationalizationTools.setLocale(language, country);
        bundle = WhiteboxInternationalizationTools.getGuiLabelsBundle(); //ResourceBundle.getBundle("whiteboxgis.i18n.GuiLabelsBundle", currentLocale);
        messages = WhiteboxInternationalizationTools.getMessagesBundle(); //ResourceBundle.getBundle("whiteboxgis.i18n.messages", currentLocale);
    }

    public boolean isCheckForUpdates() {
        return checkForUpdates;
    }

    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
    }

    public boolean isReceiveAnnouncements() {
        return receiveAnnouncements;
    }

    public void setReceiveAnnouncements(boolean receiveAnnouncements) {
        this.receiveAnnouncements = receiveAnnouncements;
    }

    public class SortIgnoreCase implements Comparator<Object> {

        @Override
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    @Override
    public int showFeedback(String message) {
        if (suppressReturnedData) {
            return - 1; // returns can be disruptive for scripts
        }
        JOptionPane.showMessageDialog(this, message);
        return -1;
    }

    @Override
    public int showFeedback(String message, int optionType, int messageType) {
        if (suppressReturnedData) {
            return - 1; // returns can be disruptive for scripts
        }
        Object[] options;
        if (optionType == JOptionPane.YES_NO_CANCEL_OPTION) {
            options = new Object[]{"Yes", "No", "Cancel"};
        } else if (optionType == JOptionPane.YES_NO_OPTION) {
            options = new Object[]{"Yes", "No"};
        } else {
            options = new Object[]{"OK"};
        }
        int n = JOptionPane.showOptionDialog(this,
                message,
                "Whitebox GAT Message",
                optionType,
                messageType,
                null, //do not use a custom Icon
                options, //the titles of buttons
                options[0]); //default button title

        return n;
    }

    String progressString = "";
    int progressValue = 0;
    private boolean isUpdateProgressEnabled = true;

    public void setUpdateProgressEnabled(boolean value) {
        isUpdateProgressEnabled = value;
    }

    @Override
    public void updateProgress(String progressLabel, int progress) {
        if (!isUpdateProgressEnabled) {
            return;
        }
        if (!progressLabel.equals(progressString) || progress != progressValue) {
            if (progress < 0) {
                progress = 0;
            }
            if (progress > 100) {
                progress = 100;
            }
            status.setProgress(progress);
            status.setProgressLabel(progressLabel);
            progressValue = progress;
            progressString = progressLabel;
        }
    }

    @Override
    public void updateProgress(int progress) {
        if (!isUpdateProgressEnabled) {
            return;
        }
        if (progress != progressValue) {
            if (progress < 0) {
                progress = 0;
            }
            if (progress > 100) {
                progress = 100;
            }
            status.setProgress(progress);
            progressValue = progress;
        }
    }

    private void populateToolTabs() {
        int maxIndex;
        if (plugInfo.size() <= 10) {
            maxIndex = plugInfo.size();
        } else {
            maxIndex = 10;
        }
        allTools.removeAll();
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_NAMES);
        }
        Collections.sort(plugInfo);
        for (int i = 0; i < plugInfo.size(); i++) {
            model.add(i, plugInfo.get(i).getDescriptiveName());
        }
        allTools.setModel(model);

        recentTools.removeAll();
        DefaultListModel model2 = new DefaultListModel();
        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_RECENT);
        }
        Collections.sort(plugInfo);
        for (int i = 0; i < maxIndex; i++) {
            model2.add(i, plugInfo.get(i).getDescriptiveName());
        }
        recentTools.setModel(model2);

        mostUsedTools.removeAll();
        DefaultListModel model3 = new DefaultListModel();
        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_USAGE);
        }
        Collections.sort(plugInfo);
        for (int i = 0; i < maxIndex; i++) {
            model3.add(i, plugInfo.get(i).getDescriptiveName());
        }
        mostUsedTools.setModel(model3);

    }

    private void refreshToolUsage() {
        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setNumTimesUsed(0);
            plugInfo.get(i).setLastUsedToNow();
        }

        populateToolTabs();
    }

    private void exportLayer() {
        try {
            // which layer is being exported?
            String fileName = "";
            MapLayer layer;
            if (selectedMapAndLayer[1] != -1) {
                // a layer has been selected for removal.
                layer = openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).getLayer(selectedMapAndLayer[1]);
            } else if (selectedMapAndLayer[2] != -1) {
                // a mapArea has been selected. remove it's active layer.
                MapArea ma = openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]);
                if (ma == null) {
                    return;
                }
                layer = ma.getLayer(ma.getActiveLayerOverlayNumber());
            } else {
                // remove the active layer
                int activeLayer = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
                layer = openMaps.get(activeMap).getActiveMapArea().getLayer(activeLayer);
            }

            MapLayerType layerType = layer.getLayerType();
            boolean isRasterLayerType = true;
            if (layerType == MapLayerType.VECTOR) {
                isRasterLayerType = false;
                VectorLayerInfo vli = (VectorLayerInfo) layer;
                fileName = vli.getFileName();
            } else {
                RasterLayerInfo rli = (RasterLayerInfo) layer;
                fileName = rli.getHeaderFile();
            }

            ArrayList<String> listItems = new ArrayList<>();
            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                if (igdf.getInteropPluginType() == InteropPluginType.exportPlugin
                        && igdf.isRasterFormat() == isRasterLayerType) {
                    String listEntry = igdf.getName() + " Files (";
                    for (String ext : igdf.getSupportedExtensions()) {
                        listItems.add(listEntry + "*." + ext.toLowerCase() + ")");
                    }
                }
            }

            Object[] objectList = new Object[listItems.size()];
            int i = 0;
            for (String str : listItems) {
                objectList[i] = str;
                i++;
            }
            Object defaultObject = objectList[0];
            if (isRasterLayerType) {
                defaultObject = objectList[1]; // the second entry will be the ArcGIS float 
            }
            String returnString = (String) JOptionPane.showInputDialog(this,
                    "Select an export data type:", "Export Data Type",
                    JOptionPane.OK_CANCEL_OPTION, null, objectList,
                    defaultObject);

            if (returnString == null) {
                return;
            }
            String[] tmp = returnString.split("\\.");
            String extension = tmp[1].replace(")", "");

            // see if the extension is in the list of supported extensions
            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                if (igdf.getInteropPluginType() == InteropPluginType.exportPlugin) {
                    for (String ext : igdf.getSupportedExtensions()) {
                        if (ext.toLowerCase().equals(extension)
                                && returnString.contains(igdf.getName())) {
                            String myExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

                            String fileType = igdf.getName();
                            String outputFile = StringUtilities.replaceLast(fileName, myExtension, extension);
                            File file2 = new File(outputFile);
                            // see if the file exists already, and if so, should it be overwritten?
                            if (file2.exists()) {
                                int n = showFeedback("You are exporting a " + fileType + " file. The "
                                        + fileType + " file already exists.\n"
                                        + "Would you like to delete it?", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);

                                if (n == JOptionPane.YES_OPTION) {
                                    file2.delete();
                                } else if (n == JOptionPane.NO_OPTION) {
                                    return;
                                }
                            }
                            String[] args = {fileName};
                            runPlugin(igdf.getInteropClass(), args);
                            return;
                        }
                    }
                }
            }

        } catch (Exception e) {
            logException("WhiteboxGIS.addLayer", e);
        }
    }

    private void addLayer() {
        try {
            // set the filter.
            ArrayList<ExtensionFileFilter> filters = new ArrayList<>();

            // how many supported file formats are there?
            int numSupportedFileFormats = 0;
            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                    numSupportedFileFormats += igdf.getSupportedExtensions().length;
                }
            }

            String filterDescription = "All Supported Files";
            String[] extensions = new String[numSupportedFileFormats + 2];
            extensions[0] = "DEP";
            extensions[1] = "SHP";
            int i = 2;
            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                for (String ext : igdf.getSupportedExtensions()) {
                    if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                        extensions[i] = ext.toUpperCase();
                        i++;
                    }
                }
            }

            ExtensionFileFilter eff = new ExtensionFileFilter(filterDescription, extensions);
            filters.add(eff);

            filterDescription = "Shapefiles (*.shp)";
            extensions = new String[]{"SHP"};
            eff = new ExtensionFileFilter(filterDescription, extensions);
            filters.add(eff);

            filterDescription = "Whitebox Raster Files (*.dep)";
            extensions = new String[]{"DEP"};
            eff = new ExtensionFileFilter(filterDescription, extensions);
            filters.add(eff);

            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                    filterDescription = igdf.getName() + " Files (";
                    extensions = new String[igdf.getSupportedExtensions().length];
                    i = 0;
                    for (String ext : igdf.getSupportedExtensions()) {
                        if (i == 0) {
                            filterDescription += "*." + ext.toLowerCase();
                        } else {
                            filterDescription += ", *." + ext.toLowerCase();
                        }
                        extensions[i] = ext.toUpperCase();
                        i++;
                    }
                    filterDescription += ")";
                    eff = new ExtensionFileFilter(filterDescription, extensions);
                    filters.add(eff);
                }
            }

            JFileChooser fc = new JFileChooser();

            fc.setCurrentDirectory(new File(workingDirectory));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setMultiSelectionEnabled(true);
            fc.setAcceptAllFileFilterUsed(false);

            for (i = 0; i < filters.size(); i++) {
                fc.addChoosableFileFilter(filters.get(i));
            }

            fc.setFileFilter(filters.get(0));

            int result = fc.showOpenDialog(this);

            String selectedFilterDescription = fc.getFileFilter().getDescription();

            File[] files = null;
            if (result == JFileChooser.APPROVE_OPTION) {
                files = fc.getSelectedFiles();
                String fileDirectory = files[0].getParentFile() + pathSep;
                if (!fileDirectory.equals(workingDirectory)) {
                    setWorkingDirectory(fileDirectory);
                }
                for (File file : files) {
                    //addLayer(file.toString());

                    String fileName = file.toString();

                    int dot = fileName.lastIndexOf(".");
                    String extension = fileName.substring(dot + 1).toLowerCase();

                    if (extension.equals("dep") || extension.equals("shp")) {
                        addLayer(file.toString());
                    } else {
                        // see if the selectedFilterDescription is ambiguous and if so
                        // ask the user for clarification
                        if (selectedFilterDescription.equals("All Supported Files")) {
                            ArrayList<String> possibleFilters = new ArrayList<>();
                            for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                                if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                                    String listEntry = igdf.getName() + " Files (";
                                    for (String ext : igdf.getSupportedExtensions()) {
                                        if (ext.toLowerCase().equals(extension)) {
                                            possibleFilters.add(listEntry + "*." + ext.toLowerCase() + ")");
                                            break;
                                        }
                                    }
                                }
                            }
                            if (possibleFilters.size() > 1) {
                                Object[] objectList = new Object[possibleFilters.size()];
                                i = 0;
                                for (String str : possibleFilters) {
                                    objectList[i] = str;
                                    i++;
                                }

                                String returnString = (String) JOptionPane.showInputDialog(this,
                                        "Select an import data type:", "Ambiguous Import Data Type",
                                        JOptionPane.OK_CANCEL_OPTION, null, objectList,
                                        objectList[0]);

                                if (returnString == null) {
                                    return;
                                }

                                selectedFilterDescription = returnString;
                            } else {
                                selectedFilterDescription = possibleFilters.get(0);
                            }
                        }

                        // see if the extension is in the list of supported extensions
                        for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                            if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                                for (String ext : igdf.getSupportedExtensions()) {
                                    if (ext.toLowerCase().equals(extension)
                                            && selectedFilterDescription.contains(igdf.getName())) {
                                        String myExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

                                        String fileType = igdf.getName();
                                        if (igdf.isRasterFormat()) {
                                            String whiteboxHeaderFile = StringUtilities.replaceLast(fileName, myExtension, "dep");
                                            File file2 = new File(whiteboxHeaderFile);
                                            // see if the file exists already, and if so, should it be overwritten?
                                            if (file2.exists()) {
                                                int n = showFeedback("You are importing a " + fileType + " file by converting it to "
                                                        + "a Whitebox Raster format. \nThe Whitebox Raster file already exists. "
                                                        + "Would you like to display the existing file instead?", JOptionPane.YES_NO_CANCEL_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);

                                                if (n == JOptionPane.YES_OPTION) {
                                                    addLayer(whiteboxHeaderFile);
                                                    return;
                                                } else if (n == JOptionPane.CANCEL_OPTION) {
                                                    return;
                                                }
                                            } else {
                                                showFeedback("You are importing a " + fileType + " file by converting it to \n"
                                                        + "a Whitebox Raster format. The newly created Whitebox \n"
                                                        + "Raster will be added to the map.");
                                            }
                                            String[] args = {fileName};
                                            runPlugin(igdf.getInteropClass(), args);
//                                            return;
                                        } else {
                                            String shapefile = StringUtilities.replaceLast(fileName, myExtension, "shp");
                                            File file2 = new File(shapefile);
                                            // see if the file exists already, and if so, should it be overwritten?
                                            if (file2.exists()) {
                                                int n = showFeedback("You are importing a " + fileType + " file by converting it to "
                                                        + "a Shapefile format. \nThe Shapefile already exists. "
                                                        + "Would you like to display the existing file instead?", JOptionPane.YES_NO_CANCEL_OPTION,
                                                        JOptionPane.QUESTION_MESSAGE);

                                                if (n == JOptionPane.YES_OPTION) {
                                                    addLayer(shapefile);
                                                    return;
                                                } else if (n == JOptionPane.CANCEL_OPTION) {
                                                    return;
                                                }
                                            } else {
                                                showFeedback("You are importing a " + fileType + " file by converting it to \n"
                                                        + "a Shapefile format. The newly created Whitebox \n"
                                                        + "Shapefile will be added to the map.");
                                            }
                                            String[] args = {fileName};
                                            runPlugin(igdf.getInteropClass(), args);
//                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (HeadlessException e) {
            logException("WhiteboxGIS.addLayer", e);
        }
    }

    private void addLayer(String fileName) {
        try {
            int mapNum;
            int mapAreaNum;
            if (selectedMapAndLayer[0] != -1) {
                mapNum = selectedMapAndLayer[0];
                mapAreaNum = selectedMapAndLayer[2];
            } else if (openMaps.isEmpty()) {
                mapNum = 0;
                mapAreaNum = 0;
            } else {
                mapNum = activeMap;
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }

            if (openMaps.isEmpty()) {
                // create a new map to overlay the layer onto.
                numOpenMaps = 1;
                MapInfo mapinfo = new MapInfo("Map1");
                mapinfo.setMargin(defaultMapMargin);
                mapinfo.setMapName("Map1");
                MapArea ma = new MapArea("MapArea1");
                ma.setUpperLeftX(-32768);
                ma.setUpperLeftY(-32768);
                ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
                mapinfo.addNewCartographicElement(ma);
                openMaps.add(mapinfo);
                drawingArea.setMapInfo(openMaps.get(0));
                activeMap = 0;
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }
            if (mapAreaNum < 0) { // there is no mapArea or the only mapArea is part of a CartographicElementGroup.
                MapArea ma = new MapArea("MapArea1");
                ma.setUpperLeftX(-32768);
                ma.setUpperLeftY(-32768);
                ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
                openMaps.get(activeMap).addNewCartographicElement(ma);
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }
            File file = new File(fileName);
            if (!file.exists()) {
                showFeedback(messages.getString("NoDataLayer"));
                return;
            }
            String fileDirectory = file.getParentFile() + pathSep;
            if (!fileDirectory.equals(workingDirectory)) {
                setWorkingDirectory(fileDirectory);
            }
            String[] defaultPalettes = {defaultQuantPalette, defaultQualPalette, "rgb.pal"};
            MapArea activeMapArea = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
            // get the file extension.
            int dot = fileName.lastIndexOf(".");
            String extension = fileName.substring(dot + 1).toLowerCase();

            if (extension.equals("dep")) {
                RasterLayerInfo newLayer = new RasterLayerInfo(file.toString(), paletteDirectory,
                        defaultPalettes, 255, activeMapArea.getNumLayers());
                activeMapArea.addLayer(newLayer);
                newLayer.setOverlayNumber(activeMapArea.getNumLayers() - 1);
            } else if (extension.equals("shp")) {
                VectorLayerInfo newLayer = new VectorLayerInfo(file.toString(), paletteDirectory,
                        255, activeMapArea.getNumLayers());
                activeMapArea.addLayer(newLayer);
                newLayer.setOverlayNumber(activeMapArea.getNumLayers() - 1);
            } else {
                // see if the extension is in the list of supported extensions
                for (InteroperableGeospatialDataFormat igdf : interopGeospatialDataFormat) {
                    if (igdf.getInteropPluginType() == InteropPluginType.importPlugin) {
                        for (String ext : igdf.getSupportedExtensions()) {
                            if (ext.toLowerCase().equals(extension)) {
                                String myExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

                                String fileType = igdf.getName();
                                if (igdf.isRasterFormat()) {
                                    String whiteboxHeaderFile = StringUtilities.replaceLast(fileName, myExtension, "dep");
                                    File file2 = new File(whiteboxHeaderFile);
                                    // see if the file exists already, and if so, should it be overwritten?
                                    if (file2.exists()) {
                                        int n = showFeedback("You are importing a " + fileType + " file by converting it to "
                                                + "a Whitebox Raster format. \nThe Whitebox Raster file already exists. "
                                                + "Would you like to display the existing file instead?", JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);

                                        if (n == JOptionPane.YES_OPTION) {
                                            addLayer(whiteboxHeaderFile);
                                            return;
                                        } else if (n == JOptionPane.CANCEL_OPTION) {
                                            return;
                                        }
                                    } else {
                                        showFeedback("You are importing a " + fileType + " file by converting it to \n"
                                                + "a Whitebox Raster format. The newly created Whitebox \n"
                                                + "Raster will be added to the map.");
                                    }
                                    String[] args = {fileName};
                                    runPlugin(igdf.getInteropClass(), args);
//                                    return;
                                } else {
                                    String shapefile = StringUtilities.replaceLast(fileName, myExtension, "shp");
                                    File file2 = new File(shapefile);
                                    // see if the file exists already, and if so, should it be overwritten?
                                    if (file2.exists()) {
                                        int n = showFeedback("You are importing a " + fileType + " file by converting it to "
                                                + "a Shapefile format. \nThe Shapefile already exists. "
                                                + "Would you like to display the existing file instead?", JOptionPane.YES_NO_CANCEL_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);

                                        if (n == JOptionPane.YES_OPTION) {
                                            addLayer(shapefile);
                                            return;
                                        } else if (n == JOptionPane.CANCEL_OPTION) {
                                            return;
                                        }
                                    } else {
                                        showFeedback("You are importing a " + fileType + " file by converting it to \n"
                                                + "a Shapefile format. The newly created Whitebox \n"
                                                + "Shapefile will be added to the map.");
                                    }
                                    String[] args = {fileName};
                                    runPlugin(igdf.getInteropClass(), args);
//                                    return;
                                }
                            }
                        }
                    }
                }
            }

            activeMapArea.setActiveLayer(activeMapArea.getNumLayers() - 1);

            recentFilesMenu.addMenuItem(fileName);
            recentFilesMenu2.addMenuItem(fileName);
            recentFilesPopupMenu.addMenuItem(fileName);

            refreshMap(true);
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } catch (Exception e) {
            logException("WhiteboxGIS.addLayer", e);
        }
    }

    private void addLayer(MapLayer mapLayer) {
        try {
            int mapNum;
            int mapAreaNum;
            if (selectedMapAndLayer[0] != -1) {
                mapNum = selectedMapAndLayer[0];
                mapAreaNum = selectedMapAndLayer[2];
            } else if (openMaps.isEmpty()) {
                mapNum = 0;
                mapAreaNum = 0;
            } else {
                mapNum = activeMap;
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }

            if (openMaps.isEmpty()) {
                // create a new map to overlay the layer onto.
                numOpenMaps = 1;
                MapInfo mapinfo = new MapInfo("Map1");
                mapinfo.setMargin(defaultMapMargin);
                mapinfo.setMapName("Map1");
                MapArea ma = new MapArea("MapArea1");
                ma.setUpperLeftX(-32768);
                ma.setUpperLeftY(-32768);
                ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
                mapinfo.addNewCartographicElement(ma);
                openMaps.add(mapinfo);
                drawingArea.setMapInfo(openMaps.get(0));
                activeMap = 0;
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }
            if (mapAreaNum < 0) { // there is no mapArea or the only mapArea is part of a CartographicElementGroup.
                MapArea ma = new MapArea("MapArea1");
                ma.setUpperLeftX(-32768);
                ma.setUpperLeftY(-32768);
                ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
                openMaps.get(activeMap).addNewCartographicElement(ma);
                mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            }
            MapArea activeMapArea = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);

            activeMapArea.addLayer(mapLayer);
            mapLayer.setOverlayNumber(activeMapArea.getNumLayers() - 1);

            activeMapArea.setActiveLayer(activeMapArea.getNumLayers() - 1);

            String fileName = "";
            if (mapLayer instanceof RasterLayerInfo) {
                RasterLayerInfo rli = (RasterLayerInfo) mapLayer;
                fileName = rli.getHeaderFile();
            } else if (mapLayer instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) mapLayer;
                fileName = vli.getFileName();
            }
            recentFilesMenu.addMenuItem(fileName);
            recentFilesMenu2.addMenuItem(fileName);
            recentFilesPopupMenu.addMenuItem(fileName);

            refreshMap(true);
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } catch (Exception e) {
            logException("WhiteboxGIS.addLayer", e);
        }
    }

    private void removeLayer() {
        if (selectedMapAndLayer[1] != -1) {
            // a layer has been selected for removal.
            openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).removeLayer(selectedMapAndLayer[1]);
            drawingArea.repaint();
            featuresPanel.setVectorLayerInfo(null);
            updateLayersTab();
        } else if (selectedMapAndLayer[2] != -1) {
            // a mapArea has been selected. remove it's active layer.
            MapArea ma = openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]);
            if (ma == null) {
                return;
            }
            ma.removeLayer(ma.getActiveLayerOverlayNumber());
            drawingArea.repaint();
            featuresPanel.setVectorLayerInfo(null);
            updateLayersTab();
        } else {
            // remove the active layer
            int activeLayer = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
            openMaps.get(activeMap).getActiveMapArea().removeLayer(activeLayer);
            drawingArea.repaint();
            featuresPanel.setVectorLayerInfo(null);
            updateLayersTab();
        }
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void newMap(String mapName) {
        numOpenMaps++;
        MapInfo mapinfo = new MapInfo(mapName);
        mapinfo.setMapName(mapName);
        mapinfo.setWorkingDirectory(workingDirectory);
        mapinfo.setPageFormat(defaultPageFormat);
        mapinfo.setDefaultFont(defaultFont);
        mapinfo.setMargin(defaultMapMargin);
        mapinfo.setPageVisible(false);

        MapArea ma = new MapArea(bundle.getString("MapArea").replace(" ", "") + "1");
        ma.setUpperLeftX(-32768);
        ma.setUpperLeftY(-32768);
        ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
        mapinfo.addNewCartographicElement(ma);

        openMaps.add(mapinfo); //new MapInfo(str));
        activeMap = numOpenMaps - 1;
        drawingArea.setMapInfo(openMaps.get(activeMap));
        drawingArea.repaint();

        updateLayersTab();

        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void newMap() {

        String str = JOptionPane.showInputDialog(messages.getString("NewMapName")
                + ": ", bundle.getString("Map") + (numOpenMaps + 1));

        if (str != null) {
            numOpenMaps++;
            MapInfo mapinfo = new MapInfo(str);
            mapinfo.setMapName(str);
            mapinfo.setWorkingDirectory(workingDirectory);
            mapinfo.setPageFormat(defaultPageFormat);
            mapinfo.setDefaultFont(defaultFont);
            mapinfo.setMargin(defaultMapMargin);
            mapinfo.setPageVisible(false);

            MapArea ma = new MapArea(bundle.getString("MapArea").replace(" ", "") + "1");
            ma.setUpperLeftX(-32768);
            ma.setUpperLeftY(-32768);
            ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
            mapinfo.addNewCartographicElement(ma);

            openMaps.add(mapinfo); //new MapInfo(str));
            activeMap = numOpenMaps - 1;
            drawingArea.setMapInfo(openMaps.get(activeMap));
            drawingArea.repaint();

            updateLayersTab();

            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        }

    }

    private void closeMap() {
        if (numOpenMaps > 0) {
            if (selectedMapAndLayer[0] == -1) {
                selectedMapAndLayer[0] = activeMap;
            }
            if (selectedMapAndLayer[0] == activeMap) {
                openMaps.remove(activeMap);
                numOpenMaps--;
                activeMap--;
                if (activeMap < 0) {
                    activeMap = 0;
                }
                if (numOpenMaps > 0) {
                    drawingArea.setMapInfo(openMaps.get(activeMap));
                } else {
                    drawingArea.setMapInfo(new MapInfo(bundle.getString("Map")));
                }
                drawingArea.repaint();
            } else {
                openMaps.remove(selectedMapAndLayer[0]);
                numOpenMaps--;
                if (selectedMapAndLayer[0] < activeMap) {
                    activeMap--;
                }

                if (activeMap < 0) {
                    activeMap = 0;
                }
                if (numOpenMaps > 0) {
                    drawingArea.setMapInfo(openMaps.get(activeMap));
                } else {
                    drawingArea.setMapInfo(new MapInfo(bundle.getString("Map")));
                }
                drawingArea.repaint();

            }
        }
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    /* Prints the active map. */
    private void printMap() {
        // The map must be displayed in the drawing area.
        if (selectedMapAndLayer[0] == -1) {
            selectedMapAndLayer[0] = activeMap;
        }
        if (selectedMapAndLayer[0] != activeMap) {
            setAsActiveMap();
        }
        PrinterJob job = PrinterJob.getPrinterJob();
//        PageFormat pf = openMaps.get(selectedMapAndLayer[0]).getPageFormat();
//        Book book = new Book();//java.awt.print.Book
//        book.append(drawingArea, pf);
//        job.setPageable(book);
        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
//        job.setPrintable(drawingArea);
        boolean ok = job.printDialog(aset);
        if (ok) {
            try {
                PageFormat pf = job.defaultPage();
                Book book = new Book();//java.awt.print.Book
                book.append(drawingArea, pf);
                job.setPageable(book);
                job.print(aset);
            } catch (PrinterException ex) {
                showFeedback(messages.getString("PrintingError") + ex);
                logger.log(Level.SEVERE, "WhiteboxGui.printMap", ex);
                /* The job did not successfully complete */
            }
        }
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    // Renames the selected map.
    private void renameMap() {
        if (selectedMapAndLayer[0] == - 1) {
            selectedMapAndLayer[0] = activeMap;
        }
        // find the title element
        int i = -1;
        for (CartographicElement ce : openMaps.get(activeMap).getCartographicElementList()) {
            if (ce instanceof MapTitle) {
                i = ce.getElementNumber();
                break;
            }
        }
//        showMapProperties(i);

        String str = JOptionPane.showInputDialog("Enter the new name: ",
                openMaps.get(selectedMapAndLayer[0]).getMapName());
        if (str != null) {
            openMaps.get(selectedMapAndLayer[0]).setMapName(str);
            updateLayersTab();
        }

        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    // Saves the selected active map.
    private void saveMap() {
        if (numOpenMaps < 1) {
            return;
        } // do nothing
        if (selectedMapAndLayer[0] == - 1) {
            selectedMapAndLayer[0] = activeMap;
        }
        if (openMaps.get(selectedMapAndLayer[0]).getFileName().equals("")) {
            saveMapAs();
        } else {

            // Any CartographicElementGroups in the map will need to be ungrouped.
            int howManyGroups = openMaps.get(selectedMapAndLayer[0]).numberOfElementGroups();
            if (howManyGroups > 0) {
                showFeedback(messages.getString("NoGroupsInSavedMap"));
                openMaps.get(selectedMapAndLayer[0]).ungroupAllElements();
            }

            File file = new File(openMaps.get(selectedMapAndLayer[0]).getFileName());

            if (file.exists()) {
                file.delete();
            }

            recentMapsMenu.addMenuItem(openMaps.get(selectedMapAndLayer[0]).getFileName());

            FileWriter fw = null;
            BufferedWriter bw = null;
            PrintWriter out = null;
            try {
                fw = new FileWriter(file, false);
                bw = new BufferedWriter(fw);
                out = new PrintWriter(bw, true);

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setPrettyPrinting();
                gsonBuilder.registerTypeAdapter(MapInfo.class, new MapInfoSerializer());
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

        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    // Finds a file name to save the active map to.
    private void saveMapAs() {
        if (numOpenMaps < 1) {
            return;
        } // do nothing
        // get the title of the active map.
        if (selectedMapAndLayer[0] == - 1) {
            selectedMapAndLayer[0] = activeMap;
        }
        String mapTitle = openMaps.get(selectedMapAndLayer[0]).getMapName();

        // Ask the user to specify a file name for saving the active map.
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setCurrentDirectory(new File(workingDirectory + pathSep + mapTitle + ".wmap"));
        fc.setAcceptAllFileFilterUsed(false);

        File f = new File(workingDirectory + pathSep + mapTitle + ".wmap");
        fc.setSelectedFile(f);

        // set the filter.
        String filterDescription = "Whitebox Map Files (*.wmap)";
        String[] extensions = {"WMAP"};
        ExtensionFileFilter eff = new ExtensionFileFilter(filterDescription, extensions);
        fc.setFileFilter(eff);

        int result = fc.showSaveDialog(this);
        File file = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            // see if file has an extension.
            String mapFile = file.toString();
            if (!mapFile.contains(".wmap")) {
                mapFile = mapFile + ".wmap";
                file = new File(mapFile);
            }

            String fileDirectory = file.getParentFile() + pathSep;
            if (!fileDirectory.equals(workingDirectory)) {
                setWorkingDirectory(fileDirectory);
            }

            // see if the file exists already, and if so, should it be overwritten?
            if (file.exists()) {
                int n = showFeedback(messages.getString("FileExists"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (n == JOptionPane.YES_OPTION) {
                    file.delete();
                } else if (n == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            openMaps.get(selectedMapAndLayer[0]).setFileName(mapFile);

            saveMap();

        }
    }

    private void openMap() {
        // set the filter.
        ArrayList<ExtensionFileFilter> filters = new ArrayList<>();
        String filterDescription = "Whitebox Map Files (*.wmap)";
        String[] extensions = {"WMAP"}; //, "XML"};
        ExtensionFileFilter eff = new ExtensionFileFilter(filterDescription, extensions);

        filters.add(eff);

        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(workingDirectory));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        fc.setAcceptAllFileFilterUsed(false);

        for (int i = 0; i < filters.size(); i++) {
            fc.setFileFilter(filters.get(i));
        }

        int result = fc.showOpenDialog(this);
        File[] files = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            files = fc.getSelectedFiles();
            String fileDirectory = files[0].getParentFile() + pathSep;
            if (!fileDirectory.equals(workingDirectory)) {
                setWorkingDirectory(fileDirectory);
            }
            for (int i = 0; i < files.length; i++) {

                try {
                    // first read the text from the file into a string
                    String mapTextData = whitebox.utilities.FileUtilities.readFileAsString(files[i].toString());

                    // now use gson to create a new MapInfo object by deserialization
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setPrettyPrinting();
                    gsonBuilder.registerTypeAdapter(MapInfo.class,
                            new MapInfoDeserializer(workingDirectory, paletteDirectory));
                    Gson gson = gsonBuilder.create();

                    MapInfo map = gson.fromJson(mapTextData, MapInfo.class);

                    openMaps.add(map);
                } catch (Exception e) {
                    showFeedback(messages.getString("MapFile")
                            + files[i].toString() + " "
                            + messages.getString("NotReadProperly"));
                    logger.log(Level.SEVERE, "WhiteboxGui.openMap", e);
                    return;
                }

                recentMapsMenu.addMenuItem(files[i].toString());
            }

            activeMap = openMaps.size() - 1;
            drawingArea.setMapInfo(openMaps.get(activeMap));
            drawingArea.repaint();
            updateLayersTab();
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
            numOpenMaps++;
        }
    }

    private void openMap(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            showFeedback(messages.getString("NoMapFile"));
            return;
        }

        String fileDirectory = file.getParentFile() + pathSep;
        if (!fileDirectory.equals(workingDirectory)) {
            setWorkingDirectory(fileDirectory);
        }

        try {
            // first read the text from the file into a string
            String mapTextData = whitebox.utilities.FileUtilities.readFileAsString(fileName);

            // now use gson to create a new MapInfo object by deserialization
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setPrettyPrinting();
            gsonBuilder.registerTypeAdapter(MapInfo.class,
                    new MapInfoDeserializer(workingDirectory, paletteDirectory));
            Gson gson = gsonBuilder.create();

            MapInfo map = gson.fromJson(mapTextData, MapInfo.class);

            openMaps.add(map);
        } catch (IOException | JsonSyntaxException e) {
            showFeedback(messages.getString("MapFile") + " "
                    + fileName + " " + messages.getString("NotReadProperly"));
            logger.log(Level.SEVERE, "WhiteboxGui.openMap", e);
            return;
        }

        recentMapsMenu.addMenuItem(fileName);

        activeMap = openMaps.size() - 1;
        drawingArea.setMapInfo(openMaps.get(activeMap));
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
        numOpenMaps++;
    }
    private int numExportedImages = 0;

    private void exportMapAsImage() {
        if (numOpenMaps < 1) {
            return;
        } // do nothing
        if (selectedMapAndLayer[0] == - 1) {
            selectedMapAndLayer[0] = activeMap;
        }

        if (numExportedImages == 0) {
            showFeedback(messages.getString("CurrentPrintResolution") + " "
                    + printResolution + " dpi.\n"
                    + messages.getString("ChangePrintResolution"));
        }

        // get the title of the active map.
        String mapTitle = openMaps.get(selectedMapAndLayer[0]).getMapName();

        // Ask the user to specify a file name for saving the active map.
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setCurrentDirectory(new File(workingDirectory + pathSep + mapTitle + ".png"));
        fc.setAcceptAllFileFilterUsed(false);

        File f = new File(workingDirectory + pathSep + mapTitle + ".png");
        fc.setSelectedFile(f);

        // set the filter.
        ArrayList<ExtensionFileFilter> filters = new ArrayList<>();
        String[] extensions = ImageIO.getReaderFormatNames(); //{"PNG", "JPEG", "JPG"};
        String filterDescription = "Image Files (" + extensions[0];
        for (int i = 1; i < extensions.length; i++) {
            filterDescription += ", " + extensions[i];
        }
        filterDescription += ")";
        ExtensionFileFilter eff = new ExtensionFileFilter(filterDescription, extensions);
        fc.setFileFilter(eff);

        int result = fc.showSaveDialog(this);
        File file = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            // see if file has an extension.
            if (file.toString().lastIndexOf(".") <= 0) {
                String fileName = file.toString() + ".png";
                file = new File(fileName);
            }

            String fileDirectory = file.getParentFile() + pathSep;
            if (!fileDirectory.equals(workingDirectory)) {
                setWorkingDirectory(fileDirectory);
            }

            // see if the file exists already, and if so, should it be overwritten?
            if (file.exists()) {
                int n = showFeedback(messages.getString("FileExists") + "\n"
                        + messages.getString("Overwrite"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (n == JOptionPane.YES_OPTION) {
                    file.delete();
                } else if (n == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            if (!drawingArea.saveToImage(file.toString())) {
                showFeedback(messages.getString("ErrorWhileSavingMap"));
            }
        }

        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;

        numExportedImages++;
    }

    private void setAsActiveLayer() {
        if (selectedMapAndLayer[0] == activeMap) {
            openMaps.get(activeMap).getMapAreaByElementNum(selectedMapAndLayer[2]).setActiveLayer(selectedMapAndLayer[1]);
            openMaps.get(activeMap).setActiveMapAreaByElementNum(selectedMapAndLayer[2]);

            updateLayersTab();
        } else {
            if (selectedMapAndLayer[0] == -1) {
                return;
            }
            // first update the activeMap
            activeMap = selectedMapAndLayer[0];
            openMaps.get(activeMap).getMapAreaByElementNum(selectedMapAndLayer[2]).setActiveLayer(selectedMapAndLayer[1]);
            openMaps.get(activeMap).setActiveMapAreaByElementNum(selectedMapAndLayer[2]);
            drawingArea.setMapInfo(openMaps.get(activeMap));
            drawingArea.repaint();
            updateLayersTab();
        }
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void toggleLayerVisibility() {
        openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).toggleLayerVisibility(selectedMapAndLayer[1]);
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void toggleAllLayerVisibility() {
        for (int i = 0; i < openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).getNumLayers(); i++) {
            openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).toggleLayerVisibility(i);
        }
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void allLayersVisibile() {
        int j, k;
        if (selectedMapAndLayer[0] != -1) {
            j = selectedMapAndLayer[0];
            k = selectedMapAndLayer[2];
        } else {
            j = activeMap;
            k = openMaps.get(j).getActiveMapAreaElementNumber();
        }
        MapArea ma = openMaps.get(j).getMapAreaByElementNum(k);
        if (ma == null) {
            return;
        }
        for (int i = 0; i < ma.getNumLayers(); i++) {
            ma.getLayer(i).setVisible(true);
        }
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void allLayersInvisibile() {
        int j, k;
        if (selectedMapAndLayer[0] != -1) {
            j = selectedMapAndLayer[0];
            k = selectedMapAndLayer[2];
        } else {
            j = activeMap;
            k = openMaps.get(j).getActiveMapAreaElementNumber();
        }
        MapArea ma = openMaps.get(j).getMapAreaByElementNum(k);
        if (ma == null) {
            return;
        }
        for (int i = 0; i < ma.getNumLayers(); i++) {
            ma.getLayer(i).setVisible(false);
        }
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void toggleLayerVisibilityInLegend() {
        openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).toggleLayerVisibilityInLegend(selectedMapAndLayer[1]);
        drawingArea.repaint();
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void setAsActiveMap() {
        if (selectedMapAndLayer[0] != -1) {
            activeMap = selectedMapAndLayer[0];
            if (selectedMapAndLayer[2] != -1) {
                //openMaps.get(activeMap).setActiveMapAreaByElementNum(selectedMapAndLayer[2]); // this may have to be changed to the overlay number rather than the element number
                openMaps.get(activeMap).setActiveMapAreaByElementNum(openMaps.get(activeMap).getCartographicElement(selectedMapAndLayer[2]).getElementNumber());
            }
            drawingArea.setMapInfo(openMaps.get(activeMap));
            drawingArea.repaint();
            updateLayersTab();
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        }
    }

    private void changeLayerTitle() {
        String str = JOptionPane.showInputDialog("Enter the new title: ",
                openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).getLayer(selectedMapAndLayer[1]).getLayerTitle());
        if (str != null) {
            openMaps.get(selectedMapAndLayer[0]).getMapAreaByElementNum(selectedMapAndLayer[2]).getLayer(selectedMapAndLayer[1]).setLayerTitle(str);
            updateLayersTab();
        }
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void layerToTop() {
        int mapNum;
        int mapAreaNum;
        int layerNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 1) {
            ma.promoteLayerToTop(layerNum);
        }
        legendEntries.clear();
        updateLayersTab();
        drawingArea.repaint();
    }

    private void layerToBottom() {
        int mapNum;
        int mapAreaNum;
        int layerNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            layerNum = selectedMapAndLayer[1];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 1) {
            ma.demoteLayerToBottom(layerNum);
        }
        legendEntries.clear();
        updateLayersTab();
        drawingArea.repaint();
    }

    private void promoteLayer() {
        int mapNum;
        int mapAreaNum;
        int layerNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }

        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 1) {
            ma.promoteLayer(layerNum);
        }
        legendEntries.clear();
        updateLayersTab();
        drawingArea.repaint();
    }

    public void demoteLayer() {
        int mapNum;
        int mapAreaNum;
        int layerNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 1) {
            ma.demoteLayer(layerNum);
        }
        legendEntries.clear();
        updateLayersTab();
        drawingArea.repaint();
    }

    /**
     * Changes the palette of a displayed raster layer.
     */
    public void changePalette() {
        int mapNum;
        int mapAreaNum;
        int layerNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getLayer(layerNum).getLayerType() == MapLayerType.RASTER) {
            RasterLayerInfo rli = (RasterLayerInfo) ma.getLayer(layerNum);
            String palette = rli.getPaletteFile();
            boolean isReversed = rli.isPaletteReversed();
            double nonlinearity = rli.getNonlinearity();
            PaletteChooser chooser = new PaletteChooser(this, true, paletteDirectory, palette,
                    isReversed, nonlinearity);
            chooser.setSize(300, 300);
            chooser.setVisible(true);

            String newPaletteFile = chooser.getValue();
            chooser.dispose();
            if (newPaletteFile != null) {
                if (!newPaletteFile.equals("") && !newPaletteFile.equals("createNewPalette")) {
                    rli.setPaletteFile(newPaletteFile);
                    rli.update();
                    refreshMap(true);
                } else if (newPaletteFile.equals("createNewPalette")) {
                    PaletteManager pm = new PaletteManager(paletteDirectory,
                            bundle);
                    pm.setVisible(true);
                }
            }
        }
    }

    public void reversePalette() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerOverlayNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.reversePaletteOfLayer(layerOverlayNum);
            refreshMap(true);
        }
    }

    @Override
    public void zoomToFullExtent() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            BoundingBox db = ma.getFullExtent();
            ma.setCurrentExtent(db.clone());
            refreshMap(false);
        }
    }

    public void fitToData() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.fitToData();
            refreshMap(false);
        }
    }

    public void fitToPage() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        BoundingBox pageExtent = openMaps.get(mapNum).getPageExtent();
        int margin = (int) (openMaps.get(mapNum).getMargin() * 72);
        int referenceMarkSize = ma.getReferenceMarksSize();
        ma.setUpperLeftX(margin);
        ma.setUpperLeftY(margin);
        ma.setWidth((int) (pageExtent.getWidth() - 2 * margin - referenceMarkSize));
        ma.setHeight((int) (pageExtent.getHeight() - 2 * margin - referenceMarkSize));
        refreshMap(false);
    }

    public void maximizeMapAreaScreenSize() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.setSizeMaximizedToScreenSize(!ma.isSizeMaximizedToScreenSize());
            refreshMap(false);
        }
    }

    @Override
    public void zoomToPage() {
        int mapNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the map
            mapNum = activeMap;
        }
        openMaps.get(mapNum).zoomToPage();
        refreshMap(false);
    }

    @Override
    public void zoomToLayer() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            if (selectedMapAndLayer[1] != -1) {
                layerOverlayNum = selectedMapAndLayer[1];
            } else {
                // use the active layer
                layerOverlayNum = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum).getActiveLayerOverlayNumber();
            }
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.calculateFullExtent();
            BoundingBox db = ma.getLayer(layerOverlayNum).getFullExtent();
            ma.setCurrentExtent(db);

            refreshMap(false);
        }
    }

    public void zoomToSelection() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            if (selectedMapAndLayer[1] != -1) {
                layerOverlayNum = selectedMapAndLayer[1];
            } else {
                // use the active layer
                layerOverlayNum = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum).getActiveLayerOverlayNumber();
            }
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active layer and map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(mapNum).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            //ma.calculateFullExtent();
            MapLayer ml = ma.getLayer(layerOverlayNum);
            if (ml instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) ml;
                BoundingBox db = vli.getSelectedExtent();
                if (db != null) {
                    ma.setCurrentExtent(db);
                    refreshMap(false);
                }
            }
        }
    }

    @Override
    public void zoomIn() {
        int mapNum;
        int mapAreaNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.zoomIn();
            refreshMap(false);
        }
    }

    @Override
    public void zoomOut() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.zoomOut();
            refreshMap(false);
        }
    }

    private void panUp() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.panUp();
            refreshMap(false);
        }
    }

    private void panDown() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.panDown();
            refreshMap(false);
        }
    }

    private void panLeft() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.panLeft();
            refreshMap(false);
        }
    }

    private void panRight() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            ma.panRight();
            refreshMap(false);
        }
    }

    private void nextExtent() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            boolean ret = ma.nextExtent();
            if (ret) {
                refreshMap(false);
            }
        }
    }

    private void previousExtent() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() > 0) {
            boolean ret = ma.previousExtent();
            if (ret) {
                refreshMap(false);
            }
        }
    }

    private void showLayerProperties() {
        try {
            int mapNum;
            int mapAreaNum;
            int layerOverlayNum;

            if (selectedMapAndLayer[0] != -1) {
                mapNum = selectedMapAndLayer[0];
                layerOverlayNum = selectedMapAndLayer[1];
                mapAreaNum = selectedMapAndLayer[2];
                selectedMapAndLayer[0] = -1;
                selectedMapAndLayer[1] = -1;
                selectedMapAndLayer[2] = -1;
            } else {
                // use the active map
                mapNum = activeMap;
                layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
                mapAreaNum = openMaps.get(activeMap).getActiveMapArea().getElementNumber();
            }
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
            if (ma == null) {
                return;
            }
            if (ma.getLayer(layerOverlayNum).getLayerType() == MapLayerType.RASTER
                    || ma.getLayer(layerOverlayNum).getLayerType() == MapLayerType.VECTOR) {
                MapLayer layer = ma.getLayer(layerOverlayNum);
                LayerProperties lp = new LayerProperties(this, false, layer, openMaps.get(mapNum));
                lp.setSize(640, 420);
                lp.setVisible(true);
                //lp.dispose();
            }
        } catch (Exception e) {
            System.out.println("BINGO");
        }
    }

    public void showMapProperties(int activeElement) {
        int mapNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
        }
        MapProperties mp;
        if (activeElement >= 0) {
            mp = new MapProperties(this, false, openMaps.get(mapNum), activeElement);
        } else {
            mp = new MapProperties(this, false, openMaps.get(mapNum));
        }
        mp.setSize(440, 600);
        mp.setLocation(new Point(10, 30));
        mp.setVisible(true);
    }

    public void showMapAreaProperties() {
        int mapNum;
        int mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
        }
        if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
            showFeedback(messages.getString("NoMapAreas"));
            return;
        }
        MapProperties mp;
        if (mapAreaNum >= 0) {
            mp = new MapProperties(this, false, openMaps.get(mapNum), mapAreaNum);
        } else {
            mp = new MapProperties(this, false, openMaps.get(mapNum));
        }
        mp.setSize(440, 600);
        mp.setLocation(new Point(10, 30));
        mp.setVisible(true);
    }

     private void showAttributesFile() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            layerOverlayNum = selectedMapAndLayer[1];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(activeMap).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
            if (layerOverlayNum < 0) {
                showFeedback(messages.getString("NoVectorLayers"));
                return;
            }
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() == 0) {
            showFeedback(messages.getString("NoVectorLayers"));
            return;
        }
        if (ma.getLayer(layerOverlayNum).getLayerType() == MapLayerType.VECTOR) {
            VectorLayerInfo vli = (VectorLayerInfo) ma.getLayer(layerOverlayNum);
            if (!vli.doesAttributeFileExist()) {
                showFeedback(messages.getString("AttributeFileDoesNotExist"));
                return;
            }
            afv = new AttributesFileViewer(this, false, vli);
//            String shapeFileName = vli.getFileName();
//            AttributesFileViewer afv = new AttributesFileViewer(this, false, shapeFileName);
            int height = 500;
            afv.setSize((int) (height * 1.61803399), height); // golden ratio.
            afv.setVisible(true);
        } else {
            showFeedback(messages.getString("FunctionForVectorsOnly"));
        }
    }
    String currentTextFile = null;

    private void showAboutDialog() {
        AboutWhitebox about = new AboutWhitebox(this, true, graphicsDirectory,
                versionName, versionNumber);
    }

    private void callSplashScreen() {
        String splashFile = graphicsDirectory + "WhiteboxLogo.png"; //"SplashScreen.png";
        SplashWindow sw = new SplashWindow(splashFile, 2000, versionName);
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            if (!sw.getValue()) {
                sw.dispose();
            }
            t1 = System.currentTimeMillis();
        } while ((t1 - t0 < 2000) && sw.getValue());
    }

    private void modifyPixelValues() {
        if (!modifyPixelVals.isVisible()) {
            modifyPixelVals.setVisible(true);
        }
        if (drawingArea.isModifyingPixels()) { // is true; unset
            drawingArea.setModifyingPixels(false);
            modifyPixels.setState(false);
        } else {
            if (openMaps.get(activeMap).getActiveMapArea().getNumRasterLayers() > 0) {
                drawingArea.setModifyingPixels(true);
                modifyPixels.setState(true);
                // you can't modify pixels and measure distances
                drawingArea.setUsingDistanceTool(false);
                distanceToolMenuItem.setState(false);
            } else {
                showFeedback(messages.getString("NoRaster"));
            }
        }
    }

    private void distanceTool() {
        if (drawingArea.isUsingDistanceTool()) { // is true; unset
            drawingArea.setUsingDistanceTool(false);
            distanceToolMenuItem.setState(false);
            distanceToolButton.setSelected(false);
        } else {
            if (openMaps.get(activeMap).getActiveMapArea().getNumLayers() > 0) {
                drawingArea.setUsingDistanceTool(true);
                distanceToolMenuItem.setState(true);
                distanceToolButton.setSelected(true);
                // you can't modify pixels and measure distances
                drawingArea.setModifyingPixels(false);
                modifyPixels.setState(false);
            } else {
                showFeedback(messages.getString("NoLayers"));
            }
        }

    }

    @Override
    public void editVector() {
        int mapNum, layerOverlayNum, mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            layerOverlayNum = selectedMapAndLayer[1];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
            mapAreaNum = openMaps.get(activeMap).getActiveMapArea().getElementNumber();
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        MapLayer layer = ma.getLayer(layerOverlayNum);
        if (layer.getLayerType() == MapLayerType.VECTOR) {
            VectorLayerInfo vli = (VectorLayerInfo) layer;
            if (vli.isActivelyEdited()) {
                vli.setActivelyEdited(false);
            } else {
                if (!editVectorButton.isSelected()) {
                    editVectorButton.setSelected(true);
                }
                if (!editVectorMenuItem.getState()) {
                    editVectorMenuItem.setState(true);
                }
                vli.setActivelyEdited(true);

                drawingArea.setModifyingPixels(false);
                modifyPixels.setState(false);
                drawingArea.setUsingDistanceTool(false);
                distanceToolMenuItem.setState(false);

                // make sure this is the active layer.
                ma.setActiveLayer(layerOverlayNum);
            }
            refreshMap(true);
        } else {
            showFeedback(messages.getString("ActiveLayerNotVector"));
        }
    }
    boolean currentlyDigitizingNewFeature = false;

    public void digitizeNewFeature() {
        int mapNum, layerOverlayNum, mapAreaNum;
        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            layerOverlayNum = selectedMapAndLayer[1];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
            mapAreaNum = openMaps.get(activeMap).getActiveMapArea().getElementNumber();
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        MapLayer layer = ma.getLayer(layerOverlayNum);
        if (layer.getLayerType() == MapLayerType.VECTOR) {
            VectorLayerInfo vli = (VectorLayerInfo) layer;
            if (currentlyDigitizingNewFeature) {
                digitizeNewFeatureButton.setSelected(false);
                digitizeNewFeatureMenuItem.setState(false);
                drawingArea.setDigitizingNewFeature(false);
                currentlyDigitizingNewFeature = false;
            } else {
                currentlyDigitizingNewFeature = true;
                digitizeNewFeatureMenuItem.setState(true);
                drawingArea.setDigitizingNewFeature(true);
                vli.getShapefile().refreshAttributeTable();
                ShapefileDatabaseRecordEntry dataRecordEntry = new ShapefileDatabaseRecordEntry(this, true, vli.getShapefile());
                dataRecordEntry.setSize(400, 300);
                dataRecordEntry.setLocation(100, 100);
                dataRecordEntry.setVisible(true);
                Object[] recData = dataRecordEntry.getValue();
                if (recData == null) {
                    digitizeNewFeatureButton.setSelected(false);
                    digitizeNewFeatureMenuItem.setState(false);
                    drawingArea.setDigitizingNewFeature(false);
                    currentlyDigitizingNewFeature = false;
                    return;
                }
                if (vli.getShapeType().getDimension() == ShapeTypeDimension.M) {
                    vli.setMValue(dataRecordEntry.getMValue());
                }
                if (vli.getShapeType().getDimension() == ShapeTypeDimension.Z) {
                    vli.setMValue(dataRecordEntry.getZValue());
                    vli.setMValue(dataRecordEntry.getMValue());
                }
                vli.openNewFeature(recData);
            }

            if (!vli.isActivelyEdited()) {
                digitizeNewFeatureButton.setSelected(false);
                digitizeNewFeatureMenuItem.setState(false);
                drawingArea.setDigitizingNewFeature(false);
            }
        } else {
            showFeedback(messages.getString("ActiveLayerNotVector"));
        }
    }

    /**
     * Used to delete a selected vector feature that is actively being edited.
     */
    @Override
    public void deleteFeature() {
        try {
            MapLayer layer = openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
            if (layer instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) layer;
                // which feature is selected?
                if (!vli.isActivelyEdited()) {
                    showFeedback(messages.getString("NotEditingVector") + " \n"
                            + messages.getString("SelectEditVector"));
                    return;
                }
                if (vli.getSelectedFeatureNumbers().isEmpty()) {
                    showFeedback(messages.getString("NoFeaturesSelected"));
                    return;
                } else {
                    int n = showFeedback(messages.getString("DeleteFeature") + "?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (n == JOptionPane.YES_OPTION) {
                        vli.deleteSelectedFeatures();
                        vli.reloadShapefile();
                        refreshMap(false);
                    } else if (n == JOptionPane.NO_OPTION) {
                        return;
                    }
                }

            } else {
                showFeedback(messages.getString("ActiveLayerNotVector"));
            }
        } catch (Exception e) {
            showFeedback(messages.getString("Error") + e.getMessage());
            logger.log(Level.SEVERE, "WhiteboxGui.deleteFeature", e);
        }
    }

    /**
     * Used to delete the last digitized node in a feature.
     */
    @Override
    public void deleteLastNodeInFeature() {
        try {
            MapLayer layer = openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
            if (layer instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) layer;
                // which feature is selected?
                if (!vli.isActivelyEdited()) {
                    showFeedback(messages.getString("NotEditingVector") + " \n"
                            + messages.getString("SelectEditVector"));
                    return;
                }
                vli.deleteLastNodeInFeature();
                drawingArea.removeLastNodeInFeature();
                refreshMap(false);

            } else {
                showFeedback(messages.getString("ActiveLayerNotVector"));
            }
        } catch (Exception e) {
            showFeedback(messages.getString("Error") + e.getMessage());
            logger.log(Level.SEVERE, "WhiteboxGui.deleteFeature", e);
        }
    }

    private void digitizeTool() {
        MapLayer layer = openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
        if (layer instanceof VectorLayerInfo) {
            VectorLayerInfo vli = (VectorLayerInfo) layer;
            if (vli.isActivelyEdited()) {
                vli.setActivelyEdited(false);

            } else {
                vli.setActivelyEdited(true);
                drawingArea.setModifyingPixels(false);
                modifyPixels.setState(false);
                drawingArea.setUsingDistanceTool(false);
                distanceToolMenuItem.setState(false);
            }
        } else {
            showFeedback(messages.getString("ActiveLayerNotVector"));
        }
    }

    private void clipLayerToExtent() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            mapAreaNum = selectedMapAndLayer[2];
            layerOverlayNum = selectedMapAndLayer[1];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getLayer(layerOverlayNum).getLayerType() == MapLayerType.RASTER) {
            // What file name should the clipped image be given?
            String str = JOptionPane.showInputDialog(null, "Enter the name of the new file: ",
                    "Whitebox", 1);

            if (str == null) {
                return;
            }

            RasterLayerInfo layer = (RasterLayerInfo) ma.getLayer(layerOverlayNum);

            // what directory is the layer in?
            String dir = layer.getHeaderFile().substring(0,
                    layer.getHeaderFile().lastIndexOf(pathSep) + 1);

            String fileName = dir + str + ".dep";

            if (new File(fileName).exists()) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(this,
                        "The file already exists.\n"
                        + "Would you like to overwrite it?",
                        "Whitebox GAT Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[0]); //default button title

                if (n == JOptionPane.YES_OPTION) {
                    // do nothing
                } else if (n == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            layer.clipLayerToExtent(ma.getCurrentExtent(), fileName);

        } else {
            showFeedback(messages.getString("FunctionNotAvailableVectors"));
        }
    }

    private void viewHistogram() {
        int mapNum;
        int mapAreaNum;
        int layerOverlayNum;

        if (selectedMapAndLayer[0] != -1) {
            mapNum = selectedMapAndLayer[0];
            layerOverlayNum = selectedMapAndLayer[1];
            mapAreaNum = selectedMapAndLayer[2];
            selectedMapAndLayer[0] = -1;
            selectedMapAndLayer[1] = -1;
            selectedMapAndLayer[2] = -1;
        } else {
            // use the active map
            mapNum = activeMap;
            mapAreaNum = openMaps.get(mapNum).getActiveMapAreaElementNumber();
            if (mapAreaNum < 0) { // there is not mapArea or the only mapArea is part of a CartographicElementGroup.
                showFeedback(messages.getString("NoMapAreas"));
                return;
            }
            layerOverlayNum = openMaps.get(activeMap).getActiveMapArea().getActiveLayerOverlayNumber();
            if (layerOverlayNum < 0) {
                showFeedback(messages.getString("NoVectorLayers"));
                return;
            }
        }
        MapArea ma = openMaps.get(mapNum).getMapAreaByElementNum(mapAreaNum);
        if (ma == null) {
            return;
        }
        if (ma.getNumLayers() == 0) {
            showFeedback(messages.getString("NoRaster"));
            return;
        }
        if (ma.getLayer(layerOverlayNum).getLayerType() == MapLayerType.RASTER) {
            RasterLayerInfo layer = (RasterLayerInfo) ma.getLayer(layerOverlayNum);
            HistogramView histo = new HistogramView(this, false, layer.getHeaderFile(), workingDirectory);
        } else {
            showFeedback(messages.getString("RastersOnly"));
        }
    }

    private void removeAllLayers() {
        int j, k;
        if (selectedMapAndLayer[0] != -1) {
            j = selectedMapAndLayer[0];
            k = selectedMapAndLayer[2];
        } else {
            j = activeMap;
            k = openMaps.get(activeMap).getActiveMapAreaElementNumber();
        }
        MapArea mapArea = openMaps.get(j).getMapAreaByElementNum(k);
        do {
            mapArea.removeLayer(openMaps.get(j).getActiveMapArea().getNumLayers() - 1);
        } while (mapArea.getNumLayers() > 0);

        drawingArea.repaint();
        featuresPanel.setVectorLayerInfo(null);
        updateLayersTab();
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void newHelp() {
        String str = JOptionPane.showInputDialog("Help File Name: ", "");
        if (str != null) {
            String fileName = helpDirectory + str;
            if (!str.endsWith(".html")) {
                fileName += ".html";
            }

            // see if the filename exists already.
            File file = new File(fileName);
            if (file.exists()) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(this,
                        "The file already exists.\n"
                        + "Would you like to overwrite it?",
                        "Whitebox GAT Message",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //do not use a custom Icon
                        options, //the titles of buttons
                        options[0]); //default button title

                if (n == JOptionPane.YES_OPTION) {
                    file.delete();
                } else if (n == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            // grab the text within the "NewHelp.txt" file in the helpDirectory;
            String defaultHelp = helpDirectory + "NewHelp.txt";
            if (!(new File(defaultHelp)).exists()) {
                showFeedback(messages.getString("NoHelp"));
                return;
            }
            try {
                String defaultText = FileUtilities.readFileAsString(defaultHelp);
                // now place this text into the new file.
                FileUtilities.fillFileWithString(fileName, defaultText);

                ViewCodeDialog vcd = new ViewCodeDialog(this, false, new File(fileName), true);
                vcd.setSize(new Dimension(800, 600));
                vcd.setVisible(true);
            } catch (IOException ioe) {
                showFeedback(messages.getString("HelpNotRead"));
                logger.log(Level.SEVERE, "WhiteboxGui.newHelp", ioe);
                return;
            }

        }
    }

    private void helpReport() {
        String pluginName;
        String fileName;
        ArrayList<String> pluginsWithoutHelpFiles = new ArrayList<>();

        for (int i = 0; i < plugInfo.size(); i++) {
            plugInfo.get(i).setSortMode(PluginInfo.SORT_MODE_NAMES);
        }
        Collections.sort(plugInfo);

        for (PluginInfo pi : plugInfo) {
            pluginName = pi.getName();
            fileName = helpDirectory + pluginName + ".html";
            File helpFile = new File(fileName);
            if (!helpFile.exists()) {
                pluginsWithoutHelpFiles.add(pi.getDescriptiveName()); //pluginName);
            }
        }
        DecimalFormat df = new DecimalFormat("###.0");
        String percentWithoutHelp = df.format((double) pluginsWithoutHelpFiles.size() / plugInfo.size() * 100.0);
        String reportOutput;
        reportOutput = "HELP COMPLETENESS REPORT:\n\n" + "We're working hard to ensure that Whitebox's help files are "
                + "complete. Currently, " + pluginsWithoutHelpFiles.size() + " (" + percentWithoutHelp
                + "%) plugins don't have help files.\n";
        if (pluginsWithoutHelpFiles.size() > 0) {
            reportOutput += "These include the following plugins:\n\n";
            for (int i = 0; i < pluginsWithoutHelpFiles.size(); i++) {
                reportOutput += pluginsWithoutHelpFiles.get(i) + "\n";
            }
        }

        reportOutput += "\nYou can contribute by writing a help entry for a plugin tool that doesn't currently have \n"
                + "one (press the 'Create new help entry' button on the tool's dialog) or by improving the help entry \n"
                + "for a tool that already has one. Email your work to jlindsay@uoguelph.ca.\n";

        returnData(reportOutput);

    }
    int printResolution = 600;

    public void setPrintResolution(int resolution) {
        this.printResolution = resolution;
        drawingArea.setPrintResolution(resolution);
    }

    public int getPrintResolution() {
        return printResolution;
    }

    public double getDefaultMapMargin() {
        return defaultMapMargin;
    }

    public void setDefaultMapMargin(double defaultMapMargin) {
        this.defaultMapMargin = defaultMapMargin;
    }

    public int getNumberOfRecentItemsToStore() {
        return numberOfRecentItemsToStore;
    }

    public void setNumberOfRecentItemsToStore(int numberOfRecentItemsToStore) {
        this.numberOfRecentItemsToStore = numberOfRecentItemsToStore;
    }

    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(Font font) {
        this.defaultFont = font;
        if (openMaps.size() > 0) {
            for (MapInfo mi : openMaps) {
                mi.setDefaultFont(font);
            }
        }
    }

    public boolean isHideAlignToolbar() {
        return hideAlignToolbar;
    }

    public void setHideAlignToolbar(boolean hideAlignToolbar) {
        this.hideAlignToolbar = hideAlignToolbar;
    }

    private void addMapImage() {
        whitebox.ui.ImageFileChooser ifc = new whitebox.ui.ImageFileChooser();
        ifc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        ifc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ifc.setMultiSelectionEnabled(false);
        ifc.setAcceptAllFileFilterUsed(false);
        ifc.setCurrentDirectory(new File(workingDirectory));

        int result = ifc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = ifc.getSelectedFile();
            String selectedFile = file.toString();
            openMaps.get(activeMap).addMapImage(selectedFile);
            refreshMap(false);
        }
    }

    @Override
    public void showHelp() {
        Help help = new Help(this, false, "index");
        help.setVisible(true);
    }

    @Override
    public void showHelp(String helpFile) {
        Help help = new Help(this, false, "index", helpFile);
        help.setVisible(true);
    }

    @Override
    public void logException(String message, Exception e) {
        logger.log(Level.SEVERE, "Whitebox " + versionNumber + ". Error\n" + message, e);
    }

    @Override
    public void logThrowable(String message, Throwable t) {
        logger.log(Level.SEVERE, "Whitebox " + versionNumber + ". Throwable\n" + message, t);
    }

    @Override
    public void logMessage(Level level, String message) {
        logger.log(level, "Whitebox " + versionNumber + ". Message\n" + message);
    }

    @Override
    public void setSelectFeature() {
        drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_FEATURE_SELECT);
        selectMenuItem.setState(false);
        selectFeatureMenuItem.setState(true);
        zoomMenuItem.setState(false);
        zoomOutMenuItem.setState(false);
        panMenuItem.setState(false);
        tabs.setSelectedIndex(1);
        if (!selectFeature.isSelected()) {
            selectFeature.setSelected(true);
        }
        select.setSelected(false);
        pan.setSelected(false);
        zoomIntoBox.setSelected(false);
    }

    @Override
    public void deselectAllFeaturesInActiveLayer() {
        try {
            MapLayer layer = openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
            if (layer instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) layer;
                vli.clearSelectedFeatures();
                refreshMap(false);
                featuresPanel.updateTable();
            } else {
                showFeedback(messages.getString("ActiveLayerNotVector"));
            }
        } catch (Exception e) {
            showFeedback(messages.getString("Error") + e.getMessage());
            logger.log(Level.SEVERE, "WhiteboxGui.deleteFeature", e);
        }
    }

    @Override
    public void saveSelection() {
        try {
            MapLayer layer = openMaps.get(activeMap).getActiveMapArea().getActiveLayer();
            if (layer instanceof VectorLayerInfo) {
                VectorLayerInfo vli = (VectorLayerInfo) layer;
                if (vli.getSelectedFeatureNumbers().isEmpty()) {
                    return;
                }

                // Ask the user to specify a file name for saving the active map.
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setCurrentDirectory(new File(workingDirectory + pathSep + "NewFile.shp"));
                fc.setAcceptAllFileFilterUsed(false);

                File f = new File(workingDirectory + pathSep + "NewFile.shp");
                fc.setSelectedFile(f);

                // set the filter.
                String filterDescription = "Whitebox Vector Files (*.shp)";
                String[] extensions = {"SHP"};
                ExtensionFileFilter eff = new ExtensionFileFilter(filterDescription, extensions);
                fc.setFileFilter(eff);

                int result = fc.showSaveDialog(this);
                File file = null;
                if (result == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    // see if file has an extension.
                    String newFile = file.toString();
                    if (!newFile.contains(".shp")) {
                        newFile = newFile + ".shp";
                        file = new File(newFile);
                    }

                    String fileDirectory = file.getParentFile() + pathSep;
                    if (!fileDirectory.equals(workingDirectory)) {
                        setWorkingDirectory(fileDirectory);
                    }

                    // see if the file exists already, and if so, should it be overwritten?
                    if (file.exists()) {
                        int n = showFeedback(messages.getString("FileExists"), JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        if (n == JOptionPane.YES_OPTION) {
                            file.delete();
                        } else if (n == JOptionPane.NO_OPTION) {
                            return;
                        }
                    }

                    String fileName = file.getPath();
                    vli.saveSelectedFeatures(fileName);
                    addLayer(fileName);
                    refreshMap(false);
                    featuresPanel.updateTable();

                }

            } else {
                showFeedback(messages.getString("ActiveLayerNotVector"));
            }
        } catch (Exception e) {
            showFeedback(messages.getString("Error") + e.getMessage());
            logger.log(Level.SEVERE, "WhiteboxGui.deleteFeature", e);
        }
    }

    private void pan() {
        drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_PAN);
        selectMenuItem.setState(false);
        selectFeatureMenuItem.setState(false);
        zoomMenuItem.setState(false);
        zoomOutMenuItem.setState(false);
        panMenuItem.setState(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        Object source = e.getSource();
        String actionCommand = e.getActionCommand();
        if (actionCommand.contains("tutorial_file:\t")) {
            String[] splitString = actionCommand.split("\t");
            Help help = new Help(this, false, "tutorials", splitString[1]);
            help.setVisible(true);
            return;
        }
        switch (actionCommand) {
            case "clearAllSelectedFeatures":
                deselectAllFeaturesInActiveLayer();
                break;
            case "saveSelection":
                saveSelection();
                break;
            case "addLayer":
                addLayer();
                break;
            case "removeLayer":
                removeLayer();
                break;
            case "close":
                close();
                break;
            case "exportLayer":
                exportLayer();
                break;
            case "linkMap":
                linkAllOpenMaps = !linkAllOpenMaps;
                linkMap.setState(linkAllOpenMaps);
                break;
            case "nimbusLAF":
                setLookAndFeel("Nimbus");
                break;
            case "systemLAF":
                setLookAndFeel(getSystemLookAndFeelName());
                break;
            case "motifLAF":
                setLookAndFeel("CDE/Motif");
                break;
            case "refreshTools":
                refreshToolUsage();
                break;
            case "newMap":
                newMap();
                break;
            case "closeMap":
                closeMap();
                break;
            case "setAsActiveLayer":
                setAsActiveLayer();
                break;
            case "toggleLayerVisibility":
                toggleLayerVisibility();
                break;
            case "toggleAllLayerVisibility":
                toggleAllLayerVisibility();
                break;
            case "allLayersVisible":
                allLayersVisibile();
                break;
            case "allLayersInvisible":
                allLayersInvisibile();
                break;
            case "toggleLayerVisibilityInLegend":
                toggleLayerVisibilityInLegend();
                break;
            case "setAsActiveMap":
                setAsActiveMap();
                break;
            case "renameMap":
                renameMap();
                break;
            case "changeLayerTitle":
                changeLayerTitle();
                break;
            case "layerToTop":
                layerToTop();
                break;
            case "layerToBottom":
                layerToBottom();
                break;
            case "raiseLayer":
                promoteLayer();
                break;
            case "lowerLayer":
                demoteLayer();
                break;
            case "changePalette":
                changePalette();
                break;
            case "reversePalette":
                reversePalette();
                break;
            case "zoomToFullExtent":
                zoomToFullExtent();
                break;
            case "zoomToLayer":
                zoomToLayer();
                break;
            case "zoomToSelection":
                zoomToSelection();
                break;
            case "zoomToPage":
                zoomToPage();
                break;
            case "layerProperties":
                showLayerProperties();
                break;
            case "zoomIn":
                zoomIn();
                break;
            case "zoomOut":
                //            zoomOut();
                openMaps.get(activeMap).deslectAllCartographicElements();
                refreshMap(false);
                drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_ZOOMOUT);
                selectMenuItem.setState(false);
                selectFeatureMenuItem.setState(false);
                zoomMenuItem.setState(false);
                zoomOutMenuItem.setState(true);
                panMenuItem.setState(false);
                break;
            case "panUp":
                panUp();
                break;
            case "panDown":
                panDown();
                break;
            case "panLeft":
                panLeft();
                break;
            case "panRight":
                panRight();
                break;
            case "zoomToBox":
                openMaps.get(activeMap).deslectAllCartographicElements();
                refreshMap(false);
                drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_ZOOM);
                selectMenuItem.setState(false);
                selectFeatureMenuItem.setState(false);
                zoomMenuItem.setState(true);
                zoomOutMenuItem.setState(false);
                panMenuItem.setState(false);
                break;
            case "pan":
                pan();
                break;
            case "select":
                drawingArea.setMouseMode(MapRenderingTool.MOUSE_MODE_SELECT);
                selectMenuItem.setState(true);
                selectFeatureMenuItem.setState(false);
                zoomMenuItem.setState(false);
                zoomOutMenuItem.setState(false);
                panMenuItem.setState(false);
                break;
            case "selectFeature":
                setSelectFeature();
                break;
            case "nextExtent":
                nextExtent();
                break;
            case "previousExtent":
                previousExtent();
                break;
            case "paletteManager":
                PaletteManager pm = new PaletteManager(paletteDirectory,
                        bundle);
                pm.setVisible(true);
                if (!paletteManager.isVisible()) {
                    paletteManager.setVisible(true);
                }
                break;
            case "rasterCalculator":
                RasterCalculator rc = new RasterCalculator(this, false, workingDirectory);
                rc.setLocation(250, 250);
                rc.setVisible(true);
                break;
//            case "selectAllText":
//                textArea.selectAll();
//                break;
//            case "copyText":
//                textArea.copy();
//                break;
//            case "pasteText":
//                textArea.paste();
//                break;
//            case "cutText":
//                textArea.cut();
//                break;
//            case "clearText":
//                textArea.setText("");
//                break;
//            case "openText":
//                openText();
//                break;
//            case "saveText":
//                saveText();
//                break;
//            case "closeText":
//                textArea.setText("");
//                currentTextFile = null;
//                break;
            case "printMap":
                printMap();
                break;
            case "saveMap":
                saveMap();
                break;
            case "saveMapAs":
                saveMapAs();
                break;
            case "openMap":
                openMap();
                break;
            case "exportMapAsImage":
                exportMapAsImage();
                break;
            case "scripter":
                Scripter scripter = new Scripter(this, false);
                scripter.setVisible(true);
                break;
            case "options":
                SettingsDialog dlg = new SettingsDialog(this, false);
                dlg.setSize(500, 400);
                dlg.setVisible(true);
                break;
            case "modifyPixels":
                modifyPixelValues();
                break;
            case "helpIndex": {
                Help help = new Help(this, false, "index");
                help.setVisible(true);
                break;
            }
            case "helpSearch": {
                Help help = new Help(this, false, "search");
                help.setVisible(true);
                break;
            }
            case "helpTutorials": {
                Help help = new Help(this, false, "tutorials");
                help.setVisible(true);
                break;
            }
            case "helpAbout":
                showAboutDialog();
                break;
            case "refreshMap":
                refreshMap(true);
                break;
            case "distanceTool":
                distanceTool();
                break;
            case "clipLayerToExtent":
                clipLayerToExtent();
                break;
            case "viewHistogram":
                viewHistogram();
                break;
            case "removeAllLayers":
                removeAllLayers();
                break;
//            case "wordWrap":
//                textArea.setLineWrap(wordWrap.getState());
//                textArea.setWrapStyleWord(wordWrap.getState());
//                break;
            case "viewAttributeTable":
                showAttributesFile();
                break;
            case "newHelp":
                newHelp();
                break;
            case "mapProperties":
                showMapProperties(0);
                break;
            case "mapAreaProperties":
                showMapAreaProperties();
                break;
            case "pageProps":
                showMapProperties(-1);
                break;
            case "insertTitle":
                openMaps.get(activeMap).addMapTitle();
                refreshMap(false);
                break;
            case "insertTextArea":
                openMaps.get(activeMap).addMapTextArea();
                refreshMap(false);
                break;
            case "insertScale":
                openMaps.get(activeMap).addMapScale();
                refreshMap(false);
                break;
            case "insertNorthArrow":
                openMaps.get(activeMap).addNorthArrow();
                refreshMap(false);
                break;
            case "insertLegend":
                openMaps.get(activeMap).addLegend();
                refreshMap(false);
                break;
            case "insertNeatline":
                openMaps.get(activeMap).addNeatline();
                refreshMap(false);
                break;
            case "insertMapArea":
                int numMapAreas = openMaps.get(activeMap).getMapAreas().size();
                MapArea ma = new MapArea(bundle.getString("MapArea").replace(" ", "") + (numMapAreas + 1));
                ma.setUpperLeftX(0);
                ma.setUpperLeftY(0);
                ma.setWidth(300);
                ma.setHeight(300);
                ma.setLabelFont(new Font(defaultFont.getName(), Font.PLAIN, 10));
                openMaps.get(activeMap).addNewCartographicElement(ma);
                refreshMap(true);
                break;
            case "insertImage":
                addMapImage();
                break;
            case "deleteMapArea":
                openMaps.get(activeMap).removeCartographicElement(selectedMapAndLayer[2]);
                refreshMap(true);
                break;
            case "fitMapAreaToData":
                fitToData();
                break;
            case "fitMapAreaToPage":
                fitToPage();
                break;
            case "maximizeMapAreaScreenSize":
                maximizeMapAreaScreenSize();
                break;
            case "helpReport":
                helpReport();
                break;
            case "centerVertical":
                if (openMaps.get(activeMap).centerSelectedElementsVertically()) {
                    refreshMap(false);
                }
                break;
            case "centerHorizontal":
                if (openMaps.get(activeMap).centerSelectedElementsHorizontally()) {
                    refreshMap(false);
                }
                break;
            case "alignTop":
                if (openMaps.get(activeMap).alignSelectedElementsTop()) {
                    refreshMap(false);
                }
                break;
            case "alignBottom":
                if (openMaps.get(activeMap).alignSelectedElementsBottom()) {
                    refreshMap(false);
                }
                break;
            case "alignRight":
                if (openMaps.get(activeMap).alignSelectedElementsRight()) {
                    refreshMap(false);
                }
                break;
            case "alignLeft":
                if (openMaps.get(activeMap).alignSelectedElementsLeft()) {
                    refreshMap(false);
                }
                break;
            case "distributeVertically":
                if (openMaps.get(activeMap).distributeSelectedElementsVertically()) {
                    refreshMap(false);
                }
                break;
            case "distributeHorizontally":
                if (openMaps.get(activeMap).distributeSelectedElementsHorizontally()) {
                    refreshMap(false);
                }
                break;
            case "digitizeTool":
                digitizeTool();
                break;
            case "groupElements":
                if (openMaps.get(activeMap).groupElements()) {
                    refreshMap(false);
                }
                break;
            case "ungroupElements":
                if (openMaps.get(activeMap).ungroupElements()) {
                    refreshMap(false);
                }
                break;
            case "editVector":
                editVector();
                break;
            case "digitizeNewFeature":
                digitizeNewFeature();
                break;
            case "deleteFeature":
                deleteFeature();
                break;
            case "deleteLastNodeInFeature":
                deleteLastNodeInFeature();
                break;
            case "newProject":
                mtb.setSelectedIndex(0);
                splitPane3.setResizeWeight(0.65);
                try {
                    ProjectDialog pd = new ProjectDialog(applicationDirectory);
                    project = new Project();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "WhiteboxGuiClone.newProject", ex);
                }
                break;
            case "saveProject":
                wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                wb.getGlassPane().setVisible(true);
                ProjectBuilder pb = new ProjectBuilder(projNameFld.getText(), projLocation.getText(), watershedDirectory);
                wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                wb.getGlassPane().setVisible(true);
                break;
            case "newScenario":
                wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                wb.getGlassPane().setVisible(true);
                ScenarioDialog sd = new ScenarioDialog(graphicsDirectory, spatialDirectory, wbProjectDirectory + "User Scenarios" + pathSep);            
                break;
            case "saveScenario":
                try {
                    currentScenario.saveScenario(damData, pondData, grazeData, tillData, forageData);
                } catch (SQLException ex) {
                    Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "baseScenario":
                try {
                    wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    wb.getGlassPane().setVisible(true);
                    baseHistoric = new ScenarioBuilder("historic", true, true, spatialDirectory, wbProjectDirectory + "Base Scenarios" + pathSep);
                    baseConventional = new ScenarioBuilder("conventional", true, false, spatialDirectory, wbProjectDirectory + "Base Scenarios" + pathSep);
                    wb.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    wb.getGlassPane().setVisible(true);
                    webs.remove(projPanel);
                    webs.add(scenPanel);
                    projSep.setVisible(true);
                    newScen.setVisible(true);
                    editScen.setVisible(true);
                    delScen.setVisible(true);
                    scenSep.setVisible(true);
                    dams.setVisible(true);
                    ponds.setVisible(true);
                    grazing.setVisible(true);
                    tillage.setVisible(true);
                    forage.setVisible(true);
                    btnSaveSc.setEnabled(false);
                    btnSaveAsSc.setEnabled(false);
                    splitPane3.setResizeWeight(0.35);
                    splitPane3.repaint();
                    wb.repaint();
                    wb.validate();
                } catch (SQLException | IOException | ClassNotFoundException ex) {
                    logger.log(Level.SEVERE, "WhiteboxGuiClone.baseScenario", ex);
                }
                break;
            case "damsBmp":
                resultsOn = false;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(0);
                currentShapeFile = spatialDirectory + "small_dam.shp";
                if (!damMap) {
                    newMap("Small Dam BMP");
                    addLayer(spatialDirectory + "boundary.shp");
                    addLayer(spatialDirectory + "stream.shp");
                    addLayer(currentShapeFile);
                    damMap = true;
                    openDam = activeMap;
                    NorthArrow na = new NorthArrow("na_dam");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_dam");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_Dam");
                    leg.setBorderVisible(true);
                    leg.setWidth(100);
                    leg.setHeight(150);
                    leg.setUpperLeftX(50);
                    leg.setUpperLeftY(365);
                    miDam = openMaps.get(openDam);
                    leg.addMapArea(miDam.getActiveMapArea());
                    ms.setMapArea(miDam.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miDam.getActiveMapArea().getActiveLayer();
                } else {
                    miDam = openMaps.get(openDam);
                    activeMap = openDam;
                    ma = miDam.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miDam);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                tableView.removeAll();
                showAttributesFile();
                mapRefreshThing(currentShapeFile);
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case "pondsBmp":
                resultsOn = false;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(0);
                currentShapeFile = spatialDirectory + "cattle_yard.shp";
                if (!pondMap) {
                    newMap("Holding Ponds BMP");
                    addLayer(spatialDirectory + "boundary.shp");
                    addLayer(spatialDirectory + "stream.shp");
                    addLayer(currentShapeFile);
                    pondMap = true;
                    openPond = activeMap;
                    NorthArrow na = new NorthArrow("na_pond");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_pond");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_pond");
                    leg.setBorderVisible(true);
                    leg.setWidth(100);
                    leg.setHeight(150);
                    leg.setUpperLeftX(50);
                    leg.setUpperLeftY(365);
                    miPond = openMaps.get(openPond);
                    leg.addMapArea(miPond.getActiveMapArea());
                    ms.setMapArea(miPond.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miPond.getActiveMapArea().getActiveLayer();
                } else {
                    miPond = openMaps.get(openPond);
                    activeMap = openPond;
                    ma = miPond.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miPond);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                tableView.removeAll();
                showAttributesFile();
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case "grazingBmp":
                resultsOn = false;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(0);
                currentShapeFile = spatialDirectory + "grazing.shp";
                if (!grazeMap) {
                    newMap("Grazing Area BMP");
                    addLayer(spatialDirectory + "boundary.shp");
                    addLayer(spatialDirectory + "stream.shp");
                    addLayer(currentShapeFile);
                    grazeMap = true;
                    openGraze = activeMap;
                    NorthArrow na = new NorthArrow("na_graze");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_graze");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_graze");
                    leg.setBorderVisible(true);
                    leg.setWidth(100);
                    leg.setHeight(150);
                    leg.setUpperLeftX(50);
                    leg.setUpperLeftY(365);
                    miGraze = openMaps.get(openGraze);
                    leg.addMapArea(miGraze.getActiveMapArea());
                    ms.setMapArea(miGraze.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miGraze.getActiveMapArea().getActiveLayer();
                } else {
                    miGraze = openMaps.get(openGraze);
                    activeMap = openGraze;
                    ma = miGraze.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miGraze);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                tableView.removeAll();
                showAttributesFile();
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case "tillageBmp":
                resultsOn = false;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(0);
                currentShapeFile = spatialDirectory + "land2010_by_land_id.shp";
                if (!tillMap) {
                    newMap("Tillage BMP");
                    addLayer(currentShapeFile);
                    tillMap = true;
                    openTill = activeMap;
                    NorthArrow na = new NorthArrow("na_till");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_till");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_till");
                    leg.setBorderVisible(true);
                    leg.setWidth(125);
                    leg.setHeight(150);
                    leg.setUpperLeftX(40);
                    leg.setUpperLeftY(365);
                    miTill = openMaps.get(openTill);
                    leg.addMapArea(miTill.getActiveMapArea());
                    ms.setMapArea(miTill.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miTill.getActiveMapArea().getActiveLayer();
                } else {
                    miTill = openMaps.get(openTill);
                    activeMap = openTill;
                    ma = miTill.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miTill);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                currentScenario.setTillageType(0);
                tableView.removeAll();
                showAttributesFile();
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case "forageBmp":
                resultsOn = false;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(0);
                currentShapeFile = spatialDirectory + "land2010_by_land_id.shp";
                if (!forageMap) {
                    newMap("Forage BMP");
                    addLayer(currentShapeFile);
                    forageMap = true;
                    openForage = activeMap;
                    NorthArrow na = new NorthArrow("na_forage");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_forage");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_forage");
                    leg.setBorderVisible(true);
                    leg.setWidth(125);
                    leg.setHeight(150);
                    leg.setUpperLeftX(40);
                    leg.setUpperLeftY(365);
                    miForage = openMaps.get(openForage);
                    leg.addMapArea(miForage.getActiveMapArea());
                    ms.setMapArea(miForage.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miForage.getActiveMapArea().getActiveLayer();
                } else {
                    miForage = openMaps.get(openForage);
                    activeMap = openForage;
                    ma = miForage.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miForage);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                tableView.removeAll();
                showAttributesFile();
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case("runEconomic"):
                try {
                    currentScenario.runEconomic(damData, pondData, grazeData, tillData, forageData);
                } catch (SQLException ex) {
                    Logger.getLogger(WhiteboxGuiClone.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("runSwat"):
                try {
                    Project project = new Project();
                    String scenarioDB, scenarioPath;
                    if(currentScenario.isBase()) {
                        scenarioDB = wbProjectDirectory + "Base Scenarios" + pathSep + currentScenario.getName();
                        scenarioPath = wbProjectDirectory + "Base Scenarios" + pathSep;
                        project.addScenario(new Scenario("", "", scenarioDB,
                            ScenarioType.Base_Conventional, BMPScenerioBaseType.Conventional, 
                            BMPSelectionLevelType.Field, ""));
                    }
                    else {
                        scenarioDB = wbProjectDirectory + "User Scenarios" + pathSep + currentScenario.getName();
                        scenarioPath = wbProjectDirectory + "User Scenarios" + pathSep;
                        project.addScenario(new Scenario("", "", scenarioDB,
                            ScenarioType.Normal, BMPScenerioBaseType.Conventional, 
                            BMPSelectionLevelType.Field, ""));
                    }
                    File database = new File(scenarioDB);
                    if(!database.exists()) database.createNewFile();
                    //ScenarioEconomicModel Eco = new ScenarioEconomicModel(scenario);
                    System.out.println("--------------Start economic model!-------------");
                    //Eco.RunEconomic();
                    currentScenario.runEconomic(damData, pondData, grazeData, tillData, forageData);
                    System.out.println("----------Economic simulation finished!----------");
                    System.out.println("\n----------------Start SWAT model!----------------");
                    project.GetCurrentScenario().RunSWAT();
                    System.out.println("------------SWAT simulation finished!------------");
                } catch(Exception ex) {
                    logger.log(Level.SEVERE, "WhiteboxGuiClone.newProject", ex);
                }
                break;
            case("results"):
                refreshSplitPaneFour();
                resultsOn = true;
                mtb.setSelectedIndex(1);
                ptb.setSelectedIndex(2);
                resultsShapeFiles[0] = spatialDirectory + "land2010_by_land_id.shp";
                resultsShapeFiles[1] = spatialDirectory + "small_dam.shp";
                resultsShapeFiles[2] = spatialDirectory + "holding_pond.shp";
                resultsShapeFiles[3] = spatialDirectory + "grazing.shp";
                if (!resultsMap) {
                    newMap("Results Map");
                    for (String rsf : resultsShapeFiles) {
                       addLayer(rsf);
                    }
                    resultsMap = true;
                    openResults = activeMap;
                    NorthArrow na = new NorthArrow("na_results");
                    na.setMarkerSize(100);
                    na.setUpperLeftX(675);
                    na.setUpperLeftY(25);
                    MapScale ms = new MapScale("ms_results");
                    ms.setScaleStyle(MapScale.ScaleStyle.STANDARD);
                    ms.setMargin(5);
                    ms.setUpperLeftX(30);
                    ms.setUpperLeftY(525);
                    Legend leg = new Legend("leg_results");
                    leg.setBorderVisible(true);
                    leg.setWidth(125);
                    leg.setHeight(150);
                    leg.setUpperLeftX(40);
                    leg.setUpperLeftY(365);
                    miResults = openMaps.get(openResults);
                    leg.addMapArea(miResults.getActiveMapArea());
                    ms.setMapArea(miResults.getActiveMapArea());
                    openMaps.get(activeMap).addNewCartographicElement(na);
                    openMaps.get(activeMap).addNewCartographicElement(ms);
                    openMaps.get(activeMap).addNewCartographicElement(leg);
                    vli = (VectorLayerInfo) miResults.getActiveMapArea().getActiveLayer();
                } else {
                    miResults = openMaps.get(openResults);
                    activeMap = openResults;
                    ma = miResults.getActiveMapArea();
                    ma.setActiveLayer(ma.getNumLayers() - 1);
                    drawingArea.setMapInfo(miResults);
                    vli = (VectorLayerInfo) ma.getActiveLayer();
                    refreshMap(true);
                }
                tableView.removeAll();
                //showAttributesFile();
                splitPane3.setResizeWeight(0.65);
                splitPane3.repaint();
                wb.repaint();
                wb.validate();
                break;
            case("dontCompare"):
                jcbComp.setEnabled(false);
                rbIntegrate.setEnabled(false);
                try {
                    rdf.SetCompareType(ResultDisplayScenarioCompareType.NoCompare, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("compare"):
                jcbComp.setEnabled(true);
                rbIntegrate.setEnabled(true);
                try {
                    rdf.SetCompareType(ResultDisplayScenarioCompareType.Compare, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("compareTo"):
                try {
                    //rdf.SetCompareScenario(compareScenario, true);
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    rdf.SetResultLevel(ResultDisplayResultLevelType.OnSite, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    rdf.SetResultLevel(ResultDisplayResultLevelType.OffSite, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    rdf.SetBMPType(BMPType.Small_Dam, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("pondsLayerOn"):
                if(cbPonds.isSelected()) {
                    cbGraze.setEnabled(false);
                }
                else {
                    cbGraze.setEnabled(true);
                }
                try {
                    rdf.SetBMPType(BMPType.Holding_Pond, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    rdf.SetBMPType(BMPType.Grazing, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("subbasinLevel"):
                try {
                    rdf.SetBMPType(BMPType.Grazing_Subbasin, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                break;
            case("fieldLevelOn"):
                try {
                    rdf.SetBMPType(BMPType.Tillage_Field, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("farmLevelOn"):
                try {
                    rdf.SetBMPType(BMPType.Tillage_Farm, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("subbasinLevelOn"):
                try {
                    rdf.SetBMPType(BMPType.Tillage_Subbasin, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("onlySwat"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                try {
                    rdf.SetResultType(ResultDisplayResultType.SWAT, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("selectSwatResult"):
                String entSwat = jcbSwat.getSelectedItem().toString();
                try {
                    switch(entSwat) {
                        case("Water Yield"):
                            rdf.SetSWATType(SWATResultColumnType.water, true);
                            refreshSplitPaneFour();
                            break;
                        case("Sediment Yield"):
                            rdf.SetSWATType(SWATResultColumnType.sediment, true);
                            refreshSplitPaneFour();
                            break;
                        case("Particulate Phosphorus"):
                            rdf.SetSWATType(SWATResultColumnType.PP, true);
                            refreshSplitPaneFour();
                            break;
                        case("Dissolved Phosphorus"):
                            rdf.SetSWATType(SWATResultColumnType.DP, true);
                            refreshSplitPaneFour();
                            break;
                        case("Total Phosphorus"):
                            rdf.SetSWATType(SWATResultColumnType.TP, true);
                            refreshSplitPaneFour();
                            break;
                        case("Particulate Nitrogen"):
                            rdf.SetSWATType(SWATResultColumnType.PN, true);
                            refreshSplitPaneFour();
                            break;
                        case("Dissolved Nitrogen"):
                            rdf.SetSWATType(SWATResultColumnType.DN, true);
                            refreshSplitPaneFour();
                            break;
                        case("Total Nitrogen"):
                            rdf.SetSWATType(SWATResultColumnType.TN, true);
                            refreshSplitPaneFour();
                            break;                      
                        default:
                            break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("onlyEconomic"):
                jcbSwat.setEnabled(false);
                // jcbEcon.setEnabled(true); // H.Shao
                if(rbOffSite.isSelected()){
                    jcbEcon.setEnabled(true);
                    jcbTillFor.setSelectedIndex(2);
                    jcbTillFor.setEnabled(false);
                } else {
                    jcbEcon.setEnabled(false);
                    jcbTillFor.setSelectedIndex(2);
                    jcbTillFor.setEnabled(true);
                }
                
                try {
                    rdf.SetResultType(ResultDisplayResultType.Economic, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("selectEconResult"):
                String entEcon = jcbEcon.getSelectedItem().toString();
                try {
                    switch(entEcon) {
                    case("Small Dam"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        rdf.SetBMPType(BMPType.Small_Dam,true);
                        refreshSplitPaneFour();
                        break;
                    case("Holding Ponds"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        rdf.SetBMPType(BMPType.Holding_Pond,true);
                        refreshSplitPaneFour();
                        break;
                    case("Grazing Area"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        rdf.SetBMPType(BMPType.Grazing,true);
                        refreshSplitPaneFour();
                        break;
                    case("Tillage"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        if (tfRbField.isSelected()){
                            rdf.SetBMPType(BMPType.Tillage_Field,true);
                            refreshSplitPaneFour();
                        } else if  (tfRbFarm.isSelected()) {
                            rdf.SetBMPType(BMPType.Tillage_Farm,true);
                            refreshSplitPaneFour();
                        } else {
                            rdf.SetBMPType(BMPType.Tillage_Subbasin,true);
                            refreshSplitPaneFour();
                        }
                        break;
                    case("Forage"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, false);
                        if (tfRbField.isSelected()){
                            rdf.SetBMPType(BMPType.Forage_Field,true);
                            refreshSplitPaneFour();
                        } else if  (tfRbFarm.isSelected()) {
                            rdf.SetBMPType(BMPType.Forage_Farm,true);
                            refreshSplitPaneFour();
                        } else {
                            rdf.SetBMPType(BMPType.Forage_Subbasin,true);
                            refreshSplitPaneFour();
                        }
                        break;
                    default:
                        break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("selectTillForageResult"):
                String entTF = jcbTillFor.getSelectedItem().toString();
                try {
                    switch(entTF) {
                    case("Yield"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Yield, true);
                        refreshSplitPaneFour();
                        break;
                    case("Revenue"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Revenue, true);
                        refreshSplitPaneFour();
                        break;
                    case("Crop Cost"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost, true);
                        refreshSplitPaneFour();
                        break;
                    case("Crop Return"):
                        rdf.SetEconomicType(ResultDisplayTillageForageEconomicResultType.net_return, true);
                        refreshSplitPaneFour();
                        break;
                    default:
                        break;
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }                
                break;
            case("integrated"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                try {
                    rdf.SetResultType(ResultDisplayResultType.Integrated, true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("startYear"):
                if(startSlide.getValue() > endSlide.getValue()) {
                    startSlide.setValue(endSlide.getValue());
                    //startSlide.updateUI();
                }
                try {
                    rdf.SetStartYear(startSlide.getValue(), true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case("endYear"):
                if(endSlide.getValue() < startSlide.getValue()) {
                    endSlide.setValue(startSlide.getValue());
                    //endSlide.updateUI();
                }
                try {
                    rdf.SetStartYear(endSlide.getValue(), true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            default:
                break;
        }
        selectedMapAndLayer[0] = -1;
        selectedMapAndLayer[1] = -1;
        selectedMapAndLayer[2] = -1;
    }

    private void close() {
        setApplicationProperties();

        for (Handler h : logger.getHandlers()) {
            h.close();   //must call h.close or a .LCK file will remain.
        }

        dispose();
        System.exit(0);
    }

    private MapRenderingTool.ScrollZoomDirection scrollZoomDir = MapRenderingTool.ScrollZoomDirection.NORMAL;

    public void setScrollZoomDirection(MapRenderingTool.ScrollZoomDirection direction) {
        this.scrollZoomDir = direction;
        drawingArea.setScrollZoomDirection(direction);
    }

    public MapRenderingTool.ScrollZoomDirection getScrollZoomDirection() {
        return this.scrollZoomDir;
    }

    @Override
    public void notifyOfThreadComplete(Runnable thread) {
        System.out.println("Thread " + thread.toString() + " complete");
    }

    @Override
    public void notifyOfProgress(int value) {
    }

    @Override
    public void passOnThreadException(Exception e) {
    }

    @Override
    public void notifyOfReturn(String ret) {
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
                    rdf.SetStartYear(slider.getValue(),true);
                    refreshSplitPaneFour();
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
                    rdf.SetEndYear(slider.getValue(),true);
                    refreshSplitPaneFour();
                } catch (Exception ex) {
                    Logger.getLogger(ResultDisplayFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
