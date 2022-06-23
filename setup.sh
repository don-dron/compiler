#!/bin/bash

cwd=$(pwd)
setup='./lang_test'

SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

while [ -n "$1" ]
do
case "$1" in
-s) setup="$2"
   shift;;
*) echo "$1 Unknown flag" ;;
esac
shift
done

absScriptPath=$(cd "$(dirname "$SCRIPTPATH")"; pwd -P)/$(basename "$SCRIPTPATH")
absSetupPath=$(cd "$(dirname "$setup")"; pwd -P)/$(basename "$setup")

mkdir $absSetupPath
cd $absScriptPath

mkdir build

gcc -O3 -c -pthread -Wall -I ./include  -o ./build/atomics.o ./lib/locks/atomics.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/wait_group.o ./lib/locks/wait_group.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/spinlock.o ./lib/locks/spinlock.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/default.o ./lib/default.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/context.o ./lib/scheduler/context.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/local_queues_with_steal_scheduler.o ./lib/scheduler/local_queues_with_steal_scheduler.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/switch_context.o ./lib/scheduler/switch_context.S && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/fiber.o ./lib/scheduler/fiber.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/scheduler.o ./lib/scheduler/scheduler.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/coroutine.o ./lib/scheduler/coroutine.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/manager.o ./lib/scheduler/manager.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/lf_stack.o ./lib/structures/lf_stack.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/list.o ./lib/structures/list.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/fibonacci_heap.o ./lib/structures/fibonacci_heap.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/rb_tree.o ./lib/structures/rb_tree.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/hash_map.o ./lib/structures/hash_map.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/thin_heap.o ./lib/structures/thin_heap.c && \
gcc -O3 -c -pthread -Wall -I ./include  -o ./build/splay_tree.o ./lib/structures/splay_tree.c && \
ar rcs $absSetupPath/core_lib.a ./build/atomics.o ./build/wait_group.o ./build/spinlock.o ./build/default.o ./build/context.o \
./build/local_queues_with_steal_scheduler.o ./build/switch_context.o ./build/fiber.o ./build/scheduler.o ./build/coroutine.o \
./build/manager.o  ./build/lf_stack.o ./build/list.o ./build/fibonacci_heap.o ./build/rb_tree.o \
./build/hash_map.o ./build/thin_heap.o ./build/splay_tree.o

mvn clean install

cp $absScriptPath/target/lang-1.0-SNAPSHOT-jar-with-dependencies.jar $absSetupPath/langc.jar
cp $absScriptPath/lib/root_lib.c $absSetupPath/root_lib.c
mkdir $absSetupPath/lang
cp $absScriptPath/resources/stdlib $absSetupPath/lang/lib
cp -R $absScriptPath/include $absSetupPath/include
cp $absScriptPath/resources/run.sh $absSetupPath/run.sh
cp $absScriptPath/resources/root.h $absSetupPath/include/root.h

rm -r build
rm -r target