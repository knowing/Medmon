package de.lmu.ifi.dbs.medmon.sensor.core.container;

import java.util.Date;

public interface IBlock {

	long getBegin();
	long getEnd();
	
	Date getFrom();
	Date getTo();
	
	String getPath();
	
}
