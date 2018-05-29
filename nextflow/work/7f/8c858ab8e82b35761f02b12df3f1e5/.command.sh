#!/bin/bash -ue
set -e
tail -n +2 generated/observations.csv | awk -F "," '{print $2, ",", $3, ",", $4}' | sed 's/ //g' > data.csv
java -cp `cat classpath` -Xmx8g matchings.PermutedClustering     --initRandom 123     --experimentConfigs.managedExecutionFolder false     --experimentConfigs.saveStandardStreams false     --experimentConfigs.recordExecutionInfo false     --experimentConfigs.recordGitInfo false     --model.nGroups 20     --model.groupSize 5     --model.observations.file data.csv     --engine PT     --engine.nScans 2_000     --engine.nThreads MAX     --engine.nChains 8
