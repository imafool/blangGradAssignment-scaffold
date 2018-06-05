package matchings;

public class PermutationStateProbability {
  /**
   * Index one and two are the indices to swap in permutation to
   * generate the next state
   */
  int indexOne, indexTwo;
  double qXY;
  
  PermutationStateProbability(int indexOne, int indexTwo) {
    this.indexOne = indexOne;
    this.indexTwo = indexTwo;
  }
}
