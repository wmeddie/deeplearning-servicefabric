#!/usr/bin/env bash

set -euf -o pipefail

function usage() {
    echo "package.sh inputFile"    
    exit 1
}

if [ "$#" -ne 1 ]
then
    usage
fi

../../activator assembly

mkdir -p target/nuget

$SPARK_HOME/bin/spark-submit \
  --master 'local[*]' \
  --class com.example.Train \
  target/scala-2.10/iris-model-assembly-1.0.jar \
  $1 \
  target/nuget/out1

mkdir -p target/nuget/package/content/
cp Package.nuspec target/nuget/package/

pushd target/nuget

cp out1.model.bin package/content/classifier.model.bin
cp out1.model.conf package/content/classifier.model.conf

pushd package
nuget pack Package.nuspec
popd
popd

