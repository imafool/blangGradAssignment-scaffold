package matchings;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
    System.out.print(essps);
    PrintWriter writer = new PrintWriter("ess.txt", "UTF-8");
    writer.println(essps);
    writer.close();
  }

  
  /**
   * Maps a sample of permuted cluster to 1 or 0.
   * In this implementation, sample is mapped to 1 by inspecting the first permutation of a sample.
   * If index 1 points to 1, then it is mapped to 1, else it is mapped to 0.
   */
  private static ArrayList<Integer> testFunction(ArrayList<ArrayList<Integer>> listOfPermutations) {
    int TEST_INDEX = 0;
    int PASS_VALUE = 1;
    int FAIL_VALUE = 0;
        
    ArrayList<Integer> testResult = new ArrayList<Integer>();
    for (ArrayList<Integer> permutation : listOfPermutations) {
      if (permutation.get(TEST_INDEX) == TEST_INDEX) {
        testResult.add(PASS_VALUE);
      } else {
        testResult.add(FAIL_VALUE);
      }
    }
    return testResult;
  }

  /**
   * Duration of sampling inference process 
   */
  private static double getDuration() {
    // TODO Auto-generated method stub
    return 1;
  }

  /**
   * @param listOfTestedPerms is a list of values samples have been mapped to
   * @return ESS by batch method
   */
  private static double computeESS(ArrayList<Integer> listOfTestedPerms) {
    int n = listOfTestedPerms.size();
    double binSize = Math.ceil(Math.sqrt(n));
    double nBins = binSize - 1;
    // varMu of bins
    double varMu = calcVarMu(listOfTestedPerms, binSize, nBins);
    double sampleVar = calcSampleVar(listOfTestedPerms);
    double ess = binSize * sampleVar / varMu;
    return ess;
  }

  
  /**
   * Calculates sample variance of a list of real valued samples
   */
  private static double calcSampleVar(ArrayList<Integer> listOfTestedPerms) {
    double sampleMean;
    
    double sum = 0;
    for (int val : listOfTestedPerms) {
      sum += val;
    }
    sampleMean = sum/listOfTestedPerms.size();
    
    double sqrDiffSum = 0;
    for (int val: listOfTestedPerms) {
      sqrDiffSum += Math.pow(val - sampleMean, 2);
    }
    
    double sampleVar = sqrDiffSum/listOfTestedPerms.size();
    
    return sampleVar;
  }

  
  
  /**
   * Calculates variance of mean of bins
   */
  private static double calcVarMu(ArrayList<Integer> listOfTestedPerms, double binSize, double nBins) {
    // calculate binMu
    ArrayList<Double> binSums = new ArrayList<Double>();
    ArrayList<Double> binMus = new ArrayList<Double>();
    for (int i = 0; i < nBins * binSize; i += binSize) {
      double binSum = 0.0;
      for (int j = 0; j < binSize && j <= listOfTestedPerms.size(); j++) {
        binSum += listOfTestedPerms.get(i+j);
      }
      binSums.add(binSum);
      binMus.add(binSum/binSize);
    }
    
    // calculate meanMu
    double meanMu = 0;
    for (double mu : binMus) {
      meanMu += mu;
    }
    meanMu /= nBins;
    
    // varMu
    double varMu;
    double sqrDiff = 0;
    for (double m : binMus) {
      sqrDiff += Math.pow(m-meanMu, 2);
    }
    varMu = sqrDiff / (binSize -1);

    return varMu;
  }

  /**
   * @return a list of permutations (list of int) from a matrix of permutations.
   */
  public static ArrayList<ArrayList<Integer>> parsePermutationCSV() throws FileNotFoundException, IOException {
    int PERM_SIZE = 5;
    int PERM_PER_SAMPLE = 20;
    int PERM_INDEX = 1;
    int SPAN = PERM_SIZE * PERM_PER_SAMPLE;
    
    File permutationFile = new File("permutations.csv");
    CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(permutationFile));
    List<CSVRecord> records = parser.getRecords();
    ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

    ArrayList<Integer> permutation = new ArrayList<Integer>();
    
    for (int i = 0; i < records.size() - SPAN; i += SPAN) {
      permutation.clear();
      for (int j = (PERM_INDEX - 1) * PERM_SIZE + 1; j <= PERM_INDEX * PERM_SIZE; j++) {
        int edge = Integer.parseInt(records.get(i+j).get(3));
        permutation.add(edge);
      }
      ArrayList<Integer> permInstance = new ArrayList<Integer>(permutation);
      result.add(permInstance);
    }
    return result;
  }
}  
  
  