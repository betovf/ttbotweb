package com.vfa.ttbot.helper;

import java.io.Serializable;
import java.util.Comparator;

import com.vfa.ttbot.model.Trend;

@SuppressWarnings("serial")
public class TrendComparator implements Comparator<Trend>, Serializable {
	
	@Override
	public int compare(Trend arg0, Trend arg1) {
		// Compare names
		return arg0.getName().compareToIgnoreCase(arg1.getName());
	}				

}
