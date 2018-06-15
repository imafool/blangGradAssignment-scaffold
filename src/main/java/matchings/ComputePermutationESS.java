package matchings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.Lists;

import bayonet.math.EffectiveSampleSize;
import blang.inits.Arg;
import blang.inits.DefaultValue;
import blang.inits.experiments.Experiment;
import briefj.BriefIO;



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
  

  @Override
  public void run() 
  {
    PrintWriter esspsWriter = null;
    try {
      esspsWriter = new PrintWriter("essps_" + String.valueOf(groupSize)+".csv");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    esspsWriter.print("groupSize,kth_Perm,testIndex,targetIndex,essps");
  
    List<Double> samples = new ArrayList<>();
    List<Map<String,String>> data = Lists.newArrayList(BriefIO.readLines(csvFile).indexCSV().skip(0));
    int m = data.size();
    for (int i=0;i<nGroups;i++) {
      for (int j=0;j<groupSize;j++) {
        for (int k=0;k<groupSize;k++) {
          int l=i*nGroups+j;
          samples.clear();
          while (l<m) {
            samples.add(Integer.parseInt(data.get(l).get("value").trim())==k ? 1. : 0.);
            l+=groupSize*nGroups;
          }
          esspsWriter.printf("\n%d,%d,%d,%d,%f", groupSize, i, j, k, EffectiveSampleSize.ess(samples)/(infDuration / 1000));
        }
      }
      esspsWriter.flush();
    }
    esspsWriter.print("\n");
    esspsWriter.close();
  }

  public static void main(String [] args) 
  {
    Experiment.startAutoExit(args);
  }
}
