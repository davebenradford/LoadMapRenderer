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
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Shao
 */
public class ResultDisplayControl extends JPanel implements ActionListener { 
    private static final Font f = new Font("Sans_Serif", Font.PLAIN, 12);
    
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
        JRadioButton rbOnSite = new JRadioButton("On-Site", true);
        JRadioButton rbOffSite = new JRadioButton("Off-Site (Outlet)", false);

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
                //SetCompareType(ResultDisplayScenarioCompareType.NoCompare);
                break;
            case("compare"):
                jcbComp.setEnabled(true);
                rbIntegrate.setEnabled(true);
                //SetCompareType(ResultDisplayScenarioCompareType.Compare);
                break;
            case("compareTo"):
                //SetCompareScenario(compareScenario);
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
                //SetResultLevel(ResultDisplayResultLevelType.OnSite);
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
                //SetResultLevel(ResultDisplayResultLevelType.OffSite);
                break;
            case("damsLayerOn"):
                if(cbDams.isSelected()) {
                    cbPonds.setEnabled(false);
                    cbGraze.setEnabled(false);
                }
                else {
                    cbPonds.setEnabled(true);
                }
                //SetBMPType(BMPType.Small_Dam);
                break;
            case("pondsLayerOn"):
                if(cbPonds.isSelected()) {
                    cbGraze.setEnabled(false);
                }
                else {
                    cbGraze.setEnabled(true);
                }
                //SetBMPType(BMPType.Holding_Pond);
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
                //SetBMPType(BMPType.Grazing);
                break;
            case("subbasinLevel"):
                //SetBMPType(BMPType.Grazing_Subbasin);
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
                //SetBMPType(BMPType.Tillage_Field);
                break;
            case("farmLevelOn"):
                //SetBMPType(BMPType.Tillage_Farm);
                break;
            case("subbasinLevelOn"):
                //SetBMPType(BMPType.Tillage_Subbasin);
                break;
            case("onlySwat"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                //SetResultType(ResultDisplayResultType.SWAT);
                break;
            case("selectSwatResult"):
                String entSwat = jcbSwat.getSelectedItem().toString();
                switch(entSwat) {
                    case("Water Yield"):
                        //SetSWATType(SWATResultColumnType.water);
                        break;
                    case("Sediment Yield"):
                        //SetSWATType(SWATResultColumnType.sediment);
                        break;
                    case("Particulate Phosphorus"):
                        //SetSWATType(SWATResultColumnType.PP);
                        break;
                    case("Dissolved Phosphorus"):
                        //SetSWATType(SWATResultColumnType.DP);
                        break;
                    case("Total Phosphorus"):
                        //SetSWATType(SWATResultColumnType.TP);
                        break;
                    case("Particulate Nitrogen"):
                        //SetSWATType(SWATResultColumnType.PN);
                        break;
                    case("Dissolved Nitrogen"):
                        //SetSWATType(SWATResultColumnType.DN);
                        break;
                    case("Total Nitrogen"):
                        //SetSWATType(SWATResultColumnType.TN);
                        break;                      
                    default:
                        break;
                }
                break;
            case("onlyEconomic"):
                jcbSwat.setEnabled(false);
                jcbEcon.setEnabled(true);
                jcbTillFor.setEnabled(true);
                //SetResultType(ResultDisplayResultType.Economic);
                break;
            case("selectEconResult"):
                String entEcon = jcbEcon.getSelectedItem().toString();
                switch(entEcon) {
                    case("Small Dam"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Yield);
                        break;
                    case("Holding Pond"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Revenue);
                        break;
                    case("Grazing Area"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost);
                        break;
                    case("Tillage"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.NetReturn);
                        break;
                    case("Forage"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.NetReturn);
                        break;
                    default:
                        break;
                }
                break;
            case("selectTillForageResult"):
                String entTF = jcbTillFor.getSelectedItem().toString();
                switch(entTF) {
                    case("Yield"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Yield);
                        break;
                    case("Revenue"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Revenue);
                        break;
                    case("Crop Cost"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.Cost);
                        break;
                    case("Crop Return"):
                        //SetEconomicType(ResultDisplayTillageForageEconomicResultType.NetReturn);
                        break;
                    default:
                        break;
                }
                break;
            case("integrated"):
                jcbSwat.setEnabled(true);
                jcbEcon.setEnabled(false);
                jcbTillFor.setEnabled(false);
                //SetResultType(ResultDisplayResultType.Integrated);
                break;
            case("startYear"):
                if(startSlide.getValue() > endSlide.getValue()) {
                    startSlide.setValue(endSlide.getValue());
                    startSlide.updateUI();
                }
                //SetStartYear(startSlide.getValue());
                break;
            case("endYear"):
                if(endSlide.getValue() < startSlide.getValue()) {
                    endSlide.setValue(startSlide.getValue());
                    endSlide.updateUI();
                }
                //SetStartYear(endSlide.getValue();
                break;
            default:
                break;
        }
    }
}
