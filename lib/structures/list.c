#include <structures/list.h>

int create_list(list *lst)
{
    lst->start = 0;
    lst->end = 0;
    lst->size = 0;

    spinlock lock;
    lock.lock = 0;
    lst->lock = lock;

    return 0;
}

void list_push_back(list *lst, list_node *node)
{
    lock_spinlock(&lst->lock);

    if (lst->size == 0)
    {
        node->next = 0;
        node->prev = 0;

        lst->start = node;
        lst->end = node;

        lst->size++;
    }
    else
    {
        lst->end->next = node;
        node->prev = lst->end;
        lst->end = node;
        node->next = 0;

        lst->size++;
    }

    unlock_spinlock(&lst->lock);
}

void list_push_front(list *lst, list_node *node)
{
    lock_spinlock(&lst->lock);
    if (lst->size == 0)
    {
        node->next = 0;
        node->prev = 0;

        lst->start = node;
        lst->end = node;

        lst->size++;
    }
    else
    {
        lst->start->prev = node;
        node->next = lst->start;
        lst->start = node;
        node->prev = 0;

        lst->size++;
    }
    unlock_spinlock(&lst->lock);
}

list_node *list_pop_back(list *lst)
{
    lock_spinlock(&lst->lock);
    if (lst->size == 0)
    {
        unlock_spinlock(&lst->lock);
        return 0;
    }
    else
    {
        list_node *result = lst->end;
        lst->end = result->prev;

        if (lst->end != 0)
        {
            lst->end->next = 0;
        }
        else
        {
            lst->start = 0;
        }

        lst->size--;
        unlock_spinlock(&lst->lock);
        return result;
    }
}

list_node *list_pop_front(list *lst)
{
    lock_spinlock(&lst->lock);
    if (lst->size == 0)
    {
        unlock_spinlock(&lst->lock);
        return 0;
    }
    else
    {
        list_node *result = lst->start;
        lst->start = result->next;

        if (lst->start != 0)
        {
            lst->start->prev = 0;
        }
        else
        {
            lst->end = 0;
        }

        lst->size--;
        unlock_spinlock(&lst->lock);
        return result;
    }
}

int free_list(list *lst, void (*free_callback)(list_node *))
{
    lock_spinlock(&lst->lock);

    list_node *current = lst->start;

    while (current != 0)
    {
        list_node *tmp = current->next;
        free_callback(current);
        free(current);
        current = tmp;
    }

    lst->start = 0;
    lst->end = 0;
    lst->size = 0;

    unlock_spinlock(&lst->lock);

    return 0;
}