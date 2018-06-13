package matchings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
  
  @Arg
  double infDuration;
  
  @Arg
  int kthPerm;

  @Arg @DefaultValue("1")
  int moment        = 1;
  
  
  public ArrayList<Double> testFunction(ArrayList<ArrayList<Double>> listOfPermutations, int testIndex, int targetIndex) {
    double PASS_VALUE = 1;
    double FAIL_VALUE = 0;
    
    ArrayList<Double> testResult = new ArrayList<Double>();
    for (ArrayList<Double> permutation : listOfPermutations) {
      if (permutation.get(testIndex) == targetIndex) {
        testResult.add(PASS_VALUE);
      } else {
        testResult.add(FAIL_VALUE);
      }
    }
    return testResult;
  }
  
  public static ArrayList<ArrayList<Double>> parsePermutationCSV(File permutationFile, int groupSize, int nGroups, int kthPerm) throws FileNotFoundException, IOException {
    int span = groupSize * nGroups;
    
    CSVParser parser = CSVFormat.DEFAULT.parse(new FileReader(permutationFile));
    List<CSVRecord> records = parser.getRecords();
    ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();

    ArrayList<Double> permutation = new ArrayList<Double>();
    
    for (int i = 1; i <= records.size()-1; i += span){
      if (i % span == kthPerm) {
        permutation.clear();
        for (int j = 0; j < groupSize; j++) {
          permutation.add((double) Integer.parseInt(records.get(i+j).get(3)));
        }
      result.add(new ArrayList<Double>(permutation));
      }
    }
    return result;
  }
  
  @Override
  public void run() 
  {
    ArrayList<Double> testResults = null;
    try {
      PrintWriter esspsWriter = new PrintWriter("essps_" + String.valueOf(groupSize)+".csv");
      esspsWriter.print("groupSize,kth_Perm,testIndex,targetIndex,essps");
      for (int i = 0 ; i < groupSize ; i++) {
        for (int j = i ; j < groupSize; j++) {
          try {
            testResults = testFunction(parsePermutationCSV(csvFile, groupSize, nGroups, kthPerm), i, j);
          } catch (IOException e) {
            e.printStackTrace();
          }
          double essps = EffectiveSampleSize.ess(testResults) / (infDuration / 1000);
          esspsWriter.printf("\n%d,%d,%d,%d,%f", groupSize, kthPerm, i, j, essps);
          esspsWriter.flush();
        }
      }
      esspsWriter.print("\n");
      esspsWriter.close();
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
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
