package loadmaprenderer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.*;

/**
 *
 * @author Dave Radford
 * @since May 2014
 * @version 0.09
 * 
 * WEBs Interface (Java): New Project Creation Dialog Box
 * 
 * This dialog box appears when the user decides to create a new project with
 * the WEBs interface.
 * 
 * Project Version History
 * 
 * v0.05: Built the New Project Dialog Box.
 * v0.06: Condensed the GridBagLayout weight setting values
 *        into one method for scalability.
 * v0.07: Cleaned up code for scalability and efficiency. Began implementing
 *        listener classes for the dialog menu.
 * v0.08: Dialog listeners mostly completed. Awaiting completion of Scenario
 *        creation class before finalizing listeners. Cleaning code in terms of
 *        function placement, component names, and overall comments.
 * v0.09: Build Scenario creation class.
 */

public class ProjectDialog {
    
    //Swing Component Declarations
    
    private final JFrame frame;
    private final JTextField nameFld;
    private final JTextField projFld;
    private final JTextField wsFld;
    private final JTextField spatFld;
    private final JTextField swatFld;
    private final JLabel nameLbl;
    private final JLabel projLbl;
    private final JLabel wsLbl;
    private final JLabel spatLbl;
    private final JLabel swatLbl;
    private final JButton confirm;
    private final JButton cancel;
    private final JButton projLocate;
    private final JButton wsLocate;
    private final JButton spatLocate;
    private final JButton swatLocate;
    
    private final Font f = new Font("Sans_Serif", Font.BOLD, 12);
    private final ImageIcon websIcon = new ImageIcon("build\\classes\\loadmaprenderer\\resources\\Images\\icon_32x32.png");
    private File dataFile;
    private final String projSource;
    private String stcSource;
    private String applicationDirectory;
    private final String sep = File.separator;
    
    public ProjectDialog() throws IOException {
        // Initialize the Dialog Box
        frame = new JFrame("Create a New WEBs Project");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(768, 200);
        frame.setResizable(false);
        frame.setIconImage(websIcon.getImage());
        frame.setLocation(frame.getWidth() / 4, frame.getHeight());
        
        applicationDirectory = java.net.URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        applicationDirectory += getClass().getName().replace('.', File.separatorChar);
        applicationDirectory = new File(applicationDirectory).getParent() + sep;
        projSource = applicationDirectory + "Projects";
        stcSource = applicationDirectory + "resources" + sep + "STC";
        
        // WEBs Project Components
        nameLbl = createLabel(new JLabel("Project Name: ", SwingConstants.RIGHT));
        GridBagConstraints gbc = setGbc(new Insets(8, 8, 8, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.4, 1.0);
        frame.add(nameLbl, gbc);
        
        nameFld = createField(new JTextField("New Project", 40), "Project Name");
        gbc = setGbc(new Insets(8, 8, 8, -24), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 0, 1, 1, 0.6, 1.0);
        frame.add(nameFld, gbc);
        
        // Watershed Location Components
        projLbl = createLabel(new JLabel("Project Folder:", SwingConstants.RIGHT));
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 0.3, 1.0);
        frame.add(projLbl, gbc);
        
        projFld = createField(new JTextField(projSource, 40), "Project File Location");
        gbc = setGbc(new Insets(8, 8, 8, -24), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 1, 1, 1, 0.7, 1.0);
        frame.add(projFld, gbc);
        
        projLocate = createButton(new JButton("..."), "Change Project Location", new directoryListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_END, 2, 1, 1, 1, 0.05, 1.0);
        frame.add(projLocate, gbc);
        
        // Watershed Location Components
        wsLbl = createLabel(new JLabel("Watershed Folder:", SwingConstants.RIGHT));
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 2, 1, 1, 0.3, 1.0);
        frame.add(wsLbl, gbc);
        
        wsFld = createField(new JTextField(stcSource, 40), "Watershed Project Name");
        gbc = setGbc(new Insets(8, 8, 8, -24), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 2, 1, 1, 1.0, 1.0);
        frame.add(wsFld, gbc);
        
        wsLocate = createButton(new JButton("..."), "Change Project Location", new directoryListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_END, 2, 2, 1, 1, 0.05, 1.0);
        frame.add(wsLocate, gbc);
        
        // Spatial Data Locaton Components
        spatLbl = createLabel(new JLabel("Spatial Folder:", SwingConstants.RIGHT));
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 3, 1, 1, 0.3, 1.0);
        frame.add(spatLbl, gbc);
        
        spatFld = createField(new JTextField(stcSource + sep + "Data" + sep + "Spatial", 40), "Spatial Data Location");
        gbc = setGbc(new Insets(8, 8, 8, -24), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 3, 1, 1, 0.7, 1.0);
        frame.add(spatFld, gbc);
        
        spatLocate = createButton(new JButton("..."), "Change Spatial Data Location", new directoryListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_END, 2, 3, 1, 1, 0.05, 1.0);
        frame.add(spatLocate, gbc);
        
        // Swat Input File Location
        swatLbl = createLabel(new JLabel("SWAT Input:", SwingConstants.RIGHT));
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 4, 1, 1, 0.3, 1.0);
        frame.add(swatLbl, gbc);
        
        swatFld = createField(new JTextField(stcSource + sep + "Data" + sep + "txtinout", 40), "SWAT Data Location");
        gbc = setGbc(new Insets(8, 8, 8, -24), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 4, 1, 1, 0.7, 1.0);
        frame.add(swatFld, gbc);
        
        swatLocate = createButton(new JButton("..."), "Change SWAT Data Location", new directoryListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.FIRST_LINE_END, 2, 4, 1, 1, 0.05, 1.0);
        frame.add(swatLocate, gbc);
        
        // Confirm and Cancel Buttons
        confirm = createButton(new JButton("OK"), "false", new confirmListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST, 1, 5, 1, 1, 0.85, 1.0);
        frame.add(confirm, gbc);
        
        cancel = createButton(new JButton("Cancel"), "false", new cancelListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST, 2, 5, 1, 1, 0.05, 1.0);
        frame.add(cancel, gbc);
        
        frame.pack();
        frame.validate();
        frame.setVisible(true);
    }
    
    /**
     * 
     * @param lbl: The JLabel component being constructed.
     * @return: Returns the JLabel to be added to the Container for which it was created.
     */
    
    private JLabel createLabel(JLabel lbl) {
        lbl.setFont(f);
        return lbl;
    }
    
    /**
     * 
     * @param txt: The JTextField component being constructed.
     * @param s: The String for the JTextField's ToolTip Text.
     * @return: Returns the JTextField to be added to the Container for which it was created.
     */
    
    private JTextField createField(JTextField txt, String s) {
        txt.setFont(f);
        txt.setToolTipText(s);
        return txt;
    }
    
    /**
     * 
     * @param btn: The JButton component being constructed.
     * @param s: The String for the JButton's ToolTip Text.
     * @param a: The ActionListener used by the JButton.
     * @return: Returns the JButton to be added to the Container for which it was created.
     */
    
    private JButton createButton(JButton btn, String s, ActionListener a) {
        btn.setFont(f);
        if(!(s.equals("false"))) {
            btn.setToolTipText(s);
        }
        btn.addActionListener(a);
        return btn;
    }
    
    /**
     * 
     * @param i: Insets for the panel. Insets object.
     * @param fill: The fill property value for the component. Integer.
     * @param a: The anchor property value for the component. Integer.
     * @param xCoord: The X-Coordinate value for the component on the Frame grid. Integer
     * @param yCoord: The Y-Coordinate value for the component on the Frame grid. Integer
     * @param wide: The width value for the component across the layout grid. Integer.
     * @param high: The height value for the component across the layout grid. Integer.
     * @param weighX: The weight value for the width of the component when placed in the container. Integer.
     * @param weighY: The weight value for the height of the component when placed in the container. Integer.
     * @return 
     */
    
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
    
    // File Chooser
    
    private void getDirectory() {
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setSelectedFile(dataFile);
        selector.showOpenDialog(null);
        File openFile = selector.getSelectedFile();

        stcSource = openFile.toString();
    }
    
    // Listeners
    
    private class directoryListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae){
            if(ae.getSource().equals(projLocate)) {
                dataFile = new File(applicationDirectory + "Projects");
            }
            else {
                dataFile = new File(applicationDirectory + "resources" + sep + "STC");
            }
            getDirectory();
            if(ae.getSource().equals(wsLocate)) {
                wsFld.setText(stcSource);
                spatFld.setText(stcSource + sep + "Data" + sep + "Spatial");
                swatFld.setText(stcSource + sep + "Data" + sep + "txtinout");
            }
            else if(ae.getSource().equals(spatLocate)) {
                spatFld.setText(stcSource);
            }
            else if(ae.getSource().equals(swatLocate)) {
                swatFld.setText(stcSource);
            }
            else if(ae.getSource().equals(projLocate)) {
                projFld.setText(stcSource);
            }
        }
    }
    
    private class confirmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            ProjectBuilder pb = new ProjectBuilder(nameFld.getText(), projFld.getText(), stcSource);
            WhiteboxGuiClone.wb.saveProj.setEnabled(true);
            WhiteboxGuiClone.wb.projNameFld.setText(nameFld.getText());
            WhiteboxGuiClone.wb.projLocation.setText(projFld.getText());
            WhiteboxGuiClone.wb.spatLocation.setText(spatFld.getText());
            WhiteboxGuiClone.wb.swatLocation.setText(swatFld.getText());
            WhiteboxGuiClone.wb.webs.add(WhiteboxGuiClone.wb.projPanel);
            WhiteboxGuiClone.wb.validate();
            WhiteboxGuiClone.wb.repaint();
            frame.dispose();
        }
    }
    
    private class cancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            frame.dispose();
        }
    }
}
