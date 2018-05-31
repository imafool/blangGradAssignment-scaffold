package matchings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

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
public class BipartiteMatchingSamplerLB implements Sampler {
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
	  // A neighbouring state and its corresponding Qs.
	  class StateProb{

		  int i, j;
		  double qPiCurr, qPiNext;
		  double piNext;
		  StateProb(int vertex1, int vertex2){
			 i = vertex1;
			 j = vertex2;
			 qPiCurr = 0;
			 qPiNext = 0;
			 piNext = 0;
		  }
	  }
	  
	  List<StateProb> stateProbabilities = new ArrayList<StateProb>();
      double piCurr = logDensity();
      double currNormFactor = 1, nextNormFactor = 1; 
	  
      // Build a list of neighbouring states 
	  for (int i = 0; i < matching.componentSize(); i++) {
		int connection = matching.getConnections().get(i);
		boolean hasConnection = connection != -1;
		if (hasConnection) {
			
			// calculate Q
			StateProb sb = new StateProb(i, BipartiteMatching.FREE);
			matching.getConnections().set(i, BipartiteMatching.FREE);
			sb.piNext = logDensity();
			matching.getConnections().set(i, connection);
			UnorderedPair<Double, Double> qCurrQNext = calcQCurrQNext(i, hasConnection, BipartiteMatching.FREE, piCurr, sb.piNext);
			sb.qPiCurr = qCurrQNext.getFirst();
			sb.qPiNext = qCurrQNext.getSecond();
			currNormFactor *= qCurrQNext.getFirst();
			nextNormFactor *= qCurrQNext.getSecond();
			stateProbabilities.add(sb);
		} else { // if it does not have an edge
			
			// Find every state possible for this vertex
			for (int j = 0; j < matching.free2().size(); j++) {
				
				// make connection
				int targetVertex = matching.free2().get(j);
				
				StateProb sb = new StateProb(i, targetVertex);
				matching.getConnections().set(i, j);
				sb.piNext = logDensity();
				matching.getConnections().set(i, BipartiteMatching.FREE);
				UnorderedPair<Double, Double> qCurrQNext = calcQCurrQNext(i, hasConnection, targetVertex, piCurr, sb.piNext);
				sb.qPiCurr = qCurrQNext.getFirst();
				sb.qPiNext = qCurrQNext.getSecond();
				currNormFactor *= qCurrQNext.getFirst();
				nextNormFactor *= qCurrQNext.getSecond();
				stateProbabilities.add(sb);
			}
		}

	}
	  
	double[] probs = new double[0];
	for (int i = 0; i< stateProbabilities.size(); i++) {
		// Normalize
		stateProbabilities.get(i).qPiCurr -= currNormFactor;
		stateProbabilities.get(i).qPiNext -= nextNormFactor;
	} 
  
	  
	  
	  
	// take next step based on list of probabilities corresponding to neighboring states.
	int proposedState = rand.nextCategorical(probs);
	
	// set matching to proposed state.
	int v1 = stateProbabilities.get(proposedState).i;
	int v2 = stateProbabilities.get(proposedState).j;
    double alpha = Math.min(1, Math.exp(stateProbabilities.get(proposedState).piNext
    								  + stateProbabilities.get(proposedState).qPiCurr 
    			                      - stateProbabilities.get(proposedState).qPiNext) 
    								  - piCurr);
    boolean accept = rand.nextBernoulli(alpha);
    if (accept) {
    	matching.getConnections().set(v1, v2);
    } else {
    	;
    }
  }
  
/**
 * Calculates the Q(curr, next) and Q(next, curr).
 */
private UnorderedPair<Double, Double> calcQCurrQNext(int componentOneVertex, boolean hadConnection, int componentTwoVertex, double piCurrent, double piNext) {
	int j = matching.getConnections().get(componentOneVertex);
	matching.getConnections().set(componentOneVertex, componentTwoVertex);	
	matching.getConnections().set(componentOneVertex, j);
	double qCurrent, qNext;
	if (hadConnection) {
		qNext = 0.5 * piCurrent - Math.log(matching.componentSize());
		qCurrent = 0.5 * piNext - Math.log(matching.componentSize()) - Math.log(matching.free1().size());
	} else {
		qNext = 0.5 * piCurrent - Math.log(matching.componentSize() - Math.log(matching.free1().size()));
		qCurrent = 0.5 * piNext - Math.log(matching.componentSize());
	}
	UnorderedPair<Double, Double> pair = UnorderedPair.of(qCurrent, qNext);
	return pair; 
}


private double logDensity() {
    double sum = 0.0;
    for (LogScaleFactor f : numericFactors)
      sum += f.logDensity();
    return sum;
  }
}
