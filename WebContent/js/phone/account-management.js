/**
 * 账户管理界面所需的js文件
 */
//账户管理页面条目的id
var accountId = '';
//定时器的id
var timeId;

var token;
/**
 * 用户权限
 */
var permission;

var dataList;

$(function(){
	permission = getCookie('permission');
	//获取cookie中的token值
	token = getCookie('token');
	//删除账户按钮的响应事件
	$('#dialog-delete-account').click(function(){
		if(accountId!=''){
			isDelete();
		}else{
			$.messager.alert("提示","请选择要删除的账户!");
		}
	});
	//查看风控消息选项按钮的响应事件
	$('#dialog-control-info').click(function(){
		if(accountId!=''){
			//初始化风控消息类型为警告
			riskControlInfoType = 1;
			setRiskControlShow();
			getRiskControlInfo();
		}else{
			$.messager.alert("提示","请选择要查看风控消息的账户!");
		}
	});
	
	//点击风控指标设置按钮的响应事件
	$('#dialog-risk-control-set').click(function(){
		if(accountId!=''){
			if(permission<5){
				$.messager.alert("提示","对不起，你没有这个权限");
			}else{
				window.location.href = "risk_config.html";
			}
		}else{
			$.messager.alert("提示","请选择要风控指标设置的账户!");
		}
	});
	//点击查看风控消息按钮的响应事件
	$('#risk-control-info-view').click(function(){
		if(accountId!=''){
			window.location.href="risk-control-info.html";
		}else{
			$.messager.alert("提示","请选择要查看风控消息的账户!");
		}
	});
	//点击注销按钮的响应事件，跳转到登录界面
	$('.account-change-button').click(function(){
		window.location.href="index.html";
	});
	
	getManagementData();
	
});

/**
 * 向服务器发送账户管理获取数据的请求
 */
function getManagementData()
{
	$.ajax({
		url:basePath+"client/getClient",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal},
		cache:false,
		async:false,
		success:function(response){
			if(response==0||response.list.length==0){
				$.messager.alert("提示","暂时没有数据!");
			}else{
				var data = response.list;
				dataList = data;
				setAccountManagemnetList(data);
				//如果没有定时器，那么则添加一个定时器，如果已经有了，那么则不需要再添加定时器
				if(timeId==null||typeof(timeId)=='undefined'){
					timeId = setInterval(getManagementDataState,1000*10);
				}
			}
		}
	});	
}

/**
 * 向服务器发送账户管理获取数据状态的请求
 */
function getManagementDataState()
{
	$.ajax({
		url:basePath+"user/stat",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal},
        cache:false,
        success:function(response){
        	var data = response;
        	if(data==0){
        		$.messager.alert("提示","对不起，你的账号已在别的地方登陆！");
        		//清除定时器
        		clearInterval(timeId);
        		window.top.location.href="../index.html";
        	}else{
        		setAccountManagemnetListStat(data);
        	}
        }
	});	
}

/**
 * 向服务器发送账户管理删除账户的请求
 */
function getManagementDelete()
{
	$.ajax({
		url:basePath+"client/delete",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:accountId},
		cache:false,
		async:false,
		success:function(response){
			if(response==1){
				$.messager.alert("提示","删除成功!");
				//更新列表数据
				getManagementData();
			}else{
				$.messager.alert("提示","删除失败!");
			}
		}
	});	
}

/**
 * 向服务器发送账户管理删除账户的请求
 */
function getManagementBasicInfo()
{
	$.ajax({
		url:basePath+"client/getBasic",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:accountId},
		cache:false,
		async:false,
		success:function(response){
			if(response==0){
				$.messager.alert("提示","查看基本信息失败!");
			}else{
				showAccountBasicInfo(response);
			}
		}
	});	
}

/**
 * 此方法用于设置账户管理界面的列表显示内容
 * @param data
 */
function setAccountManagemnetList(data){
	setCookie('clientId',data[0].id,30);
	var html = "";
//	html = html + "	<ul data-role=\"listview\" id=\"contentList\" class=\"contentList\">";
	for (var v = 0; v < data.length; v++) {
		html = html + "<li id=\""+data[v].id+"\" class=\"account-list "+data[v].id+"\">" ;
		html = html + "<input type=\"radio\" name=\"account-list\" id=\""+data[v].id+"\">";
		html = html + "<div class=\"ui-btn-inner ui-li\" style=\"margin-top:16px;\">";
//		html = html + "<a class=\"ui-link-inherit main-opener\" href=\"#\">";
//		html = html + data[v].name;
		html = html + "<h3 class=\"listtitle ui-li-heading\">"+"账户"+data[v].name+" </h3>";
//		html = html + "</a>";
		html = html + "</div>";
		html = html + "</li>";
	}
//	html = html + "</ul>";
	
	$('#contentList').empty();
	$(html).appendTo('#contentList').trigger('create');
	
	for (var v = 0; v < data.length; v++) {
		setAccountManagemnetListBackground(data[v].stat,data[v].id);
	}
	
	//点击条目获取条目id的响应事件
	$('.account-list').click(function(){
		accountId = $(this).attr("id");
		setCookie('clientId',accountId,30);
		$('#'+accountId+' input').prop("checked",true);
	});
	
}

/**
 * 此方法用于设置账户管理界面的列表显示内容的背景颜色
 * @param data
 */
function setAccountManagemnetListStat(data){
	for (var v = 0; v < data[0].length; v++) {
		setAccountManagemnetListBackground(data[1][v],data[0][v]);
	}
	
	for (var v = 0; v < dataList.length; v++) {
		if(data[0][v]==dataList[v].id&&(data[1][v]==1||data[1][v]==2)){
			window.top.showRiskMsg(dataList[v].name,data[0][v],data[1][v]+'');
			break;
		}
	}
}

/**
 * 此方法用于设置账户管理界面的列表显示内容的背景颜色
 * @param stat 条目状态值
 * @param id 条目id
 */
function setAccountManagemnetListBackground(stat,id){
	if(stat=='0'){//正常，绿色
		$('.'+id).css("background","#67E167");
	}else if(stat=='1'){//警告，黄色
		$('.'+id).css("background","#FFFF67");
//		warningRemind();
	}else if(stat=='2'){//平仓，红色
		$('.'+id).css("background","#FF4F4F");
//		warningRemind();
	}
}

/**
 * 删除账户提示框
 */
function isDelete() {
	var r = confirm('确认删除？');
	showDeleteConfirm(r);
}
/**
 * 删除账户对话框按钮的响应函数
 */
function showDeleteConfirm(r) {
	//1表示点击确定
	if (r) {
		getManagementDelete();
	}
}