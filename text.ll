; ModuleID = 'main'
source_filename = "main"
declare i64* @malloc(i32)
declare i32 @putchar(i32)
declare i32 @getchar()
define i32 @main(i32){
	header_0:
		%$1_a = alloca i32
		store i32 %0, i32* %$1_a
		%$$_ret_value_0 = alloca i32
		br label %entry_2
	return_1:
		%1 = load i32,i32* %$$_ret_value_0
		ret i32 %1
	entry_2:
		%$2_b = alloca i32**
		%$array_0 = alloca i32**
		%$size_0 = alloca i32
		%$alloc_iterator_0 = alloca i32
		store i32 0, i32* %$alloc_iterator_0
		%2 = mul i32 8,10
		store i32 %2, i32* %$size_0
		%3 = call i64* @malloc(i32 %2)
		%4 = bitcast i64* %3 to i32**
		store i32** %4, i32*** %$array_0
		br label %alloc_condition_3
	alloc_condition_3:
		%5 = load i32,i32* %$size_0
		%6 = load i32,i32* %$alloc_iterator_0
		%7 = icmp slt i32 %6 ,  %5
		br i1 %7, label %alloc_body_4,label %alloc_merge_8
	alloc_body_4:
		%8 = load i32**,i32*** %$array_0
		%9 = load i32,i32* %$alloc_iterator_0
		%$array_access_0 = alloca i32**
		%10 = getelementptr inbounds i32* , i32** %8 , i32 %9
		%$size_1 = alloca i32
		%$alloc_iterator_1 = alloca i32
		store i32 0, i32* %$alloc_iterator_1
		%11 = mul i32 4,10
		store i32 %11, i32* %$size_1
		%12 = call i64* @malloc(i32 %11)
		%13 = bitcast i64* %12 to i32*
		store i32* %13, i32** %10
		br label %alloc_condition_3
	alloc_condition_5:
		%14 = load i32,i32* %$size_1
		%15 = load i32,i32* %$alloc_iterator_1
		%16 = icmp slt i32 %15 ,  %14
		br i1 %16, label %alloc_body_6,label %alloc_merge_7
	alloc_body_6:
		%17 = load i32*,i32** %10
		%18 = load i32,i32* %$alloc_iterator_1
		%$array_access_1 = alloca i32*
		%19 = getelementptr inbounds i32 , i32* %17 , i32 %18
		store i32 0, i32* %19
		%20 = load i32,i32* %$alloc_iterator_1
		%21 = add i32 %20,1
		store i32 %21, i32* %$alloc_iterator_1
		br label %alloc_condition_5
	alloc_merge_7:
		%22 = load i32,i32* %$alloc_iterator_0
		%23 = add i32 %22,1
		store i32 %23, i32* %$alloc_iterator_0
		br label %alloc_condition_5
	alloc_merge_8:
		%24 = load i32**,i32*** %$array_0
		store i32** %24, i32*** %$2_b
		%$3_sum = alloca i32
		store i32 1, i32* %$3_sum
		br label %local_return_9
	local_return_9:
		%25 = load i32,i32* %$3_sum
		store i32 %25, i32* %$$_ret_value_0
		br label %return_1
	dummy_10:

		br label %return_1
}