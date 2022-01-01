#include <scheduler/splay_tree_scheduler.h>

static int cmp(const void *lhs, const void *rhs)
{
    fiber_node *first = (fiber_node *)lhs;
    fiber_node *second = (fiber_node *)rhs;

    if (first->fib->level < second->fib->level)
    {
        return 1;
    }
    else if (first->fib->level > second->fib->level)
    {
        return -1;
    }
    else
    {
        if (first->fib->vruntime < second->fib->vruntime)
        {
            return 1;
        }
        else if (first->fib->vruntime > second->fib->vruntime)
        {
            return -1;
        }
        else
        {
            if (((unsigned long)first->fib) > ((unsigned long)second->fib))
            {
                return 1;
            }
            else if (((unsigned long)first->fib) < ((unsigned long)second->fib))
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }
}

int create_scheduler_manager(scheduler *sched)
{
    sched->manager = (scheduler_manager *)malloc(sizeof(scheduler_manager));
    scheduler_manager *manager = sched->manager;
    init_spinlock(&manager->lock);

    manager->tree = splay_tree_new_tree((splay_tree_cmp)cmp);
    asm volatile("mfence":::"memory");
    return 0;
}

fiber *get_from_pool()
{
    scheduler* current_scheduler = get_current_scheduler();
    asm volatile("mfence":::"memory");
    lock_spinlock(&current_scheduler->manager->lock);
    splay_tree *tree = current_scheduler->manager->tree;
    splay_node *node = splay_tree_first(tree);
    if (node)
    {
        splay_tree_delete_hint(tree, node);
        fiber *res = ((fiber_node *)node->value)->fib;
        unlock_spinlock(&current_scheduler->manager->lock);
        asm volatile("mfence":::"memory");
        return res;
    }
    else
    {
        unlock_spinlock(&current_scheduler->manager->lock);
        asm volatile("mfence":::"memory");
        return NULL;
    }
}

void return_to_pool(scheduler *sched, fiber *fib)
{
    asm volatile("mfence":::"memory");
    lock_spinlock(&sched->manager->lock);
    fiber_node *fib_node = (fiber_node *)malloc(sizeof(fiber_node));
    fib_node->fib = fib;
    splay_tree_insert(sched->manager->tree, fib_node);
    unlock_spinlock(&sched->manager->lock);
    asm volatile("mfence":::"memory");
}

int free_scheduler_manager(scheduler *sched)
{
    // scheduler_manager *manager = sched->manager;

    // Free TODO
    free(sched->manager);
    return 0;
}