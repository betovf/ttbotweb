package com.vfa.ttbot.service;

import java.io.IOException;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.vfa.ttbot.dao.TrendLogMapper;
import com.vfa.ttbot.dao.TrendMapper;
import com.vfa.ttbot.model.Trend;
import com.vfa.ttbot.model.TrendLog;

public class DBDataService implements IDataService {

	private SqlSessionFactory sqlSessionFactory;  
	
	public DBDataService() {
		String resource = "mybatis-config.xml";
		Reader reader;
		try {
			reader = Resources.getResourceAsReader(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			// 
			e.printStackTrace();
		}
	}
	

	@Override
	public List<TrendLog> getTrendLogsByDate(Date ini, Date end) {
		SqlSession session = sqlSessionFactory.openSession();

		List<TrendLog> listTrendLogs = null;

		try{
			TrendLogMapper mapper = session.getMapper(TrendLogMapper.class);
			listTrendLogs = mapper.searchTrendLogsByDate(ini, end);
		} catch (Exception e) {
			// 
			e.printStackTrace();
		} finally {
			session.close();
		}	
		return listTrendLogs;
	}

	@Override
	public Trend getTrend(int id) {
		SqlSession session = sqlSessionFactory.openSession();
		
		Trend trend = null;
		
		try {
			TrendMapper mapper = session.getMapper(TrendMapper.class);
			trend = mapper.getTrendById(id);
		} catch (Exception e) {
			// 
			e.printStackTrace();
		} finally {
			session.close();
		}	

		return trend;
	}

}
