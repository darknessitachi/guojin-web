$.extend($.fn.textbox.defaults,{
	missingMessage : "此输入框不能为空"
});
/**
 * 登录界面所需的js文件
 */
var token;
/**
 * 用户权限
 */
var permission;
/**
 * 此变量用于标识用户是否点击记住登录名，true表示选择记住登录名，false表示不记住登录名
 */
var loginRememberFlag = false;

function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}

$(function(){
	//如果参数传了用户和密码，直接登录
	if(getQueryString("loginName") != null){
		var name = getQueryString('loginName');
		var pwd = getQueryString('loginPassword');
		loginAction(name, pwd);
	}
	
	//记住登录名选项按钮的响应事件
	$('#remember-login').click(function(){
		if($(this).prop("checked")){
			loginRememberFlag = true;
		}else{
			loginRememberFlag = false;
		}
	});
	loginRememberFlag = getCookie('loginRememberFlag');
	//如果有保存值，则显示本地保存的值
	if(loginRememberFlag=='true'){
		$('#loginName').textbox("setValue",getCookie('name'));
		$('#loginPassword').textbox("setValue",strDec(getCookie('pwd'),key1,key2,key3));
		$('#remember-login').prop('checked',true);
	}else{
		$('#remember-login').prop('checked',false);
	}
	
	//响应对话框中确定按钮事件
//	$('#login-button').click(function(){
//		;
//	}); 
	
	//监听回车事件
	document.onkeydown=function(event){
		//判断回车事件是否来自登陆输入密码文本框
		var e = event || window.event || arguments.callee.caller.arguments[0];
		if(e && e.keyCode==13 && document.activeElement.className=="textbox-text validatebox-text"){ // enter 键
			$("#remember-login").trigger("focus");
//			$("#login-button").trigger("focus");
			setTimeout(checkLoginInfo,300);
		}
	}; 

});

/**
 * 此方法用于验证用户输入的登陆信息是否合法，然后根据不同的情况进行不同操作
 */
function checkLoginInfo(){
	//账号
	var name = $('#loginName').textbox("getValue");
	//密码
	var pwd = $('#loginPassword').textbox("getValue");
	if($('#loginName').textbox("isValid") && $('#loginPassword').textbox("isValid")){
        //第一个参数必须；第二个、第三个参数可选
        //加密方法        
        var  encodePwd = strEnc(pwd,key1,key2,key3);            
		loginAction(name, encodePwd);
	}
}

function loginAction(name,pwd){
	$.ajax({
		url:"user/login",
        dataType: 'json',
		type:"post",
        data:{name:name,password:pwd},
		success:function(data){
			if(data.token==""||data.token==null){//返回的null表示登录不成功
				$.messager.alert("登录失败","用户名或者密码错误!");
			}else{
				token = data.token;
				permission = data.permission;
				//用户点击了记住登录名按钮，则在此将登录名和密码保存到本地
				setCookie('token',token,30);
				setCookie('permission',permission,30);
				setCookie('loginRememberFlag',loginRememberFlag,30);
				setCookie('name',name,30);
				setCookie('pwd',pwd,30);
				if(data.redirectIp != null && data.redirectIp != window.location.host){
					window.location.href="http://" + data.redirectIp + window.location.pathname + "?loginName=" + name + "&loginPassword=" + pwd;
					return;
				}
				window.location.href="phone/manage-frame.html";
			}
		}
	});
}


//扩展easyui表单的验证
$.extend($.fn.validatebox.defaults.rules, {
	//校验比例
	length : {//value值为文本框中的值
        validator: function (value, param) {
        	 if (value.length < param[0] || value.length > param[1]) {
                 $.fn.validatebox.defaults.rules.length.message = '输入长度必须在' + param[0] + '至' + param[1] + '范围';
                 return false;
             }
        	 return true;
        },
        message: ""
    }
});