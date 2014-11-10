package com.resourcemonitor.common;
import java.text.DecimalFormat;

/**
 * The monitor data coming from process and node
 */
public class MonitorData {
    // cpu
    private double cpu;
    // memory
    private double memory;
    // daemon id
    private int workerId;
    // timestamp
    private long time;
    // weather these are process data or cpu data
    private boolean process = false;
	
	String formatNumber(double input){
		DecimalFormat DF = new DecimalFormat("#.#####");
		DF.setMinimumFractionDigits(5);
		return DF.format(input);
	}
	
	public void parseString(String row){
		String elements[] = row.split(",");
		this.setMemory(Double.parseDouble(elements[0]));
		this.setCpu(Double.parseDouble(elements[1]));
		this.setWorkerId(Integer.parseInt(elements[2]));
		this.setTime(Long.parseLong(elements[3]));
		this.setProcess("1".equals(elements[4]));
	}

    public double getCpu() {
        return cpu;
    }

    public void setCpu(double cpu) {
        this.cpu = cpu;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }

    public boolean isProcess() {
        return process;
    }

    public void setProcess(boolean processId) {
        this.process = processId;
    }
	
	public String toString() {
		return 	
			this.formatNumber(this.getMemory()) + "," +
			this.formatNumber(this.getCpu()) + "," +
			this.getWorkerId() + "," +
				String.valueOf(this.getTime()) + "," +
					(this.isProcess() ? "1" : "0");
	}
}
