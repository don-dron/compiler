#include <scheduler/scheduler.h>
#include <structures/rb_tree.h>

struct scheduler_manager
{
    struct rbtree* tree;
    spinlock lock;
};

typedef struct fiber_node
{
    struct rbnode rb_node;
    fiber *fib;
} fiber_node;