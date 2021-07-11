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
		%$array_access_0 = alloca i32**
		store i32 0, i32* %$alloc_iterator_0
		%2 = mul i32 8,10
		store i32 %2, i32* %$size_0
		%$size_1 = alloca i32
		%$alloc_iterator_1 = alloca i32
		%$array_access_1 = alloca i32*
		store i32 0, i32* %$alloc_iterator_1
		%3 = mul i32 4,10
		store i32 %3, i32* %$size_1
		%4 = load i32,i32* %$size_0
		%5 = call i64* @malloc(i32 %4)
		%6 = bitcast i64* %5 to i32**
		store i32** %6, i32*** %$array_0
		store i32** %6, i32*** %$array_access_0
		br label %alloc_condition_3
	alloc_condition_3:
		%7 = load i32,i32* %$size_0
		%8 = load i32,i32* %$alloc_iterator_0
		%9 = icmp slt i32 %8 ,  %7
		br i1 %9, label %alloc_body_4,label %alloc_merge_8
	alloc_body_4:
		%10 = load i32**,i32*** %$array_access_0
		%11 = load i32,i32* %$alloc_iterator_0
		%12 = getelementptr inbounds i32* , i32** %10 , i32 %11
		%13 = load i32,i32* %$size_1
		%14 = call i64* @malloc(i32 %13)
		%15 = bitcast i64* %14 to i32*
		store i32* %15, i32** %12
		store i32* %15, i32** %$array_access_1
		br label %alloc_condition_5
	alloc_condition_5:
		%16 = load i32,i32* %$size_1
		%17 = load i32,i32* %$alloc_iterator_1
		%18 = icmp slt i32 %17 ,  %16
		br i1 %18, label %alloc_body_6,label %alloc_merge_7
	alloc_body_6:
		%19 = load i32*,i32** %$array_access_1
		%20 = load i32,i32* %$alloc_iterator_1
		%21 = getelementptr inbounds i32 , i32* %19 , i32 %20
		store i32 0, i32* %21
		%22 = load i32,i32* %$alloc_iterator_1
		%23 = add i32 %22,1
		store i32 %23, i32* %$alloc_iterator_1
		br label %alloc_condition_5
	alloc_merge_7:
		%24 = load i32,i32* %$alloc_iterator_0
		%25 = add i32 %24,1
		store i32 %25, i32* %$alloc_iterator_0
		br label %alloc_condition_3
	alloc_merge_8:
		%26 = load i32**,i32*** %$array_0
		store i32** %26, i32*** %$2_b
		%$3_sum = alloca i32
		store i32 8, i32* %$3_sum
		br label %local_return_9
	local_return_9:
		%27 = load i32,i32* %$3_sum
		store i32 %27, i32* %$$_ret_value_0
		br label %return_1
	dummy_10:

		br label %return_1
}