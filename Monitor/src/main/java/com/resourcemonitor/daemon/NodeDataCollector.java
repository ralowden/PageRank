package com.resourcemonitor.daemon;

import com.resourcemonitor.common.MonitorBroker;
import com.resourcemonitor.common.MonitorData;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.SigarException;


/**
 * Worker thread per node that sends
 * cpu and mem usage data.
 */
public class NodeDataCollector extends AbstractDataCollector {
    /**
     * Constructor
     * @param workerId daemon id
     * @param broker broker
     * @param sampleTime sampling time
     */
    public NodeDataCollector(int workerId, MonitorBroker broker, long sampleTime) {
        super(workerId, broker, sampleTime);
    }

    /**
     * Get machine data using Sigar APIs. This function will be called time to time. This time is configured by
     * sample.time property.
     *
     * The student needs to get the machine CPU usage and Memory usage using Sigar APIs,
     * create a MonitorData object and return.
     *
     * The Sigar instance is defined in <code>AbstractDataCollector</code>.
     *
     * @return a monitor data
     */
    public MonitorData getMonitorData() {
        MonitorData message = new MonitorData();
        Mem mem = null;
        CpuPerc cpuPerc = null;
        /*  implement your code here
	Get machine data using Sigar APIs. This function will be called time to time. This time is configured by sample.time in the monitor.properties.
        */
			try{
				mem = sigar.getMem();
				cpuPerc = sigar.getCpuPerc();
				message.setCpu(cpuPerc.getCombined());
				message.setMemory(mem.getUsedPercent()/100.0);
				message.setWorkerId(workerId);
				long unixTime = (long) System.currentTimeMillis();
				message.setTime(unixTime);
				}
			catch (SigarException e) {
				e.printStackTrace();
			}

        return message;

    }
}
