package com.vfa.ttbot.dao;

import com.vfa.ttbot.model.Trend;

public interface TrendMapper {

	public int insertTrend(Trend trend);
	
	public Integer searchTrendByName(String name);
	
	public Trend getTrendById(int id);
}
