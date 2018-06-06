package matchings;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


public class ESS {

  /**
   * An ESS/s computer for PermutedClustering model.
   */
  public static void main(String[] args) throws FileNotFoundException, IOException{
    
    ArrayList<ArrayList<Integer>> listOfPermutations;
    ArrayList<Integer> listOfTestedPerms;
    double ess, duration;
    
    listOfPermutations = parsePermutationCSV();
    listOfTestedPerms = testFunction(listOfPermutations);
    ess = computeESS(listOfTestedPerms);
    duration = getDuration();

    double essps = ess/duration;
  }

  
  /**
   * 
   * @param listOfPermutations
   * @return
   */
  private static ArrayList<Integer> testFunction(ArrayList<ArrayList<Integer>> listOfPermutations) {
    // TODO Auto-generated method stub
    return null;
  }

  private static double getDuration() {
    // TODO Auto-generated method stub
    return 1;
  }

  private static double computeESS(ArrayList<Integer> listOfTestedPerms) {
    int n = listOfTestedPerms.size();
    double binSize = Math.ceil(Math.sqrt(n));
    double numBins = binSize;
    double varMu = calcVarMu(listOfTestedPerms, binSize);
    double sampleVar = calcSampleVar(listOfTestedPerms);
    double ess = binSize * sampleVar / varMu;
    return ess;
  }

  private static double calcSampleVar(ArrayList<Integer> listOfTestedPerms) {
    // TODO Auto-generated method stub
    return 0;
  }

  private static double calcVarMu(ArrayList<Integer> listOfTestedPerms, double binSize) {
    // TODO Auto-generated method stub
    return 0;
  }

  public static ArrayList<ArrayList<Integer>> parsePermutationCSV() throws FileNotFoundException, IOException {
 
    File permutationFile = new File("permutations.csv");
    CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(permutationFile));
    List<CSVRecord> records = parser.getRecords();
    
    ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
    ArrayList<Integer> permutation = new ArrayList<Integer>();
    for (int i = 1 ; i < records.size() ; i++) {
      if (i % 10 == 1) {
       permutation.clear();
      }
      permutation.add(Integer.parseInt(records.get(i).get(2)));
      if (i % 10 == 0) {
        result.add(new ArrayList<Integer>(permutation));
      }
    }
    return result;
  }
}  
  
  