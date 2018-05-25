package matchings;

import java.util.List;

import bayonet.distributions.Multinomial;
import bayonet.distributions.Random;
import blang.core.LogScaleFactor;
import blang.distributions.Generators;
import blang.mcmc.ConnectedFactor;
import blang.mcmc.SampledVariable;
import blang.mcmc.Sampler;
import briefj.collections.UnorderedPair;

/**
 * Each time a Permutation is encountered in a Blang model, 
 * this sampler will be instantiated. 
 */
public class BipartiteMatchingSampler implements Sampler {
  /**
   * This field will be populated automatically with the 
   * permutation being sampled. 
   */
  @SampledVariable BipartiteMatching matching;
  /**
   * This will contain all the elements of the prior or likelihood 
   * (collectively, factors), that depend on the permutation being 
   * resampled. 
   */
  @ConnectedFactor List<LogScaleFactor> numericFactors;

  
  @Override
  public void execute(Random rand) {


	  // current log pi density
	  double logPiX = logDensity();

	  // uniformly pick a vertex and check if it is matched	  
	  int i = rand.nextInt(matching.componentSize());
	  double logQXY, logQYX;
	  int j = 0;
	  
	  boolean isFree = matching.free1().contains(i);
	  if (isFree) {
		  // add edge
		  logQYX = - Math.log((matching.componentSize() * matching.free2().size()));
		  j = matching.free2().get(rand.nextInt(matching.free2().size()));
		  matching.getConnections().set(i, j);
		  logQXY = - Math.log(matching.componentSize());
	  } else {
		  // remove edge
		  j = matching.getConnections().get(i);
	  	  logQYX = - Math.log((matching.componentSize()));
		  matching.getConnections().set(i, BipartiteMatching.FREE);
		  logQXY = - Math.log((matching.componentSize() * matching.free2().size()));
	  }
	  		
	  // proposed log pi density
	  double logPiY = logDensity();

	  // calculate acceptance rate and accept or reject.
	  double alpha = Math.min(1.0, Math.exp(  logPiY + logQXY
			  							    - logPiX - logQYX));
//	  System.out.println(alpha);

	  boolean accept = rand.nextBernoulli(alpha);
	  
	  if (accept) {
		  ; // we already made the move.
	  } else {
		  if (isFree){
			  // remove proposed edge
			  matching.getConnections().set(i, BipartiteMatching.FREE);
		  } else {
			  // rebuild current edge
			  matching.getConnections().set(i, j);
		  }
	  }
	  
  }
  
  private double logDensity() {
    double sum = 0.0;
    for (LogScaleFactor f : numericFactors)
      sum += f.logDensity();
    return sum;
  }
}