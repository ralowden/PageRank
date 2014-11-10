package indiana.cgl.hadoop.pagerank.helper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * Create a graph structure suitable for our computation.
 */
public class GraphMapper extends Mapper<LongWritable, Text, LongWritable, Text> {

    /**
     * We will get each line of the input file as the value and the key will be the line number.
     * We cannot use this as it is in the computation. So we are going to transform the line in to
     * this format "sourceUrl\trankValue#targetUrlsList" Note there is a \t in between  sourceUrl and rankValue.
     * Actually this reducer is going to emit sourceUrl as the key and rankValue#targetUrlsListseparatedby# as the value.
     * So in the output file it will look like  sourceUrl\trankValue#targetUrlsList
     *
     * @param key line number which we are going to ignore
     * @param value the text line
     * @param context hadoop context
     * @throws IOException if an error occurs
     * @throws InterruptedException if an error occurs
     */
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        int numUrls = context.getConfiguration().getInt("numUrls", 1);
        double val = 1.0 / (double) numUrls;

        String[] strArray = value.toString().split(" ");
        StringBuilder sb = new StringBuilder();

        int sourceUrl, targetUrl;
        // source url
        sourceUrl = Integer.parseInt(strArray[0]);
        // put the initial rank
        sb.append(String.valueOf(val));

        // append the rest of the outbound links
        for (int i = 1; i < strArray.length; i++) {
            targetUrl = Integer.parseInt(strArray[i]);
            sb.append("#").append(targetUrl);
        }

        // send it to next phase
        context.write(new LongWritable(sourceUrl), new Text(sb.toString()));
    }
}
