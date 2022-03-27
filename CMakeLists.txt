cmake_minimum_required(VERSION 3.16)

set(CMAKE_ASM_NASM_SOURCE_FILE_EXTENSIONS nasm asm S)
set(CMAKE_ASM_NASM_OBJECT_FORMAT bin)
set(CMAKE_ASM_NASM_FLAGS "-e")
set(CMAKE_ASM_NASM_LINK_EXECUTABLE "nasm lib/scheduler/switch_context.S -o switch_context.o")
ENABLE_LANGUAGE(ASM)

project(compiler_target C ASM)
set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 14)

include_directories(include)
include_directories(include/locks)
include_directories(include/scheduler)
include_directories(include/structures)

set(CAN_USE_ASSEMBLER TRUE)
enable_language(ASM)

add_custom_command(
        OUTPUT ../lib/default.c
        COMMAND ../compile.sh
)

if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0 -ggdb -D IS_LINUX")
else ()
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O0 -lldb")
endif ()

#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} --main-stacksize=223232")

set_property(SOURCE lib/scheduler/switch_context.S PROPERTY LANGUAGE C)


find_package(Threads REQUIRED)
add_executable(compiler_target
        out.o
        lib/scheduler/switch_context.S
        lib/locks/atomics.c
        lib/locks/spinlock.c
        lib/locks/wait_group.c
        lib/scheduler/context.c
        lib/scheduler/coroutine.c
        lib/scheduler/fiber.c
        lib/scheduler/manager.c
        lib/scheduler/scheduler.c
        lib/scheduler/local_queues_with_steal_scheduler.c
        lib/structures/fibonacci_heap.c
        lib/structures/hash_map.c
        lib/structures/lf_stack.c
        lib/structures/list.c
        lib/structures/rb_tree.c
        lib/structures/splay_tree.c
        lib/structures/thin_heap.c
        lib/default.c
        lib/root_lib.c)

target_link_libraries (compiler_target ${CMAKE_THREAD_LIBS_INIT})

#SRCS = $(shell find ./lib -type f -name "*.c")
#HEAD = $(shell find ./include -type f -name "*.h")
#OBJS = $(SRCS:.c=.o)
#DEPS = Makefile.depend
#
#clean:
#rm -rf build
#
#build:
#mvn clean install && \
#mvn exec:java -Dexec.mainClass="lang.Main" -Dexec.args="-i ./project9" && \
#mkdir build && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/atomics.o ./lib/locks/atomics.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/wait_group.o ./lib/locks/wait_group.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/spinlock.o ./lib/locks/spinlock.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/default.o ./lib/default.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/context.o ./lib/scheduler/context.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/rb_tree_scheduler.o ./lib/scheduler/rb_tree_scheduler.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/switch_context.o ./lib/scheduler/switch_context.S && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/fiber.o ./lib/scheduler/fiber.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/scheduler.o ./lib/scheduler/scheduler.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/coroutine.o ./lib/scheduler/coroutine.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/manager.o ./lib/scheduler/manager.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/root_lib.o ./lib/root_lib.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/lf_stack.o ./lib/structures/lf_stack.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/list.o ./lib/structures/list.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/fibonacci_heap.o ./lib/structures/fibonacci_heap.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/rb_tree.o ./lib/structures/rb_tree.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/hash_map.o ./lib/structures/hash_map.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/thin_heap.o ./lib/structures/thin_heap.c && \
#gcc -O3 -c -pthread -Wall -I ./include -D IS_LINUX -o ./build/splay_tree.o ./lib/structures/splay_tree.c && \
#ar rcs lib.a ./build/atomics.o ./build/wait_group.o ./build/spinlock.o ./build/default.o ./build/context.o \
#./build/rb_tree_scheduler.o ./build/switch_context.o ./build/fiber.o ./build/scheduler.o ./build/coroutine.o \
#./build/manager.o ./build/root_lib.o ./build/lf_stack.o ./build/list.o ./build/fibonacci_heap.o ./build/rb_tree.o \
#./build/hash_map.o ./build/thin_heap.o ./build/splay_tree.o && \
#llc -filetype=obj --relocation-model=pic out.ll && \
#gcc -g out.o lib.a -lpthread -D IS_LINUX -o program
#
#run:
#./program
#
#all: clean build run