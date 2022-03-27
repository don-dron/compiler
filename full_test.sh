#!/bin/bash
#clang -S -emit-llvm text.c
mvn clean install && \
mvn exec:java -Dexec.mainClass="lang.Main" -Dexec.args="-i ./project9"
llc -filetype=obj --relocation-model=pic out.ll
cmake --configure ./CMakeLists.txt
cmake --build ./ --target clean
cmake --build ./ --target compiler_target
./compiler_target