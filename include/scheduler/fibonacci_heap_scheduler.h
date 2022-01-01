#include <scheduler/scheduler.h>
#include <structures/fibonacci_heap.h>

struct scheduler_manager
{
    struct fib_heap* heap;
    spinlock lock;
};