#pragma once

#include <stdlib.h>
#include <stdio.h>
#include <locks/spinlock.h>

struct list_node;

typedef struct list_node
{
    struct list_node *prev;
    struct list_node *next;
} list_node;

typedef struct list
{
    list_node *start;
    list_node *end;
    spinlock lock;
    size_t size;
} list;

int create_list(list* lst);

void list_push_back(list *lst, list_node *node);

void list_push_front(list *lst, list_node *node);

list_node *list_pop_back(list *lst);

list_node *list_pop_front(list *lst);

int free_list(list *lst, void (*free_callback)(list_node *));