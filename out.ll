; ModuleID = 'main'
source_filename = "main"
define i32 @main() {
skip_0:
	%ret$val$2 = alloca i32
	br label %entry_2
return_1:
	%0 = load i32, i32 *%ret$val$2
	ret i32 %0
entry_2:
	%c$3 = alloca i32
	store i32 1, i32 *%c$3
	%1 = load i32, i32 *%c$3
	%2 = add i32 %1, 2
	store i32 %2, i32 *%c$3
	%3 = load i32, i32 *%c$3
	%4 = add i32 3, %3
	store i32 %4, i32 *%c$3
	%5 = load i32, i32 *%c$3
	%6 = sdiv i32 %5, 2
	%7 = load i32, i32 *%c$3
	%8 = sdiv i32 %7, 2
	%9 = add i32 %6, %8
	store i32 %9, i32 *%c$3
	br label %local_return_3
local_return_3:
	%10 = load i32, i32 *%c$3
	%11 = add i32 %10, 1
	store i32 %11, i32 *%ret$val$2
	br label %return_1
dummy_block_4:
	br label %return_1
}
