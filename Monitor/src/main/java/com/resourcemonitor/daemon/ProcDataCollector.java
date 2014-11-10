package com.resourcemonitor.daemon;

import com.resourcemonitor.common.MonitorBroker;
import com.resourcemonitor.common.MonitorData;
import com.resourcemonitor.common.ReceiveHandler;
import org.hyperic.sigar.*;
import org.hyperic.sigar.ptql.*;
import org.hyperic.sigar.CpuInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.text.DecimalFormat;
/**
 * Worker thread per node that sends
 * cpu and mem usage data.
 */
public class ProcDataCollector extends AbstractDataCollector {
	
	private long procIds[];
	String query;
	long sampleTime;
	int workerId;

    /**
     * Constructor to create a process data collector
     * @param workerId daemon id
     * @param broker broker
     * @param sampleTime sampling time
     */
	public ProcDataCollector(int workerId, MonitorBroker broker, long sampleTime) {
		super(workerId, broker, sampleTime);
		this.sampleTime = sampleTime;
		query = broker.getConfiguration().getPTQL();
		this.workerId = workerId;
        Thread q = new Thread(new QueryListener());
        q.start();
	}

    
    private class QueryListener implements Runnable {
		@Override
		public void run(){
			System.out.println("Setting up PTQL receiver");
			broker.send("[HEARTBEAT] "+workerId);
			broker.startReceive(new ReceiveHandler() {
            public void onMessage(String text) {
				if(text.indexOf("[PTQL]") == 0){
					query = text.substring(6);
					System.out.println("Received new query: " + query);
				}
				else if(text.indexOf("[HEARTBEAT]") == 0){
					String[] data = text.split(" ");
					System.out.println("Received heartbeat request");
					if(data.length == 2){
						System.out.println("Heartbeat request: " + text + " @ "+workerId);
						if(Integer.parseInt(data[1]) != workerId){
							broker.send("[PTQL]"+query);
						}
					}
					else{
						broker.send("[PTQL]"+query);
					}
				}
            }
			});
		}
	}

    /**
     * Get process data using Sigar APIs. This function will be called time to time. This time is configured by
     * sample.time property.
     * The student needs to get the machine CPU usage and Memory usage using Sigar APIs,
     * create a MonitorData object and return
     *
     * If the procIds are not null go through them and collect data for these process IDs
     *
     * Please look at the <code>AbstractDataCollector</code> to get the sigar instance etc.
     *
     * @return a monitor data
     */
	public MonitorData getMonitorData() {
        MonitorData message = new MonitorData();
		MultiProcCpu procCpu = new MultiProcCpu();
		ProcMem procMem = null;
		
		double cpu = 0, cpuPerc = 0;
		long mem = 0, elapsedTime = 0;
		
        long list[] = null;
		
		try {
			org.hyperic.sigar.ptql.ProcessFinder processfinder = new org.hyperic.sigar.ptql.ProcessFinder(sigar);
			list = processfinder.find(query);
			System.out.println(query+ " :: " +Arrays.toString(list));
		}
		catch (Exception e) {
			System.out.println("Faulty query: "+query);
			e.printStackTrace();
		}
		
		this.procIds = list;
		
        if (this.procIds == null) {
            return null;
        }
        long mpiProcIds[] = this.procIds;
        /* implement your code here
        1. Get processes’ CPU and Mem using Sigar APIs. This function will be called time to time. This time is configured by sample.time in the monitor.properties.
        2. If the mpiProcIds are not null, go through them and collect performance data for these process IDs
        3. Calculate average CPU and Mem usage on current node
        */
			int live_pids = 0;
		try{
			int processors = Runtime.getRuntime().availableProcessors();
			
			for(long pid : mpiProcIds) {
				try {
					//Summation of memory utilization per process (in Mb)
					procMem = sigar.getProcMem(pid);
					mem += procMem.getResident();
	
					procCpu.gather(sigar, pid);
					cpu += procCpu.getPercent();
					live_pids++;
				}
				catch(Exception e){
					System.out.println("Exception: " + e.getMessage());
				}
			}
			//Calculating average cpu and memory, setting message fields
			
			message.setCpu(0);
			message.setMemory(0);
			message.setWorkerId(workerId);
			message.setTime((long)System.currentTimeMillis());
			message.setProcess(true);
			
			if(live_pids > 0){
				message.setCpu(cpu/((double)live_pids * processors));
				message.setMemory(mem / (1024 * 1024));
				message.setWorkerId(workerId);
				message.setTime((long) System.currentTimeMillis());
				message.setProcess(true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
        return message;
    }


    public long[] getProcIds() {
        long list[] = null;
		try {
			org.hyperic.sigar.ptql.ProcessFinder processfinder = new org.hyperic.sigar.ptql.ProcessFinder(sigar);
			list = processfinder.find(query);
			System.out.println(query+ " :: " +Arrays.toString(list));
		}
		catch (Exception e) {
			System.out.println("Faulty query: "+query);
			e.printStackTrace();
		}
        return list;
    }
}
