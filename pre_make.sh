#!/bin/sh

#VERSION=13
#BUILD=003
#DATE=13.08.2016 / 18\:34\:42

echo ==msearch======================
ver=13
echo Version: $ver

nr=$(cat src/main/resources/version.properties | grep NR | sed 's#NR=##g')
echo Nr-vor:  $nr

nr=$((nr + 1))
echo Nr-nach: $nr

buildDate=$(date +%d.%m.%y\ -\ %H:%M:%S)
echo Date:    $buildDate
echo ==msearch======================

echo VERSION=$ver > src/main/resources/version.properties
echo NR=$nr >> src/main/resources/version.properties
echo BUILD=$ver-$nr >> src/main/resources/version.properties
echo DATE=$buildDate >> src/main/resources/version.properties

cp src/main/resources/version.properties build/classes
#cp src/main/resources/version.properties /home/emil/daten/software/mediathek/mediathek/src
