package indiana.cgl.hadoop.pagerank;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;

/**
 * This class has to be implemented by the student.
 */
public class PageRankReducer extends Reducer<LongWritable, Text, LongWritable, Text> {

    /**
     * Implement this. For a particular key you will get an array containing text values. Each text value can be one of two formats.
     * 1. A single rank value contributed to this url key from other pages
     * 2. The list of out urls along with the rank. i.e. sourceUrl\trankValue#targetUrlsList.
     * @param key source url
     * @param values values are describes above
     * @param context hadoop context
     * @throws IOException if an error occurs
     * @throws InterruptedException if an error occurs
     */

		
    public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		System.out.println("["+key+"] Reducing...");
		Configuration conf = context.getConfiguration();
		
		//Values to be used in while loop
		double rank = 0.0;
		RankRecord record = null;

		while(values.iterator().hasNext()){
			String row = values.iterator().next().toString();  
			
			//Assigning dangling sum 
			/*if(key.equals(new LongWritable(-1))) {
				danglingSum += Double.parseDouble(row); 
			} else if(row.contains("\t")){
				record = new RankRecord(row);
			} else {
				rank += Double.parseDouble(row);
			} */
			if(row.contains("\t")){
				record = new RankRecord(row);
			} else {
				rank += Double.parseDouble(row);
			}	
				
		}	
		
		if(record != null) {
			rank = .15/(double) conf.getInt("numUrls", 1) + .85 * rank;
			//rank = .15/(double) conf.getInt("numUrls", 1) + .85 * (rank + danglingSum);
			String value = String.valueOf(rank);
			for(Integer outlink : record.targetUrlsList) {
				value += "#" + outlink;
			}
			context.write(new LongWritable(record.sourceUrl), new Text(value));
		}
		
		
    }
}

	
