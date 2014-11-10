import java.io.*;
import java.util.*;

public class SequentialPageRank {
    // adjacency matrix read from file
    private HashMap<Integer, ArrayList<Integer>> adjMatrix = new HashMap<Integer, ArrayList<Integer>>();
    // input file name
    private String inputFile = "";
    // output file name
    private String outputFile = "";
    // number of iterations
    private int iterations = 10;
    // damping factor
    private double df = 0.85;
    // number of URLs
    private int size = 0;
    // calculating rank values
    private HashMap<Integer, Double> rankValues = new HashMap<Integer, Double>();
    //storing urls of out bounds
    private HashMap<Integer, ArrayList<Integer>> outbounds= new HashMap<Integer, ArrayList<Integer>>();
 

    /**
     * Parse the command line arguments and update the instance variables. Command line arguments are of the form
     * <input_file_name> <output_file_name> <num_iters> <damp_factor>
     *
     * @param args arguments
     */
    public void parseArgs(String[] args) {
    	if(args.length == 4) {
    		inputFile = args[0];
    		outputFile = args[1];
    		iterations = Math.max(1, Integer.parseInt(args[2]));
    		df = Math.max(.01, Math.min(.99, Double.parseDouble(args[3])));
    	} else {
    		System.out.println("Incorrect number of arguments. Need <inputFile> <outputFile> <num_iters> <damp_factor>.");
    		System.exit(0);
    	}
    }

    /**
     * Read the input from the file and populate the adjacency matrix
     *
     * The input is of type
     *
     0
     1 2
     2 1
     3 0 1
     4 1 3 5
     5 1 4
     6 1 4
     7 1 4
     8 1 4
     9 4
     10 4
     * The first value in each line is a URL. Each value after the first value is the URLs referred by the first URL.
     * For example the page represented by the 0 URL doesn't refer any other URL. Page
     * represented by 1 refer the URL 2.
     *
     * @throws java.io.IOException if an error occurs
     */
    public void loadInput() throws IOException { 
    	Scanner s = null;
    	try {
    		s = new Scanner(new BufferedReader(new FileReader(inputFile)));
    		ArrayList<Integer> values;
    		String[] splits;
    		String line = "";
    		
    		while(s.hasNext()) {
    			line = s.nextLine();
    			values = new ArrayList<Integer>();
    			splits = line.split("\\s+");
    			
   				for(int i = 0; i < splits.length; i++) { //filling outbounds
   					if(i == 0) {
    					outbounds.put(Integer.parseInt(splits[i]), values);	
    					adjMatrix.put(Integer.parseInt(splits[i]), new ArrayList<Integer>());
    				}
    				else {
   						values.add(Integer.parseInt(splits[i]));
   					}
   				} 
   			} 
    		size = adjMatrix.size();
    		//filling adjMatrix
    		ArrayList<Integer> outboundsArray, inboundsArray;
    		for(int i = 0; i < size; i++) { 
    			outboundsArray = outbounds.get(i);
    			if(outboundsArray.size() == 0) {
    				for(int j = 0; j < size; j++) { //url of adjMatrix
    					if(j != i) {
    						inboundsArray = adjMatrix.get(j);
    						inboundsArray.add(i);
    					}
    				} 
    			} else {
    				for(int j = 0; j < outboundsArray.size(); j++) {
    					inboundsArray = adjMatrix.get(outboundsArray.get(j));
    					inboundsArray.add(i);
    				}
    			}
    		}
    	} catch(IOException e) {
    		System.out.println("IOException: " + e.getMessage());
    	}
    	
    	finally {
    		if(s != null) s.close();
    	}
    }

    /**
     * Do fixed number of iterations and calculate the page rank values. You may keep the
     * intermediate page rank values in a hash table.
     */
    public void calculatePageRank() {
    	double N = adjMatrix.size();
    	double PRv, Lv, sum;
    	HashMap<Integer, Double> interValues = new HashMap<Integer, Double>();
    	//initial assignment of values 
    	for(int i = 0; i < N; i++) { //iterating through adjMatrix
    		double startValue = 1.0/N;
    		interValues.put(i, startValue);
    	}
    
    	//Iterative loop
    	for(int iters = 0; iters < iterations; iters++) {
    		ArrayList<Integer> inboundArray;
    		ArrayList<Integer> outboundArray; 
    		for(int i = 0; i < size; i++) { //going through each url
    			sum = 0;
    			inboundArray = adjMatrix.get(i);
    			for(int j = 0; j < inboundArray.size(); j++) { //going through each outbound link
    				outboundArray = outbounds.get(inboundArray.get(j));
    				PRv = interValues.get(inboundArray.get(j));
    				Lv = outboundArray.size();
    				if(Lv == 0) Lv = N-1;
    				sum += PRv/Lv;
    			} 
    			rankValues.put(i, (1-df)/N + df * sum);
    		}
    		interValues.putAll(rankValues);
    		rankValues.clear(); //don't clear rankValues if last iteration
    	} 
    	rankValues.putAll(interValues);
    }
    

    /**
     * Print the pagerank values. Before printing you should sort them according to decreasing order.
     * Print all the values to the output file. Print only the first 10 values to console.
     *
     * @throws IOException if an error occurs
     */
    public void printValues() throws IOException {
    	ArrayList<double[]> rVList = hashMapToArrayList(rankValues);
    	ArrayList<double[]> combined = mergeSort(rVList);
    	
    	String str = print(combined);
    	
    	try {
    		File file = new File(outputFile);
    		if (!file.exists()) file.createNewFile();
    		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    		writer.write(str);
    		writer.close();
    	} catch(IOException e) {
    		System.out.println("IOException: " + e.getMessage());
    	} 
    	
    	System.out.println("Number of iterations: " + iterations);
    	for(int i = 0; i < 10; i++) {
    		System.out.println((int) combined.get(i)[1] + "\t" + combined.get(i)[0]);
    	}  	
    }
     
    private ArrayList<double[]> mergeSort(ArrayList<double[]> list) {
    	ArrayList<double[]> combined = new ArrayList<double[]>(); 
    	if(list.size() > 1) {
    		int half = list.size()/2;
    		ArrayList<double[]> lower = copyOfRange(list, 0, half-1);
    		ArrayList<double[]> upper = copyOfRange(list, half, list.size()-1);
    		combined = merge(mergeSort(lower), mergeSort(upper));
    	}
    	if(combined.isEmpty()) {
    		return list;
    	}
    	else {
    		return combined; 
    	}
    }
    
    private ArrayList<double[]> merge(ArrayList<double[]> lower, ArrayList<double[]> upper) {
    	int newSize = lower.size() + upper.size();
    	ArrayList<double[]> combined = new ArrayList<double[]>(newSize);
    	int i = 0, lowerI = 0, upperI = 0;
    	while(i < newSize) {
    		if((lowerI < lower.size()) && (upperI < upper.size())) {
    			if(lower.get(lowerI)[0] > upper.get(upperI)[0]) {
    				combined.add(i, lower.get(lowerI));
    				i++;
    				lowerI++;
    			} else {
    				combined.add(i, upper.get(upperI));
    				i++;
    				upperI++;
    			}
    		} else {
    			if(lowerI >= lower.size()) {
    				while(upperI < upper.size()) {
    					combined.add(i, upper.get(upperI));
    					i++;
    					upperI++;
    				}
    			} if(upperI >= upper.size()) {
    				while(lowerI < lower.size()) {
    					combined.add(i, lower.get(lowerI));
    					lowerI++;
    					i++;
    				}
    			}
    		}
    	}
    	return combined;
    }
    
    private double sum(HashMap<Integer, Double> map) {
    	double sum = 0;
    	for(int i = 0; i < map.size(); i++) {
    		sum += map.get(i);
    	}
    	return sum;
    }
    
    private ArrayList<double[]> hashMapToArrayList(HashMap<Integer, Double> map) {
    	ArrayList<double[]> list = new ArrayList<double[]>();
    	for(int i = 0; i < map.size(); i++) {
    		double[] array = {map.get(i), i};
    		list.add(array);
    	}
    	return list; 
    }
    
    private ArrayList<double[]> copyOfRange(ArrayList<double[]> list, int start, int end) {
    	ArrayList<double[]> newList = new ArrayList<double[]>(end-start+1);
    	for(int i = start; i <=end; i++) {
    		newList.add(list.get(i));
    	} 	
    	return newList;
    }
     
    private String print(ArrayList<double[]> list) {
    	String str = "";
    	for(int i = 0; i < list.size(); i++) {
    		str += (int) list.get(i)[1] + "\t" + list.get(i)[0] + "\n";
    	}
    	str += "";
    	return str;
    }

    public static void main(String[] args) throws IOException {
    	SequentialPageRank sequentialPR = new SequentialPageRank();
    	sequentialPR.parseArgs(args);
        sequentialPR.loadInput();
        sequentialPR.calculatePageRank();
        sequentialPR.printValues();
    } 
}
