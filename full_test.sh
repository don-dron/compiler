#!/bin/bash
./compile.sh
cmake ./
cmake --build ./cmake-build-debug --target compiler
./cmake-build-debug/compiler