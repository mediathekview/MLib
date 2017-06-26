package de.mediathekview.mlib.tool;

public class Ram {
	
	private long totalram;
	private long maxram;
	private long freeram;
	
	
	public Ram(long totalram, long maxram, long freeram) {
		this.totalram = totalram;
		this.maxram = maxram;
		this.freeram = freeram;
	}


	public long getTotalram() {
		return totalram;
	}


	public long getMaxram() {
		return maxram;
	}


	public long getFreeram() {
		return freeram;
	}
	
	public long getTotalramMB() {
		return totalram / (1024L * 1024L);
	}


	public long getMaxramMB() {
		return maxram / (1024L * 1024L);
	}


	public long getFreeramMB() {
		return freeram / (1024L * 1024L);
	}
	
	
	@Override
	public String toString() {
		return totalram+"/"+maxram+" Frei: "+freeram;
	}

}
