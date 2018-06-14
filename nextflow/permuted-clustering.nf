#!/usr/bin/env nextflow

deliverableDir = 'deliverables/' + workflow.scriptName.replace('.nf','')

nGroups = 2
minGroupSize=3
maxGroupSize=5

samplerName="PermutationSampler"
excludedSampler="PermutationSamplerLB"

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
  jars_hash3
}
classpath.into {
  classpath1
  classpath2
  classpath3
}


process generateData {
  cache 'deep'
  input:
    each x from minGroupSize..maxGroupSize
    file classpath1
    file jars_hash1
  output:
    file "generated_${x}" into data
    
  """
  set -e
  java -cp `cat classpath` -Xmx8g matchings.PermutedClustering \
    --experimentConfigs.managedExecutionFolder false \
    --experimentConfigs.saveStandardStreams false \
    --experimentConfigs.recordExecutionInfo false \
    --experimentConfigs.recordGitInfo false \
    --samplers.excluded matchings.${excludedSampler} \
    --model.nGroups $nGroups \
    --model.groupSize ${x} \
    --engine Forward
  mv samples generated_${x}
  """
}

process runInference {
  cache 'deep'
  input:
    val excludedSampler
    each x from minGroupSize..maxGroupSize
    file data from data.collect()
    file classpath2
    file jars_hash2
  output:
    file "samples_${x}" into samples
    file "monitoring_${x}" into monitorings
  """
  set -e
  tail -n +2 generated_${x}/observations.csv | awk -F "," '{print \$2, ",", \$3, ",", \$4}' | sed 's/ //g' > data.csv
  java -cp `cat classpath` -Xmx8g matchings.PermutedClustering \
    --initRandom 123 \
    --experimentConfigs.managedExecutionFolder false \
    --experimentConfigs.saveStandardStreams false \
    --experimentConfigs.recordExecutionInfo false \
    --experimentConfigs.recordGitInfo false \
    --samplers.excluded matchings.${excludedSampler} \
    --model.nGroups $nGroups \
    --model.groupSize ${x} \
    --model.observations.file data.csv \
    --engine PT \
    --engine.nScans 2_000 \
    --engine.nThreads MAX \
    --engine.nChains 1
  mv samples samples_${x}
  mv monitoring monitoring_${x}
  """   
}

process calculateESS {
  cache 'deep'
  input:
    each x from minGroupSize..maxGroupSize
    file samples from samples.collect()
    file monitoring from monitorings.collect()
    file classpath3
    file jars_hash3  
  output:
    file "essps_${x}.csv" into essps
  """
  INF_DURATION=\$(tail -n +2 monitoring_${x}/runningTimeSummary.tsv | cut -c16-99 | tr -d '[:space:]')
  set -e
  java -cp `cat classpath` -Xmx8g matchings.ComputePermutationESS \
    --nGroups $nGroups \
    --groupSize ${x} \
    --csvFile samples_${x}/permutations.csv \
    --infDuration \$INF_DURATION \
    --kthPerm 1
  """
}

process aggregateCSV {
  cache 'deep'
  input:
    val samplerName
    file essps from essps.collect()
  output:
    file "aggregated_${samplerName}.csv" into aggregatedCSV
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  head -n 1 essps_${minGroupSize}.csv > aggregated_${samplerName}.csv
  for x in `seq $minGroupSize $maxGroupSize`;
  do
    tail -n +2 essps_\$x.csv >> aggregated_${samplerName}.csv
  done
  """
}


process plot {
  cache 'deep'
  input:
    file aggregatedCSV
    val samplerName
  output:
    file 'essps_plot.pdf'
  publishDir deliverableDir, mode: 'copy', overwrite: true
  """
  /usr/bin/Rscript ../../../plot.R "aggregated_${samplerName}.csv"
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
