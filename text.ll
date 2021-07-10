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
		%2 = mul i32 8,10
		%3 = call i64* @malloc(i32 %2)
		%4 = bitcast i64* %3 to i32**
		store i32** %4, i32*** %$2_b
		%$3_i = alloca i32
		store i32 0, i32* %$3_i
		%$4_j = alloca i32
		store i32 0, i32* %$4_j
		%$5_res = alloca i32
		store i32 0, i32* %$5_res
		br label %while_condition_3
	while_condition_3:
		%5 = load i32,i32* %$3_i
		%6 = icmp slt i32 %5 ,  10
		br i1 %6, label %while_body_5,label %while_merge_10
	while_body_5:
		%7 = load i32**,i32*** %$2_b
		%8 = load i32,i32* %$3_i
		%9 = getelementptr inbounds i32* , i32** %7 , i32 %8
		%10 = mul i32 8,10
		%11 = call i64* @malloc(i32 %10)
		%12 = bitcast i64* %11 to i32*
		store i32* %12, i32** %9
		store i32 0, i32* %$4_j
		br label %while_condition_6
	while_condition_6:
		%13 = load i32,i32* %$4_j
		%14 = icmp slt i32 %13 ,  10
		br i1 %14, label %while_body_8,label %while_merge_9
	while_body_8:
		%15 = load i32**,i32*** %$2_b
		%16 = load i32,i32* %$3_i
		%17 = getelementptr inbounds i32* , i32** %15 , i32 %16
		%18 = load i32*,i32** %17
		%19 = load i32,i32* %$4_j
		%20 = getelementptr inbounds i32 , i32* %18 , i32 %19
		%21 = load i32,i32* %$3_i
		%22 = mul i32 %21,10
		%23 = load i32,i32* %$4_j
		%24 = add i32 %22,%23
		store i32 %24, i32* %20
		%25 = load i32,i32* %$4_j
		%26 = add i32 %25,1
		store i32 %26, i32* %$4_j
		br label %while_condition_6
	while_merge_9:
		%27 = load i32,i32* %$3_i
		%28 = add i32 %27,1
		store i32 %28, i32* %$3_i
		br label %while_condition_3
	while_merge_10:
		store i32 0, i32* %$3_i
		br label %while_condition_11
	while_condition_11:
		%29 = load i32,i32* %$3_i
		%30 = icmp slt i32 %29 ,  9
		br i1 %30, label %while_body_13,label %local_return_15
	while_body_13:
		%31 = load i32,i32* %$5_res
		%32 = load i32**,i32*** %$2_b
		%33 = load i32,i32* %$3_i
		%34 = add i32 %33,1
		%35 = getelementptr inbounds i32* , i32** %32 , i32 %34
		%36 = load i32*,i32** %35
		%37 = load i32,i32* %$3_i
		%38 = add i32 %37,1
		%39 = getelementptr inbounds i32 , i32* %36 , i32 %38
		%40 = load i32,i32* %39
		%41 = add i32 %31,%40
		%42 = load i32**,i32*** %$2_b
		%43 = load i32,i32* %$3_i
		%44 = getelementptr inbounds i32* , i32** %42 , i32 %43
		%45 = load i32*,i32** %44
		%46 = load i32,i32* %$3_i
		%47 = getelementptr inbounds i32 , i32* %45 , i32 %46
		%48 = load i32,i32* %47
		%49 = sub i32 %41,%48
		store i32 %49, i32* %$5_res
		%50 = load i32,i32* %$3_i
		%51 = add i32 %50,1
		store i32 %51, i32* %$3_i
		br label %while_condition_11
	local_return_15:
		%52 = load i32,i32* %$5_res
		store i32 %52, i32* %$$_ret_value_0
		br label %return_1
}