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
    List<PermutationStateProbability> candidates = getNeighbours();
    PermutationStateProbability proposedState = getProposedState(candidates, rand);
    int candidateIndex = candidates.indexOf(proposedState);
    double alpha = calculateAlpha(candidateIndex, proposedState);
    decide(proposedState, alpha, rand);
  }
   
  /**
   * From list of neighbouring states, pick one categorically. 
   */
  private PermutationStateProbability getProposedState(List<PermutationStateProbability> candidates, Random rand) {
    double[] qXY = new double[candidates.size()];
    for (int i = 0 ; i < qXY.length ; i++) {
      qXY[i] = Math.exp(candidates.get(i).qXY);
    }
    int candidateIndex = rand.nextCategorical(qXY);
    PermutationStateProbability proposal = candidates.get(candidateIndex);

    return proposal;
  }

  /**
   * Return a list of neighbouring states 
   * holding their respective normalized qXY, qYX, and indices values
   */
  private List<PermutationStateProbability> getNeighbours() {
    List<PermutationStateProbability> neighbours = new ArrayList<PermutationStateProbability>();
    // initialize the logProbability's normalization factor with 0 (e^NEG_INF)
    double sumPiY = Double.NEGATIVE_INFINITY;
    int size = permutation.componentSize();
    for (int indexOne = 0; indexOne < size; indexOne++) {
      for (int indexTwo = indexOne + 1; indexTwo < size; indexTwo++) {
        PermutationStateProbability state = new PermutationStateProbability(indexOne, indexTwo);
        calculateQ(state);
        sumPiY = NumericalUtils.logAdd(sumPiY, state.qXY);
        neighbours.add(state);
      }
    }
    // Normalize 
    for (PermutationStateProbability state : neighbours) {
      state.qXY -= sumPiY;
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
  private double getPiNext(PermutationStateProbability psb) {
    move(psb);
    double piY = logDensity();
    move(psb);
    return piY;
  }



  /**
   * @param qYX and qXY are Q_root
   */
  private double calculateAlpha(int candidateIndex, PermutationStateProbability proposedState) {
    double piX = logDensity();
    double piY = getPiNext(proposedState);
    double qXY = proposedState.qXY;
    double qYX = getQYX(proposedState, candidateIndex);
   return Math.min(1.0, Math.exp(piY - piX + qYX - qXY));
  }



  /**
   * Calculates qYX by first moving to the next state, then revert changes made.
   */
  private double getQYX(PermutationStateProbability proposedState, int candidateIndex) {
    move(proposedState);
    List<PermutationStateProbability> candidates = getNeighbours();
    double qYX = candidates.get(candidateIndex).qXY;
    move(proposedState);
     return qYX;
  }

  /**
   * @param state is modified to hold non-normalized Q_root values
   * namely qXY
   */
  private void calculateQ(PermutationStateProbability state) {
    state.qXY = 0.5 * getPiNext(state);
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
