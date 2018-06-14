#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

process permutedClustering {
  '''
  cd ../../..
  nextflow run permuted-clustering.nf
  '''
}

process permutedClusteringLB {
  '''
  cd ../../..
  nextflow run permuted-clustering-lb.nf
  '''
}

process plot {
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  echo "123"
  """

}
