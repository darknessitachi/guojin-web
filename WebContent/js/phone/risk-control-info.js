/**
 * 风控消息界面所需的js文件
 */
//此变量用于标识用户点击的是警告按钮还是平仓按钮，1表示警告按钮，2表示平仓按钮
var riskControlInfoType = 1;
var token;
var accountId;

$(function(){
	//获取cookie中的token值
	token = getCookie('token');
	accountId = getCookie('clientId');
	getRiskControlInfo();
	//风控消息中警告和强平按钮的响应事件
	$('#risk-control-info-tab').tabs({
		border:true,
		onSelect:function(title){
			if(title=='警告'){
				riskControlInfoType = 1;
				getRiskControlInfo();
			}else if(title=='强平'){
				riskControlInfoType = 2;
				getRiskControlInfo();
			}
		}
	});
	//账户列表按钮的点击响应事件
	$(".account-list-button").click(function(){
		window.location.href="account-manage.html";
	});
});

/**
 * 向服务器发送风控消息获取警告或者平仓数据的请求
 */
function getRiskControlInfo()
{
	$.ajax({
		url:basePath+"message/get",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:token,clientID:accountId,type:riskControlInfoType},
		cache:false,
		async:false,
		success:function(response){
			if(response.list.length==0){
				$.messager.alert("提示","暂时没有数据!");
				$('#risk-control-info-list01').empty();
				$('#risk-control-info-list02').empty();
			}else{
				var data = response.list;
				showRiskControlInfo(data);
			}
		}
	});	
}

/**
 * 此方法用于显示风控消息的列表
 * @param data 从数据库获得的数据
 * @returns
 */
function showRiskControlInfo(data){
	var html = "";
	html = html + "<ul style=\"padding-left:0px;\">";
	for(var v = 0;v<data.length;v++){
		if(v==0){
			html = html + "<li style=\"	border-top: 1px solid #CCC;border-bottom: 1px solid #CCC;\">";
		}else{
			html = html + "<li style=\"border-bottom: 1px solid #CCC;\">";
		}
		html = html + "<div style=\"color:#000;\">";
		html = html + "于"+formatTime(data[v].time,'yyyy-MM-dd HH:mm:ss') + "</div>";
		html = html + "" + data[v].content + "";
		
		html = html + "<div style=\"height:10px;\"></div>";
		html = html + "</li>";
	}
	html = html + "</ul>";
	
	if(riskControlInfoType==1){
		$('#risk-control-info-list01').empty();
		$(html).appendTo('#risk-control-info-list01').trigger('create');
	}else if(riskControlInfoType==2){
		$('#risk-control-info-list02').empty();
		$(html).appendTo('#risk-control-info-list02').trigger('create');
	}
}

/**
 * 设置风控消息页面默认的显示
 */
function setRiskControlShow(){
	 
	 $('#risk-control-warning-button01').addClass("ui-tabs-active ui-state-active");
	 $('#risk-control-warning-button01').attr("aria-selected","true");
	 $('#risk-control-warning-button02').removeClass("ui-tabs-active ui-state-active");
	 $('#risk-control-warning-button02').removeAttr("aria-selected");
	 
	 $('#one').show();
	 $('#one').attr("aria-hidden","false");
	 $('#one').attr("aria-expanded","true");
	 $('#two').hide();
	 $('#two').attr("aria-hidden","true");
	 $('#two').attr("aria-expanded","false");
	
}

