params.sampler = "PermutationSamplerLB"

params.minGS = 10
params.maxGS = 12

// params.deliverableDir = "deliverables/${workflow.scriptName.replace('.nf', '')}/" + 
//                         "compsLB0.1-0.9/" + "GS${params.minGS}-${params.maxGS}"

lbFactors = (1..9).toList().collect({it/10})
lbFactors = [0.2, 0.4, 0.6, 0.8]

args = lbFactors.collect({'"' + it + '"'}).join(" ")

process permutedClustering {
  echo true
  input:
    each lbFactor from lbFactors.collect()
  output:
    val "done" into status
  """
  cd ../../../
  nextflow run permuted-clustering.nf \
    --sampler "$params.sampler" \
    --minGS $params.minGS \
    --maxGS $params.maxGS \
    --lbFactor ${lbFactor}
  """
}


// process plotComparison {
//   input:
//     val v from status.toList()
//   """
//   cd ../../..
//   Rscript comparePlot.R $args
//   """

// }
