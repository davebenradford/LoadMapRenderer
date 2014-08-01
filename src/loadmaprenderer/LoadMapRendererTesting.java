/*
 * Copyright (C) 2011-2013 Dr. John Lindsay <jlindsay@uoguelph.ca>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package loadmaprenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import whitebox.cartographic.MapArea;
import whitebox.cartographic.MapInfo;
import whitebox.geospatialfiles.VectorLayerInfo;
import whitebox.interfaces.MapLayer;
import whitebox.interfaces.WhiteboxPlugin;
import whitebox.interfaces.WhiteboxPluginHost;
import whitebox.utilities.FileUtilities;
import whiteboxgis.LegendEntryPanel;
import whiteboxgis.PluginInfo;
import whiteboxgis.PluginService;
import whiteboxgis.user_interfaces.FeatureSelectionPanel;
import whiteboxgis.user_interfaces.IconTreeNode;
import whiteboxgis.user_interfaces.Scripter;
import whiteboxgis.user_interfaces.TreeNodeRenderer;

/**
 *
 * @author radfordd
 */

public class LoadMapRendererTesting extends JFrame implements whitebox.interfaces.WhiteboxPluginHost {
    private static final Font f = new Font("Sans_Serif", Font.PLAIN, 12);
    private static ArrayList<MapInfo> openMaps = new ArrayList<>();
    private static ArrayList<PluginInfo> plugInfo = null;
    private static MapRenderingTool drawingArea = new MapRenderingTool();
    private static String scriptingLanguage = "python";
    private static int activeMap = 0;
    private static int tbTabsIndex = 0;
    private static int qlTabsIndex = 0;
    private static int splitterLoc1 = 250;
    private static int[] selectedMapAndLayer = new int[3];    
    private static boolean requestForOperationCancel;
    private static boolean linkAllOpenMaps = false;
    private static JPanel layersPanel;
    private static JButton deleteFeatureButton;
    private static JButton deleteLastNodeInFeatureButton;
    private static JToggleButton editVectorButton;
    private static JToggleButton digitizeNewFeatureButton;
    private static JCheckBoxMenuItem editVectorMenuItem;
    private static JCheckBoxMenuItem digitizeNewFeatureMenuItem;
    private static JMenuItem deleteFeatureMenuItem;
    private static JMenuItem deleteLastNodeInFeatureMenuItem;
    private static JTextField searchText = new JTextField();
    private static JTabbedPane tb;
    private static JTabbedPane qlTabs;
    private static JTabbedPane tabs;
    private static JSplitPane splitPane;
    private static JSplitPane splitPane2;
    private static JTree tree;
    private static JList allTools;
    private static JList recentTools;
    private static JList mostUsedTools;
    private static ScriptEngine engine;
    private static PluginService pluginService;
    private static FeatureSelectionPanel featuresPanel;
    private static ArrayList<LegendEntryPanel> legendEntries = new ArrayList<>();
    private static JScrollPane scrollView = new JScrollPane();
    private static LoadMapRendererTesting lmrt;
    
    private static void getLegendEntries() {
        legendEntries.clear();
        // add the map nodes
        int i = 0;
        for (MapInfo mi : openMaps) {
            LegendEntryPanel legendMapEntry;
            if (i == activeMap) {
                legendMapEntry = new LegendEntryPanel(mi.getMapName(),
                        lmrt, f, i, -1, -1, (i == selectedMapAndLayer[0]));
            } else {
                legendMapEntry = new LegendEntryPanel(mi.getMapName(),
                        lmrt, f, i, -1, -1, (i == selectedMapAndLayer[0]));
            }
            legendEntries.add(legendMapEntry);

            for (MapArea mapArea : mi.getMapAreas()) {
                LegendEntryPanel legendMapAreaEntry;
//                legendMapAreaEntry = new LegendEntryPanel(mapArea, 
//                    this, fonts.get("inactiveMap"), i, mapArea.getElementNumber(), 
//                        -1, (mapArea.getElementNumber() == selectedMapAndLayer[2]));
                legendMapAreaEntry = new LegendEntryPanel(mapArea,
                        lmrt, f, i, mapArea.getElementNumber(),
                        -1, (mapArea.getElementNumber() == selectedMapAndLayer[2]
                        & selectedMapAndLayer[1] == -1));
                legendEntries.add(legendMapAreaEntry);

                for (int j = mapArea.getNumLayers() - 1; j >= 0; j--) {
                    // add them to the tree in the order of their overlayNumber
                    MapLayer layer = mapArea.getLayer(j);
                    LegendEntryPanel legendLayer;
                    if (j == mapArea.getActiveLayerOverlayNumber()) {
                        legendLayer = new LegendEntryPanel(layer, lmrt, f,
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
                        legendLayer = new LegendEntryPanel(layer, lmrt, f,
                                i, mapArea.getElementNumber(), j, (j == selectedMapAndLayer[1]));
                    }
                    legendEntries.add(legendLayer);
                }
            }
            i++;

        }

    }
    
    private static void updateLayersTab() {
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
            for (LegendEntryPanel lep : legendEntries) {
                if (lep.getLegendEntryType() == 0) { // it's a map
                    if (lep.getMapNum() == activeMap) {
                        lep.setTitleFont(f);
                    } else {
                        lep.setTitleFont(f);

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
                        lep.setTitleFont(f);
                    } else {
                        lep.setTitleFont(f);
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
                                lep.setTitleFont(f);
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
                                lep.setTitleFont(f);
                            }
                        } else {
                            lep.setTitleFont(f);
                        }
                    } else {
                        lep.setTitleFont(f);
                    }
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
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private static void searchForWords() {
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
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private static ActionListener searchFieldListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            searchForWords();
        }
    };
    
    private static void executeScriptFile(String scriptFile, String[] args, boolean runOnDedicatedThread) {
        try {
            if (scriptFile == null) {
                return;
            }

            String myScriptingLanguage;
            if (scriptFile.toLowerCase().endsWith(".py")) {
                myScriptingLanguage = "python";
            } else if (scriptFile.toLowerCase().endsWith(".groovy")) {
                myScriptingLanguage = "groovy";
            } else if (scriptFile.toLowerCase().endsWith(".js")) {
                myScriptingLanguage = "javascript";
            } else {
                Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, new Exception());
                return;
            }

            if (engine == null || !myScriptingLanguage.equals(scriptingLanguage)) {
                scriptingLanguage = myScriptingLanguage;
                ScriptEngineManager mgr = new ScriptEngineManager();
                engine = mgr.getEngineByName(scriptingLanguage);
            }

            if (scriptingLanguage.equals("python")) {
                engine.put("__file__", scriptFile);
            }
            requestForOperationCancel = false;
            engine.put("pluginHost", (WhiteboxPluginHost) lmrt);
            engine.put("args", args);

            final String scriptContents = new String(Files.readAllBytes(Paths.get(scriptFile)));

            if (runOnDedicatedThread) {
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            engine.eval(scriptContents);
                        } catch (ScriptException e) {
                            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                };
                final Thread t = new Thread(r);
                t.start();
            } else {
                engine.eval(scriptContents);
            }
        } catch (IOException | ScriptException e) {
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private static void editScript(String scriptName) {
        Scripter scripter = new Scripter(lmrt, false, scriptName);
        scripter.openFile(scriptName);
        scripter.setVisible(true);
    }    
    
    private static void populateToolTabs() {
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

    private static void showToolDescription(String pluginName) {
        for (PluginInfo pi : plugInfo) {
            if (pi.getDescriptiveName().equals(pluginName)) {
                break;
            }
        }
    }    
    
    private static String getScriptFile(String pluginName) {
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
    
    private static boolean isToolAScript(String pluginName) {
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
    
    public static class SortIgnoreCase implements Comparator<Object> {
        @Override
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
    
    private static ArrayList<String> findToolsInToolbox(String toolbox) {
        Iterator<WhiteboxPlugin> iterator = pluginService.getPlugins();
        ArrayList<String> plugs = new ArrayList<>();
        String plugName;
        String plugDescriptiveName;

        for (PluginInfo pi : plugInfo) {
            String[] tbox = pi.getToolboxes();
            for (int i = 0; i < tbox.length; i++) {
                if (tbox[i].equals(toolbox)) {
                    plugName = pi.getName();
                    if(ResourceBundle.getBundle(plugName).containsKey(plugName)) {
                        plugDescriptiveName = plugName;
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
    
    private static DefaultMutableTreeNode populateTree(Node n) {
        ArrayList<String> t;
        Element e = (Element) n;
        String label = e.getAttribute("label");
        String toolboxName = e.getAttribute("name");
        IconTreeNode result;
        if (ResourceBundle.getBundle(toolboxName).containsKey(toolboxName)) {
            result = new IconTreeNode(toolboxName);
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
                if (ResourceBundle.getBundle("whiteboxgis.i18n.GuiLabelsBundle").equals(toolboxName)) {
                    childTreeNode = new IconTreeNode(toolboxName);
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
        return result;

    }
    
    private static JSplitPane getToolbox() {
        try {
            File file = new File("resources\\toolbox.xml");

            if (!file.exists()) {
                return null;
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            Node topNode = doc.getFirstChild();
            DefaultMutableTreeNode result = populateTree(topNode);

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

            tree = new JTree(result);

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
                                        JMenuItem mi = new JMenuItem("EditScript");
                                        mi.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                editScript(scriptName);
                                            }
                                        });
                                        pm.add(mi);
                                        
                                        mi = new JMenuItem("UpdateScript");
                                        mi.addActionListener(new ActionListener() {
                                            @Override
                                            public void actionPerformed(ActionEvent e) {
                                                String[] args = new String[1];
                                                args[0] = FileUtilities.getShortFileName(scriptName);
                                                //String updateScript = resourcesDirectory + "plugins" + pathSep + "Scripts" + pathSep + "UpdateScriptFile.groovy";
                                                //executeScriptFile(updateScript, args, false);
                                            }
                                        });
                                        pm.add(mi);
                                        
                                        pm.show((JComponent) e.getSource(), e.getX(), e.getY());

                                    }
                                } else {
                                    showToolDescription(label);
                                }
                            } else if (n.toString().equals("topmost")) {
                            }
                        } else if (e.getClickCount() == 2) {
                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                            if (n.getChildCount() == 0) {
                                label = selPath.getLastPathComponent().toString();
                                //launchDialog(label);
                            }
                        }
                    }
                }
            };
            tree.addMouseListener(ml);
/**
            HashMap icons = new HashMap();
            icons.put("toolbox", new ImageIcon(graphicsDirectory + "opentools.png", ""));
            icons.put("tool", new ImageIcon(graphicsDirectory + "tool.png", ""));
            icons.put("script", new ImageIcon(graphicsDirectory + "ScriptIcon2.png", ""));
*/
            //tree.putClientProperty("JTree.icons", icons);
            tree.setCellRenderer(new TreeNodeRenderer());

            JScrollPane treeView = new JScrollPane(tree);

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
                        //launchDialog(label);

                    }

                }
            };

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
            box.add(new JLabel("Search:"));
            box.add(Box.createHorizontalStrut(3));
            searchText.setMaximumSize(new Dimension(275, 22));
            box.setMaximumSize(new Dimension(275, 24));
            searchText.addActionListener(searchFieldListener);
            box.add(searchText);
            allToolsPanel.add(box);
            allToolsPanel.add(scroller1);

            qlTabs.insertTab("All", null, allToolsPanel, "", 0); // + plugInfo.size() + " tools", null, scroller1, "", 2);
            qlTabs.insertTab("Most_Used", null, scroller3, "", 1);
            qlTabs.insertTab("Recent", null, scroller2, "", 2);

            qlTabs.setSelectedIndex(qlTabsIndex);

            splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeView, qlTabs);
            splitPane2.setResizeWeight(1);
            splitPane2.setOneTouchExpandable(true);

            return splitPane2;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    private static JTabbedPane createTabbedPane() {
        try {
            JSplitPane wbTools = getToolbox();
            tabs.insertTab("Tools", null, wbTools, "", 0);
            layersPanel = new JPanel(new BorderLayout());
            layersPanel.setBackground(Color.white);
            updateLayersTab();
            tabs.insertTab("Layers", null, layersPanel, "", 1);
            featuresPanel = new FeatureSelectionPanel(ResourceBundle.getBundle("whiteboxgis.i18n.GuiLabelsBundle"), lmrt);
            tabs.insertTab("Features", null, featuresPanel, "", 2);

            return tabs;
        } catch (Exception e) {
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }

    }
    
    public static void main(String[] args) {
        lmrt = new LoadMapRendererTesting();
        lmrt.setVisible(true);
    }
    
    public LoadMapRendererTesting() {
        try {
            MapInfo mapinfo = new MapInfo("small_dam");
            mapinfo.setMapName("small_dam");
            mapinfo.setDefaultFont(f);
            //mapinfo.setMargin(defaultMapMargin);

            MapArea ma = new MapArea("small_dam.shp");
            ma.setUpperLeftX(-32768);
            ma.setUpperLeftY(-32768);
            ma.setLabelFont(f);
            mapinfo.addNewCartographicElement(ma);

            openMaps.add(mapinfo);
            activeMap = 0;
            drawingArea.setMapInfo(mapinfo);
            drawingArea.setHost(this);

            
            tb = createTabbedPane();
            tb.setMaximumSize(new Dimension(150, 50));
            tb.setPreferredSize(new Dimension(splitterLoc1, 50));
            tb.setSelectedIndex(tbTabsIndex);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tb, drawingArea); //splitPane3);
            splitPane.setResizeWeight(0);
            splitPane.setOneTouchExpandable(false);
            splitPane.setDividerSize(3);
            this.getContentPane().add(splitPane);

            this.setMinimumSize(new Dimension(700, 500));
            this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

            // set the message indicating the number of plugins that were located.
            //status.setMessage(" " + plugInfo.size() + " plugins were located");

            splitPane2.setDividerLocation(0.75); //splitterToolboxLoc);

            pack();
        } catch (SecurityException | IllegalArgumentException e) {
            Logger.getLogger(LoadMapRendererTesting.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void editVector() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLanguageCountryCode() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setLanguageCountryCode(String code) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List returnPluginList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelOperation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void launchDialog(String pluginName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void returnData(Object ret) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void pluginComplete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProgress(String progressLabel, int progress) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateProgress(int progress) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refreshMap(boolean updateLayersTab) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteFeature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteLastNodeInFeature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Font getDefaultFont() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRequestForOperationCancelSet() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void resetRequestForOperationCancel() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showHelp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void showHelp(String helpFile) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setSelectFeature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deselectAllFeaturesInActiveLayer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MapLayer getActiveMapLayer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<MapLayer> getAllMapLayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomToFullExtent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomIn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomOut() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomToLayer() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomToPage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void zoomToSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getWorkingDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setWorkingDirectory(String workingDirectory) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getApplicationDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setApplicationDirectory(String applicationDirectory) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getResourcesDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getLogDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getHelpDirectory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int showFeedback(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int showFeedback(String message, int optionType, int messageType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResourceBundle getGuiLabelsBundle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ResourceBundle getMessageBundle() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logException(String message, Exception e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logThrowable(String message, Throwable t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void logMessage(Level level, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getCurrentlyDisplayedFiles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void runPlugin(String pluginName, String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void runPlugin(String pluginName, String[] args, boolean runOnDedicatedThread) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void runPlugin(String pluginName, String[] args, boolean runOnDedicatedThread, boolean suppressReturnedData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
