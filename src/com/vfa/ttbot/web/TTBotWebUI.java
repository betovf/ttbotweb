package com.vfa.ttbot.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

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
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vfa.ttbot.helper.DateTimeHelper;
import com.vfa.ttbot.helper.LinkHelper;
import com.vfa.ttbot.helper.ModelHelper;
import com.vfa.ttbot.helper.TrendComparator;
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
	private ListMultimap<Date,TrendLog> mapTrendLogs;
	private ListMultimap<Integer,TrendLog> mapTrends;
	private ArrayList<Date> dates;
	private Date endDate;
	private Date iniDate;
	private Chart chart;
	private PopupDateField iniDateField;
	private PopupDateField endDateField;
	private ListSelect selectTrends;
	private List<Trend> filteredTrends = new ArrayList<Trend>();
	private Button button;
	private ProgressBar loading;
	private ComboBox combomMaxTrends;
	private int maxTrends;
	private boolean filter=true;
	private boolean limit=false;
	private List<Trend> weightedTrends;
	
	@Override
	protected void init(VaadinRequest request) {
		// Page title
		Page.getCurrent().setTitle("TTBot.es");

		// Resource bundle for internationalized messages	
		Locale locale = request.getLocale();
		final ResourceBundle bundle = ResourceBundle.getBundle("Messages", locale);
		
		// Main layout
		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		// Chart
		chart = new Chart(ChartType.LINE);
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
					getUI().getPage().open(twitterUrl, "Twitter");
				} else {
					Notification.show(bundle.getString("error"), bundle.getString("urlError"), Notification.Type.ERROR_MESSAGE);
				}
            }
        });
        
        // First time drawing
		this.initDates();
		this.loadData();		
		this.populateChart();
		
		layout.addComponent(chart);
		
		// Form fields
		HorizontalLayout formLayout = new HorizontalLayout();
		formLayout.setSpacing(true);
		layout.addComponent(formLayout);

		iniDateField = new PopupDateField(bundle.getString("initialDateTime"), this.iniDate);
		iniDateField.setDescription(bundle.getString("iniDateDesc"));
		iniDateField.setResolution(Resolution.MINUTE);
		iniDateField.setRequired(true);
		iniDateField.setRequiredError(bundle.getString("iniDateRequiredError"));
		iniDateField.setRangeStart(this.getMinDate());
		iniDateField.setRangeEnd(this.getMaxDate());
		iniDateField.addValidator(new Validator() {
			@Override
			public void validate(Object value) throws InvalidValueException {
				// Check against min date
				if (!(value instanceof Date && ((Date)value).after(getMinDate()))) {
					throw new InvalidValueException(bundle.getString("iniDateInvalidMessage"));
				}
			}			
		});
		iniDateField.setImmediate(true);
		
		endDateField = new PopupDateField(bundle.getString("endDateTime"), this.endDate);
		endDateField.setDescription(bundle.getString("endDateDesc"));
		endDateField.setResolution(Resolution.MINUTE);
		endDateField.setRequired(true);
		endDateField.setRequiredError(bundle.getString("endDateRequiredError"));
		endDateField.setRangeStart(this.getMinDate());
		endDateField.setRangeEnd(this.getMaxDate());
		endDateField.addValidator(new Validator() {
			@Override
			public void validate(Object value) throws InvalidValueException {
				// Check against initial date
				if (!(value instanceof Date && ((Date)value).after(iniDateField.getValue()))) {
					throw new InvalidValueException(bundle.getString("endDateInvalidMessage"));
				}
			}			
		});
		endDateField.setImmediate(true);
		
		// Button for setting current time in end date field
		Button nowButton = new Button(bundle.getString("setNow"));
		nowButton.addClickListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		    	// Set value of end date field
				endDateField.setValue(DateTimeHelper.getDate());
		    }
		});
		
		// Create vertical layout to pack end date field and now button together
		VerticalLayout endDateLayout = new VerticalLayout();
		endDateLayout.addComponent(endDateField);
		endDateLayout.addComponent(nowButton);
		
		// Have an option group
		OptionGroup group = new OptionGroup(bundle.getString("filterGroup"));
		group.addItem(bundle.getString("none"));
		group.addItem(bundle.getString("select"));
		group.addItem(bundle.getString("limit"));
		group.setValue(bundle.getString("none"));
		group.addValueChangeListener(new ValueChangeListener() {			
			@Override
			public void valueChange(ValueChangeEvent event) {
				// enable selected
				String val = (String) event.getProperty().getValue();
				filter = bundle.getString("select").equalsIgnoreCase(val);
				limit = bundle.getString("limit").equalsIgnoreCase(val);
				if (filter) {
					selectTrends.setEnabled(true);
					combomMaxTrends.setEnabled(false);					
				} else if (limit){
					selectTrends.setEnabled(false);
					combomMaxTrends.setEnabled(true);					
				} else {
					selectTrends.setEnabled(false);
					combomMaxTrends.setEnabled(false);					
				}
			}
		});
		
		// Select field for filtering trends
		selectTrends = new ListSelect(bundle.getString("selectTrends"));
		selectTrends.setDescription(bundle.getString("selectTrendsDesc"));
		selectTrends.setMultiSelect(true);
		populateSelectTrends();
		selectTrends.setItemCaptionMode(ItemCaptionMode.PROPERTY);
	    selectTrends.setItemCaptionPropertyId("name");
	    selectTrends.setEnabled(false);
		
	    // Combo box for limiting trends shown
	    combomMaxTrends = new ComboBox(bundle.getString("maxTrends"));
	    combomMaxTrends.setDescription(bundle.getString("maxTrendsDesc"));
	    populateComboMaxTrends();
	    combomMaxTrends.setEnabled(false);
	    
		button = new Button(bundle.getString("submit"));
		button.addClickListener(new Button.ClickListener() {
		    public void buttonClick(ClickEvent event) {
		    	// Proceed only if both dates are valid (validators run immediately)
				if(iniDateField.isValid() && endDateField.isValid()) {
					refreshChart();
				}
		    }
		});
		
		// Create loading progress bar
		loading = new ProgressBar();
		loading.setIndeterminate(true);
		loading.setVisible(false);
		
		// Create horizontal layout to pack combo, submit button and progress bar together
		HorizontalLayout hLayout = new HorizontalLayout();
		hLayout.setSpacing(true);
		hLayout.addComponent(combomMaxTrends);
		hLayout.addComponent(button);
		hLayout.setComponentAlignment(button, Alignment.BOTTOM_CENTER);
		hLayout.addComponent(loading);
		hLayout.setComponentAlignment(loading, Alignment.BOTTOM_CENTER);

		formLayout.addComponent(iniDateField);
		formLayout.addComponent(endDateLayout);
		formLayout.addComponent(group);
		formLayout.addComponent(selectTrends);
		formLayout.addComponent(hLayout);
		
	}

	private void populateComboMaxTrends() {
		combomMaxTrends.removeAllItems();
	    for(int i=10; i<this.trends.size();i += 10) {
	    	combomMaxTrends.addItem(String.valueOf(i));
	    }
	}

	protected void refreshChart() {
		// Check if dates changed
		boolean unchanged = this.iniDate.equals(this.iniDateField.getValue()) && this.endDate.equals(this.endDateField.getValue());
		
		// Get new dates 
		this.iniDate = this.iniDateField.getValue();
		this.endDate = this.endDateField.getValue();

		// Get possible trends filtered
		this.filteredTrends.clear();
		if (filter) {
			@SuppressWarnings("unchecked")
			Set<Trend> selected = (Set<Trend>) this.selectTrends.getValue();
			if (!selected.isEmpty()) {
				this.filteredTrends.addAll(selected);
			}
		} else {
			this.selectTrends.setValue(null);
		}
		
		// Get possible max number of trends to show
		this.maxTrends = 0;
		if (limit) {
			String selId = (String) this.combomMaxTrends.getValue();
			try {
				this.maxTrends = Integer.valueOf(selId);
			} catch (NumberFormatException e) {
				// Default option, show all
			}
		} else {
			this.combomMaxTrends.setValue(null);
		}
		
		if (unchanged) {
			// Reload data in chart directly
			this.populateChart();		
			this.chart.drawChart();
		} else {
			// Load data in another thread
			final WorkThread thread = new WorkThread();
			thread.start();
			
			// Enable polling and set frequency to 0.5 seconds
			UI.getCurrent().setPollInterval(500);
			
			// Disable the button until the work is done
			this.button.setEnabled(false);
			
			// Show progress
			this.loading.setVisible(true);
		}
	}

	private void initDates() {
		// Init dates for fields
		this.endDate = DateTimeHelper.getDate();		
		this.iniDate = DateTimeHelper.getDate(-2);		
	}
	
	protected Date getMinDate() {
		// Can't show beyond one day in the past
		return DateTimeHelper.getDate(-24);
	}

	protected Date getMaxDate() {
		// For disabling dates in the future
		return DateTimeHelper.getDate(24);
	}

	private void loadData() {
		try {
			// Get all logs from date to date
			this.trendLogs = dataService.getTrendLogsByDate(this.iniDate, this.endDate);
		
			// Group those logs by date-time
			this.mapTrendLogs = ModelHelper.groupTrendLogsByDateTime(this.trendLogs);
			
			// Get list of dates from previous map
			this.dates = new ArrayList<Date>(this.mapTrendLogs.keySet());
			Collections.sort(this.dates);
			
			// Group log also by trend
			ListMultimap<Integer,TrendLog> mapTrends = ModelHelper.groupTrendLogsByTrend(this.trendLogs);
			
			// Get trends names from id-trend list from previous map
			this.trends = dataService.getTrends(mapTrends.keySet());
			Collections.sort(this.trends, new TrendComparator());
			
			// Get the final map matching trends vs dates
			this.mapTrends = ModelHelper.groupTrendLogsByTrendAndDate(this.mapTrendLogs, this.dates, this.trends);
			
			// Get alternative list of trends ordered by weight
			this.weightedTrends = ModelHelper.getWeightedTrends(trends, mapTrends);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void populateChart() {
		// New series
		List<Series> newSeries = new ArrayList<Series>();
		
		// All trends by default
		List<Trend> targetTrends = this.trends;
		
		// Check if filtered
		if (filter && !this.filteredTrends.isEmpty()) {
			targetTrends = this.filteredTrends;
		}
		
		// Check if limited
		if (limit) {
			targetTrends = this.weightedTrends;
		}
		boolean limited = this.maxTrends != 0;
		int count = 0;
		
		// Configure series according to data
		for (Trend trend : targetTrends) {
			// Each trend will be a series
			DataSeries series = new DataSeries(trend.getName());
			
			// Loop over its log entries
			for (TrendLog log : this.mapTrends.get(trend.getId())) {
				// Create data item
				DataSeriesItem item = new DataSeriesItem(log.getDateTime(), log.getPosition());
				series.add(item);
			}
			// Add trend series
			newSeries.add(series);
			
			// Check if limited
			if (limited && ++count > this.maxTrends) {
				break;
			}
		}
		// Set series to chart (discarding previous ones)
		this.chart.getConfiguration().setSeries(newSeries);
	}
	
	private void populateSelectTrends() {
		BeanItemContainer<Trend> trendContainer = new BeanItemContainer<Trend>(Trend.class, trends);
		selectTrends.setContainerDataSource(trendContainer);
	}

	class WorkThread extends Thread {
		@Override
		public void run() {
			// Retrieve data
			loadData();
			
			// Update the UI thread-safely
			UI.getCurrent().access(new Runnable() {
				@Override
				public void run() {
					// Reload data in chart
					populateChart();					
					chart.drawChart();
					
					// Update ListSelect Items
					populateSelectTrends();
					
					// Update max trends combo
					populateComboMaxTrends();
					
					// Hide notification
					loading.setVisible(false);
					
					// Enable submit button
					button.setEnabled(true);
					
					// Stop polling
					UI.getCurrent().setPollInterval(-1);
				}
			});
		}
	}
}
