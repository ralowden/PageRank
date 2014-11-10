package com.resourcemonitor.client;

import com.resourcemonitor.common.MonitorData;

import java.util.*;

/**
 * Create averages from the values and update the model
 */
public class Aggregator {
	// the raw message collection
	private DataCollection dataCollection;

	// this is the model we are going to render
	private GraphModel summary;

	// the time between two calculations
	private long window;
	
	double clamp(double min, double max, double val){
		return Math.max(min, Math.min(val, max));
	}

	/**
	 * Create an aggregator
	 * 
	 * @param dataCollection
	 *            row data coming from the broker
	 * @param summary
	 *            summary view
	 * @param window
	 *            the size of the window
	 */
	public Aggregator(DataCollection dataCollection, GraphModel summary,
			long window) {
		this.dataCollection = dataCollection;
		this.summary = summary;
		this.window = window;
	}

	/**
	 * Run through the messages in data collection and create a summary from
	 * this data. This method is going to get called every milliseconds
	 * configured by "aggregate.time" property. The middle of the current window
	 * can be found by "summary.getLastTime() + window". Student can calculate
	 * the average for each of the values you need to display in this window by
	 * going through the raw data collection.
	 * 
	 * Make sure you delete the data from the row data collection that are
	 * already processed or that are below the current window. If they are node
	 * deleted the data collection can grow.
	 * 
	 * After the aggregate values are calculated you should add these values to
	 * the summary.
	 * 
	 * In this method you may need to calculate values for multiple windows to
	 * compensate for the timing delays.
	 * 
	 * ********* Student should implement this method ************
	 */
	public void generate() {
		Map<Integer, ArrayList<MonitorData>> nodeMessageMap = dataCollection.getNodes();
		Map<Integer, ArrayList<MonitorData>> procMessageMap = dataCollection.getProcs();
		long time = 0, time_node = 0, time_proc = 0;				
		double cpu, mem, procCpu, procMem;
		int node_total, proc_total;
		
		do {
			synchronized (this) {
				
				HashMap<Integer, Double> node_multipliers = new HashMap<Integer, Double>();
				HashMap<Integer, Double> proc_multipliers = new HashMap<Integer, Double>();
				HashMap<Integer, double[]> node_sums = new HashMap<Integer, double[]>();
				HashMap<Integer, double[]> proc_sums = new HashMap<Integer, double[]>();

				//Node count total
				
				int nodeCount = 0;

				//Get node data
				
				time_node = 0;
				node_total = 0;
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : nodeMessageMap.entrySet()) {
					ArrayList<MonitorData> list = entry.getValue();
					node_multipliers.put(entry.getKey(), (double) list.size());
					node_total += list.size();
					if(list.size() > 0){
						nodeCount++;
					}
				}
				
				summary.nodeCount = nodeCount;
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : nodeMessageMap.entrySet()) {
					node_multipliers.put(entry.getKey(), 
					node_multipliers.get(entry.getKey()) / (double) node_total);
				}
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : nodeMessageMap.entrySet()) {
					ArrayList<MonitorData> list = entry.getValue();
					double vals[] = new double[2];
					
					for (int i = 0; i < list.size(); i++) {
						MonitorData md = (MonitorData) list.get(i);
						if (md == null) break;
						vals[0] += md.getCpu();
						vals[1] += md.getMemory();
						time_node = Math.max(time, md.getTime());
					}
					
					if(list.size() > 0){
						vals[0] /= (double)list.size();
						vals[1] /= (double)list.size();
					}
					
					node_sums.put(entry.getKey(), vals);
					nodeMessageMap.put(entry.getKey(), new ArrayList<MonitorData>());
				}
				
				//Get proc data
				
				time_proc = 0;
				proc_total = 0;
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : procMessageMap.entrySet()) {
					ArrayList<MonitorData> list = entry.getValue();
					proc_multipliers.put(entry.getKey(), (double) list.size());
					proc_total += list.size();
				}
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : procMessageMap.entrySet()) {
					proc_multipliers.put(entry.getKey(), 
					proc_multipliers.get(entry.getKey()) / (double) proc_total);
				}
				
				for (Map.Entry<Integer, ArrayList<MonitorData>> entry : procMessageMap.entrySet()) {
					ArrayList<MonitorData> list = entry.getValue();
					double vals[] = new double[2];
					
					for (int i = 0; i < list.size(); i++) {
						MonitorData md = (MonitorData) list.get(i);
						if (md == null) break;
						System.out.println(md);
						vals[0] += md.getCpu();
						vals[1] += md.getMemory();
						time_proc = Math.max(time, md.getTime());
					}
					
					if(list.size() > 0){
						vals[0] /= (double)list.size();
						vals[1] /= (double)list.size();
					}
					
					proc_sums.put(entry.getKey(), vals);
					procMessageMap.put(entry.getKey(), new ArrayList<MonitorData>());
				}
			
			cpu = 0;
			mem = 0;
			
			for(Map.Entry<Integer, double[]> entry: node_sums.entrySet()){
				double multiplier = node_multipliers.get(entry.getKey());
				double vals[] = entry.getValue();
				vals[0] *= multiplier;
				vals[1] *= multiplier;
				cpu += vals[0];
				mem += vals[1];
			}
			
			procCpu = 0;
			procMem = 0;
			
			for(Map.Entry<Integer, double[]> entry : proc_sums.entrySet()){
				double multiplier = proc_multipliers.get(entry.getKey());
				double vals[] = entry.getValue();
				//System.out.println(Arrays.toString(vals));
				vals[0] *= multiplier;
				//Do not multiply memory
				//vals[1] *= multiplier;
				procCpu += vals[0];
				procMem += vals[1];
				
			}
			
			cpu = clamp(0.0, 1.0, cpu);
			mem = clamp(0.0, 1.0, mem);
			procCpu = clamp(0.0, 1.0, procCpu);
			procMem = clamp(0.0, Double.MAX_VALUE, procMem);
			int lag = 1000;
			time = Math.max(Math.max(time_proc, time_node) + lag, System.currentTimeMillis());
						
			summary.add(cpu, mem, procCpu, procMem, time);
			
			}
		} while (time < System.currentTimeMillis() - window);
		//System.out.println("Stopped aggregating at "+time);
	}
}
