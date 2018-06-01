package matchings;

public class PermutationStateProbability {
  int index_one, index_two;
  double rootPiKXY, rootPiKYX, kXY;
  double normFactor;
  
  PermutationStateProbability(int idx1, int idx2, double rootPiKXY, double rootPiKYX){
    this.index_one = idx1;
    this.index_two = idx2;
    // Not yet normalized rootPi * K(x,y)
    this.rootPiKXY = rootPiKXY;
    this.rootPiKYX = rootPiKYX;
  }
}
