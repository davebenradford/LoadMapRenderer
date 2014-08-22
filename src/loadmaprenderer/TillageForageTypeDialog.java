package loadmaprenderer;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/**
 *
 * @author radfordd
 */

public class TillageForageTypeDialog {
    private final JFrame frame;
    private final JRadioButton rbField;
    private final JRadioButton rbFarm;
    private final JRadioButton rbSubbasin;
    private final JLabel topNote;
    private final JLabel botNote;
    private final JButton confirm;
    private final JButton cancel;
    
    private final Font f = new Font("Sans_Serif", Font.BOLD, 12);
    private final ImageIcon websIcon = new ImageIcon("build\\classes\\loadmaprenderer\\resources\\Images\\icon_32x32.png");
    
    public static void main(String[] args) {
        TillageForageTypeDialog tftd = new TillageForageTypeDialog();
    }
    
    public TillageForageTypeDialog() {
        frame = new JFrame("Crop BMP Level Selection");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setIconImage(websIcon.getImage());
        // frame.setLocation(WhiteboxGuiClone.wb.getWidth() / 4, WhiteboxGuiClone.wb.getHeight());
        
        rbField = new JRadioButton("Field");
        rbFarm = new JRadioButton("Farm");
        rbSubbasin = new JRadioButton("Subbasin");
        
        ButtonGroup group = new ButtonGroup();
        group.add(rbField);
        group.add(rbFarm);
        group.add(rbSubbasin);
        
        GridBagConstraints gbc = setGbc(new Insets(16, 12, 8, 8), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 0, 1, 1, 0.0, 1.0);
        frame.add(rbField, gbc);
        
        gbc = setGbc(new Insets(16, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 1, 0, 1, 1, 0.0, 1.0);
        frame.add(rbFarm, gbc);
        
        gbc = setGbc(new Insets(16, 8, 8, 8), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 2, 0, 1, 1, 0.0, 1.0);
        frame.add(rbSubbasin, gbc);
        
        topNote = new JLabel("Note: The BMP Level is for Tillage and Forage Conversion.", SwingConstants.LEFT);
        gbc = setGbc(new Insets(4, 16, 4, 16), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 1, 4, 1, 0.0, 1.0);
        frame.add(topNote, gbc);
        
        botNote = new JLabel("Once selected, it CANNOT be changed!", SwingConstants.LEFT);
        gbc = setGbc(new Insets(4, 50, 4, 16), GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0, 2, 4, 1, 0.0, 1.0);
        frame.add(botNote, gbc);
        
        confirm = createButton(new JButton("OK"), "false", new confirmListener());
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST, 2, 3, 1, 1, 1.0, 1.0);
        frame.add(confirm, gbc);
        
        cancel = createButton(new JButton("Cancel"), "false", new cancelListener());
        gbc = setGbc(new Insets(4, 4, 4, 4), GridBagConstraints.NONE, GridBagConstraints.SOUTHEAST, 3, 3, 1, 1, 1.0, 1.0);
        frame.add(cancel, gbc);
        
        frame.pack();
        frame.validate();
        frame.setVisible(true);
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
    
    // Listeners
    
    private class confirmListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent ae) {
            // WhiteboxGuiClone.wb.validate();
            // WhiteboxGuiClone.wb.repaint();
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
