
#ifdef __linux__
#define FUNCTION_NAME(name) name
#else
#define FUNCTION_NAME(name) _name
#endif

.global FUNCTION_NAME(switch_from_to)

FUNCTION_NAME(switch_from_to):

    // For example: call from fiber code

    // Save registers on stack

    pushq %r15
    pushq %r14
    pushq %r13
    pushq %r12

    pushq %rbx
    pushq %rbp

    // Change stack pointer switch run -------------------------------->    For example: to scheduler code
    movq %rsp, (%rdi)

    // Return after switch             <--------------------------------    For example: from scheduler code
    movq (%rsi), %rsp

    // Load registers from stack
    popq %rbp
    popq %rbx

    popq %r12
    popq %r13
    popq %r14
    popq %r15

    retq

    // For example: return to fiber code
