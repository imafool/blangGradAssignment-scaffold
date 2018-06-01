package matchings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bayonet.distributions.Random;
import bayonet.math.SpecialFunctions;
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
public class PermutationSampler implements Sampler {
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

  @Override
  public void execute(Random rand) {
    // Find current density
    double currentLogDensity = logDensity();

    // swap two vertices as a proposed step, then find proposed log density
    UnorderedPair<Integer, Integer> pair = Generators.distinctPair(rand, permutation.componentSize());
    Collections.swap(permutation.getConnections(), pair.getFirst(), pair.getSecond());
    double proposedLogDensity = logDensity();

    // Find acceptance rate/probability
    // min of (1, P(a_{i+1}) / P(a_i)) = min of (1, proposed/current)
    // NOTE: By e^proLogDen / e^currLogDen defeats the purpose of logDensities.
    // Will be interpreted as 0/0 (or at least less efficient in computation).
    double acceptanceRate = Math.min(1, Math.exp(proposedLogDensity - currentLogDensity));

    // Do we accept the proposal?
    boolean decision = rand.nextBernoulli(acceptanceRate);
    // if we do NOT accept, then swap again to get back to current.
    // We swap instead of saving an instance because it will be resource heavy to
    // save many instances
    if (!decision) {
      Collections.swap(permutation.getConnections(), pair.getFirst(), pair.getSecond());
    }

  }
  
  
  private double logDensity() {
    double sum = 0.0;
    for (LogScaleFactor f : numericFactors)
      sum += f.logDensity();
    return sum;
  }
}