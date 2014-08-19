package com.vfa.ttbot.service;

import java.lang.Iterable;
import java.util.Date;
import java.util.List;

import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public interface IDataService {

	public List<TrendLog> getTrendLogsByDate(Date ini, Date end) throws Exception;
	
	public Trend getTrend(int id) throws Exception;
	
	public List<Trend> getTrends(Iterable<Integer> ids) throws Exception;
}
