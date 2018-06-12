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
  
  @Arg
  int groupSize;
  
  @Arg
  int nGroups;
  
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
    System.out.println(testResult.size());
    return testResult;
  }
  
  public static ArrayList<ArrayList<Double>> parsePermutationCSV(File permutationFile, int groupSize, int nGroups) throws FileNotFoundException, IOException {
    int kthPermInGroup = 1;
    int span = groupSize * nGroups;
    
    CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(permutationFile));
    List<CSVRecord> records = parser.getRecords();
    ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();

    ArrayList<Double> permutation = new ArrayList<Double>();
    
    for (int i = 1; i <= records.size()-1; i += span){
      if (i % span == kthPermInGroup) {
        permutation.clear();
        for (int j = 0; j < groupSize; j++) {
          permutation.add((double) Integer.parseInt(records.get(i+j).get(3)));
        }
      result.add(new ArrayList<Double>(permutation));
      }
    }
    System.out.println(result);
    return result;
  }
  
  @Override
  public void run() 
  {
    ArrayList<Double> testResults = null;
    try {
      testResults = testFunction(parsePermutationCSV(csvFile, groupSize, nGroups));
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