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
	br label %local_return_3
local_return_3:
	%1 = load i32, i32 *%c$3
	%2 = add i32 %1, 1
	store i32 %2, i32 *%ret$val$2
	br label %return_1
}
