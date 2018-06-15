#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

process permutedClustering {
  echo true
  input:
  val sampler from "permuted-clustering", "permuted-clustering-lb"
  output:
    val "done" into status
  """
  cd ../../..
  nextflow run ${sampler}.nf
  echo ${sampler} DONE
  """
}


process plotComp {
  input:
    val v from status.toList()
  """
  cd ../../..
  #!Rscript comparePlot.R "PermutationSampler" "PermutationSamplerLB"
  """

}
