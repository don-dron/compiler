#!/bin/bash
./compile.sh
cmake --configure ./CMakeLists.txt
cmake --build ./ --target clean
cmake --build ./ --target compiler
./compiler