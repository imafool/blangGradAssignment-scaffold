package matchings

import org.eclipse.xtend.lib.annotations.Data
import blang.inits.experiments.tabwriters.TidilySerializable
import blang.inits.experiments.tabwriters.TidySerializer.Context
import java.util.List
import blang.inits.DesignatedConstructor
import blang.inits.ConstructorArg
import blang.inits.Arg
import java.util.Optional
import blang.inits.DefaultValue

/**
 * Shared functionality and representation used by Permutation and 
 * BipartiteMatching.
 */
@Data abstract class MatchingBase implements TidilySerializable {
  /**
   * Assume the vertices are indexed 0, 1, ..., N in the first bipartite component, and 
   * also 0, 1, 2, .., N in the second bipartite component. 
   * 
   * For vertex i in the first component, connections.get(i) 
   * give the index in the second bipartite component. 
   */
   
   /** 
    * If the permutation is locally optimized, 
    */
  
 
   
  val List<Integer> connections
  
  /**
   * Initialize to the identity permutation.
   */
   
   
	val Double lbFactor
   
   
  @DesignatedConstructor
  new (@ConstructorArg("component size") int componentSize)
  	
  	 {connections = (0 ..< componentSize).toList
  	 	lbFactor = 0.5
    }
    
      new (double d, @ConstructorArg("component size") int componentSize)
  	 {connections = (0 ..< componentSize).toList
  	 lbFactor = d
    }
    
   


  
  /**
   * The number of vertices in each of the two bipartite components.
   */
   
  def int componentSize() { 
    return connections.size
  } 
  
  override String toString() { 
    return connections.toString
  }
  
  /**
   * Used to output samples into a tidy format. 
   */
  override void serialize(Context context) {
    for (int i : 0 ..< componentSize)
      context.recurse(connections.get(i), "permutation_index", i)
  } 
}
