#pragma once

#include <stdlib.h>
#include <unistd.h>

struct lf_stack_node;

typedef struct lf_stack_node
{
    struct lf_stack_node *next;
    size_t list_mutex;
} lf_stack_node;

typedef struct lf_stack_head
{
    lf_stack_node *next;
    size_t list_mutex;
} lf_stack_head;

typedef struct lf_stack
{
    lf_stack_head *head;
    size_t size;
} lf_stack;

int create_lf_stack(lf_stack* stack);

void push_lf_stack(lf_stack *stack, lf_stack_node *node);

lf_stack_node *pop_lf_stack(lf_stack *stack);

void free_lf_stack(lf_stack *stack);