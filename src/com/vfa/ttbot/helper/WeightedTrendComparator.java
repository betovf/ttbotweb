package com.vfa.ttbot.helper;

import java.io.Serializable;
import java.util.Comparator;

import com.vfa.ttbot.model.WeightedTrend;

@SuppressWarnings("serial")
public class WeightedTrendComparator implements Comparator<WeightedTrend>, Serializable {

	TrendComparator trendComparator = new TrendComparator();
	
	@Override
	public int compare(WeightedTrend o1, WeightedTrend o2) {
		// Compare by weight, higher values go first
		int diff = o2.getWeight() - o1.getWeight();
		if (diff == 0) {
			// If equal, compare by name
			diff = this.trendComparator.compare(o1, o2);
		}
		return diff;
	}

}
