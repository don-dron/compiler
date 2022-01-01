#include <scheduler/rb_tree_scheduler.h>

static int cmp(const void *lhs, const void *rhs)
{
    fiber *first = (fiber *)lhs;
    fiber *second = (fiber *)rhs;

    if (first->level < second->level)
    {
        return 1;
    }
    else if (first->level > second->level)
    {
        return -1;
    }
    else
    {
        if (first->vruntime < second->vruntime)
        {
            return 1;
        }
        else if (first->vruntime > second->vruntime)
        {
            return -1;
        }
        else
        {
            if (((unsigned long)first) > ((unsigned long)second))
            {
                return -1;
            }
            else if (((unsigned long)first) < ((unsigned long)second))
            {
                return 1;
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
    lock_spinlock(&sched->manager->lock);

    manager->tree = (struct rbtree *)malloc(sizeof(struct rbtree));
    struct rbnode *root = (struct rbnode *)malloc(sizeof(struct rbnode));
    root->key = NULL;
    rbtree_init(manager->tree, root, cmp);
    asm volatile("mfence" ::
                     : "memory");
    unlock_spinlock(&sched->manager->lock);
    return 0;
}

fiber *get_from_pool()
{
    scheduler* current_scheduler = get_current_scheduler();
    asm volatile("mfence" ::
                     : "memory");
    lock_spinlock(&current_scheduler->manager->lock);
    struct rbtree *tree = current_scheduler->manager->tree;
    struct rbnode *fib_node = rbtree_min(tree);

    if (fib_node == NULL)
    {
        unlock_spinlock(&current_scheduler->manager->lock);
        return NULL;
    }

    fiber *res = (fiber *)fib_node->key;
    rbtree_delete(tree, fib_node);

    unlock_spinlock(&current_scheduler->manager->lock);
    asm volatile("mfence" ::
                     : "memory");
    return res;
}

void return_to_pool(scheduler *sched, fiber *fib)
{
    asm volatile("mfence" ::
                     : "memory");
    struct rbnode *fib_node = (struct rbnode *)malloc(sizeof(struct rbnode));
    fib_node->key = fib;

    lock_spinlock(&sched->manager->lock);
    rbtree_insert(sched->manager->tree, (struct rbnode *)fib_node);
    unlock_spinlock(&sched->manager->lock);
    asm volatile("mfence" ::
                     : "memory");
}

int free_scheduler_manager(scheduler *sched)
{
    scheduler_manager *manager = sched->manager;

    // rbtree_destroy(manager->tree);
    free(manager->tree);
    free(sched->manager);
    return 0;
}