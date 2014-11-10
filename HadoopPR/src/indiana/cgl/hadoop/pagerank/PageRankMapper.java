package indiana.cgl.hadoop.pagerank;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;


import java.io.IOException;
import indiana.cgl.hadoop.pagerank.RankRecord;

/**
 * This class has to be implemented by the student.
 */
public class PageRankMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
    /**
     * The key has to be ignored in this one. The value is the output from the GraphMapper/Reducer
     * each line by line.
     *
     * @param key key is ignored in the computation
     * @param value the text value from the mapper. it is of the form sourceUrl\trankValue#targetUrlsList.
     * @param context hadoop context
     * @throws IOException if an error occurs
     * @throws InterruptedException if an error occurs
     */
	
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		System.out.println("["+key+"] Mapping " + value.toString());
		RankRecord page = new RankRecord(value.toString());
		Configuration conf = context.getConfiguration(); 
		double numOutbounds = (double) page.targetUrlsList.size();	
	
		if(numOutbounds == 0.0) {
			int totalUrls = conf.getInt("numUrls", 1);
			for(int i = 0; i < totalUrls; i++) {
				context.write(new LongWritable(i), new Text(String.valueOf(page.rankValue/(double) totalUrls)));				
			}
			
			//context.write(new LongWritable(-1), new Text(String.valueOf(page.rankValue/(double) conf.getInt("numUrls", 1))));
		//Regular Node
		} else {			
			for(Integer link : page.targetUrlsList){
				String newVal = String.valueOf(page.rankValue/numOutbounds);
				context.write(new LongWritable(link), new Text(newVal));
			}
		}	
		context.write(new LongWritable(page.sourceUrl), value);
    }
}
