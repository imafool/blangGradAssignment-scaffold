#!/bin/bash

nextflow run $(pwd)/permuted-clustering.nf & nextflow run $(pwd)/permuted-clustering-lb.nf
Rscript $(pwd)/comparePlot.R "PermutationSampler" "PermutationSamplerLB"
