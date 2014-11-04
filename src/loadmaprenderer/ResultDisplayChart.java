/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.w3c.dom.ranges.Range;

/**
 *
 * @author Shao
 */
public class ResultDisplayChart extends JPanel {
    private JFreeChart chart;
    private JLabel label = new JLabel("No Selection or No Result Data");
    private XYSeries dataLine;
    private XYSeries averageLine;
    
    public ResultDisplayChart(){        
        this.removeAll();
        this.setLayout(new BorderLayout());
    }
    
    public void DrawChart(List<ResultDataPair> values, String chartTitle, 
            String dataTitle, double average, boolean setAverage) {
        GridBagConstraints gbc;
        this.removeAll();
        if (values == null || values.isEmpty() || allZero(values))
        {
            if (values == null || values.isEmpty())
                label.setText("No Selection or No Result Data");
            else
                label.setText("ALL ZERO");
            this.setLayout(new GridBagLayout());
            this.add(label);
            return;
        }
        if (dataLine != null) dataLine.clear();
        if (averageLine != null) averageLine.clear();
        if (!setAverage) average = getAverage(values);
        XYDataset dataset = makeChartDataset(values, dataTitle, average);
        chart = makeChart(dataset, chartTitle, dataTitle);
        
        gbc = setGbc(new Insets(0, 0, 0, 0), GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 0, 1, 1, 1, 1.0, 1.0);
        
        ChartPanel cp = new ChartPanel(chart);
        
        this.setLayout(new GridBagLayout());
        this.add(cp, gbc);
        this.setPreferredSize(new Dimension(500,150));
    }
    
    private XYSeriesCollection makeChartDataset(List<ResultDataPair> values, 
            String dataTitle, double average) {
        XYSeriesCollection collection = new XYSeriesCollection();
        dataLine = new XYSeries(dataTitle);
        averageLine = new XYSeries("Average");
        for(int i = 0; i < values.size(); i++) {            
            dataLine.add(values.get(i).getYear(), values.get(i).getData());
            averageLine.add(values.get(i).getYear(), average);
        }
        collection.addSeries(dataLine);
        collection.addSeries(averageLine);
        return collection;
    }
    
    private JFreeChart makeChart(XYDataset dataset, String chartTitle, 
            String dataTitle) {
        JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, "Year", 
                dataTitle, dataset, PlotOrientation.VERTICAL, false, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLUE);
        plot.setRangeGridlinePaint(Color.BLUE);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(true);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        Double range = dataLine.getMaxY() - dataLine.getMinY();
        if (Math.abs(range) < 0.1) range = dataLine.getMaxY();
        rangeAxis.setRange(dataLine.getMinY() - range * 0.1,
                dataLine.getMaxY() + range * 0.1);
        return chart;
    }
    
    private double getAverage(List<ResultDataPair> values){
        if (values.isEmpty()) return 0.0;
        double totalValue = 0;
        for (int i = 0; i < values.size(); i++) 
            totalValue += values.get(i).getData();
        return totalValue / values.size();
    }
    
    private boolean allZero(List<ResultDataPair> values)
    {
        for (ResultDataPair value : values)
            if (value.getData() != 0.0) return false;
        return true;
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
}
