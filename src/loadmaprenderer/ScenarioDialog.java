package loadmaprenderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author radfordd
 */

public class ScenarioDialog {
    private final JFrame frame;
    private final JPanel panel;
    private final JTextField nameFld;
    private final JTextArea descArea;
    private final JLabel nameLbl;
    private final JLabel descLbl;
    private final JLabel noteLbl;
    private final JRadioButton rbHist;
    private final JRadioButton rbConv;
    private final JButton confirm;
    private final JButton cancel;
    
    private final Font f = new Font("Sans_Serif", Font.BOLD, 12);
    private final ImageIcon websIcon = new ImageIcon("build\\classes\\loadmaprenderer\\resources\\Images\\icon_32x32.png");
    
    public ScenarioDialog() {    
        frame = new JFrame("Create a New Scenario");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setIconImage(websIcon.getImage());
        frame.setLocation(frame.getWidth() / 4, frame.getHeight());
        
        nameLbl = createLabel(new JLabel("Scenario Name", SwingConstants.RIGHT));
        GridBagConstraints gbc = setGbc(new Insets(4, 36, 4, 4), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.0, 1.0);
        frame.add(nameLbl, gbc);
        
        nameFld = createField(new JTextField("New Scenario"), "Scenario Name");
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 0, 2, 1, 1.0, 1.0);
        frame.add(nameFld, gbc);
        
        descLbl = createLabel(new JLabel("Scenario Description", SwingConstants.RIGHT));
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 0.0, 1.0);
        frame.add(descLbl, gbc);
        
        descArea = new JTextArea(3, 20);
        descArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 1, 2, 1, 1.0, 1.0);
        frame.add(descArea, gbc);
        
        panel = createPanel(new JPanel(), "Scenario Type", true);
        rbHist = new JRadioButton("Historic");
        rbConv = new JRadioButton("Conventional");
        
        ButtonGroup group = new ButtonGroup();
        group.add(rbHist);
        group.add(rbConv);
        
        gbc = setGbc(new Insets(8, 8, 8, 4), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.0, 1.0);
        panel.add(rbHist, gbc);
        gbc = setGbc(new Insets(8, 4, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 1, 0, 1, 1, 1.0, 1.0);
        panel.add(rbConv, gbc);
        
        noteLbl = createLabel(new JLabel("Note: The scenario type CANNOT be changed once it has been selected.", SwingConstants.LEFT));
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 1, 3, 1, 1.0, 1.0);
        panel.add(noteLbl, gbc);
        
        gbc = setGbc(new Insets(2, 4, 2, 4), GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, 0, 2, 3, 1, 1.0, 0.0);
        frame.add(panel, gbc);
        
        confirm = createButton(new JButton("OK"), "false", new confirmListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.EAST, 1, 3, 1, 1, 1.0, 0.0);
        frame.add(confirm, gbc);
        
        cancel = createButton(new JButton("Cancel"), "false", new cancelListener());
        gbc = setGbc(new Insets(8, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST, 2, 3, 1, 1, 0.0, 0.0);
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
    
    private JPanel createPanel(JPanel pnl, String s, Boolean v) {
        pnl.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), s, TitledBorder.LEFT, TitledBorder.TOP, f));
        pnl.setLayout(new GridBagLayout());
        pnl.setVisible(v);
        return pnl;
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
    
    private class confirmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
    
    private class cancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            frame.dispose();
        }
    }
}
