package matchings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import bayonet.math.EffectiveSampleSize;
import blang.inits.Arg;
import blang.inits.DefaultValue;
import blang.inits.experiments.Experiment;



public class ComputePermutationESS extends Experiment 
{
  @Arg 
  File csvFile;
  
  @Arg 
  Optional<String> field = Optional.empty();
  
  @Arg @DefaultValue("1")
  int moment        = 1;
  
  
  public ArrayList<Double> testFunction(ArrayList<ArrayList<Double>> listOfPermutations) {
    int TEST_INDEX = 0;
    double PASS_VALUE = 1;
    double FAIL_VALUE = 0;
    
    ArrayList<Double> testResult = new ArrayList<Double>();
    for (ArrayList<Double> permutation : listOfPermutations) {
      if (permutation.get(TEST_INDEX) == TEST_INDEX) {
        testResult.add(PASS_VALUE);
      } else {
        testResult.add(FAIL_VALUE);
      }
    }
    return testResult;
  }
  
  public static ArrayList<ArrayList<Double>> parsePermutationCSV(File permutationFile) throws FileNotFoundException, IOException {
    int PERM_SIZE = 3;
    int PERM_PER_SAMPLE = 2;
    int PERM_INDEX = 1;
    int SPAN = PERM_SIZE * PERM_PER_SAMPLE;
    
    CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(permutationFile));
    List<CSVRecord> records = parser.getRecords();
    ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();

    ArrayList<Double> permutation = new ArrayList<Double>();
    
    for (int i = 0; i < records.size() - SPAN; i += SPAN) {
      permutation.clear();
      for (int j = (PERM_INDEX - 1) * PERM_SIZE + 1; j <= PERM_INDEX * PERM_SIZE; j++) {
        double edge = (double) Integer.parseInt(records.get(i+j).get(3));
        permutation.add(edge);
      }
      ArrayList<Double> permInstance = new ArrayList<Double>(permutation);
      result.add(permInstance);
    }
    return result;
  }
  
  @Override
  public void run() 
  {
    ArrayList<Double> testResults = null;
    try {
      testResults = testFunction(parsePermutationCSV(csvFile));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    System.out.println(moment == 1 ?
       EffectiveSampleSize.ess(testResults) :
       EffectiveSampleSize.ess(testResults, x -> Math.pow(x, moment)));
  }

  public static void main(String [] args) 
  {
    Experiment.startAutoExit(args);
  }
}