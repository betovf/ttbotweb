package com.vfa.ttbot.web.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.collect.ListMultimap;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.PointClickEvent;
import com.vaadin.addon.charts.PointClickListener;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vfa.ttbot.helper.LinkHelper;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public class VaadinChartWrapper implements IChartWrapper {

	private Chart chart;
	
	public VaadinChartWrapper(final ResourceBundle bundle, Locale locale) {
		
		Chart chart = new Chart(ChartType.LINE);
		chart.setWidth("100%");
		chart.setHeight("500px");
		        
		// Modify configuration
		Configuration conf = chart.getConfiguration();
		conf.setTitle(bundle.getString("title"));
		conf.setSubTitle(bundle.getString("subtitle"));
		
		// Set the X axis to handle date-time
		XAxis xaxis = new XAxis();
		xaxis.setType(AxisType.DATETIME);
		xaxis.setTitle(bundle.getString("dateTime"));
		conf.addxAxis(xaxis);
	
		// Set the Y axis to show positions from 1 to 10 
		YAxis yaxis = new YAxis();
		yaxis.setTitle(bundle.getString("position"));
		yaxis.setTickInterval(1);
		yaxis.setMin(1);
		yaxis.setMax(10);
		yaxis.setReversed(true);
		conf.addyAxis(yaxis);
		
	    if ("es".equalsIgnoreCase(locale.getLanguage())) {
			// Spanish convention for date formatting
			conf.getTooltip().setxDateFormat("%e/%m/%Y %H:%M");
		}
	    
		// Add click listener to open trend in Twitter 
	    chart.addPointClickListener(new PointClickListener() {            
	        @Override
	        public void onClick(PointClickEvent event) {
	            // Get trend clicked
	        	String trend = event.getSeries().getName();
	        	
	        	// Open twitter in a new browser tab
	            String twitterUrl = LinkHelper.getTwitterUrlForTrend(trend);
				if (twitterUrl != null) {
					// All following links opened will go to the same window
					Notification.show(bundle.getString("information"), bundle.getString("newBrowserWindow"), Notification.Type.TRAY_NOTIFICATION);
					Page.getCurrent().open(twitterUrl, "Twitter");
				} else {
					Notification.show(bundle.getString("error"), bundle.getString("urlError"), Notification.Type.ERROR_MESSAGE);
				}
	        }
	    });
	    
	    // assign to field
	    this.chart = chart;
	}
	
	public void populate(List<Trend> trends, ListMultimap<Integer,TrendLog> mapTrends, int maxTrends) {
		// New series
		List<Series> newSeries = new ArrayList<Series>();
		
		boolean limited = maxTrends != 0;
		int count = 0;
		
		// Configure series according to data
		for (Trend trend : trends) {
			// Each trend will be a series
			DataSeries series = new DataSeries(trend.getName());
			
			// Loop over its log entries
			for (TrendLog log : mapTrends.get(trend.getId())) {
				// Create data item
				DataSeriesItem item = new DataSeriesItem(log.getDateTime(), log.getPosition());
				series.add(item);
			}
			// Add trend series
			newSeries.add(series);
			
			// Check if limited
			if (limited && ++count > maxTrends) {
				break;
			}
		}
		// Set series to chart (discarding previous ones)
		this.chart.getConfiguration().setSeries(newSeries);

		this.chart.drawChart();
	}

	@Override
	public Component getChart() {
		return this.chart;
	}
}
