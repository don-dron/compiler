cmake_minimum_required(VERSION 3.17)

set(CMAKE_ASM_NASM_SOURCE_FILE_EXTENSIONS nasm asm S)
set(CMAKE_ASM_NASM_OBJECT_FORMAT bin)
set(CMAKE_ASM_NASM_FLAGS "-e")
set(CMAKE_ASM_NASM_LINK_EXECUTABLE "nasm lib/scheduler/switch_context.S -o switch_context.o")
ENABLE_LANGUAGE(ASM)

project(compiler C ASM)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 14)

include_directories(include)
include_directories(include/locks)
include_directories(include/scheduler)
include_directories(include/structures)

set(CAN_USE_ASSEMBLER TRUE)
enable_language(ASM)

if (APPLE)
    string(APPEND CMAKE_ASM_FLAGS "--prefix _")
endif (APPLE)

set_property(SOURCE lib/scheduler/switch_context.S PROPERTY LANGUAGE C)

add_executable(compiler
        lib/scheduler/switch_context.S
        include/locks/atomics.h
        include/locks/spinlock.h
        include/locks/wait_group.h
        include/scheduler/context.h
        include/scheduler/coroutine.h
        include/scheduler/fiber.h
        include/scheduler/fibonacci_heap_scheduler.h
        include/scheduler/local_queues_scheduler.h
        include/scheduler/local_queues_with_steal_scheduler.h
        include/scheduler/manager.h
        include/scheduler/rb_tree_scheduler.h
        include/scheduler/scheduler.h
        include/scheduler/splay_tree_scheduler.h
        include/scheduler/thin_heap_scheduler.h
        include/structures/fibonacci_heap.h
        include/structures/hash_map.h
        include/structures/lf_stack.h
        include/structures/list.h
        include/structures/rb_tree.h
        include/structures/splay_tree.h
        include/structures/thin_heap.h
        include/root.h
        lib/locks/atomics.c
        lib/locks/spinlock.c
        lib/locks/wait_group.c
        lib/scheduler/context.c
        lib/scheduler/coroutine.c
        lib/scheduler/fiber.c
        lib/scheduler/manager.c
        lib/scheduler/scheduler.c
        lib/scheduler/rb_tree_scheduler.c
        lib/structures/fibonacci_heap.c
        lib/structures/hash_map.c
        lib/structures/lf_stack.c
        lib/structures/list.c
        lib/structures/rb_tree.c
        lib/structures/splay_tree.c
        lib/structures/thin_heap.c
        lib/default.c
        lib/root_lib.c)