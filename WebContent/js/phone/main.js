/**
 * 所有界面所需的一些共用方法的js文件
 */
//扩展easyui表单的验证
$.extend($.fn.validatebox.defaults.rules, {
	//校验金额
	largeThan : {//value值为文本框中的值
        validator: function (value, param) {
        	var compareVal = $(param[0]).textbox("getValue");
        	if(compareVal == null || compareVal == "" || compareVal-1 < value-1){
        		$.fn.validatebox.defaults.rules.largeThan.message = '填写的值必须大于' + param[1];
        		return false;
        	}
        	return true;
        }
	},
	//校验比例
	percent : {//value值为文本框中的值
        validator: function (value, param) {
        	 if (value == null || value == "" || value.length < param[0] || value.length > param[1]) {
                 $.fn.validatebox.defaults.rules.percent.message = '百分比长度必须在' + param[0] + '至' + param[1] + '范围';
                 return false;
             } else {
            	 if (!/^(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*))$/.test(value)) {//正浮点数
                     $.fn.validatebox.defaults.rules.percent.message = '填写百分比，例如：85、84.1、85.15';
                     return false;
                 } else if(value > 100){
                	 $.fn.validatebox.defaults.rules.percent.message = '填写百分比，不能大于100%';
                	 return false;
                 } else {
                     return true;
                 }
             }
        }
    },
    //校验金额
	integer : {//value值为文本框中的值
        validator: function (value, param) {
        	 if (value.length < param[0] || value.length > param[1]) {
                 $.fn.validatebox.defaults.rules.integer.message = '填写的整数位数必须在' + param[0] + '至' + param[1] + '范围';
                 return false;
             } else {
                 if (!/^[0-9]*$/.test(value)) {//整浮点数
                     $.fn.validatebox.defaults.rules.integer.message = '填写例如：1002910 这样的数值';
                     return false;
                 } else {
                     return true;
                 }
             }
        },
        message: "填写例如：1002910 这样的数值"
    },
    //合约品种号
    variety: {//value值为文本框中的值
        validator: function (value, param) {
        	 if (value.length < param[0] || value.length > param[1]) {
                 $.fn.validatebox.defaults.rules.variety.message = '合约品种号长度必须在' + param[0] + '至' + param[1] + '范围';
                 return false;
             } else {
                 if (!/^[A-Za-z0-9]+$/.test(value)) {
                     $.fn.validatebox.defaults.rules.variety.message = '填写合约及品种号，如：IF 、IF1505等等';
                     return false;
                 } else {
                     return true;
                 }
             }
        },
        message: '填写合约及品种号，如：IF 、IF1505等等'
    }
});

//全局textbox配置
$.fn.textbox.defaults = $.extend({},$.fn.textbox.defaults,$.fn.validatebox.defaults);
$.fn.textbox.defaults = $.extend({},$.fn.textbox.defaults,{
	missingMessage : "此输入框不能为空"
});

//全局jquery ajax配置
(function($){  
    //备份jquery的ajax方法   
    var _ajax=$.ajax;  
    //重写jquery的ajax方法   
    $.ajax=function(opt){  
        //备份opt中error和success方法   
        var fn = {  
            error:function(XMLHttpRequest, textStatus, errorThrown){},
            success:function(data, textStatus){},
            beforeSend:function(XHR){}
        }  
        if(opt.error){  
            fn.error=opt.error;  
        }  
        if(opt.success){  
            fn.success=opt.success;  
        }
        if(opt.beforeSend){
        	fn.beforeSend = opt.beforeSend;
        }
        if(opt.url){
        	if(opt.url.indexOf("?") < 0){
        		opt.url += "?";
        	}
        	if(opt.url.indexOf("?") == opt.url.length - 1){
        		opt.url += "random=" + new Date().getTime();
        	}else{
        		opt.url += "&random=" + new Date().getTime();
        	}
        }
        //扩展增强处理   
        var _opt = $.extend(opt,{ 
            error:function(XMLHttpRequest, textStatus, errorThrown){
            	if(textStatus == "parsererror"){
            		if(errorThrown.message != null && typeof(errorThrown.message) != "undefined" && 
                	typeof(errorThrown.message) == "string"){
                		if(XMLHttpRequest.responseText.indexOf("window.top.location.href") > -1){//session过期了！！
                			alert("用户在另一处登录，您已经下线");
                			window.top.location.href = "../index.html";
	                	}
	                }
            	}
                //错误方法增强处理   
                fn.error(XMLHttpRequest, textStatus, errorThrown);  
            },  
            success:function(data, textStatus){
                //成功回调方法增强处理   
                fn.success(data, textStatus);  
            },
            beforeSend:function(XHR){
            	fn.beforeSend(XHR);
            }
        });  
        _ajax(_opt);  
    };  
})(jQuery);

var basePath = getBasePath();
var tokenVal = getCookie("token");

function getBasePath(){ 
	var obj=window.location; 
	var contextPath=obj.pathname.split("/")[1]; 
	var basePath=obj.protocol+"//"+obj.host+"/"+contextPath+"/"; 
	return basePath; 
}

/**
 * 此变量用于标识是否是第一次动态生成操作的下拉列表显示，如果是第一次那么就不用刷新样式，要不然显示是有问题的，如果不是第一次那么就刷新样式
 */
var tab_title = {
	'account-management':"账户管理"	,
	'money-management':'资金管理',
	'positions-management':'持仓管理'
}
var operateSelectShowFlag = 0;

var input_text = 
{
		"name_null":"名称不能为空",
		"pwd_null":"密码不能为空",
		"account_null":"账号不能为空",
		"init":"初始资金不能小于等于0",
		"bad":"劣后资金不能小于等于0",
		"name_over":"你输入的名称超过规定长度50,请重新输入！",
		"info":"请仔细检查账号和密码是否正确！"
};
var oprerateFlag = true;

/**
 * 点击退出的响应事件
 * @param text
 */
function mainOptionClick(text){
	if(text=="退出"){
		isExit();
	}else if(text=="注销"){
//		$('.change-user').attr("href","#main");
		//清除定时器
		clearInterval(timeId);
		$.mobile.changePage("#login");
	}
}

/**
 * 此方法用于格式化时间
 */
var formatTime = function(time, format){
    var t = new Date(time);
    var tf = function(i){return (i < 10 ? '0' : '') + i};
    return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a){
        switch(a){
            case 'yyyy':
                return tf(t.getFullYear());
                break;
            case 'MM':
                return tf(t.getMonth() + 1);
                break;
            case 'mm':
                return tf(t.getMinutes());
                break;
            case 'dd':
                return tf(t.getDate());
                break;
            case 'HH':
                return tf(t.getHours());
                break;
            case 'ss':
                return tf(t.getSeconds());
                break;
        }
    });
}

/**
 * 此方法用于验证用户输入的数字是否合法，并根据不同情况进行提示
 * @param num来自资金比例方式的编号
 * @param t 该输入框对象
 * @returns
 */
function checkDigital(num,t){
	var data = $(t).val();
	if(data.indexOf('元')>=0){
		data = data.split('元')[0];
	} 
	data = new Number(data);
	if(!riskControlCheckDigital(data)){
		alert("你只能输入大于0的数字");
	}
}

/**
 * 此方法用于验证用户输入的百分比是否合法，并根据不同情况进行提示
 * @param num来自资金比例方式的编号
 * @param t 该输入框对象
 * @returns
 */
function checkRate(num,t){
	var data = $(t).val();
	if(data.indexOf('%')>=0){
		data = data.split('%')[0];
	} 
	data = new Number(data);
	if(!riskControlCheckRate(data)){
		alert("你只能输入大于等于0小于等于100的数字");
	}
}

/**
 * 此方法用于验证用户输入的百分比是否合法，并根据不同情况进行提示
 * @param num来自资金比例方式的编号
 * @param t 该输入框对象
 * @returns
 */
function checkType(num,t){
	var str = $(t).val();
	if(!riskControlCheckType(str)){
		alert("品种号只能是1-2个的英文字母，请重新输入！");
	}
}

/**
 * 此方法用于验证用户输入的时间是否合法，并根据不同情况进行提示，格式:7:00
 * @param num来自资金比例方式的编号
 * @param t 该输入框对象
 * @returns
 */
function checkTime(num,t){
	if(!riskControlCheckTime($(t).val())){
		alert("请输入时间段范围00:00到23:59的数据格式!");
	}
}

/**
 * 此方法用于检查用户输入数字的合法性，合法则返回true，不合法返回false;
 * @param num需要检查合法性的数字
 * @returns {Boolean}
 */
function riskControlCheckDigital(num){
	var flag = false;
	if(num>0&&num<1000000000){
		flag = true;
	}
	return flag;
}

/**
 * 此方法用于检查用户输入百分比的合法性，合法则返回true，不合法返回false;
 * @param num需要检查合法性的数字
 * @returns {Boolean}
 */
function riskControlCheckRate(num){
	var flag = false;
	if(num>=0&&num<=100){
		flag = true;
	}
	return flag;
}

/**
 * 此方法用于校验品种号是否符合要求
 * @param str 用户输入
 * @returns {Boolean} true表示合法，false表示不合法
 */
function riskControlCheckType(str){
	var flag = false;
	var r = str.match(/^[A-Za-z]{1,2}$/);
	if(r!=null){
		flag = true;
	}
	return flag;
}


/**
 * 此方法用于检查用户输入时间段的合法性，合法则返回true，不合法返回false;
 * @param str需要检查合法性的时间字符
 * @returns {Boolean}
 */
function riskControlCheckTime(str){
	var flag = false;
	var r = str.match(/^(\d{1,2}):(\d{1,2})$/);
	if(r!=null){
		//比如07:59,冒号前面的数字必须小于24，而冒号后面的数字必须小于60
		if(new Number(str.split(":")[0])<24&&new Number(str.split(":")[1])<60){
			flag = true;
		}
	}
	return flag;
}

/**
 * 此方法用于对数字进行小数点保留两位，并且进行四舍五入，比如7.999 结果为：8.00  7.333 结果为：7.33
 * @param data
 * @returns
 */
function handleDigital(data){
	return data.toFixed(2);
}

/**
 * 此方法用于设置操作下拉列表的显示
 */
function setOperateSelectMenu(id,selectId){
	var html = "";
//	html = html + "<div class=\"ui-btn-right\">";
	html = html + "<select id=\""+selectId+"\" data-native-menu=\"false\" data-mini=\"true\" data-inline=\"true\" data-icon=\"arrow-d\" >";
	html = html + "<option value=\"choose-one\" data-placeholder=\"true\">操作</option>";
	html = html + "<option value=\"0\" class=\"change\"><a href=\"#\" class=\"change-user\" data-transition=\"none\" >注销</a></option>";
//	html = html + "<option value=\"1\" class=\"exit\">退出</option>";
	html = html + "</select>";
//	html = html + "</div>";
	$('#'+id).empty();
	$(html).appendTo('#'+id).trigger('create');
	if(operateSelectShowFlag >= 3){
		$("#"+selectId).selectmenu(); 
		$("#"+selectId).selectmenu("refresh");
	}

	operateSelectShowFlag ++;
	//点击退出按钮
 	$('.ui-btn-right #'+selectId).bind("change",function(){
 		var text = $(this).find("option:selected").text();
 		mainOptionClick(text);
 	});
}

