#pragma once

#include <unistd.h>

typedef struct spinlock
{
    volatile unsigned long lock;
} spinlock;

void init_spinlock(spinlock *spin_lock);

void lock_spinlock(spinlock *spin_lock);

void unlock_spinlock(spinlock *spin_lock);

int try_lock_spinlock(spinlock *spin_lock);