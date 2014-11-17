package com.vfa.ttbot.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTime;

import com.google.common.collect.ListMultimap;
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
import com.vfa.ttbot.helper.ModelHelper;
import com.vfa.ttbot.helper.TrendComparator;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;
import com.vfa.ttbot.service.DBDataService;
import com.vfa.ttbot.service.IDataService;
import com.vfa.ttbot.web.view.IChartWrapper;
import com.vfa.ttbot.web.view.JFreeChartTrendsWrapper;

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
	private IChartWrapper chart;
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
	private boolean dataError;
	private Notification dataErrorNotif;
	private VerticalLayout layout;
	
	@Override
	protected void init(VaadinRequest request) {
		// Page title
		Page.getCurrent().setTitle("TTBot.es");

		// Resource bundle for internationalized messages	
		Locale locale = request.getLocale();
		final ResourceBundle bundle = ResourceBundle.getBundle("Messages", locale);
		
		// TODO get user's timezone
		TimeZone timezone = TimeZone.getTimeZone("Europe/Madrid");
		
		// Main layout
		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);

		// Chart
		chart = new JFreeChartTrendsWrapper(bundle, locale);//new VaadinChartWrapper(bundle, locale);//
        
		layout.addComponent(chart.getChart());
		
		// Form fields
		HorizontalLayout formLayout = new HorizontalLayout();
		formLayout.setSpacing(true);
		layout.addComponent(formLayout);

		this.initDates();

		iniDateField = new PopupDateField(bundle.getString("initialDateTime"), this.iniDate);
		iniDateField.setTimeZone(timezone);
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
		endDateField.setTimeZone(timezone);
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
		selectTrends.setItemCaptionMode(ItemCaptionMode.PROPERTY);
	    selectTrends.setItemCaptionPropertyId("name");
	    selectTrends.setEnabled(false);
		
	    // Combo box for limiting trends shown
	    combomMaxTrends = new ComboBox(bundle.getString("maxTrends"));
	    combomMaxTrends.setDescription(bundle.getString("maxTrendsDesc"));
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
		
		// Create notification for data error
		this.dataErrorNotif = new Notification(bundle.getString("error"), bundle.getString("dataError"), Notification.Type.ERROR_MESSAGE);
		
		layout.removeComponent(this.chart.getChart());

		// First time drawing
		this.loadData();		
		this.populateChart();
		this.populateSelectTrends();
		this.populateComboMaxTrends();
		
		layout.addComponent(this.chart.getChart(), 0);
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
		// Set error flag to false
		this.dataError = false;
		
		try {
			// Get all logs from date to date
			this.trendLogs = dataService.getTrendLogsByDate(this.iniDate, this.endDate);
		} catch (Exception ex) {
			this.dataError = true;
			this.trendLogs = new ArrayList<TrendLog>();
		}
		
		// Group those logs by date-time
		this.mapTrendLogs = ModelHelper.groupTrendLogsByDateTime(this.trendLogs);
		
		// Get list of dates from previous map
		this.dates = new ArrayList<Date>(this.mapTrendLogs.keySet());
		Collections.sort(this.dates);
		
		// Group log also by trend
		ListMultimap<Integer,TrendLog> mapTrends = ModelHelper.groupTrendLogsByTrend(this.trendLogs);
			
		try {
			// Get trends names from id-trend list from previous map
			this.trends = dataService.getTrends(mapTrends.keySet());
		} catch (Exception ex) {
			this.dataError = true;
			this.trends = new ArrayList<Trend>();
		}
		Collections.sort(this.trends, new TrendComparator());
		
		// Get the final map matching trends vs dates
		this.mapTrends = ModelHelper.groupTrendLogsByTrendAndDate(this.mapTrendLogs, this.dates, this.trends);
		
		// Get alternative list of trends ordered by weight
		this.weightedTrends = ModelHelper.getWeightedTrends(trends, mapTrends);
			
	}

	private void populateChart() {
		// Check for possible data error
		if (this.dataError) {
			this.dataErrorNotif.show(Page.getCurrent());
			return;
		}
		
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
		
		layout.removeComponent(this.chart.getChart());
		
		// populate chart
		this.chart.populate(targetTrends, mapTrends, maxTrends);
		
		layout.addComponent(this.chart.getChart(), 0);
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
