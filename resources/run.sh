#!/bin/bash

cwd=$(pwd)
input='./'
output='program'


while [ -n "$1" ]
do
case "$1" in
-i) input="$2"
   shift;;
-o) output="$2"
   shift;;
*) echo "$1 Unknown flag" ;;
esac
shift
done

SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

echo 'Compile Lang code to LLVM'

absIPath=$(cd "$(dirname "$input")"; pwd -P)/$(basename "$input")
absOPath=$(cd "$(dirname "$output")"; pwd -P)/$(basename "$output")

echo input: $absIPath
echo output: $absOPath

cd $SCRIPTPATH
echo $SCRIPTPATH

java -jar ./langc.jar -i $absIPath -o $absOPath

echo 'Compile LLVM to object file'
llc -filetype=obj --relocation-model=pic out.ll

echo 'Build execution file'
gcc -g out.o core_lib.a root_lib.c -I include -lpthread  -o $absOPath

rm out.ll
rm out.o

cd $cwd