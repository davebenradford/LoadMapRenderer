/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package loadmaprenderer;

import java.awt.Color;
import java.util.List;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/**
 *
 * @author Shao
 */
/**
 *
 * @author user
 */
public class ChartTest extends JFrame {
    /**
     * @param args the command line arguments
     */
    
    public ChartTest(List<ResultDataPair> data) {
        setTitle("Chart");
        XYDataset dataset = makeCollection(data);
        JFreeChart jfc = makeChart(dataset);
        ChartPanel cp = new ChartPanel(jfc);
        setContentPane(cp);
    }
    
    private XYSeriesCollection makeCollection(List<ResultDataPair> data) {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries seriesA = new XYSeries("Cost/Year");
        XYSeries seriesB = new XYSeries("Average");
        Double average = GetAverage(data);
        for(int i = 0; i < data.size(); i++) {            
            seriesA.add(data.get(i).getYear(), data.get(i).getData());
            seriesB.add(data.get(i).getYear(), average);
        }
        collection.addSeries(seriesA);
        collection.addSeries(seriesB);
        return collection;
    }
    
    private JFreeChart makeChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart("Cost/Year Chart", "Year", "Cost", dataset,
                           PlotOrientation.VERTICAL, false, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLUE);
        plot.setRangeGridlinePaint(Color.BLUE);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(rootPaneCheckingEnabled);
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(rootPaneCheckingEnabled);
        return chart;
    }
    
    private double GetAverage(List<ResultDataPair> values){
        if (values.isEmpty()) return 0.0;
        double totalValue = 0;
        for (int i = 0; i < values.size(); i++) 
            totalValue += values.get(i).getData();
        return totalValue / values.size();
    }
}

