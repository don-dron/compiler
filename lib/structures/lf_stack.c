#include <structures/lf_stack.h>

// static lf_stack_node *create_lf_stack_node(lf_stack_node *next_node)
// {
//     lf_stack_node *created_node = (lf_stack_node *)malloc(sizeof(lf_stack_node));
//     created_node->next = next_node;
//     created_node->list_mutex = 0;
//     return created_node;
// }

static lf_stack_head *create_lf_stack_head(lf_stack_node *next_node)
{
    lf_stack_head *created_head = (lf_stack_head *)malloc(sizeof(lf_stack_head));
    created_head->next = next_node;
    created_head->list_mutex = 0;
    return created_head;
}

static void free_lf_stack_nodes(lf_stack *stack)
{
    lf_stack_node *top = stack->head->next;
    lf_stack_node *curr_top = top;
    while (top != 0)
    {
        curr_top = top;
        top = top->next;
        free(curr_top);
    }
}

int create_lf_stack(lf_stack* stack)
{
    stack->head = create_lf_stack_head(0);
    stack->head->next = 0;
    stack->size = 0;
    return 0;
}

void push_lf_stack(lf_stack *stack, lf_stack_node *node)
{
    lf_stack_node *tb, *oldhead;
    tb = node;

    oldhead = stack->head->next;
    tb->next = oldhead;

    while (!__atomic_compare_exchange(&(stack->head->next), &oldhead, &tb, 0, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST))
    {
        usleep(1);
        oldhead = stack->head->next;
        tb->next = oldhead;
    }

    __atomic_fetch_add(&stack->size, 1, __ATOMIC_SEQ_CST);
}

lf_stack_node* pop_lf_stack(lf_stack *stack)
{
    lf_stack_node *current;

    while (!__sync_bool_compare_and_swap(&(stack->head->list_mutex), 0, 1))
    {
        usleep(1);
    }

    current = stack->head->next;

    while (current && !__atomic_compare_exchange(&(stack->head->next), &current, &(current->next), 0, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST))
    {
        usleep(1);
        current = stack->head->next;
    }

    if (current)
    {
        __atomic_fetch_sub(&stack->size, 1, __ATOMIC_SEQ_CST);
    }

    while (!__sync_bool_compare_and_swap(&(stack->head->list_mutex), 1, 0))
    {
        usleep(1);
    }

    return current;
}

void free_lf_stack(lf_stack *stack)
{
    free_lf_stack_nodes(stack);
    free(stack->head);
    free(stack);
}