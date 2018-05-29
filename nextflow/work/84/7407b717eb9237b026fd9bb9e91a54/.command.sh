#!/bin/bash -ue
set -e
current_dir=`pwd`
cd ../../../..
./gradlew build
./gradlew printClasspath | grep CLASSPATH-ENTRY | sort | sed 's/CLASSPATH[-]ENTRY //' > $current_dir/temp_classpath
for file in `ls build/libs/*jar`
do
  echo `pwd`/$file >> $current_dir/temp_classpath
done
cd -
touch jars_hash
for jar_file in `cat temp_classpath`
do
  shasum $jar_file >> jars_hash
done
cat temp_classpath | paste -sd ":" - > classpath
