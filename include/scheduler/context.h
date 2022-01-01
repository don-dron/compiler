#pragma once

#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>

// Fiber , coroutine stack size = 64 Kbytes
static const size_t STACK_SIZE = 64 * 1024;

extern unsigned long switch_count_atom;
extern unsigned long switch_count;
extern unsigned long interrupt_count;
extern unsigned long interrupt_failed_count;

typedef struct execution_context
{
  void *rsp;
  void *stack;
} execution_context;

typedef struct stack_saved_context
{
  void *rbp;
  void *rbx;

  void *r12;
  void *r13;
  void *r14;
  void *r15;

  void *rip;
} stack_saved_context;

typedef struct stack
{
  void *memory;
  size_t size;
} stack;

typedef struct stack_builder
{
  int word_size;
  char *top;
} stack_builder;

typedef struct statistic
{
  unsigned long switch_count_atom;
  unsigned long switch_count;
  unsigned long interrupt_count;
  unsigned long interrupt_failed_count;
} statistic;

extern void switch_from_to(execution_context *from, execution_context *to);

void switch_context(execution_context *from, execution_context *to);

void align_next_push(stack_builder *builder, size_t alignment);

void allocate(stack_builder *builder, size_t bytes);

size_t pages_to_bytes(size_t count);

statistic get_statistic(void);

void print_statistic(void);
