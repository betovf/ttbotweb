package com.vfa.ttbot.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

public class LinkHelper {
	private static final String TWITTER_URL="https://twitter.com/";
	private static final String TWITTER_SEARCH="search?q=%s";
	private static final String TWITTER_HASHTAG="hashtag/%s";
	
	public static String getTwitterUrlForTrend(String trend) {
		String url = null;
		
		// Check string not empty
		if (StringUtils.isNotEmpty(trend)) {
			// Distinguish between hastag and search
			if('#' == trend.charAt(0)) {
				// Twitter url for hashtags. Remove initial '#' (no need to encode)
				url = String.format(TWITTER_URL.concat(TWITTER_HASHTAG), trend.substring(1));
			} else {
				try {
					// Twitter url for searchs. Search term needs encoding
					url = String.format(TWITTER_URL.concat(TWITTER_SEARCH), URLEncoder.encode(trend, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// Just for debug. Will return null.
					e.printStackTrace();
				}
			}
		}
		return url;
	}
}
