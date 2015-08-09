/**
 * 向服务器发送资金管理获取账户请求，获取结果
 */
function renderAccountList()
{
	$.ajax({
		url:basePath+"client/getClient",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal},
		//cache:false,
		success:function(response){
			if(response.list.length==0){
				$.messager.show({title:'提示',
					msg:'暂时没有账户数据，请先添加账户',
					timeout:1000,
					style:{
						top:50
					}});
			}else{
				var data = response.list;
				var treeArrayData = [];
				var selectedId = "";
				for (var v = 0; v < data.length; v++) {
					var treeNodeData = {};
					treeNodeData.id = data[v].id;
					treeNodeData.text = data[v].name;
					treeNodeData.iconCls = 'icon-man';
					if(v==0){
						selectedId = treeNodeData.id;
					}
					treeArrayData.push(treeNodeData);
				}
				$("#accounts").tree({
					data : treeArrayData,
					onClick: function(node){
						renderMoneyDetailData(node.id);
					}
				});
				$("#accounts").tree("find",selectedId).target.click();
			}
		}
	});	
}

/**
 * 向服务器发送资金管理获取账户详细请求，获取结果
 */
function renderMoneyDetailData(id)
{
	$(".easyui-textbox").textbox("clear");
	$.ajax({
		url:basePath+"client/getFunds",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{token:tokenVal,clientID:id},
		//cache:false,
		async:false,
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
