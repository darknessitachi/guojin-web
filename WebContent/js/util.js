var input_name_text = {
		"name_null":"名称不能为空",
		"name_over":"你输入的名称超过规定长度50,请重新输入！",
		"name_exist":"你输入的名称已经存在,请重新输入！"
};

var input_num_text = {
		"num_null":"投顾数不能为空",
		"num_account_null":"投顾账号不能为空",
		"num_over":"你输入的投顾数超过规定范围0-500，请重新输入！",
		"not_num":"你输入的为非数字，请输入数字！"
};

var input_pwd_text = {
		"pwd_null":"密码不能为空",
		"pwd_equal":"你输入的密码前后不一致！"
};
//名称和密码
var adminName = 'admin';
var adminPassword = 'guojin123';
var basePath = "";

/**
 * 文本框获取焦点时，检查文本框状态，然后恢复到默认的状态
 * @param t 文本框对象
 */
function checkNameFocus(t){
	if(t.value==input_name_text["name_null"]||t.value==input_name_text["name_over"]||t.value==input_name_text["name_exist"]){
		t.value='';
		t.style.color='#000';
	}
}

/**
 * 文本框失去焦点时，检查文本是否有输入值，有则不提示，无则提示用户输入
 * @param t
 */
function checkNameBlur(t){
	if(t.value==''){
		$(t).val(input_name_text["name_null"]);
		t.style.color='#999';
	}
}

/**
 * 检查文本内容是否符合要求
 * @param id 文本框的id
 * @param name 文本框用户输入的值
 * @returns {Boolean} 返回true表示用户输入符合要求，返回false表示输入不符合要求
 */
function checkName(id,name){
	var flag = false;
	if(name==''||name==input_name_text["name_over"]||name==input_name_text["name_null"]
	||name==input_name_text["name_exist"]){
		$(id).val(input_name_text["name_null"]);
		$(id).css('color','#999');
	}else if(name.length>50){
		$(id).val(input_name_text["name_over"]);
		$(id).css('color','#999');
	}else if(isExistName(name)){
		$(id).val(input_name_text["name_exist"]);
		$(id).css('color','#999');
	}else{
		flag = true;
	}
	return flag;
}

/**
 * 数字框获取焦点时，检查数字框状态，然后恢复到默认的状态
 * @param t 文本框对象
 */
function checkNumFocus(t){
	if(t.value==input_num_text["num_null"]||t.value==input_num_text["num_over"]
	    ||t.value==input_num_text["not_num"]||t.value==input_num_text["num_account_null"]){
		t.value='';
		t.style.color='#000';
	}
}

/**
 * 数字框失去焦点时，检查数字框是否有输入值，有则不提示，无则提示用户输入
 * @param t
 */
function checkPwdBlur(t){
	if(t.value==''){
		$(t).attr("type","text");
		$(t).val(input_pwd_text["pwd_null"]);
		t.style.color='#999';
	} 
}

/**
 * 数字框获取焦点时，检查数字框状态，然后恢复到默认的状态
 * @param t 文本框对象
 */
function checkPwdFocus(t){
	if(t.value==input_pwd_text["pwd_null"]||t.value==input_pwd_text["pwd_equal"]){
		$(t).attr("type","password");
		t.value='';
		t.style.color='#000';
	}
}

/**
 * 数字框失去焦点时，检查数字框是否有输入值，有则不提示，无则提示用户输入
 * @param t
 */
function checkNumBlur(t){
	if(t.value==''){
		if($(t).attr('id').indexOf('account')>=0){
			$(t).val(input_num_text["num_account_null"]);
		}else{
			$(t).val(input_num_text["num_null"]);
		}
		t.style.color='#999';
	} 
}

/**
 * 检查数字是否符合要求
 * @param id 数字框id
 * @param num 用户输入的数字值
 * @returns {Boolean} 返回true表示用户输入符合要求，返回false表示输入不符合要求
 */
function checkNum(id,num){
	var flag = false;
	if(num==''||num==input_num_text["num_over"]||num==input_num_text["num_null"]
	||num==input_num_text["not_num"]){
		$(id).val(input_num_text["num_null"]);
		$(id).css('color','#999');
	}else if(isNaN(num)){
		$(id).val(input_num_text["not_num"]);
		$(id).css('color','#999');
	}else if(num<0||num>500){
		$(id).val(input_num_text["num_over"]);
		$(id).css('color','#999');
	}else{
		flag = true;
	}
	return flag;
}

/**
 * 检查账号是否符合要求
 * @param id 数字框id
 * @param num 用户输入的数字值
 * @returns {Boolean} 返回true表示用户输入符合要求，返回false表示输入不符合要求
 */
function checkAccount(id,num){
	var flag = false;
	if(num==''||num==input_num_text["num_over"]||num==input_num_text["not_num"]||num==input_num_text["num_account_null"]){
		$(id).val(input_num_text["num_account_null"]);
		$(id).css('color','#999');
	}else if(isNaN(num)){
		$(id).val(input_num_text["not_num"]);
		$(id).css('color','#999');
	}else{
		flag = true;
	}
	return flag;
}


/**
 * 检查数字是否符合要求
 * @param id 数字框id
 * @param num 用户输入的数字值
 * @returns {Boolean} 返回true表示用户输入符合要求，返回false表示输入不符合要求
 */
function checkPassWord(id,pwd){
	var flag = false;
	if(pwd==''||pwd==input_pwd_text["pwd_null"]
	||pwd==input_pwd_text["pwd_equal"]){
		$(id).attr("type","text");
		$(id).val(input_pwd_text["pwd_null"]);
		$(id).css('color','#999');
	}else{
		$(id).attr("type","password");
		flag = true;
	}
	return flag;
}

/**
 * 此方法用于检验用户输入的组名称是否已经存在
 * @param name 用户输入的组名称
 * @returns {Boolean} 如果用户输入的组名称已经存在那么返回true,不存在则返回false
 */
function isExistName(name){
	var flag = false;
	for (var v = 0; v < listData.length; v++) {
		if(name==listData[v].name&&v!=num){
			flag = true;
			break;
		}
	}
	return flag;
}