package indiana.cgl.hadoop.pagerank.helper;

import indiana.cgl.hadoop.pagerank.RankRecord;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Ignore the targetUrlList from the input and just output the rankValue for the URL. This will
 * make the final output look like "sourceUrl rankValue" in each line
 */
public class CleanupResultsMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String strLine = value.toString();
        RankRecord rrd = new RankRecord(strLine);
        context.write(new LongWritable(rrd.sourceUrl), new Text(String.valueOf(rrd.rankValue)));
    }
}
