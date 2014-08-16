package com.vfa.ttbot.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;
import com.vfa.ttbot.model.WeightedTrend;
import com.vfa.ttbot.service.IDataService;

public class ModelHelper {

	public static ListMultimap<Integer,TrendLog> groupTrendLogsByTrendAndDate(ListMultimap<Date, TrendLog> trendLogs, List<Date> dates, List<Trend> trends) {
		ListMultimap<Integer, TrendLog> map = ArrayListMultimap.create();

		// Loop over all trends
		for (Trend trend : trends) {
			// Loop over all possible date-times
			for (Date date : dates) {
				// Loop over its log positions
				List<TrendLog> logs = trendLogs.get(date);
				TrendLog selLog = null;
				for (TrendLog log : logs) {
					// Search a match to current trend
					if (log.getIdTrend().equals(trend.getId())) {
						selLog = log;
					}
				}
				// The log might be null if current trend did no appear in the top ten that time
				if (selLog == null) {
					// Create a log with a null position for that date-time
					selLog = new TrendLog();
					selLog.setDateTime(date);
				}
				map.put(trend.getId(), selLog);
			}
		}		
		return map;
	}
		


	public static ListMultimap<Integer,TrendLog> groupTrendLogsByTrend(List<TrendLog> trendLogs) {
		ListMultimap<Integer, TrendLog> map = ArrayListMultimap.create();
		
		for (TrendLog log : trendLogs) {
			// Just add the log for that trend id
			map.put(log.getIdTrend(), log);
		}
		
		return map;
	}
	
	public static ListMultimap<Date,TrendLog> groupTrendLogsByDateTime(List<TrendLog> trendLogs) {
		ListMultimap<Date, TrendLog> map = ArrayListMultimap.create();
		
		for (TrendLog log : trendLogs) {
			// Just add the log for that date
			map.put(log.getDateTime(), log);
		}
		
		return map;
	}
	
	public static List<Trend> getWeightedTrends(List<Trend> trends, ListMultimap<Integer,TrendLog> mapTrends) {
		// Create a TreeSet for ordering trends by weight
		Set<WeightedTrend> trendsSet = new TreeSet<WeightedTrend>(new WeightedTrendComparator());
		
		for (Trend t : trends) {
			// Its weight will be its number of log appearances
			int weight = mapTrends.get(t.getId()).size();
			WeightedTrend wt = new WeightedTrend();
			wt.setId(t.getId());
			wt.setName(t.getName());
			wt.setWeight(weight);
			trendsSet.add(wt);
		}
		// Create list from set
		return new ArrayList<Trend>(trendsSet);
	}
}
