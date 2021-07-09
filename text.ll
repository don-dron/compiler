; ModuleID = 'main'
source_filename = "main"
declare i32* @malloc(i32)
define i32 @main(){
	header_0:
		%$$_ret_value_0 = alloca i32
		br label %entry_2
	return_1:
		%0 = load i32,i32* %$$_ret_value_0
		ret i32 %0
	entry_2:
		%$9_size = alloca i32
		store i32 100, i32* %$9_size
		%$10_a = alloca i32*
		%1 = load i32,i32* %$9_size
		%2 = mul i32 4,%1
		%3 = call i32* @malloc(i32 %2)
		store i32* %3, i32** %$10_a
		%$11_i = alloca i32
		store i32 0, i32* %$11_i
		br label %while_condition_3
	while_condition_3:
		%4 = load i32,i32* %$11_i
		%5 = load i32,i32* %$9_size
		%6 = icmp slt i32 %4 ,  %5
		br i1 %6, label %while_body_5,label %merge_4
	merge_4:

		br label %while_merge_6
	while_body_5:
		%7 = load i32*,i32** %$10_a
		%8 = load i32,i32* %$11_i
		%9 = getelementptr inbounds i32 , i32* %7 , i32 %8
		%10 = load i32,i32* %$9_size
		%11 = load i32,i32* %$11_i
		%12 = sub i32 %10,%11
		store i32 %12, i32* %9
		%13 = load i32,i32* %$11_i
		%14 = add i32 %13,1
		store i32 %14, i32* %$11_i
		br label %while_condition_3
	while_merge_6:
		store i32 0, i32* %$11_i
		%$12_sum = alloca i32
		store i32 0, i32* %$12_sum
		br label %while_condition_7
	while_condition_7:
		%15 = load i32,i32* %$11_i
		%16 = load i32,i32* %$9_size
		%17 = icmp slt i32 %15 ,  %16
		br i1 %17, label %while_body_9,label %merge_8
	merge_8:

		br label %while_merge_10
	while_body_9:
		%18 = load i32,i32* %$12_sum
		%19 = load i32*,i32** %$10_a
		%20 = load i32,i32* %$11_i
		%21 = getelementptr inbounds i32 , i32* %19 , i32 %20
		%22 = load i32,i32* %21
		%23 = add i32 %18,%22
		store i32 %23, i32* %$12_sum
		%24 = load i32,i32* %$11_i
		%25 = add i32 %24,1
		store i32 %25, i32* %$11_i
		br label %while_condition_7
	while_merge_10:

		br label %local_return_11
	local_return_11:
		%26 = load i32*,i32** %$10_a
		%27 = load i32,i32* %$9_size
		%28 = sub i32 %27,1
		%29 = call i32 @$0_quickSort(i32* %26,i32 0,i32 %28)
		store i32 %29, i32* %$$_ret_value_0
		br label %return_1
	dummy_12:

		br label %return_1
}
define i32 @$0_quickSort(i32*,i32,i32){
	header_13:
		%$2_numbers = alloca i32*
		store i32* %0, i32** %$2_numbers
		%$3_left = alloca i32
		store i32 %1, i32* %$3_left
		%$4_right = alloca i32
		store i32 %2, i32* %$4_right
		%$$_ret_value_1 = alloca i32
		br label %entry_15
	return_14:
		%3 = load i32,i32* %$$_ret_value_1
		ret i32 %3
	entry_15:
		%$5_pivot = alloca i32
		%$6_l_hold = alloca i32
		%4 = load i32,i32* %$3_left
		store i32 %4, i32* %$6_l_hold
		%$7_r_hold = alloca i32
		%5 = load i32,i32* %$4_right
		store i32 %5, i32* %$7_r_hold
		%6 = load i32*,i32** %$2_numbers
		%7 = load i32,i32* %$3_left
		%8 = getelementptr inbounds i32 , i32* %6 , i32 %7
		%9 = load i32,i32* %8
		store i32 %9, i32* %$5_pivot
		br label %while_condition_16
	while_condition_16:
		%10 = load i32,i32* %$3_left
		%11 = load i32,i32* %$4_right
		%12 = icmp slt i32 %10 ,  %11
		br i1 %12, label %while_body_18,label %merge_17
	merge_17:

		br label %while_merge_35
	while_body_18:

		br label %while_condition_19
	while_condition_19:
		%13 = load i32*,i32** %$2_numbers
		%14 = load i32,i32* %$4_right
		%15 = getelementptr inbounds i32 , i32* %13 , i32 %14
		%16 = load i32,i32* %15
		%17 = load i32,i32* %$5_pivot
		%18 = icmp sge i32 %16 ,  %17
		%19 = load i32,i32* %$3_left
		%20 = load i32,i32* %$4_right
		%21 = icmp slt i32 %19 ,  %20
		%22 = and i1 %18,%21
		br i1 %22, label %while_body_21,label %merge_20
	merge_20:

		br label %while_merge_22
	while_body_21:
		%23 = load i32,i32* %$4_right
		%24 = sub i32 %23,1
		store i32 %24, i32* %$4_right
		br label %while_condition_19
	while_merge_22:

		br label %if_condition_23
	if_condition_23:
		%25 = load i32,i32* %$3_left
		%26 = load i32,i32* %$4_right
		%27 = icmp ne i32 %25 ,  %26
		br i1 %27, label %if_then_24,label %if_else_25
	if_then_24:
		%28 = load i32*,i32** %$2_numbers
		%29 = load i32,i32* %$3_left
		%30 = getelementptr inbounds i32 , i32* %28 , i32 %29
		%31 = load i32*,i32** %$2_numbers
		%32 = load i32,i32* %$4_right
		%33 = getelementptr inbounds i32 , i32* %31 , i32 %32
		%34 = load i32,i32* %33
		store i32 %34, i32* %30
		%35 = load i32,i32* %$3_left
		%36 = add i32 %35,1
		store i32 %36, i32* %$3_left
		br label %merge_26
	if_else_25:

		br label %merge_26
	merge_26:

		br label %while_condition_27
	while_condition_27:
		%37 = load i32*,i32** %$2_numbers
		%38 = load i32,i32* %$3_left
		%39 = getelementptr inbounds i32 , i32* %37 , i32 %38
		%40 = load i32,i32* %39
		%41 = load i32,i32* %$5_pivot
		%42 = icmp sle i32 %40 ,  %41
		%43 = load i32,i32* %$3_left
		%44 = load i32,i32* %$4_right
		%45 = icmp slt i32 %43 ,  %44
		%46 = and i1 %42,%45
		br i1 %46, label %while_body_29,label %merge_28
	merge_28:

		br label %while_merge_30
	while_body_29:
		%47 = load i32,i32* %$3_left
		%48 = add i32 %47,1
		store i32 %48, i32* %$3_left
		br label %while_condition_27
	while_merge_30:

		br label %if_condition_31
	if_condition_31:
		%49 = load i32,i32* %$3_left
		%50 = load i32,i32* %$4_right
		%51 = icmp ne i32 %49 ,  %50
		br i1 %51, label %if_then_32,label %if_else_33
	if_then_32:
		%52 = load i32*,i32** %$2_numbers
		%53 = load i32,i32* %$4_right
		%54 = getelementptr inbounds i32 , i32* %52 , i32 %53
		%55 = load i32*,i32** %$2_numbers
		%56 = load i32,i32* %$3_left
		%57 = getelementptr inbounds i32 , i32* %55 , i32 %56
		%58 = load i32,i32* %57
		store i32 %58, i32* %54
		%59 = load i32,i32* %$4_right
		%60 = sub i32 %59,1
		store i32 %60, i32* %$4_right
		br label %merge_34
	if_else_33:

		br label %merge_34
	merge_34:

		br label %while_condition_16
	while_merge_35:
		%61 = load i32*,i32** %$2_numbers
		%62 = load i32,i32* %$3_left
		%63 = getelementptr inbounds i32 , i32* %61 , i32 %62
		%64 = load i32,i32* %$5_pivot
		store i32 %64, i32* %63
		%65 = load i32,i32* %$3_left
		store i32 %65, i32* %$5_pivot
		%66 = load i32,i32* %$6_l_hold
		store i32 %66, i32* %$3_left
		%67 = load i32,i32* %$7_r_hold
		store i32 %67, i32* %$4_right
		%$8_v = alloca i32
		store i32 0, i32* %$8_v
		br label %if_condition_36
	if_condition_36:
		%68 = load i32,i32* %$3_left
		%69 = load i32,i32* %$5_pivot
		%70 = icmp slt i32 %68 ,  %69
		br i1 %70, label %if_then_37,label %if_else_38
	if_then_37:
		%71 = load i32*,i32** %$2_numbers
		%72 = load i32,i32* %$3_left
		%73 = load i32,i32* %$5_pivot
		%74 = sub i32 %73,1
		%75 = call i32 @$0_quickSort(i32* %71,i32 %72,i32 %74)
		br label %merge_39
	if_else_38:

		br label %merge_39
	merge_39:

		br label %if_condition_40
	if_condition_40:
		%76 = load i32,i32* %$4_right
		%77 = load i32,i32* %$5_pivot
		%78 = icmp sgt i32 %76 ,  %77
		br i1 %78, label %if_then_41,label %if_else_42
	if_then_41:
		%79 = load i32*,i32** %$2_numbers
		%80 = load i32,i32* %$5_pivot
		%81 = load i32,i32* %$4_right
		%82 = call i32 @$0_quickSort(i32* %79,i32 %80,i32 %81)
		br label %merge_43
	if_else_42:

		br label %merge_43
	merge_43:

		br label %local_return_44
	local_return_44:
		%83 = load i32,i32* %$8_v
		store i32 %83, i32* %$$_ret_value_1
		br label %return_14
	dummy_45:

		br label %return_14
}