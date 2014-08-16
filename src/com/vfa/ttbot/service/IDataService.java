package com.vfa.ttbot.service;

import java.util.Date;
import java.util.List;

import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public interface IDataService {

	public List<TrendLog> getTrendLogsByDate(Date ini, Date end);
	
	public Trend getTrend(int id);
	
	public List<Trend> getTrends(List<Integer> ids);
}
