/**
 * 账户管理界面所需的js文件
 */
//账户管理页面条目的id
var accountId = '';
//定时器的id
var timeId;
//用户权限
var permission;
var dataList;
var treeArrayData;

$(function(){
	//获取cookie中的permission值
	permission = getCookie('permission');
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
	//添加账户按钮的响应事件
	$('#add-account-button').click(function(){
		renderAccountTable();
	});
	//编辑账户按钮的响应事件
	$('#dialog-basic-info').click(function(){
		if(accountId!=''){
			var data = getBasicInfoData(accountId);
			data.clientID = accountId;
			data.clientName = data.name;
			data.name = data.investorID;
			if(data){
				renderAccountTable(data);
			}
		}else{
			$.messager.alert("提示","请选择要编辑的账户!");
		}
	});
	//删除账户按钮的响应事件
	$('#dialog-delete-account').click(function(){
		if(accountId!=''){
			$.messager.confirm("确认","确认删除吗？",function(r){
				if(r){
					getManagementDelete();
				}
			});
		}else{
			$.messager.alert("提示","请选择要删除的账户!");
		}
	});
	$("#containAfterFunds").change(function(){
		if($(this).prop("checked")){
			$("#formAfterFunds").textbox("enable");
		}else{
			$("#formAfterFunds").textbox("disable");
		}
	})
	$("#dlg-buttons").click(function(){
		var isValid = true;
		$("#account-table").find("input.textbox-f:enabled").each(function(){
			if(!$(this).textbox("isValid")){
				isValid = false;
			}
		})
		if(!isValid){
			$.messager.alert("错误","表单填写有误");
			return;
		}
		var dataForm = $("#account-table").serializeArray();
		var data = {};
		for(var i = 0 ; i < dataForm.length; i++){
			if(dataForm[i].name == "password"){
				data[dataForm[i].name] = strEnc(dataForm[i].value,key1,key2,key3);
			}else{
				data[dataForm[i].name] = dataForm[i].value;
			}
		}
		var formUrl = "";
		if(data.clientID == undefined || data.clientID == null || data.clientID == ""){
			formUrl = basePath+"client/add"
		}else{
			formUrl = basePath+"client/change"
		}
		$.messager.progress({interval:5*1000});
		$.ajax({
			url:formUrl + "?token="+tokenVal,
	        dataType: "json",
			type:"post",
	        data:data,
	        //cache:false,
			success:function(data){
				//返回的0表示登录不成功
				if(data.msg=="0"){
					$.messager.alert("错误","保存失败，原因可能是网络断开或者输入信息有误");
				}else if(data.msg=="1"){//返回1表示成功
					//动态为标签添加属性
					$.messager.alert("提示","保存成功");
					$("#account-dlg").dialog('close');
					getManagementData();
				}else{
					$.messager.alert("提示",data.msg);
				}
				$.messager.progress('close');
			},error : function(){
				$.messager.progress('close');
				$.messager.alert("错误","保存失败，原因可能是网络断开或者输入信息有误");
			}
		});	
	});
	getManagementData();
	
});

/**
 * 向服务器发送账户管理获取数据的请求
 */
function getManagementData(){
	$.ajax({
		url:basePath+"client/getClient",
        dataType: 'json',
		type:"get",
        data:{token:tokenVal},
		//cache:false,
		success:function(response){
			if(response==0||response.list.length==0){
				setAccountManagemnetList([]);
				$.messager.show({title:'提示',
					msg:'暂时没有管理的期货帐号！',
					timeout:1000,
					style:{
						top:50
					}});
			}else{
				var data = response.list;
				dataList = data;
				setAccountManagemnetList(data);
				getManagementDataState();
				//如果没有定时器，那么则添加一个定时器，如果已经有了，那么则不需要再添加定时器
				if(timeId==null||timeId==undefined){
					timeId = setInterval(getManagementDataState,1000*10);//10秒一次 风控
					setInterval(refreshPositionsDetail,1000*10);//10秒一次刷新持仓
					setInterval(refreshMoneyDetailData,1000*10);//10秒一次刷新资金
				}
			}
		}
	});	
}

/**
 * 向服务器发送账户管理获取数据状态的请求
 */
function getManagementDataState(){
	$.ajax({
		url:basePath+"user/stat",
        dataType: 'json',
		type:"get",
        data:{token:tokenVal},
        //cache:false,
        success:function(response){
        	var data = response;
    		setAccountManagemnetListStat(data);
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
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:accountId},
		//cache:false,
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
 * 此方法用于设置账户管理界面的列表显示内容
 * @param data
 */
function setAccountManagemnetList(data){
	treeArrayData = [];
	var selectedId = "";
	for (var v = 0; v < data.length; v++) {
		var treeNodeData = {};
		treeNodeData.id = data[v].id;
		treeNodeData.org_text = data[v].name;
		treeNodeData.text = data[v].name;
		if(data[v].stat=='0'){//正常，绿色
			treeNodeData.text+="（正常）";
			treeNodeData.iconCls = 'icon-ok';
		}else if(data[v].stat=='1'){//警告，黄色
			treeNodeData.text+="（预警线告警）";
			treeNodeData.iconCls = 'icon-tip';
		}else if(data[v].stat=='2'){//平仓，红色
			treeNodeData.text+="（平仓线告警）";
			treeNodeData.iconCls = 'icon-no';
		}else{//未设置风控
			treeNodeData.iconCls = 'icon-man';
		}
		if(v==0){
			selectedId = treeNodeData.id;
		}
		treeArrayData.push(treeNodeData);
	}
	$("#accounts").tree({
		data : treeArrayData,
		onClick: function(node){
			//点击条目获取条目id的响应事件
			accountId = node.id;
			setCookie('clientId',accountId,30);
			$('#'+accountId+' input').prop("checked",true);
			renderMoneyDetailData(accountId);
			renderBasicInfoForRead(accountId);
			renderPositionsDetail(accountId);
		}
	});
	if(selectedId != ""){
		$("#accounts").tree("find",selectedId).target.click();
	}
}

/**
 * 此方法用于设置账户管理界面的列表显示内容的背景颜色
 * @param data
 */
function setAccountManagemnetListStat(data){
	if(data.length == 0){
		return;
	}
	for (var v = 0; v < data[0].length; v++) {
		for(var k = 0 ; k < treeArrayData.length ; k++){
			if(treeArrayData[k].id == data[0][v]){
				if(data[1][v]==1){//警告
					treeArrayData[k].text = treeArrayData[k].org_text + "（预警线告警）";
					treeArrayData[k].iconCls = 'icon-tip';
				}else if(data[1][v]==2){//平仓
					treeArrayData[k].text = treeArrayData[k].org_text + "（平仓线告警）";
					treeArrayData[k].iconCls = 'icon-no';
				}else if(data[1][v]==0){//正常
					treeArrayData[k].text = treeArrayData[k].org_text + "（正常）";
					treeArrayData[k].iconCls = 'icon-ok';
				}else{//没有配置风控 
					treeArrayData[k].text = treeArrayData[k].org_text + "";
					treeArrayData[k].iconCls = 'icon-man';
				}
				var node = $("#accounts").tree("find",treeArrayData[k].id);
				$('#accounts').tree('update', {target:node.target,iconCls:treeArrayData[k].iconCls,text:treeArrayData[k].text});
			}
		}
	}
//	var node = $("#accounts").tree("getSelected");
//	$("#accounts").tree("update",{data:treeArrayData});
//	TODO
//	$("#accounts").tree("select",node.target);
	
	for (var v = 0; v < data[0].length; v++) {
		if(data[1][v]==1||data[1][v]==2){
			for(var i = 0 ; i < dataList.length ; i++){
				if(data[0][v] == dataList[i].id){
					window.top.showRiskMsg(dataList[i].name,data[0][v],data[1][v]+'');
					break;
				} 
			}
			break;
		}
	}
}

function refreshMoneyDetailData(){
	var node = $("#accounts").tree("getSelected");
	if(node != null){
		renderMoneyDetailData(node.id,"client/refreshFunds")
	}
}

/**
 * 向服务器发送资金管理获取账户详细请求，获取结果
 */
function renderMoneyDetailData(id,newUrl){
	$("#cashMsg").find(".easyui-textbox").textbox("clear");
	var url = "client/getFunds";
	if(newUrl != undefined && newUrl != null && newUrl != ""){
		url = newUrl;
	}
	$.ajax({
		url:basePath + url,
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:id},
		//cache:false,
		success:function(response){
			if(response.initFunds == 0 && response.staticRight == 0){
				$.messager.show({title:'提示',
					msg:'暂时没有账户资金有效数据！',
					timeout:1000,
					style:{
						top:50
					}});
			}else{
				var data = response;
				$('#initFunds').textbox("setValue",handleDigital(data.initFunds)); 
				$('#profit').textbox("setValue",handleDigital(data.profit)); 
				$('#staticRight').textbox("setValue",handleDigital(data.staticRight)); 
				$('#dynamicRight').textbox("setValue",handleDigital(data.dynamicRight)); 
				$('#deposit').textbox("setValue",handleDigital(data.deposit)); 
				$('#expendableFunds').textbox("setValue",handleDigital(data.expendableFunds)); 
				$('#freeze').textbox("setValue",handleDigital(data.freeze)); 
				if(data.risk==0){
					$('#risk').textbox("setValue","0.00%");
				}else{
					$('#risk').textbox("setValue",handleDigital(data.risk*100));
				};
			}
		}
	});	
}
/**
 * 渲染账户基本信息
 * @param accountId
 */
function renderBasicInfoForRead(accountId){
	var data = getBasicInfoData(accountId)
	if(data){
		$('#investorID').textbox("setValue",data.investorID); 
		$('#clientName').textbox("setValue",data.name); 
		$('#initFunds').textbox("setValue",data.initFunds); 
		$('#afterFunds').textbox("setValue",data.afterFunds); 
	}
}

function refreshPositionsDetail(){
	var node = $("#accounts").tree("getSelected");
	if(node != null){
		renderPositionsDetail(node.id,"positions/refresh");
	}
}

/**
 * 向服务器发送持仓管理下表格获取数据的请求
 */
function renderPositionsDetail(id,newUrl)
{
	var url = "positions/detail";
	if(newUrl != undefined && newUrl != null && newUrl != ""){
		url = newUrl;
	}
	$.get(basePath + url + '?token='+tokenVal+"&clientID="+id,
		function(data){
		var gridData = data.list;
		$('#positionMsg').datagrid({    
		    data: gridData, 
		    columns:[[    
		        {field:'contractID',title:'合约号',width:50,
		        	formatter: function(value,rowData,index){
		        		return rowData.contractID;
		        	}
		        },    
		        {field:'contractName',title:'合约名',width:50,
		        	formatter: function(value,rowData,index){
		        		return rowData.contractName;
		        	}
		        },   
		        {field:'contractType',title:'买卖',width:50,
		        	formatter: function(value,rowData,index){
		        		if(rowData.contractType==2){
		        			return '<font color=\'red\'>买</font>';
		        		}else if(rowData.contractType==3){
		        			return '<font color=\'green\'>卖</font>';
		        		}
		        	}
		        },   
		        {field:'totalPositions',title:'总持仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.totalPositions);
		        	}
		        },   
		        {field:'lastPositions',title:'昨仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.lastPositions);
		        	}
		        },   
		        {field:'nowPositions',title:'今仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.nowPositions);
		        	}
		        },   
		        {field:'usablePositions',title:'可平量',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.usablePositions);
		        	}
		        },   
		        {field:'avePrice',title:'持仓均价',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.avePrice);
		        	}
		        },   
		        {field:'profit',title:'持仓盈亏',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.profit);
		        	}
		        },   
		        {field:'deposit',title:'占用保证金',width:50,
		        	formatter: function(value,rowData,index){
		        		return handleDigital(rowData.deposit);
		        	}
		        }    
		    ]]    
		});  
		$('#positionMsg').datagrid('reload');
	},'json');
}

/**
 * 此方法用于根据数字的不同返回不同的数据，如果数字小于0则返回0.00，如果数字不小于0则返回保留小数两位的数据
 * @param data 需要处理的数字
 * @returns 返回的数据
 */
function formatNumberMoney(data){
	if(data<0){
		return '<font color=\'green\'>' + handleDigital(data) + '</font>';
	}else if(data>0){
		return '<font color=\'red\'>' +handleDigital(data) + '</font>';
	}else{
		return "0.00";
	}
}

/**
 * 向服务器发送账户管理账户的请求
 */
function getBasicInfoData(accountId)
{
	var returnData = null;
	$.ajax({
		url:basePath+"client/getBasic",
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:accountId},
		//cache:false,
		async:false,
		success:function(response){
			if(response==0){
				$.messager.show({title:'提示',
					msg:'获取基本信息数据失败！',
					timeout:1000,
					style:{
						top:50
					}});
				return false;
			}else{
				returnData = response;
			}
		}
	});
	return returnData;
}
/**
 * 渲染提交账户信息的form
 * @param data
 */
function renderAccountTable(data){
	if(data != null && data != undefined){
		$("#account-dlg").dialog('open').dialog('setTitle','编辑账户');
		$("#account-table").form('load',data);
		$("#password").textbox("setValue",strDec(data.password,key1,key2,key3));
		if(data.afterFunds != null && data.afterFunds != "" && data.afterFunds != undefined){
			$("#containAfterFunds").prop("checked",true);
		}else{
			$("#containAfterFunds").prop("checked",false);
		}
		$("#containAfterFunds").change();
	}else{
		$("#account-dlg").dialog('open').dialog('setTitle','添加账户');
		$("#account-table").form('clear');
		$("#containAfterFunds").prop("checked",false);
		$("#containAfterFunds").change();
	}
}
