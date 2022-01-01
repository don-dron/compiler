#pragma once

#include <stdlib.h>
#include <unistd.h>

unsigned long inc(volatile unsigned long *variable);

unsigned long dec(volatile unsigned long *variable);