package com.vfa.ttbot.web;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vfa.ttbot.helper.ModelHelper;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;
import com.vfa.ttbot.service.DBDataService;
import com.vfa.ttbot.service.IDataService;

@SuppressWarnings("serial")
@Theme("ttbotweb")
public class TTBotWebUI extends UI {

	private static IDataService dataService = new DBDataService();
	private List<TrendLog> trendLogs;
	private List<Trend> trends;
	private Map<Date, List<TrendLog>> mapTrendLogs;
	private ArrayList<Date> dates;
	
	@Override
	protected void init(VaadinRequest request) {
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		setContent(layout);

		Chart chart = new Chart(ChartType.LINE);
		chart.setWidth("1000px");
		chart.setHeight("500px");
		        
		// Modify the default configuration a bit
		Configuration conf = chart.getConfiguration();
		conf.setTitle("TTs");
		conf.setSubTitle("Evolución de TTs en España");
		//conf.getLegend().setEnabled(false); // Disable legend

		// The data
		this.loadData();
		
		for (Trend trend : this.trends) {
			DataSeries series = new DataSeries(trend.getName());
			
			for (Date date : this.dates) {
				List<TrendLog> logs = this.mapTrendLogs.get(date);
				Integer pos = null;
				for (TrendLog log : logs) {
					if (log.getIdTrend().equals(trend.getId())) {
						pos = log.getPosition();
					}
				}
				DataSeriesItem item = new DataSeriesItem(date,pos);
				series.add(item);
			}
			conf.addSeries(series);
		}
		
		// Set the category labels on the axis correspondingly
		XAxis xaxis = new XAxis();
		xaxis.setType(AxisType.DATETIME);
		xaxis.setTitle("Fecha-Hora");
		conf.addxAxis(xaxis);

		// Set the Y axis title
		YAxis yaxis = new YAxis();
		yaxis.setTitle("Posición");
		yaxis.setMin(1);
		yaxis.setMax(10);
		yaxis.setReversed(true);
//		String[] categories = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
//		yaxis.setCategories(categories);
		//yaxis.getLabels().setFormatter("Math.abs(this.y-10)");
		conf.addyAxis(yaxis);
		        
		layout.addComponent(chart);
	}

	private void loadData() {
		//SimpleDateFormat sdFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		try {
			Date end = new Date();//sdFmt.parse("2014-02-22 22:28");//
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(end);
			calendar.add(Calendar.HOUR_OF_DAY, -2);
			Date ini = calendar.getTime();
			
			this.trendLogs = dataService.getTrendLogsByDate(ini, end);
		
			this.mapTrendLogs = ModelHelper.groupTrendLogsByDateTime(trendLogs);
			
			this.dates = new ArrayList<Date>(mapTrendLogs.keySet());
			Collections.sort(this.dates);
			
			Map<Integer, List<TrendLog>> mapTrends = ModelHelper.groupTrendLogsByTrend(trendLogs);
			
			this.trends = ModelHelper.getTrends(dataService, new ArrayList<Integer>(mapTrends.keySet()));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
	
}