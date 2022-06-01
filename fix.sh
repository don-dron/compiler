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
ar rcs core_lib.a ./build/atomics.o ./build/wait_group.o ./build/spinlock.o ./build/default.o ./build/context.o \
./build/local_queues_with_steal_scheduler.o ./build/switch_context.o ./build/fiber.o ./build/scheduler.o ./build/coroutine.o \
./build/manager.o  ./build/lf_stack.o ./build/list.o ./build/fibonacci_heap.o ./build/rb_tree.o \
./build/hash_map.o ./build/thin_heap.o ./build/splay_tree.o
#gcc -g out.o core_lib.a -lpthread  -o program