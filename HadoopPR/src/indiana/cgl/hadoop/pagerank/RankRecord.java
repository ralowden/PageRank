package indiana.cgl.hadoop.pagerank;

import java.util.ArrayList;

/**
 * Utility class for parsing a record in the input
 */
public class RankRecord {
    // source url
    public int sourceUrl;
    // rank value for this source url
    public double rankValue;
    // the outbound urls
    public ArrayList<Integer> targetUrlsList;

    /**
     * Parse the given string and assing the values to sourceUrl, rankValue and targetUrlsList
     * @param strLine line of text of the format sourceUrl\trankValue#targetUrlsList
     */
    public RankRecord(String strLine) {
        String[] strArray = strLine.split("#");

        sourceUrl = Integer.parseInt(strArray[0].split("\t")[0]);

        rankValue = Double.parseDouble(strArray[0].split("\t")[1]);

        targetUrlsList = new ArrayList<Integer>();

        for (int i = 1; i < strArray.length; i++) {
            targetUrlsList.add(Integer.parseInt(strArray[i]));
        }
    }
}
