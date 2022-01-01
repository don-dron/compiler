#include <structures/fibonacci_heap.h>

#define LOG2(X) ({ \
		unsigned int _i = 0; \
		__asm__("bsr %1, %0" : "=r" (_i) : "r" ((X))); \
		_i; })

#define CHECK_MALLOC(X) ({ \
		if((X)==NULL) { \
			fprintf(stderr, \
				"fib_heap.c: Error allocating memory\n"); \
			exit(1); \
		}; })

#define CHECK_INPUT(X, M) ({ \
		if(!(X)) {\
			fprintf(stderr, "fib_heap.c: %s\n", (M)); \
			exit(1); \
		}; })

static void dblink_list_concat(struct fib_heap_node *a, struct fib_heap_node *b)
{
    struct fib_heap_node *temp;
    CHECK_INPUT(a != NULL, "dblink_list_concat: a==NULL");
    CHECK_INPUT(b != NULL, "dblink_list_concat: b==NULL");

    temp = a->right;
    a->right = b->right;
    b->right->left = a;
    b->right = temp;
    temp->left = b;
}

struct fib_heap *fibheap_init(int (*compr)(struct fib_heap_data *, struct fib_heap_data *))
{
    struct fib_heap *H = (struct fib_heap *)malloc(sizeof(struct fib_heap));
    CHECK_MALLOC(H);
    H->the_one = NULL;
    H->cons_array = (struct fib_heap_node **)
        malloc(64 * sizeof(struct fib_heap_node *));
    CHECK_MALLOC(H->cons_array);
    H->total_nodes = 0U;

    H->compr = compr;

    return H;
}

struct fib_heap_node *fibheap_insert(struct fib_heap *H, struct fib_heap_data *d)
{
    struct fib_heap_node *node = (struct fib_heap_node *)
        malloc(sizeof(struct fib_heap_node));

    node->data = d;
    node->degree = 0U;
    node->marked = 0;
    node->parent = NULL;
    node->child = NULL;

    if ((H->the_one) == NULL)
    {
        node->left = node;
        node->right = node;
        H->the_one = node;
    }
    else
    {
        node->left = H->the_one->left;
        node->right = H->the_one;
        H->the_one->left->right = node;
        H->the_one->left = node;
        if (H->compr(H->the_one->data, node->data) > 0)
            H->the_one = node;
    }
    H->total_nodes++;

    return node;
}

struct fib_heap_data *fibheap_read(struct fib_heap *H)
{
    CHECK_INPUT(H != NULL, "fibheap_read: H==NULL");
    return (H->the_one->data);
}

struct fib_heap *fibheap_union(struct fib_heap *a, struct fib_heap *b)
{
    if ((a->the_one) == NULL)
    {
        a->the_one = b->the_one;
        a->total_nodes = b->total_nodes;
        free(b->cons_array);
        free(b);
    }
    else if ((b->the_one) == NULL)
    {
        free(b->cons_array);
        free(b);
    }
    else
    {
        dblink_list_concat(a->the_one, b->the_one);
        if ((a->compr)(a->the_one->data, b->the_one->data) > 0)
            a->the_one = b->the_one;
        a->total_nodes += b->total_nodes;
        free(b->cons_array);
        free(b);
    }
    return a;
}

static void fibheap_link(struct fib_heap_node *y, struct fib_heap_node *x)
{
    struct fib_heap_node *temp;

    y->left->right = y->right;
    y->right->left = y->left;

    y->parent = x;
    if (x->child == NULL)
    {
        x->child = y;
        y->left = y;
        y->right = y;
    }
    else
    {
        temp = x->child->right;
        x->child->right = y;
        y->left = x->child;
        temp->left = y;
        y->right = temp;
    }

    (x->degree)++;
    y->marked = 0;
}

static void fibheap_consolidate(struct fib_heap *H)
{
    unsigned int D;
    unsigned int i, d;
    struct fib_heap_node *x, *w, *y, *temp, *new_one;

    if (H->total_nodes == 0U)
    {
        free(H->the_one);
        H->the_one = NULL;
        return;
    }

    D = LOG2(H->total_nodes);
    for (i = 0U; i <= D; i++)
        H->cons_array[i] = NULL;

    w = H->the_one->right;
    new_one = w;
    while (w != H->the_one)
    {
        x = w;
        d = x->degree;
        while ((d <= D) && (H->cons_array[d] != NULL))
        {
            y = H->cons_array[d];
            if ((H->compr)(x->data, y->data) > 0)
            {
                temp = x;
                x = y;
                y = temp;
            }
            if (w == y)
                w = w->left;
            fibheap_link(y, x);
            H->cons_array[d] = NULL;
            d++;
        }
        H->cons_array[d] = x;
        if ((H->compr)(x->data, new_one->data) <= 0)
            new_one = x;
        w = w->right;
    }

    H->the_one->left->right = H->the_one->right;
    H->the_one->right->left = H->the_one->left;
    free(H->the_one);
    H->the_one = new_one;
}

struct fib_heap_data *fibheap_extract(struct fib_heap *H)
{
    struct fib_heap_data *ret;
    unsigned int i;
    CHECK_INPUT(H != NULL, "fibheap_extract: H==NULL");

    if (H->the_one == NULL)
        return NULL;

    ret = H->the_one->data;

    for (i = 0U; i < H->the_one->degree; i++)
    {
        H->the_one->child->parent = NULL;
        H->the_one->child = H->the_one->child->right;
    }

    if (H->the_one->child != NULL)
        dblink_list_concat(H->the_one, H->the_one->child);
    (H->total_nodes)--;
    fibheap_consolidate(H);

    return ret;
}

static void fibheap_cut(struct fib_heap *H, struct fib_heap_node *x, struct fib_heap_node *y)
{
    (y->degree)--;
    if (y->degree == 0U)
    {
        y->child = NULL;
    }
    else
    {
        y->child = x->right;
        x->left->right = x->right;
        x->right->left = x->left;
    }

    x->right = x;
    x->left = x;
    x->parent = NULL;
    x->marked = 0;
    dblink_list_concat(H->the_one, x);
}

static void fibheap_casc_cut(struct fib_heap *H, struct fib_heap_node *y)
{
    struct fib_heap_node *z = y->parent;

    if (z == NULL)
        return;

    if (!(y->marked))
    {
        y->marked = 1;
        return;
    }

    fibheap_cut(H, y, z);
    return fibheap_casc_cut(H, z);
}

void fibheap_increase(struct fib_heap *H, struct fib_heap_node *node)
{
    struct fib_heap_node *y;
    CHECK_INPUT(H != NULL, "fibheap_increase: H==NULL");
    CHECK_INPUT(node != NULL, "fibheap_increase: node==NULL");

    y = node->parent;
    if (y != NULL && ((H->compr)(y->data, node->data) > 0))
    {
        fibheap_cut(H, node, y);
        fibheap_casc_cut(H, y);
    }
    if ((H->compr)(H->the_one->data, node->data) > 0)
        H->the_one = node;
}

void fibheap_decrease(struct fib_heap *H, struct fib_heap_node *node)
{
    unsigned int i;
    struct fib_heap_node *y;
    CHECK_INPUT(H != NULL, "fibheap_decrease: H==NULL");
    CHECK_INPUT(node != NULL, "fibheap_decrease: node==NULL");

    for (i = 0U; i < node->degree; i++)
    {
        node->child->parent = NULL;
        node->child = node->child->right;
    }
    if (node->child != NULL)
        dblink_list_concat(H->the_one, node->child);
    node->child = NULL;
    node->degree = 0U;

    y = node->parent;
    if (y != NULL)
    {
        fibheap_cut(H, node, y);
        fibheap_casc_cut(H, y);
    }
    else if (H->the_one == node)
    {
        y = node->right;
        while (y != node)
        {
            if ((H->compr)(node->data, y->data) > 0)
                H->the_one = y;
            y = y->right;
        }
    }
}

void fibheap_delete(struct fib_heap *H, struct fib_heap_node *node)
{
    struct fib_heap_node *y;
    struct fib_heap_data *d;
    CHECK_INPUT(H != NULL, "fibheap_delete: H==NULL");
    CHECK_INPUT(node != NULL, "fibheap_delete: node==NULL");

    y = node->parent;
    if (y != NULL)
    {
        fibheap_cut(H, node, y);
        fibheap_casc_cut(H, y);
    }

    H->the_one = node;
    d = fibheap_extract(H);
    free(d);
}

static void fibheap_destroy_rec(struct fib_heap_node *node)
{
    struct fib_heap_node *start = node;

    if (node == NULL)
        return;

    do
    {
        fibheap_destroy_rec(node->child);
        node = node->right;
        free(node->left->data);
        free(node->left);
    } while (node != start);
}

void fibheap_destroy(struct fib_heap *H)
{
    CHECK_INPUT(H != NULL, "fibheap_destroy: H==NULL");

    fibheap_destroy_rec(H->the_one);
    free(H->cons_array);
    free(H);
}