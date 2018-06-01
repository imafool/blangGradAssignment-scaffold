package matchings;

import java.util.ArrayList;
import java.util.Collections;
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

    ArrayList<PermutationStateProbability> stateProbs = new ArrayList<PermutationStateProbability>();

    // Populate stateProbs
    stateProbs = getNeighbours();
    // Normalize and populate a list of QPis.
    double[] probs = new double[stateProbs.size()];
    for (int i = 0; i < stateProbs.size(); i++) {
      probs[i] = Math.exp(stateProbs.get(i).rootPiKXY - stateProbs.get(0).normFactor);
    }

    // Choose state to accept or reject
    int proposedState = rand.nextCategorical(probs);
    double piX = logDensity();
    Collections.swap(permutation.getConnections(),
                     stateProbs.get(proposedState).index_one,
                     stateProbs.get(proposedState).index_two);
    double piY = logDensity();
    double alpha = Math.min(1.0, getAlpha(stateProbs.get(proposedState), piY, piX));
    boolean accept = rand.nextBernoulli(alpha);

    if (!accept) {
      Collections.swap(permutation.getConnections(),
                       stateProbs.get(proposedState).index_one,
                       stateProbs.get(proposedState).index_two);
    }
  }

  private ArrayList<PermutationStateProbability> getNeighbours() {
    ArrayList<PermutationStateProbability> stateProbs = new ArrayList<PermutationStateProbability>();
    double normX = Double.NEGATIVE_INFINITY;
    double piY = 0;
    double piX = logDensity();
    for (int x = 0; x < permutation.componentSize(); x++) {
      for (int targetIndex = x + 1; targetIndex < permutation.componentSize(); targetIndex++) {
        Collections.swap(permutation.getConnections(), x, targetIndex);
        piY = logDensity();
        Collections.swap(permutation.getConnections(), x, targetIndex);

        PermutationStateProbability sb = new PermutationStateProbability(x, targetIndex,
            getRootPiK(piY, x, targetIndex), getRootPiK(piX, targetIndex, x));
        stateProbs.add(sb);
        normX = NumericalUtils.logAdd(normX, sb.rootPiKXY);

      }
    }
    stateProbs.get(0).normFactor = normX;
    return stateProbs;
  }

  private double getAlpha(PermutationStateProbability sb, double piY, double piX) {
    double alpha = Math.exp(1.5 * (piY - piX));
    return alpha;
  }

  private double getRootPiK(double pi, int x, int y) {
    double k, rootPiK;
    k = -Math.log(permutation.componentSize() * (permutation.componentSize() - 1));
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
