#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

nGroups = 20
groupSize = 3

process build {
  cache false
  output:
    file 'jars_hash' into jars_hash
    file 'classpath' into classpath
    
  """
  set -e
  current_dir=`pwd`
  cd ../../../..
  ./gradlew build
  ./gradlew printClasspath | grep CLASSPATH-ENTRY | sort | sed 's/CLASSPATH[-]ENTRY //' > \$current_dir/temp_classpath
  for file in `ls build/libs/*jar`
  do
    echo `pwd`/\$file >> \$current_dir/temp_classpath
  done
  cd -
  touch jars_hash
  for jar_file in `cat temp_classpath`
  do
    shasum \$jar_file >> jars_hash
  done
  cat temp_classpath | paste -sd ":" - > classpath
  """
}

jars_hash.into {
  jars_hash1
  jars_hash2
}
classpath.into {
  classpath1
  classpath2
}

process generateData {
  cache 'deep'
  input:
    file classpath1
    file jars_hash1
  output:
    file 'generated' into data
    
  """
  set -e
  java -cp `cat classpath` -Xmx8g matchings.PermutedClustering \
    --experimentConfigs.managedExecutionFolder false \
    --experimentConfigs.saveStandardStreams false \
    --experimentConfigs.recordExecutionInfo false \
    --experimentConfigs.recordGitInfo false \
    --model.nGroups $nGroups \
    --model.groupSize $groupSize \
    --engine Forward
  mv samples generated
  """
}

data.into {
  data1
  data2
}

process plotData {
  input:
    file data1
  output:
    file "*.pdf" 
  publishDir deliverableDir, mode: 'copy', overwrite: true

  """
  #!/usr/bin/env Rscript
  require("ggplot2")
  require("dplyr")

  data <- read.csv("generated/observations.csv")

  p <- ggplot(data, aes(x=value)) +
    geom_histogram() +
    xlab("Observation") + 
    ylab("Density") + 
    theme_bw()
  ggsave("data.pdf", width = 15, height = 5)
  
  data <- read.csv("generated/permutations.csv")
    
  p <- ggplot(data, aes(x = factor(index_0),fill = factor(value))) + 
    geom_bar(position = "fill") +
    xlab("Group index") + 
    ylab("Proportion") + 
    facet_grid(permutation_index ~ .) + 
    theme_bw()
    
  ggsave("permutations-truth.pdf", width = 15, height = 20)
  """
}

process runInference {
  cache 'deep'
  input:
    file data2
    file classpath2
    file jars_hash2
  output:
    file 'samples' into samples
  """
  set -e
  tail -n +2 generated/observations.csv | awk -F "," '{print \$2, ",", \$3, ",", \$4}' | sed 's/ //g' > data.csv
  java -cp `cat classpath` -Xmx8g matchings.PermutedClustering \
    --initRandom 123 \
    --experimentConfigs.managedExecutionFolder false \
    --experimentConfigs.saveStandardStreams false \
    --experimentConfigs.recordExecutionInfo false \
    --experimentConfigs.recordGitInfo false \
    --model.nGroups $nGroups \
    --model.groupSize $groupSize \
    --model.observations.file data.csv \
    --engine PT \
    --engine.nScans 2_000 \
    --engine.nThreads MAX \
    --engine.nChains 8
  """   
}


// Gary Zhu's version
process calculateESS {
  input:
    file samples
  publishDir deliverableDir, mode: 'copy', overwrite: true  
  """
  #!/usr/bin/Rscript
  
  
  # use this line to run
  # rm -rf work | rm trace.txt | nextflow run ./permuted-clustering.nf -with-trace
    
  nGroups = $nGroups
  groupSize = $groupSize
  from_vertex = 0
  to_vertex = 0
  
  trace = read.table("../../../trace.txt",sep='\t',header=TRUE)
  data <- read.csv("samples/permutations.csv")

  dur = as.double(substr(trace[5,8],0,4))

  x = rep(0,as.integer(dim(data)[1]/(groupSize*nGroups)))
  k = 0
  for (i in 1:as.integer(dim(data)[1]/(groupSize*nGroups))) {
    for (j in 1:groupSize) {
      if (j == from_vertex+1 & as.integer(data[k+j,'value']) == to_vertex) {
        x[i] = 1
      }
    }
    k = k + groupSize*nGroups
  }

  N = length(x)
  v_up = sum((x-sum(x)/N)^2)/(N-1)

  I = x[1:sqrt(N)]
  batch_size = sqrt(N)

  incr = floor(sqrt(N))
  up_idx = floor(sqrt(N))
  num_batch = N%/%incr
  I = rep(0,num_batch)

  i = 1
  while (i<=num_batch) {
    x_batch = x[(up_idx-incr+1):up_idx]
    I[i] = mean(x_batch)
    up_idx = up_idx + incr
    i = i + 1
  }
  
  M = length(I)
  v_down = sum((I-sum(I)/M)^2)/(M-1)
  ess = v_up/v_down*sqrt(N)
  ess_per_sec = ess/dur

  write(paste("ess_per_sec =",ess/dur),file="../../../deliverables/permuted-clustering/ess_per_sec.txt")

  """
}

process plotPosterior {
  input:
    file samples
  output:
    file "*.pdf" 
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  #!/usr/bin/env Rscript
  require("ggplot2")
  require("dplyr")

  data <- read.csv("samples/permutations.csv")
    
  p <- ggplot(data, aes(x = factor(index_0),fill = factor(value))) + 
    geom_bar(position = "fill") +
    xlab("Group index") + 
    ylab("Proportion") + 
    facet_grid(permutation_index ~ .) + 
    theme_bw()
    
  ggsave("permutations-posterior.pdf", width = 15, height = 20)
  
  data <- read.csv("samples/means.csv")
  
  p <- ggplot(data, aes(x=sample,y=value,colour=factor(index_0))) +
    geom_line() +
    theme_bw()
  ggsave("means-trace.pdf", width = 15, height = 5)
  """
}

process summarizePipeline {
  cache false
  
  output:
      file 'pipeline-info.txt'
      
  publishDir deliverableDir, mode: 'copy', overwrite: true
  
  """
  echo 'scriptName: $workflow.scriptName' >> pipeline-info.txt
  echo 'start: $workflow.start' >> pipeline-info.txt
  echo 'runName: $workflow.runName' >> pipeline-info.txt
  echo 'nextflow.version: $workflow.nextflow.version' >> pipeline-info.txt
  """
}
