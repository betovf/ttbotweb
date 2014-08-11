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
	
	public static List<Trend> getTrends(IDataService service, List<Integer> ids) {		
		List<Trend> trends = new ArrayList<Trend>();
		
		for (Integer id : ids) {
			trends.add(service.getTrend(id));
		}
		
		return trends;		
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
