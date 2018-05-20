#!/bin/bash

ProjectPath=../data/Projects
ProjectName=ant
outputPath=../data/LiveStudy
FindBugsPath=../data/findbugs-3.0.1
java -Xmx2g -jar ${FindBugsPath}/lib/findbugs.jar -textui -low -xml -outputFile ${outputPath}/${ProjectName}.xml $ProjectPath/$ProjectName/
