
; ModuleID = 'main'
source_filename = "main"
declare i64* @malloc(i32)
declare i32 @putchar(i32)
declare i32 @getchar()

define i32 @main(){
	header_5:
		%$$_ret_value_0 = alloca i32
		br label %entry_7
	return_6:
		%0 = load i32,i32* %$$_ret_value_0
		ret i32 %0
	entry_7:
		%$3_b = alloca i32**
		%$array_0 = alloca i32**
		%$size_0 = alloca i32
		%$alloc_iterator_0 = alloca i32
		%$array_access_0 = alloca i32**
		store i32 0, i32* %$alloc_iterator_0
		store i32 13, i32* %$size_0
		%$size_1 = alloca i32
		%$alloc_iterator_1 = alloca i32
		%$array_access_1 = alloca i32*
		store i32 0, i32* %$alloc_iterator_1
		store i32 17, i32* %$size_1
		store i32 0, i32* %$alloc_iterator_0
		%1 = load i32,i32* %$size_0
		%2 = mul i32 8,%1
		%3 = call i64* @malloc(i32 %2)
		%4 = bitcast i64* %3 to i32**
		store i32** %4, i32*** %$array_0
		store i32** %4, i32*** %$array_access_0
		br label %alloc_condition_8
	alloc_condition_8:
		%5 = load i32,i32* %$size_0
		%6 = load i32,i32* %$alloc_iterator_0
		%7 = icmp slt i32 %6 ,  %5
		br i1 %7, label %alloc_body_9,label %alloc_merge_13
	alloc_body_9:
		%8 = load i32**,i32*** %$array_access_0
		%9 = load i32,i32* %$alloc_iterator_0
		%10 = getelementptr inbounds i32* , i32** %8 , i32 %9
		store i32 0, i32* %$alloc_iterator_1
		%11 = load i32,i32* %$size_1
		%12 = mul i32 4,%11
		%13 = call i64* @malloc(i32 %12)
		%14 = bitcast i64* %13 to i32*
		store i32* %14, i32** %10
		store i32* %14, i32** %$array_access_1
		br label %alloc_condition_10
	alloc_condition_10:
		%15 = load i32,i32* %$size_1
		%16 = load i32,i32* %$alloc_iterator_1
		%17 = icmp slt i32 %16 ,  %15
		br i1 %17, label %alloc_body_11,label %alloc_merge_12
	alloc_body_11:
		%18 = load i32*,i32** %$array_access_1
		%19 = load i32,i32* %$alloc_iterator_1
		%20 = getelementptr inbounds i32 , i32* %18 , i32 %19
		store i32 0, i32* %20
		%21 = load i32,i32* %$alloc_iterator_1
		%22 = add i32 %21,1
		store i32 %22, i32* %$alloc_iterator_1
		br label %alloc_condition_10
	alloc_merge_12:
		%23 = load i32,i32* %$alloc_iterator_0
		%24 = add i32 %23,1
		store i32 %24, i32* %$alloc_iterator_0
		br label %alloc_condition_8
	alloc_merge_13:
		%25 = load i32**,i32*** %$array_0
		store i32** %25, i32*** %$3_b
		%$4_i = alloca i32
		store i32 0, i32* %$4_i
		%$5_j = alloca i32
		store i32 0, i32* %$5_j
		br label %while_condition_14
	while_condition_14:
		%26 = load i32,i32* %$4_i
		%27 = icmp slt i32 %26 ,  13
		br i1 %27, label %while_body_16,label %merge_15
	merge_15:

		br label %while_merge_21
	while_body_16:
		store i32 0, i32* %$5_j
		br label %while_condition_17
	while_condition_17:
		%28 = load i32,i32* %$5_j
		%29 = icmp slt i32 %28 ,  17
		br i1 %29, label %while_body_19,label %merge_18
	merge_18:

		br label %while_merge_20
	while_body_19:
		%30 = load i32**,i32*** %$3_b
		%31 = load i32,i32* %$4_i
		%32 = getelementptr inbounds i32* , i32** %30 , i32 %31
		%33 = load i32*,i32** %32
		%34 = load i32,i32* %$5_j
		%35 = getelementptr inbounds i32 , i32* %33 , i32 %34
		%36 = load i32,i32* %$4_i
		%37 = mul i32 %36,10
		%38 = load i32,i32* %$5_j
		%39 = add i32 %37,%38
		store i32 %39, i32* %35
		%40 = load i32,i32* %$5_j
		%41 = add i32 %40,1
		store i32 %41, i32* %$5_j
		br label %while_condition_17
	while_merge_20:
		%42 = load i32,i32* %$4_i
		%43 = add i32 %42,1
		store i32 %43, i32* %$4_i
		br label %while_condition_14
	while_merge_21:
		store i32 0, i32* %$4_i
		br label %while_condition_22
	while_condition_22:
		%44 = load i32,i32* %$4_i
		%45 = icmp slt i32 %44 ,  13
		br i1 %45, label %while_body_24,label %merge_23
	merge_23:

		br label %while_merge_29
	while_body_24:
		store i32 0, i32* %$5_j
		br label %while_condition_25
	while_condition_25:
		%46 = load i32,i32* %$5_j
		%47 = icmp slt i32 %46 ,  17
		br i1 %47, label %while_body_27,label %merge_26
	merge_26:

		br label %while_merge_28
	while_body_27:
		%48 = load i32**,i32*** %$3_b
		%49 = load i32,i32* %$4_i
		%50 = getelementptr inbounds i32* , i32** %48 , i32 %49
		%51 = load i32*,i32** %50
		%52 = load i32,i32* %$5_j
		%53 = getelementptr inbounds i32 , i32* %51 , i32 %52
		%54 = load i32,i32* %53
		%55 = call i32 @putchar(i32 %54)
		%56 = load i32,i32* %$5_j
		%57 = add i32 %56,1
		store i32 %57, i32* %$5_j
		br label %while_condition_25
	while_merge_28:
		%58 = load i32,i32* %$4_i
		%59 = add i32 %58,1
		store i32 %59, i32* %$4_i
		br label %while_condition_22
	while_merge_29:
		%$6_sum = alloca i32
		store i32 8, i32* %$6_sum
		br label %local_return_30
	local_return_30:
		%60 = load i32,i32* %$6_sum
		store i32 %60, i32* %$$_ret_value_0
		br label %return_6
	dummy_31:

		br label %return_6
}