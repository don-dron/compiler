#!/bin/bash
cd ..
mvn clean install && \
mvn exec:java -Dexec.mainClass="lang.Main" -Dexec.args="-i ./project9"
llc -filetype=obj --relocation-model=pic out.ll