package com.vfa.ttbot.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;
import com.vfa.ttbot.model.WeightedTrend;
import com.vfa.ttbot.service.IDataService;

public class ModelHelper {

	public static Map<Integer, List<TrendLog>> groupTrendLogsByTrend(List<TrendLog> trendLogs) {
		Map<Integer, List<TrendLog>> map = new HashMap<Integer, List<TrendLog>>();
		
		for (TrendLog log : trendLogs) {
			List<TrendLog> list = map.get(log.getIdTrend());
			if (list == null) {
				list = new ArrayList<TrendLog>();
				map.put(log.getIdTrend(), list);
			}
			list.add(log);
		}
		
		return map;
	}
	
	public static Map<Date, List<TrendLog>> groupTrendLogsByDateTime(List<TrendLog> trendLogs) {
		Map<Date, List<TrendLog>> map = new HashMap<Date, List<TrendLog>>();
		
		for (TrendLog log : trendLogs) {
			List<TrendLog> list = map.get(log.getDateTime());
			if (list == null) {
				list = new ArrayList<TrendLog>(10);
				map.put(log.getDateTime(), list);
			}
			list.add(log);
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
	
	public static List<Trend> getWeightedTrends(List<Trend> trends, Map<Integer, List<TrendLog>> mapTrendLogs) {
		// Create a TreeSet for ordering trends by weight
		Set<WeightedTrend> trendsSet = new TreeSet<WeightedTrend>(new WeightedTrendComparator());
		
		for (Trend t : trends) {
			// Its weight will be its number of log appearances
			int weight = mapTrendLogs.get(t.getId()).size();
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
