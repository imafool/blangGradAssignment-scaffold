#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

process permutedClustering {
  output:
    file "done1" into status1
  '''
  cd ../../..
  nextflow run permuted-clustering.nf
  touch done1
  '''
}

process permutedClusteringLB {
  output:
    file "done2" into status2
  '''
  cd ../../..
  nextflow run permuted-clustering-lb.nf
  touch done2
  '''
}

process plot {
  input:
    file status1
    file status2
  output:
    file "essps_comparison_plot.pdf"
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  #!/usr/bin/Rscript ../../../comparePlot.R "PermutationSampler" "PermutationSamplerLB"
  """

}
