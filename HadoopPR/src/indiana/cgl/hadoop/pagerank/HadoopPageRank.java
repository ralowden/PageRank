package indiana.cgl.hadoop.pagerank;

import indiana.cgl.hadoop.pagerank.helper.CleanupResultsMapper;
import indiana.cgl.hadoop.pagerank.helper.CleanupResultsReducer;
import indiana.cgl.hadoop.pagerank.helper.GraphMapper;
import indiana.cgl.hadoop.pagerank.helper.GraphReducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * This is the driver class that runs the map reduce jobs to calculate the PageRanks.
 * First it will create a
 */
public class HadoopPageRank extends Configured implements Tool {
    private String inputDir;
    private String outputDir;
    private int numUrls;
    private int numIterations;

    // Run the pagerank on hadoop
    public static void main(String[] args) throws Exception {
        System.out.println("*********************************************");
        System.out.println("*           Hadoop PageRank                 *");
        System.out.println("*********************************************");

        ToolRunner.run(new Configuration(), new HadoopPageRank(), args);
    }

    /**
     * This method will be called when this class runs
     * @param args the command line arguments
     * @return the exit code
     * @throws Exception if an error occurs
     */
    public int run(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        // parse the arguments
        parseArgs(args);
        // get the configuration for use later
        Configuration configuration = getConf();

        // set the number of urls
        configuration.setInt("numUrls", numUrls);

        // the first job is to format the input into a consistent format for the mappers
        Job graphCreateJob = new Job(configuration, "CreateGraph");

        // standard set up of the mappers / reducers etc
        FileSystem fs = FileSystem.get(configuration);
        graphCreateJob.setJarByClass(HadoopPageRank.class);
        graphCreateJob.setMapperClass(GraphMapper.class);
        graphCreateJob.setReducerClass(GraphReducer.class);
        graphCreateJob.setOutputKeyClass(LongWritable.class);
        graphCreateJob.setOutputValueClass(Text.class);

        // in each iteration we are going to put the output of the map reduce into a
        // directory with the name as the iteration number
        int outputIndex = 0;

        // input path for the first job
        FileInputFormat.setInputPaths(graphCreateJob, new Path(inputDir));
        FileOutputFormat.setOutputPath(graphCreateJob, new Path(String.valueOf(outputIndex)));

        // we don't need a reducer for this job
        graphCreateJob.setNumReduceTasks(1);

        // run the job and wait for it....
        graphCreateJob.waitForCompletion(true);
        // if failed exit
        if (!graphCreateJob.isSuccessful()) {
            System.out.println("Hadoop CreateGraph failed, exit...");
            System.exit(-1);
        }
	
        System.out.println("Starting PageRank calculation...\n");

        // core computation that require multiple iterations to make rank values results converge
        // we are going to put the output of one iteration to a folder and get this output as
        // input to the next iteration
        for (int i = 0; i < numIterations; i++) {
            System.out.println("Hadoop PageRank iteration " + i + "...\n");
            // standard stuff for the iteration job configuration
            Job iterationJob = new Job(getConf(), "HadoopPageRank");
            iterationJob.setJarByClass(HadoopPageRank.class);
            iterationJob.setMapperClass(PageRankMapper.class);
            iterationJob.setReducerClass(PageRankReducer.class);

            iterationJob.setOutputKeyClass(LongWritable.class);
            iterationJob.setOutputValueClass(Text.class);

            //the output in the current iteration will become input in next iteration.
            FileInputFormat.setInputPaths(iterationJob, new Path(String.valueOf(outputIndex)));
            FileOutputFormat.setOutputPath(iterationJob, new Path(String.valueOf(outputIndex + 1)));

            // we are going to use 10 reducers, you can use any number but don't go too big
            iterationJob.setNumReduceTasks(10);
            // wait for it....
            iterationJob.waitForCompletion(true);
            if (!iterationJob.isSuccessful()) {
                System.out.format("Hadoop PageRank iteration:{" + i + "} failed, exit...", i);
                System.exit(-1);
            }

            //clean the intermediate data directories.
            fs.delete(new  Path(String.valueOf(outputIndex)), true);
            outputIndex++;
        }

        System.out.println("Hadoop CleanUptResults starts...\n");

        // we have the intermediate results in a wierd format. So make it look presentable
        // the format is sourceUrl\trankValue#targetUrlsList so make it "sourceUrl rank"
        Job cleanUptResults = new Job(configuration, "CleanUptResults");

        // standard stuff
        cleanUptResults.setJarByClass(HadoopPageRank.class);
        cleanUptResults.setMapperClass(CleanupResultsMapper.class);
        cleanUptResults.setReducerClass(CleanupResultsReducer.class);
        cleanUptResults.setOutputKeyClass(LongWritable.class);
        cleanUptResults.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(cleanUptResults, new Path(String.valueOf(outputIndex)));
        FileOutputFormat.setOutputPath(cleanUptResults, new Path(String.valueOf(outputDir)));

        // we are going to use 1 reducer because we need to calculate the total sum
        cleanUptResults.setNumReduceTasks(1);

        // wait for it....
        cleanUptResults.waitForCompletion(false);
        if (!cleanUptResults.isSuccessful()){
            System.out.println("Hadoop CleanUptResults failed, exit...");
            System.exit(-1);
        }

        // delete the last output from iterations
        fs.delete(new Path(String.valueOf(outputIndex)), true);

        double executionTime = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("Hadoop PageRank Job took " + executionTime + " sec.");

        return 0;
    }

    /**
     * Parse the arguments to the program.
     * The arguments are inputDir outputDir numIterations numUrls
     * @param args list of command line arguments
     */
    public void parseArgs(String[] args) {
        if (args.length != 4) {
            String errorReport = "Usage:: \n"
                    + "hadoop jar HadoopPageRank.jar "
                    + "[inputDir][outputDir][numUrls][maximum loop count]\n";

            System.out.println(errorReport);
            System.exit(-1);
        }

        inputDir = args[0];
        outputDir = args[1];
        numUrls = Integer.parseInt(args[2]);
        numIterations = Integer.parseInt(args[3]);

        System.out.println(numIterations);
        System.out.println(numUrls);
    }

}
