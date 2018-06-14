#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf,'')

process permutedClustering {
  output:
    delivs1 
  '''
  nextflow run $baseDir/permuted-clustering.nf
  '''
}

process permutedClusteringLB {
  output:
    delivs2 
  '''
  nextflow run $baseDir/permuted-clustering-lb.nf
  '''
}

process plot {
  input:
    file delivs1 from delivs1.collcet()
    file delivs2 from delivs2.collect()
  output:
    file "comparison_plot.csv"
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  /usr/bin/Rscript ../../../comparePlot.R ""  
  """

}
