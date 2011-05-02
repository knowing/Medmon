package de.lmu.ifi.dbs.medmon.sensor.core.container;

import java.util.Date;

public class Block implements IBlock{

	private final long begin;
	private final long end;
	
	private final Date from;
	private final Date to;
	
	private String path;
	
	public Block(long begin, long end, Date from, Date to, String path) {
		this.begin = begin;
		this.end = end;
		this.from = from;
		this.to = to;
		this.path = path;
	}
	
	public Block(long begin, long end, Date from, Date to) {
		this(begin, end, from, to, System.getProperty("user.home"));
	}
	
	public Block(long end, Date from, Date to) {
		this(0, end, from, to, System.getProperty("user.home"));
	}

	@Override
	public long getBegin() {
		return begin;
	}

	@Override
	public long getEnd() {
		return end;
	}

	@Override
	public Date getFrom() {
		return from;
	}

	@Override
	public Date getTo() {
		return to;
	}

	@Override
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return "Block [begin=" + begin + ", end=" + end + ", from=" + from + ", to=" + to + ", path=" + path + "]";
	}
}
