package matchings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class PermutationSamplerLB implements Sampler {
  /**
   * This field will be populated automatically with the 
   * permutation being sampled. 
   */
  @SampledVariable Permutation permutation;
  /**
   * This will contain all the elements of the prior or likelihood 
   * (collectively, factors), that depend on the permutation being 
   * resampled. 
   */
  @ConnectedFactor List<LogScaleFactor> numericFactors;
  
  class StateProbability {
	 int x, y;
	 double rootPiKXY, rootPiKYX, kXY;
	 StateProbability(int x, int y, double rootPiKXY, double rootPiKYX){
		this.x = x;
		this.y = y;
		// Log probability
		this.rootPiKXY = rootPiKXY;
		this.rootPiKYX = rootPiKYX;
	 }
  }
  
  @Override
  public void execute(Random rand) {
	   
	  ArrayList<StateProbability> stateProbs = new ArrayList<StateProbability>();
	  
	  // Populate stateProbs 
	  double normX = 1;
	  double normY = 1;
	  double piY = 0, piX = 0;
	  for (int x = 0; x < permutation.componentSize(); x++) {
			  for (int targetIndex = x+1; targetIndex < permutation.componentSize(); targetIndex++) {
				  piX = logDensity();
				  Collections.swap(permutation.getConnections(), x, targetIndex);
				  piY = logDensity();
				  Collections.swap(permutation.getConnections(), x, targetIndex);

				  StateProbability sb = new StateProbability(x, 
														     targetIndex,
                                                             getRootPiK(piY, x, targetIndex),
                                                             getRootPiK(piX, targetIndex, x));
				  stateProbs.add(sb);
			  	  normX *= sb.x;
			  	  normY *= sb.y;
			  
			  }
	  }
	  
	  // Normalize and populate a list of QPis.
	  double[] probs = new double[stateProbs.size()];
	  for (int i = 0; i < stateProbs.size(); i++) {
		  probs[i] = Math.exp(stateProbs.get(i).rootPiKXY - normX);
	  }
    
	  // Choose state to accept or reject
	  
	  int proposedState = rand.nextCategorical(probs);
	  double alpha = Math.min(1.0, getAlpha(stateProbs.get(proposedState), piY, piX, normX, normY));
	  boolean accept = rand.nextBernoulli(alpha);
	  
	  if (accept) {
		  int proposedX = stateProbs.get(proposedState).x;
		  int proposedY = stateProbs.get(proposedState).y;
		  Collections.swap(permutation.getConnections(), proposedX, proposedY);
	  }
  }
  
  
  private double getAlpha(StateProbability sb, double piY, double piX, double normX, double normY) {
	  double zX = sb.rootPiKXY / normX;
	  double zY = sb.rootPiKYX / normY;
	  double alpha = Math.exp(1.5 * piY + sb.rootPiKXY + zY 
			  				 -1.5 * piX - sb.rootPiKYX - zX);
	  return alpha;
}


private double getRootPiK(double pi, int x, int y) {
	  double k, rootPiK;
	  k = - Math.log(permutation.componentSize() * (permutation.componentSize() - 1));
	  rootPiK = 0.5 * pi + k;
	return rootPiK;
}


private double logDensity() {
    double sum = 0.0;
    for (LogScaleFactor f : numericFactors)
      sum += f.logDensity();
    return sum;
  }
}
