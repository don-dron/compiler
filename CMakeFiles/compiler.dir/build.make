# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.16

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = "/home/dron/Рабочий стол/bachelor/compiler"

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = "/home/dron/Рабочий стол/bachelor/compiler"

# Include any dependencies generated for this target.
include CMakeFiles/compiler.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/compiler.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/compiler.dir/flags.make

CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o: lib/scheduler/switch_context.S
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_1) "Building C object CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/switch_context.S"

CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/switch_context.S" > CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.i

CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/switch_context.S" -o CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.s

CMakeFiles/compiler.dir/lib/locks/atomics.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/locks/atomics.c.o: lib/locks/atomics.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_2) "Building C object CMakeFiles/compiler.dir/lib/locks/atomics.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/locks/atomics.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/atomics.c"

CMakeFiles/compiler.dir/lib/locks/atomics.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/locks/atomics.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/atomics.c" > CMakeFiles/compiler.dir/lib/locks/atomics.c.i

CMakeFiles/compiler.dir/lib/locks/atomics.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/locks/atomics.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/atomics.c" -o CMakeFiles/compiler.dir/lib/locks/atomics.c.s

CMakeFiles/compiler.dir/lib/locks/spinlock.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/locks/spinlock.c.o: lib/locks/spinlock.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_3) "Building C object CMakeFiles/compiler.dir/lib/locks/spinlock.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/locks/spinlock.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/spinlock.c"

CMakeFiles/compiler.dir/lib/locks/spinlock.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/locks/spinlock.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/spinlock.c" > CMakeFiles/compiler.dir/lib/locks/spinlock.c.i

CMakeFiles/compiler.dir/lib/locks/spinlock.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/locks/spinlock.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/spinlock.c" -o CMakeFiles/compiler.dir/lib/locks/spinlock.c.s

CMakeFiles/compiler.dir/lib/locks/wait_group.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/locks/wait_group.c.o: lib/locks/wait_group.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_4) "Building C object CMakeFiles/compiler.dir/lib/locks/wait_group.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/locks/wait_group.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/wait_group.c"

CMakeFiles/compiler.dir/lib/locks/wait_group.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/locks/wait_group.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/wait_group.c" > CMakeFiles/compiler.dir/lib/locks/wait_group.c.i

CMakeFiles/compiler.dir/lib/locks/wait_group.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/locks/wait_group.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/locks/wait_group.c" -o CMakeFiles/compiler.dir/lib/locks/wait_group.c.s

CMakeFiles/compiler.dir/lib/scheduler/context.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/context.c.o: lib/scheduler/context.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_5) "Building C object CMakeFiles/compiler.dir/lib/scheduler/context.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/context.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/context.c"

CMakeFiles/compiler.dir/lib/scheduler/context.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/context.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/context.c" > CMakeFiles/compiler.dir/lib/scheduler/context.c.i

CMakeFiles/compiler.dir/lib/scheduler/context.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/context.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/context.c" -o CMakeFiles/compiler.dir/lib/scheduler/context.c.s

CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o: lib/scheduler/coroutine.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_6) "Building C object CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/coroutine.c"

CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/coroutine.c" > CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.i

CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/coroutine.c" -o CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.s

CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o: lib/scheduler/fiber.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_7) "Building C object CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/fiber.c"

CMakeFiles/compiler.dir/lib/scheduler/fiber.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/fiber.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/fiber.c" > CMakeFiles/compiler.dir/lib/scheduler/fiber.c.i

CMakeFiles/compiler.dir/lib/scheduler/fiber.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/fiber.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/fiber.c" -o CMakeFiles/compiler.dir/lib/scheduler/fiber.c.s

CMakeFiles/compiler.dir/lib/scheduler/manager.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/manager.c.o: lib/scheduler/manager.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_8) "Building C object CMakeFiles/compiler.dir/lib/scheduler/manager.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/manager.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/manager.c"

CMakeFiles/compiler.dir/lib/scheduler/manager.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/manager.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/manager.c" > CMakeFiles/compiler.dir/lib/scheduler/manager.c.i

CMakeFiles/compiler.dir/lib/scheduler/manager.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/manager.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/manager.c" -o CMakeFiles/compiler.dir/lib/scheduler/manager.c.s

CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o: lib/scheduler/scheduler.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_9) "Building C object CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/scheduler.c"

CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/scheduler.c" > CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.i

CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/scheduler.c" -o CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.s

CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o: lib/scheduler/local_queues_with_steal_scheduler.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_10) "Building C object CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/local_queues_with_steal_scheduler.c"

CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/local_queues_with_steal_scheduler.c" > CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.i

CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/scheduler/local_queues_with_steal_scheduler.c" -o CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.s

CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o: lib/structures/fibonacci_heap.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_11) "Building C object CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/fibonacci_heap.c"

CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/fibonacci_heap.c" > CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.i

CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/fibonacci_heap.c" -o CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.s

CMakeFiles/compiler.dir/lib/structures/hash_map.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/hash_map.c.o: lib/structures/hash_map.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_12) "Building C object CMakeFiles/compiler.dir/lib/structures/hash_map.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/hash_map.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/hash_map.c"

CMakeFiles/compiler.dir/lib/structures/hash_map.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/hash_map.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/hash_map.c" > CMakeFiles/compiler.dir/lib/structures/hash_map.c.i

CMakeFiles/compiler.dir/lib/structures/hash_map.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/hash_map.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/hash_map.c" -o CMakeFiles/compiler.dir/lib/structures/hash_map.c.s

CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o: lib/structures/lf_stack.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_13) "Building C object CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/lf_stack.c"

CMakeFiles/compiler.dir/lib/structures/lf_stack.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/lf_stack.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/lf_stack.c" > CMakeFiles/compiler.dir/lib/structures/lf_stack.c.i

CMakeFiles/compiler.dir/lib/structures/lf_stack.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/lf_stack.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/lf_stack.c" -o CMakeFiles/compiler.dir/lib/structures/lf_stack.c.s

CMakeFiles/compiler.dir/lib/structures/list.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/list.c.o: lib/structures/list.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_14) "Building C object CMakeFiles/compiler.dir/lib/structures/list.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/list.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/list.c"

CMakeFiles/compiler.dir/lib/structures/list.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/list.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/list.c" > CMakeFiles/compiler.dir/lib/structures/list.c.i

CMakeFiles/compiler.dir/lib/structures/list.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/list.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/list.c" -o CMakeFiles/compiler.dir/lib/structures/list.c.s

CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o: lib/structures/rb_tree.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_15) "Building C object CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/rb_tree.c"

CMakeFiles/compiler.dir/lib/structures/rb_tree.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/rb_tree.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/rb_tree.c" > CMakeFiles/compiler.dir/lib/structures/rb_tree.c.i

CMakeFiles/compiler.dir/lib/structures/rb_tree.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/rb_tree.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/rb_tree.c" -o CMakeFiles/compiler.dir/lib/structures/rb_tree.c.s

CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o: lib/structures/splay_tree.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_16) "Building C object CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/splay_tree.c"

CMakeFiles/compiler.dir/lib/structures/splay_tree.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/splay_tree.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/splay_tree.c" > CMakeFiles/compiler.dir/lib/structures/splay_tree.c.i

CMakeFiles/compiler.dir/lib/structures/splay_tree.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/splay_tree.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/splay_tree.c" -o CMakeFiles/compiler.dir/lib/structures/splay_tree.c.s

CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o: lib/structures/thin_heap.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_17) "Building C object CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/thin_heap.c"

CMakeFiles/compiler.dir/lib/structures/thin_heap.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/structures/thin_heap.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/thin_heap.c" > CMakeFiles/compiler.dir/lib/structures/thin_heap.c.i

CMakeFiles/compiler.dir/lib/structures/thin_heap.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/structures/thin_heap.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/structures/thin_heap.c" -o CMakeFiles/compiler.dir/lib/structures/thin_heap.c.s

CMakeFiles/compiler.dir/lib/default.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/default.c.o: lib/default.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_18) "Building C object CMakeFiles/compiler.dir/lib/default.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/default.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/default.c"

CMakeFiles/compiler.dir/lib/default.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/default.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/default.c" > CMakeFiles/compiler.dir/lib/default.c.i

CMakeFiles/compiler.dir/lib/default.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/default.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/default.c" -o CMakeFiles/compiler.dir/lib/default.c.s

CMakeFiles/compiler.dir/lib/root_lib.c.o: CMakeFiles/compiler.dir/flags.make
CMakeFiles/compiler.dir/lib/root_lib.c.o: lib/root_lib.c
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_19) "Building C object CMakeFiles/compiler.dir/lib/root_lib.c.o"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -o CMakeFiles/compiler.dir/lib/root_lib.c.o   -c "/home/dron/Рабочий стол/bachelor/compiler/lib/root_lib.c"

CMakeFiles/compiler.dir/lib/root_lib.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/compiler.dir/lib/root_lib.c.i"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E "/home/dron/Рабочий стол/bachelor/compiler/lib/root_lib.c" > CMakeFiles/compiler.dir/lib/root_lib.c.i

CMakeFiles/compiler.dir/lib/root_lib.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/compiler.dir/lib/root_lib.c.s"
	/usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S "/home/dron/Рабочий стол/bachelor/compiler/lib/root_lib.c" -o CMakeFiles/compiler.dir/lib/root_lib.c.s

# Object files for target compiler
compiler_OBJECTS = \
"CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o" \
"CMakeFiles/compiler.dir/lib/locks/atomics.c.o" \
"CMakeFiles/compiler.dir/lib/locks/spinlock.c.o" \
"CMakeFiles/compiler.dir/lib/locks/wait_group.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/context.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/manager.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o" \
"CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o" \
"CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o" \
"CMakeFiles/compiler.dir/lib/structures/hash_map.c.o" \
"CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o" \
"CMakeFiles/compiler.dir/lib/structures/list.c.o" \
"CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o" \
"CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o" \
"CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o" \
"CMakeFiles/compiler.dir/lib/default.c.o" \
"CMakeFiles/compiler.dir/lib/root_lib.c.o"

# External object files for target compiler
compiler_EXTERNAL_OBJECTS = \
"/home/dron/Рабочий стол/bachelor/compiler/out.o"

compiler: CMakeFiles/compiler.dir/lib/scheduler/switch_context.S.o
compiler: CMakeFiles/compiler.dir/lib/locks/atomics.c.o
compiler: CMakeFiles/compiler.dir/lib/locks/spinlock.c.o
compiler: CMakeFiles/compiler.dir/lib/locks/wait_group.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/context.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/coroutine.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/fiber.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/manager.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/scheduler.c.o
compiler: CMakeFiles/compiler.dir/lib/scheduler/local_queues_with_steal_scheduler.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/fibonacci_heap.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/hash_map.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/lf_stack.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/list.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/rb_tree.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/splay_tree.c.o
compiler: CMakeFiles/compiler.dir/lib/structures/thin_heap.c.o
compiler: CMakeFiles/compiler.dir/lib/default.c.o
compiler: CMakeFiles/compiler.dir/lib/root_lib.c.o
compiler: out.o
compiler: CMakeFiles/compiler.dir/build.make
compiler: CMakeFiles/compiler.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir="/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles" --progress-num=$(CMAKE_PROGRESS_20) "Linking C executable compiler"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/compiler.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/compiler.dir/build: compiler

.PHONY : CMakeFiles/compiler.dir/build

CMakeFiles/compiler.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/compiler.dir/cmake_clean.cmake
.PHONY : CMakeFiles/compiler.dir/clean

CMakeFiles/compiler.dir/depend:
	cd "/home/dron/Рабочий стол/bachelor/compiler" && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" "/home/dron/Рабочий стол/bachelor/compiler" "/home/dron/Рабочий стол/bachelor/compiler" "/home/dron/Рабочий стол/bachelor/compiler" "/home/dron/Рабочий стол/bachelor/compiler" "/home/dron/Рабочий стол/bachelor/compiler/CMakeFiles/compiler.dir/DependInfo.cmake" --color=$(COLOR)
.PHONY : CMakeFiles/compiler.dir/depend

