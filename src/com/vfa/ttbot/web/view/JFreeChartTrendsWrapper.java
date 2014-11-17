package com.vfa.ttbot.web.view;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.vaadin.addon.JFreeChartWrapper;

import com.google.common.collect.ListMultimap;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public class JFreeChartTrendsWrapper implements IChartWrapper {

	private ResourceBundle bundle;
	private Locale locale;
	private Component wrapper;
	
	public JFreeChartTrendsWrapper(final ResourceBundle bundle, Locale locale) {
		this.bundle = bundle;
		this.locale = locale;
		this.wrapper = new Label("foo");
	}
	
	@Override
	public Component getChart() {
		return this.wrapper;
	}

	@Override
	public void populate(List<Trend> trends, ListMultimap<Integer, TrendLog> mapTrends, int maxTrends) {
		// New dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection(TimeZone.getTimeZone("Europe/Madrid"));
		
		boolean limited = maxTrends != 0;
		int count = 0;
		
		// Configure series according to data
		for (Trend trend : trends) {
			// Each trend will be a series
			TimeSeries series = new TimeSeries(trend.getName());
			
			// Loop over its log entries
			for (TrendLog log : mapTrends.get(trend.getId())) {
				// Create data item
				TimeSeriesDataItem item = new TimeSeriesDataItem(new Minute(log.getDateTime()), log.getPosition());
				series.add(item);
			}
			// Add trend series
			dataset.addSeries(series);
			
			// Check if limited
			if (limited && ++count > maxTrends) {
				break;
			}
		}
	        
		// TODO create/refresh chart
        // Generate the graph
        JFreeChart chart = ChartFactory.createTimeSeriesChart(bundle.getString("title"), // Title
        		bundle.getString("dateTime"), // x-axis Label
        		bundle.getString("position"), // y-axis Label
                dataset, // Dataset
                //PlotOrientation.VERTICAL, // Plot Orientation
                true, // Show Legend
                true, // Use tooltips
                false // Configure chart to generate URLs?
                );
        
        // set chart background
        chart.setBackgroundPaint(Color.white);
        
        // set a few custom plot features
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinesVisible(false);
        //plot.
        
        // Create an NumberAxis
        NumberAxis yAxis = new NumberAxis();//plot.getRangeAxis()
        yAxis.setTickUnit(new NumberTickUnit(1));
        yAxis.setRange(0.5d, 10.5d);
        
        // Invert Y-axis to show positions in natural order
        yAxis.setInverted(true);
        
        // Assign it to the chart
        plot.setRangeAxis(yAxis);
        
        // render shapes and lines
//        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
//        plot.setRenderer(renderer);
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
        	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
	        renderer.setBaseShapesVisible(true);
	        renderer.setBaseShapesFilled(true);
	        renderer.setDrawSeriesLineAsPath(true);
        }
        // set the renderer's stroke
//        Stroke stroke = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
//        renderer.setBaseOutlineStroke(stroke);
        
        this.wrapper = new JFreeChartWrapper(chart);
        this.wrapper.setWidth("80%");
        //refrescar aquí -> no se puede crear otro...
	}

}
