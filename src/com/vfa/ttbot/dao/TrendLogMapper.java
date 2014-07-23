package com.vfa.ttbot.dao;

import java.util.Date;
import java.util.List;

import com.vfa.ttbot.model.TrendLog;

public interface TrendLogMapper {

	public int insertTrendLog(TrendLog trendLog);
	
	public List<TrendLog> searchTrendLogsByDate(Date ini, Date end);
	
}
