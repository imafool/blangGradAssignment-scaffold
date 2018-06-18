// params with default values:
params.SAMPLERS = ["PermutationSampler", "PermutationSamplerLB"] 
params.sampler = "PermutationSampler" 
params.minGS = 3
params.maxGS = 5
params.nGroups = 2
params.lbFactor = 0.5

params.deliverableDir = "deliverables/" +
                        "${workflow.scriptName.replace('.nf', '')}/" + 
                        params.sampler + 
                        "/" + 
                        "GS${params.minGS}-${params.maxGS}LBF${params.lbFactor}"
                      
excludedSamplers = (params.SAMPLERS - params.sampler).collect({"matchings." + it}).join(" ")

// nextflow run permuted-clustering.nf --sampler "PermutationSamplerLB" --minGS 6 --maxGS 7 --lbFactor 0.6


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
    each x from params.minGS..params.maxGS
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
    --samplers.excluded $excludedSamplers \
    --model.nGroups $params.nGroups \
    --model.lbFactor $params.lbFactor \
    --model.groupSize ${x} \
    --engine Forward
  mv samples generated_${x}
  """
}

process runInference {
  cache 'deep'
  input:  
    each x from params.minGS..params.maxGS
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
    --samplers.excluded $excludedSamplers \
    --model.nGroups $params.nGroups \
    --model.lbFactor $params.lbFactor \
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
    each x from params.minGS..params.maxGS
    file samples from samples.collect()
    file monitoring from monitorings.collect()
    file classpath3
    file jars_hash3  
  output:
    file "essps_${x}.csv" into essps
  """
  INF_DURATION=\$(tail -n +2 monitoring_${x}/runningTimeSummary.tsv | cut -c16-99 | tr -d '[:space:]')
  set -e
  java -cp `cat classpath` -Xmx8g matchings.ComputePermutationESS \\
    --nGroups $params.nGroups \
    --groupSize ${x} \
    --csvFile samples_${x}/permutations.csv \
    --infDuration \$INF_DURATION \
    --kthPerm 1
  """
}

process aggregateCSV {
  cache 'deep'
  input:
    file essps from essps.collect()
  output:
    file "aggregated.csv" into aggregatedCSV
  publishDir params.deliverableDir, mode: 'copy', overwrite: true
  """
  head -n 1 essps_${params.minGS}.csv > aggregated.csv
  for x in `seq $params.minGS $params.maxGS`;
  do
    tail -n +2 essps_\$x.csv >> aggregated.csv
  done
  """
}

process plot {
  cache 'deep'
  input:
    file aggregatedCSV
  output:
  file "essps_plot.pdf"
  publishDir params.deliverableDir, mode: 'copy', overwrite: true
  """
  Rscript ${workflow.launchDir}/plot.R "aggregated.csv"
  """
}

process summarizePipeline {
  cache false
  output:
      file "pipeline-info.txt"
      
  publishDir params.deliverableDir, mode: 'copy', overwrite: true
  
  """
  echo 'scriptName: $workflow.scriptName' >> pipeline-info.txt
  echo 'start: $workflow.start' >> pipeline-info.txt
  echo 'runName: $workflow.runName' >> pipeline-info.txt
  echo 'nextflow.version: $workflow.nextflow.version' >> pipeline-info.txt
  """
}
