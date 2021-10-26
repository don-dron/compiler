; ModuleID = 'main'
source_filename = "main"
%struct.$0_String = type {i8*,i32}
@.str0 = private unnamed_addr constant [5 x i8] c"Hello"
@.str1 = private unnamed_addr constant [7 x i8] c" world!"
declare i32 @puts(i32*)
declare i32 @putchar(i32)
declare i32 @getchar()
declare i64* @malloc(i32)
define %struct.$0_String* @$_constructor_0(i8*,i32){
	header_0:
		%$36_bytes = alloca i8*
		store i8* %0, i8** %$36_bytes
		%$37_length = alloca i32
		store i32 %1, i32* %$37_length
		%$$_ret_value_0 = alloca %struct.$0_String*
		%$$_this_value_0 = alloca %struct.$0_String*
		%2 = call i64* @malloc(i32 12)
		%3 = bitcast i64* %2 to %struct.$0_String*
		store %struct.$0_String* %3, %struct.$0_String** %$$_this_value_0
		store %struct.$0_String* %3, %struct.$0_String** %$$_ret_value_0
		br label %entry_2
	return_1:
		%4 = load %struct.$0_String*,%struct.$0_String** %$$_ret_value_0
		ret %struct.$0_String* %4
	entry_2:
		%5 = load %struct.$0_String*,%struct.$0_String** %$$_this_value_0
		%6 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %5 , i64 0 , i32 0
		%7 = load i8*,i8** %$36_bytes
		store i8* %7, i8** %6
		%8 = load %struct.$0_String*,%struct.$0_String** %$$_this_value_0
		%9 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %8 , i64 0 , i32 1
		%10 = load i32,i32* %$37_length
		store i32 %10, i32* %9
		br label %return_1
}
define i32 @$4_getLength(%struct.$0_String*){
	header_3:
		%$$_this_value_1 = alloca %struct.$0_String*
		store %struct.$0_String* %0, %struct.$0_String** %$$_this_value_1
		%$$_ret_value_1 = alloca i32
		br label %entry_5
	return_4:
		%1 = load i32,i32* %$$_ret_value_1
		ret i32 %1
	entry_5:

		br label %local_return_6
	local_return_6:
		%2 = load %struct.$0_String*,%struct.$0_String** %$$_this_value_1
		%3 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %2 , i64 0 , i32 1
		%4 = load i32,i32* %3
		store i32 %4, i32* %$$_ret_value_1
		br label %return_4
	dummy_7:

		br label %return_4
}
define void @$5_println(%struct.$0_String*){
	header_8:
		%$21_s = alloca %struct.$0_String*
		store %struct.$0_String* %0, %struct.$0_String** %$21_s
		br label %entry_10
	return_9:

		ret void
	entry_10:
		%$22_i = alloca i32
		store i32 0, i32* %$22_i
		%$23_l = alloca i32
		%1 = load %struct.$0_String*,%struct.$0_String** %$21_s
		%2 = call i32 @$4_getLength(%struct.$0_String* %1)
		store i32 %2, i32* %$23_l
		%$24_bytes = alloca i8*
		%3 = load %struct.$0_String*,%struct.$0_String** %$21_s
		%4 = call i8* @$3_getBytes(%struct.$0_String* %3)
		store i8* %4, i8** %$24_bytes
		br label %while_condition_11
	while_condition_11:
		%5 = load i32,i32* %$22_i
		%6 = load i32,i32* %$23_l
		%7 = icmp slt i32 %5 ,  %6
		br i1 %7, label %while_body_13,label %merge_12
	merge_12:

		br label %while_merge_14
	while_body_13:
		%$25_c = alloca i32
		%8 = load i8*,i8** %$24_bytes
		%9 = load i32,i32* %$22_i
		%10 = getelementptr inbounds i8 , i8* %8 , i32 %9
		%11 = load i8,i8* %10
		%12 = sext i8 %11 to i32
		store i32 %12, i32* %$25_c
		%13 = load i32,i32* %$25_c
		%14 = call i32 @putchar(i32 %13)
		%15 = load i32,i32* %$22_i
		%16 = add i32 %15,1
		store i32 %16, i32* %$22_i
		br label %while_condition_11
	while_merge_14:
		%17 = call i32 @putchar(i32 10)
		br label %return_9
}
define %struct.$0_String* @$8_concat(%struct.$0_String*,%struct.$0_String*){
	header_15:
		%$26_s1 = alloca %struct.$0_String*
		store %struct.$0_String* %0, %struct.$0_String** %$26_s1
		%$27_s2 = alloca %struct.$0_String*
		store %struct.$0_String* %1, %struct.$0_String** %$27_s2
		%$$_ret_value_2 = alloca %struct.$0_String*
		br label %entry_17
	return_16:
		%2 = load %struct.$0_String*,%struct.$0_String** %$$_ret_value_2
		ret %struct.$0_String* %2
	entry_17:
		%$28_l1 = alloca i32
		%3 = load %struct.$0_String*,%struct.$0_String** %$26_s1
		%4 = call i32 @$4_getLength(%struct.$0_String* %3)
		store i32 %4, i32* %$28_l1
		%$29_l2 = alloca i32
		%5 = load %struct.$0_String*,%struct.$0_String** %$27_s2
		%6 = call i32 @$4_getLength(%struct.$0_String* %5)
		store i32 %6, i32* %$29_l2
		%$30_buff = alloca i8*
		%$array_0 = alloca i8*
		%$size_0 = alloca i32
		%$alloc_iterator_0 = alloca i32
		%$array_access_0 = alloca i8*
		store i32 0, i32* %$alloc_iterator_0
		%7 = load i32,i32* %$28_l1
		%8 = load i32,i32* %$29_l2
		%9 = add i32 %7,%8
		store i32 %9, i32* %$size_0
		store i32 0, i32* %$alloc_iterator_0
		%10 = load i32,i32* %$size_0
		%11 = mul i32 1,%10
		%12 = call i64* @malloc(i32 %11)
		%13 = bitcast i64* %12 to i8*
		store i8* %13, i8** %$array_0
		store i8* %13, i8** %$array_access_0
		br label %alloc_condition_18
	alloc_condition_18:
		%14 = load i32,i32* %$size_0
		%15 = load i32,i32* %$alloc_iterator_0
		%16 = icmp slt i32 %15 ,  %14
		br i1 %16, label %alloc_body_19,label %alloc_merge_20
	alloc_body_19:
		%17 = load i8*,i8** %$array_access_0
		%18 = load i32,i32* %$alloc_iterator_0
		%19 = getelementptr inbounds i8 , i8* %17 , i32 %18
		store i8 0, i8* %19
		%20 = load i32,i32* %$alloc_iterator_0
		%21 = add i32 %20,1
		store i32 %21, i32* %$alloc_iterator_0
		br label %alloc_condition_18
	alloc_merge_20:
		%22 = load i8*,i8** %$array_0
		store i8* %22, i8** %$30_buff
		%$31_b1 = alloca i8*
		%23 = load %struct.$0_String*,%struct.$0_String** %$26_s1
		%24 = call i8* @$3_getBytes(%struct.$0_String* %23)
		store i8* %24, i8** %$31_b1
		%$32_b2 = alloca i8*
		%25 = load %struct.$0_String*,%struct.$0_String** %$27_s2
		%26 = call i8* @$3_getBytes(%struct.$0_String* %25)
		store i8* %26, i8** %$32_b2
		%$33_i = alloca i32
		store i32 0, i32* %$33_i
		%$34_i1 = alloca i32
		store i32 0, i32* %$34_i1
		%$35_i2 = alloca i32
		store i32 0, i32* %$35_i2
		br label %while_condition_21
	while_condition_21:
		%27 = load i32,i32* %$34_i1
		%28 = load i32,i32* %$28_l1
		%29 = icmp slt i32 %27 ,  %28
		br i1 %29, label %while_body_23,label %merge_22
	merge_22:

		br label %while_merge_24
	while_body_23:
		%30 = load i8*,i8** %$30_buff
		%31 = load i32,i32* %$33_i
		%32 = getelementptr inbounds i8 , i8* %30 , i32 %31
		%33 = load i8*,i8** %$31_b1
		%34 = load i32,i32* %$34_i1
		%35 = getelementptr inbounds i8 , i8* %33 , i32 %34
		%36 = load i8,i8* %35
		store i8 %36, i8* %32
		%37 = load i32,i32* %$33_i
		%38 = add i32 %37,1
		store i32 %38, i32* %$33_i
		%39 = load i32,i32* %$34_i1
		%40 = add i32 %39,1
		store i32 %40, i32* %$34_i1
		br label %while_condition_21
	while_merge_24:

		br label %while_condition_25
	while_condition_25:
		%41 = load i32,i32* %$35_i2
		%42 = load i32,i32* %$29_l2
		%43 = icmp slt i32 %41 ,  %42
		br i1 %43, label %while_body_27,label %merge_26
	merge_26:

		br label %while_merge_28
	while_body_27:
		%44 = load i8*,i8** %$30_buff
		%45 = load i32,i32* %$33_i
		%46 = getelementptr inbounds i8 , i8* %44 , i32 %45
		%47 = load i8*,i8** %$32_b2
		%48 = load i32,i32* %$35_i2
		%49 = getelementptr inbounds i8 , i8* %47 , i32 %48
		%50 = load i8,i8* %49
		store i8 %50, i8* %46
		%51 = load i32,i32* %$33_i
		%52 = add i32 %51,1
		store i32 %52, i32* %$33_i
		%53 = load i32,i32* %$35_i2
		%54 = add i32 %53,1
		store i32 %54, i32* %$35_i2
		br label %while_condition_25
	while_merge_28:

		br label %local_return_29
	local_return_29:
		%55 = load i8*,i8** %$30_buff
		%56 = load i32,i32* %$33_i
		%57 = call %struct.$0_String* @$_constructor_0(i8* %55,i32 %56)
		store %struct.$0_String* %57, %struct.$0_String** %$$_ret_value_2
		br label %return_16
	dummy_30:

		br label %return_16
}
define void @$11_reverseArray(i32*,i32){
	header_31:
		%$12_arr = alloca i32*
		store i32* %0, i32** %$12_arr
		%$13_length = alloca i32
		store i32 %1, i32* %$13_length
		br label %entry_33
	return_32:

		ret void
	entry_33:
		%$14_index = alloca i32
		store i32 0, i32* %$14_index
		br label %while_condition_34
	while_condition_34:
		%2 = load i32,i32* %$14_index
		%3 = load i32,i32* %$13_length
		%4 = sdiv i32 %3,2
		%5 = icmp slt i32 %2 ,  %4
		br i1 %5, label %while_body_36,label %merge_35
	merge_35:

		br label %while_merge_37
	while_body_36:
		%$15_c = alloca i32
		%6 = load i32*,i32** %$12_arr
		%7 = load i32,i32* %$14_index
		%8 = getelementptr inbounds i32 , i32* %6 , i32 %7
		%9 = load i32,i32* %8
		store i32 %9, i32* %$15_c
		%10 = load i32*,i32** %$12_arr
		%11 = load i32,i32* %$14_index
		%12 = getelementptr inbounds i32 , i32* %10 , i32 %11
		%13 = load i32*,i32** %$12_arr
		%14 = load i32,i32* %$13_length
		%15 = load i32,i32* %$14_index
		%16 = sub i32 %14,%15
		%17 = sub i32 %16,1
		%18 = getelementptr inbounds i32 , i32* %13 , i32 %17
		%19 = load i32,i32* %18
		store i32 %19, i32* %12
		%20 = load i32*,i32** %$12_arr
		%21 = load i32,i32* %$13_length
		%22 = load i32,i32* %$14_index
		%23 = sub i32 %21,%22
		%24 = sub i32 %23,1
		%25 = getelementptr inbounds i32 , i32* %20 , i32 %24
		%26 = load i32,i32* %$15_c
		store i32 %26, i32* %25
		%27 = load i32,i32* %$14_index
		%28 = add i32 %27,1
		store i32 %28, i32* %$14_index
		br label %while_condition_34
	while_merge_37:

		br label %return_32
}
define i32 @main(){
	header_38:
		%$$_ret_value_3 = alloca i32
		br label %entry_40
	return_39:
		%0 = load i32,i32* %$$_ret_value_3
		ret i32 %0
	entry_40:
		%$38_str1 = alloca %struct.$0_String*
		%1 = call i64* @malloc(i32 12)
		%2 = bitcast i64* %1 to %struct.$0_String*
		%3 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %2 , i64 0 , i32 0
		%4 = bitcast [5 x i8]* @.str0 to i8*
		store i8* %4, i8** %3
		%5 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %2 , i64 0 , i32 1
		store i32 5, i32* %5
		store %struct.$0_String* %2, %struct.$0_String** %$38_str1
		%$39_str2 = alloca %struct.$0_String*
		%6 = call i64* @malloc(i32 12)
		%7 = bitcast i64* %6 to %struct.$0_String*
		%8 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %7 , i64 0 , i32 0
		%9 = bitcast [7 x i8]* @.str1 to i8*
		store i8* %9, i8** %8
		%10 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %7 , i64 0 , i32 1
		store i32 7, i32* %10
		store %struct.$0_String* %7, %struct.$0_String** %$39_str2
		%11 = load %struct.$0_String*,%struct.$0_String** %$38_str1
		call void @$5_println(%struct.$0_String* %11)
		%12 = load %struct.$0_String*,%struct.$0_String** %$39_str2
		call void @$5_println(%struct.$0_String* %12)
		%$40_cnct = alloca %struct.$0_String*
		%13 = load %struct.$0_String*,%struct.$0_String** %$38_str1
		%14 = load %struct.$0_String*,%struct.$0_String** %$39_str2
		%15 = call %struct.$0_String* @$8_concat(%struct.$0_String* %13,%struct.$0_String* %14)
		store %struct.$0_String* %15, %struct.$0_String** %$40_cnct
		%16 = load %struct.$0_String*,%struct.$0_String** %$40_cnct
		call void @$5_println(%struct.$0_String* %16)
		%17 = load %struct.$0_String*,%struct.$0_String** %$38_str1
		%18 = call i32 @$4_getLength(%struct.$0_String* %17)
		call void @$6_printNumber(i32 %18)
		%19 = load %struct.$0_String*,%struct.$0_String** %$39_str2
		%20 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %19 , i64 0 , i32 1
		%21 = load i32,i32* %20
		call void @$6_printNumber(i32 %21)
		%22 = load %struct.$0_String*,%struct.$0_String** %$40_cnct
		%23 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %22 , i64 0 , i32 1
		%24 = load i32,i32* %23
		call void @$6_printNumber(i32 %24)
		br label %local_return_41
	local_return_41:
		store i32 0, i32* %$$_ret_value_3
		br label %return_39
	dummy_42:

		br label %return_39
}
define i8* @$3_getBytes(%struct.$0_String*){
	header_43:
		%$$_this_value_2 = alloca %struct.$0_String*
		store %struct.$0_String* %0, %struct.$0_String** %$$_this_value_2
		%$$_ret_value_4 = alloca i8*
		br label %entry_45
	return_44:
		%1 = load i8*,i8** %$$_ret_value_4
		ret i8* %1
	entry_45:

		br label %local_return_46
	local_return_46:
		%2 = load %struct.$0_String*,%struct.$0_String** %$$_this_value_2
		%3 = getelementptr inbounds %struct.$0_String , %struct.$0_String* %2 , i64 0 , i32 0
		%4 = load i8*,i8** %3
		store i8* %4, i8** %$$_ret_value_4
		br label %return_44
	dummy_47:

		br label %return_44
}
define void @$6_printNumber(i32){
	header_48:
		%$16_num = alloca i32
		store i32 %0, i32* %$16_num
		br label %entry_50
	return_49:

		ret void
	entry_50:

		br label %if_condition_51
	if_condition_51:
		%1 = load i32,i32* %$16_num
		%2 = icmp eq i32 %1 ,  0
		br i1 %2, label %if_then_52,label %if_else_55
	if_then_52:
		%3 = call i32 @putchar(i32 48)
		br label %local_return_53
	local_return_53:

		br label %return_49
	dummy_54:

		br label %merge_56
	if_else_55:

		br label %merge_56
	merge_56:
		%$17_buffer = alloca i32*
		%$array_1 = alloca i32*
		%$size_1 = alloca i32
		%$alloc_iterator_1 = alloca i32
		%$array_access_1 = alloca i32*
		store i32 0, i32* %$alloc_iterator_1
		store i32 16, i32* %$size_1
		store i32 0, i32* %$alloc_iterator_1
		%4 = load i32,i32* %$size_1
		%5 = mul i32 4,%4
		%6 = call i64* @malloc(i32 %5)
		%7 = bitcast i64* %6 to i32*
		store i32* %7, i32** %$array_1
		store i32* %7, i32** %$array_access_1
		br label %alloc_condition_57
	alloc_condition_57:
		%8 = load i32,i32* %$size_1
		%9 = load i32,i32* %$alloc_iterator_1
		%10 = icmp slt i32 %9 ,  %8
		br i1 %10, label %alloc_body_58,label %alloc_merge_59
	alloc_body_58:
		%11 = load i32*,i32** %$array_access_1
		%12 = load i32,i32* %$alloc_iterator_1
		%13 = getelementptr inbounds i32 , i32* %11 , i32 %12
		store i32 0, i32* %13
		%14 = load i32,i32* %$alloc_iterator_1
		%15 = add i32 %14,1
		store i32 %15, i32* %$alloc_iterator_1
		br label %alloc_condition_57
	alloc_merge_59:
		%16 = load i32*,i32** %$array_1
		store i32* %16, i32** %$17_buffer
		%$18_i = alloca i32
		store i32 0, i32* %$18_i
		%$19_cur = alloca i32
		%17 = load i32,i32* %$16_num
		store i32 %17, i32* %$19_cur
		br label %while_condition_60
	while_condition_60:
		%18 = load i32,i32* %$19_cur
		%19 = icmp ne i32 %18 ,  0
		br i1 %19, label %while_body_62,label %merge_61
	merge_61:

		br label %while_merge_63
	while_body_62:
		%20 = load i32*,i32** %$17_buffer
		%21 = load i32,i32* %$18_i
		%22 = getelementptr inbounds i32 , i32* %20 , i32 %21
		%23 = load i32,i32* %$19_cur
		%24 = srem i32 %23,10
		%25 = add i32 %24,48
		store i32 %25, i32* %22
		%26 = load i32,i32* %$19_cur
		%27 = sdiv i32 %26,10
		store i32 %27, i32* %$19_cur
		%28 = load i32,i32* %$18_i
		%29 = add i32 %28,1
		store i32 %29, i32* %$18_i
		br label %while_condition_60
	while_merge_63:
		%30 = load i32*,i32** %$17_buffer
		%31 = load i32,i32* %$18_i
		call void @$11_reverseArray(i32* %30,i32 %31)
		%$20_j = alloca i32
		store i32 0, i32* %$20_j
		br label %while_condition_64
	while_condition_64:
		%32 = load i32,i32* %$20_j
		%33 = load i32,i32* %$18_i
		%34 = icmp slt i32 %32 ,  %33
		br i1 %34, label %while_body_66,label %merge_65
	merge_65:

		br label %while_merge_67
	while_body_66:
		%35 = load i32*,i32** %$17_buffer
		%36 = load i32,i32* %$20_j
		%37 = getelementptr inbounds i32 , i32* %35 , i32 %36
		%38 = load i32,i32* %37
		%39 = call i32 @putchar(i32 %38)
		%40 = load i32,i32* %$20_j
		%41 = add i32 %40,1
		store i32 %41, i32* %$20_j
		br label %while_condition_64
	while_merge_67:

		br label %return_49
}