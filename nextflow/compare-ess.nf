deliverableDir = 'deliverables/' + 
                  workflow.scriptName.replace('.nf','') + 
                  "/comparisons" +


process permutedClustering {
  echo true
  input:
    val sampler from SAMPLERS
  output:
    val "done" into status
  """
  cd ../../..
nextflow run permuted-clustering.nf --sampler "PermutationSamplerLB" --minGS 10 --maxGS 20  --lbFactor 0.45
  echo ${sampler} DONE
  """
}


process plotComparison {
  input:
    val v from status.toList()
  """
  cd ../../..
  Rscript comparePlot.R "PermutationSampler" "PermutationSamplerLB"
  """

}
