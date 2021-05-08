; ModuleID = 'main'
source_filename = "main"
define i32 @main() {
skip_0:
	br label %entry_2
return_1:
	ret i32 %ret_val$2
entry_2:
	%c$3 = alloca i32
	store i32 1, i32 *%c$3
	br label %local_return_3
local_return_3:
	%0 = load i32, i32 *%c$3
	%1 = load i32, i32 1
	%2 = add i32 %0, %1
	store i32 %2, i32 *%ret_val$2
	br label %return_1
}
