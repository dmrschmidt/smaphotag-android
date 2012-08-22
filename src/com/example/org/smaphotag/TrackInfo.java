package com.example.org.smaphotag;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackInfo {

	private String name;
	private Date start;
	private Date stop;
	
	private SimpleDateFormat dateformat;
	 
	
	public TrackInfo() {
		dateformat=new SimpleDateFormat("yyyyMMdd");
	}
	
	public TrackInfo(String _name,Date _start,Date _stop) {
		this();
		start=_start;
		stop=_stop;
		name=_name;
	}
	
	
	public String getStartAsString() {
		return dateformat.format(start);
	}
	

	public String getStopString() {
		return dateformat.format(stop);
	}
	
	public String getName() {
		return name;
	}
	
}