#include <scheduler/scheduler.h>
#include <structures/list.h>

struct scheduler_manager
{
    list **queues;
};

typedef struct fiber_node
{
    list_node lst_node;
    fiber *fib;
} fiber_node;