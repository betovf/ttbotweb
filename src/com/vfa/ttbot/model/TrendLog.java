package com.vfa.ttbot.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class TrendLog implements Serializable {

	private Integer id;
	private Integer idTrend;
	private Date dateTime;
	private Integer position;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIdTrend() {
		return idTrend;
	}
	public void setIdTrend(Integer idTrend) {
		this.idTrend = idTrend;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	
}
