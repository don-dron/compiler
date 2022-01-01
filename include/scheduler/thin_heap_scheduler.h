#include <scheduler/scheduler.h>
#include <structures/thin_heap.h>

struct scheduler_manager
{
    struct thin_heap* heap;
    spinlock lock;
};

typedef struct fiber_node
{
    struct thin_heap_node lst_node;
    fiber *fib;
} fiber_node;