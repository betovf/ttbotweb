package com.vfa.ttbot.web.view;

import java.util.List;

import com.google.common.collect.ListMultimap;
import com.vaadin.ui.Component;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public interface IChartWrapper {

	Component getChart();
	
	void populate(List<Trend> trends, ListMultimap<Integer,TrendLog> mapTrends, int maxTrends);
}
