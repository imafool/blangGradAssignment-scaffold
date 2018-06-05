package matchings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import bayonet.distributions.Random;
import bayonet.math.NumericalUtils;
import blang.core.LogScaleFactor;
import blang.distributions.Generators;
import blang.mcmc.ConnectedFactor;
import blang.mcmc.SampledVariable;
import blang.mcmc.Sampler;
import briefj.collections.UnorderedPair;

/**
 * Each time a Permutation is encountered in a Blang model, this sampler will be
 * instantiated.
 */
public class PermutationSamplerLB implements Sampler {
  /**
   * This field will be populated automatically with the permutation being
   * sampled.
   */
  @SampledVariable
  Permutation permutation;
  /**
   * This will contain all the elements of the prior or likelihood (collectively,
   * factors), that depend on the permutation being resampled.
   */
  @ConnectedFactor
  List<LogScaleFactor> numericFactors;

  @Override
  public void execute(Random rand) {
    PermutationStateProbability proposedState = getProposedState(rand);
    double alpha = calculateAlpha(proposedState);
    decide(proposedState, alpha, rand);
  }
   
  /**
   * From a list of neighbouring states, pick one categorically. 
   */
  private PermutationStateProbability getProposedState(Random rand) {
    List<PermutationStateProbability> candidates = getNeighbours();
    double qYX[] = new double[candidates.size()];
    for (int i = 0 ; i < qYX.length ; i++) {
      qYX[i] = Math.exp(candidates.get(i).qYX);
    }
    int candidateIndex = rand.nextCategorical(qYX);
    PermutationStateProbability proposal = candidates.get(candidateIndex);

    return proposal;
  }

  /**
   * Return a list of neighbouring states 
   * holding their respective normalized qXY, qYX, and indices values
   */
  private List<PermutationStateProbability> getNeighbours() {
    List<PermutationStateProbability> neighbours = new ArrayList<PermutationStateProbability>();
    // initialize with e^neg_inf (0)
    double normX = Double.NEGATIVE_INFINITY;
    double normY = Double.NEGATIVE_INFINITY;
    int size = permutation.componentSize();
    for (int indexOne = 0; indexOne < size; indexOne++) {
      for (int indexTwo = indexOne + 1; indexTwo < size; indexTwo++) {
        PermutationStateProbability state = new PermutationStateProbability(indexOne, indexTwo);
        calculateQ(state);
        normX = NumericalUtils.logAdd(normX, state.qXY);
        normY = NumericalUtils.logAdd(normY, state.qYX);
        neighbours.add(state);
      }
    }
    // Normalize
    for (PermutationStateProbability state : neighbours) {
      state.qXY -= normY;
      state.qYX -= normX;
    }
    
    return neighbours;
  }

  
  /**
   * @param psb 's indices dictate which state we will move to.
   * permutation.getConnections() is modified accordingly
   */
  private void move(PermutationStateProbability psb) {
    Collections.swap(permutation.getConnections(), psb.indexOne, psb.indexTwo);
  }



  /**
   * @return piY without affecting permutation.getConnections()
   */
  private double getPiY(PermutationStateProbability psb) {
    move(psb);
    double piY = logDensity();
    move(psb);
    return piY;
  }



  /**
   * @param qYX and qXY are Q_root
   */
  private double calculateAlpha(PermutationStateProbability proposedState) {
    double piX = logDensity();
    double piY = getPiY(proposedState);
    double qYX = proposedState.qYX;
    double qXY = proposedState.qXY;
    return Math.min(1.0, Math.exp(piY - piX + qYX - qXY));
  }



  /**
   * @param state is modified to hold non-normalized Q_root values
   * namely qXY and qYX
   */
  private void calculateQ(PermutationStateProbability state) {
    state.qXY = 0.5 * getPiY(state);
    state.qYX = 0.5 * logDensity();
    return;
  }


  private double logDensity() {
    double sum = 0.0;
    for (LogScaleFactor f : numericFactors)
      sum += f.logDensity();
    return sum;
  }


  /**
   * Take a step or not, depending if alpha passes test.
   */
  private void decide(PermutationStateProbability proposedState, double alpha, Random rand) {
    boolean accept = rand.nextBernoulli(alpha);
    if (accept) {
      move(proposedState);
    } else {
      // do nothing
    }
       
  }


}
