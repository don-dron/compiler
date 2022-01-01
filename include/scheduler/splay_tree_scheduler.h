#include <scheduler/scheduler.h>
#include <structures/splay_tree.h>

struct scheduler_manager
{
    splay_tree* tree;
    spinlock lock;
};

typedef struct fiber_node
{
    splay_node node;
    fiber *fib;
} fiber_node;