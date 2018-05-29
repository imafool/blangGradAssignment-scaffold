#!/bin/bash -ue
set -e
java -cp `cat classpath` -Xmx8g matchings.PermutedClustering     --experimentConfigs.managedExecutionFolder false     --experimentConfigs.saveStandardStreams false     --experimentConfigs.recordExecutionInfo false     --experimentConfigs.recordGitInfo false     --model.nGroups 20     --model.groupSize 5     --engine Forward
mv samples generated
