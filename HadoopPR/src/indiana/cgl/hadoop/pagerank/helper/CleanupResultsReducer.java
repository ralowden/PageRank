package indiana.cgl.hadoop.pagerank.helper;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * We are going to calculate the sum here
 */
public class CleanupResultsReducer extends Reducer<LongWritable, Text, LongWritable, Text> {
    // keep the sum
    private double sum = 0;

    public void reduce(LongWritable key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {
        // we are going to keep the sum
        Text val = values.iterator().next();
        sum += Double.parseDouble(val.toString());
        // just output what we get
        context.write(key, val);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        // print the sum at the end
        System.out.println("Total sum: " + sum);
    }
}
