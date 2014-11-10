import org.hyperic.sigar.ptql.ProcessFinder;
import org.hyperic.sigar.ptql.ProcessQuery;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.ThreadCpu;
import java.text.DecimalFormat;

class SigarTester {
    static String formatNumber(double input){
	DecimalFormat DF = new DecimalFormat("#.#####");
	DF.setMinimumFractionDigits(5);
	return DF.format(input);
    }

	public static void main(String[] args) {
		Sigar sigar = new Sigar();
		ProcessFinder processfinder = new ProcessFinder(sigar);
		ProcCpu procCpu = new ProcCpu();
		ProcMem procMem = null;
		long mem = 0;
		double cpu = 0;
		long startTime = 0;
		long endTime = 0;
		long cpuTime = 0;
		long elapsedTime = 0;

		try {
		    long[] pids = processfinder.find(sigar, "CredName.User.eq=ivmalopi");
		    System.out.println("Got the pids? " + pids);
			for(long pid : pids) {
			    procCpu.gather(sigar, pid);
			    
				procMem = sigar.getProcMem(pid);
				mem = procMem.getRss();
				cpu = procCpu.getPercent();
				startTime = procCpu.getStartTime();
				endTime = procCpu.getLastTime();
				cpuTime = procCpu.getTotal();
				elapsedTime = endTime - startTime;
				System.out.println("StartTime for pid " + pid + " = " + startTime);
				System.out.println("EndTime for pid " + pid + " = " + endTime);
				System.out.println("CpuPercentage: " + formatNumber((double) cpuTime / (double) elapsedTime));
			}
			
		
		}
		catch(Exception e) {

		    System.out.println(e.getMessage());
		}
	}

}