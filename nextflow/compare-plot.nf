#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

// samplers based on permutations (perfect matchings)
SAMPLERS = ["PermutationSampler", "PermutationSamplerLB"]

process permutedClustering {
  echo true
  input:
  val sampler from SAMPLERS
  output:
    val "done" into status
  """
  cd ../../..
  nextflow run permuted-clustering.nf --sampler "$sampler"
  echo ${sampler} DONE
  """
}


process plotComp {
  input:
    val v from status.toList()
  """
  cd ../../..
  Rscript comparePlot.R "PermutationSampler" "PermutationSamplerLB"
  """

}
