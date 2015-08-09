/**
 * 添加账户界面所需的js文件
 */
/**
 * 此变量用于标识点击来自添加账户还是查看账户信息，这样再进行保存时，来区分该调用add请求还是change请求，1表示来自添加账户、2来自查看账户信息。
 */
var addChangeFlag;
$(function(){
	//跳转到添加账户页面的按钮的响应事件
	$('#management-add-account-button').click(function(){
//		$('#add-account-table tr td').find('input').removeAttr("readonly");
		$('#add-account-table div').find('input').val('');
		$("#add-account-table div input[type='checkbox']").prop("checked",false);
		$('#add-account-dlg').dialog('open').dialog('setTitle','添加账户');
		$('#add-account-table').form('clear');
		setAddAccountAlertPosition();
		addChangeFlag = 1;
	});
	//基本信息按钮的响应事件
	$('#dialog-basic-info').click(function(){
		if(accountId!=''){
			getManagementBasicInfo();
			$('#add-account-dlg').dialog('open').dialog('setTitle','编辑账户信息');
			$('.add-account-info').css('display','none');
			setAddAccountAlertPosition();
			addChangeFlag = 2;
		}else{
			$.messager.alert("提示","请选择要编辑的账户!");
		}
	});
	
//	//在添加账户界面跳转到风控设置界面按钮的响应事件
//	$('#add-to-risk-control-setting').click(function(){
//		if(permission<5){
//			$.messager.alert("对不起，你没有这个权限");
//		}else{
//			getRiskDataRequest();
//			$.mobile.changePage("#risk-control-setting-page");
//		}
//	});
	
	//添加账户按钮的响应事件
	$('#add-account-button').click(function(){
		var account = $('#add-account-table div:eq(0)').find('input')[0].value;
		var pwd = $('#add-account-table div:eq(1)').find('input')[0].value;
		var name = $('#add-account-table div:eq(2)').find('input')[0].value;
		var initMoney = new Number($('#add-account-table div:eq(3)').find('input')[0].value);
		var badMoney = new Number($('#add-account-table div:eq(5)').find('input')[0].value);
		
		//获取用户是否选中checkbox控件，true表示选中，false表示未选中
		var badFlag = $('#add-account-table div:eq(4)').find('input')[0].checked;
		//未勾选劣后资金的时候传值为-1,如果勾选则传递用户输入的值
		if(!badFlag){
			badMoney = -1;
		}else{
			if(badMoney<0||badMoney>1000000000){
				$('#add-account-table div .add-account-info:eq(4)').text(input_text["bad"]);
				$('#add-account-table div .add-account-info:eq(4)').css("display","block");
			}
		}
		
		$('#add-account-table div .add-account-info').css("display","none");
		if(account==""){
			$('#add-account-table div .add-account-info:first').text(input_text["account_null"]);
			$('#add-account-table div .add-account-info:first').css("display","block");
		}else if(pwd==""){
			$('#add-account-table div .add-account-info:eq(1)').text(input_text["pwd_null"]);
			$('#add-account-table div .add-account-info:eq(1)').css("display","block");
		}else if(name==''){
			$('#add-account-table div .add-account-info:eq(2)').text(input_text["name_null"]);
			$('#add-account-table div .add-account-info:eq(2)').css("display","block");
		}else if(initMoney<0||initMoney>1000000000){
			$('#add-account-table div .add-account-info:eq(3)').text(input_text["init"]);
			$('#add-account-table div .add-account-info:eq(3)').css("display","block");
		}else{
			//满足以上的条件的输入将进行添加请求
			if(addChangeFlag==1){
				getAddAccountData(account,pwd,name,initMoney,badMoney);
			}else if(addChangeFlag==2){
				getChangeAccountData(account,pwd,name,initMoney,badMoney);
			}
		}
		
	});
});

/**
 * 向服务器发送添加账号请求，获取添加的结果
 */
function getAddAccountData(account,pwd,name,initMoney,badMoney)
{
	$.ajax({
		url:basePath+"client/addClient",
        dataType: 'json',
		type:"post",
        data:{token:token,name:account,password:pwd,
        	  clientName:name,initFunds:initMoney,afterFunds:badMoney},
		async:false,
		success:function(data){
			//返回的0表示登录不成功
			if(data=="0"){
//				$('#name-info span').text(input_text["info"]);
//				$('#name-info').css("display","block");
				$.messager.alert("提示","添加失败，原因可能是网络断开或者输入信息有误");
			}else if(data=="1"){//返回1表示成功
				//动态为标签添加属性
//				$('#login-button').attr("href","#main");
				$.messager.alert("提示","添加成功");
				$('#add-account-dlg').window('close'); 
				getManagementData();
			}
		}
	});	
}

/**
 * 向服务器发送修改账号请求，获取修改的结果
 */
function getChangeAccountData(account,pwd,name,initMoney,badMoney)
{
	$.ajax({
		url:basePath+"client/changeClient",
        dataType: 'json',
		type:"post",
        data:{token:token,name:account,password:pwd,
        	  clientName:name,initFunds:initMoney,afterFunds:badMoney,clientID:accountId},
		async:false,
		success:function(data){
			//返回的0表示登录不成功
			if(data=="0"){
//				$('#name-info span').text(input_text["info"]);
//				$('#name-info').css("display","block");
				$.messager.alert("提示","修改失败，原因可能是网络断开或者输入信息有误");
			}else if(data=="1"){//返回1表示成功
				//动态为标签添加属性
//				$('#login-button').attr("href","#main");
				$.messager.alert("提示","修改成功");
				getManagementData();
			}
		}
	});	
}

/**
 * 显示账户的基本信息
 * @param data数据库中查到的用户基本账户信息
 */
function showAccountBasicInfo(data){
//	$('#add-account-table tr td').find('input').attr("readonly","true");
//	$('#add-account-button').hide();
	setAddAccountInputValue(data);
}

/**
 * 设置添加账户页面输入框中的值
 */
function setAddAccountInputValue(data){
	//如果劣后资金从数据库读出的数据是-1，则表示不包含劣后资金，反之则包含
	if(data.afterFunds!=-1){
		$("#add-account-table div input[type='checkbox']").prop("checked",true);
		$('#add-account-table').form('load',{
			account:data.investorID,
			password:data.password,
			firstname:data.name,
			initmoney:data.initFunds,
			badmoney:data.afterFunds
		});
	}else{
		$("#add-account-table div input[type='checkbox']").prop("checked",false);
		$('#add-account-table').form('load',{
			account:data.investorID,
			password:data.password,
			firstname:data.name,
			initmoney:data.initFunds,
			badmoney:''
		});
	}
}
/**
 * 此方法用于对添加账户弹出框进行定位
 */
function setAddAccountAlertPosition(){
	$('.panel').css('top','100px');
	$('.panel').css('left','72px');
	$('.window-shadow').css('top','100px');
	$('.window-shadow').css('left','72px');
}
