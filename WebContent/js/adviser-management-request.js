/**
 * 此文件提供投顾管理页面js请求数据的部分
 */

/**
 * 向服务器发送投顾管理获取账户列表发送请求，获取结果
 */
function getManagementData()
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/manager/get",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{adminName:adminName,adminPassword:adminPassword},
		cache:false,
		async:false,
		success:function(response){
			if(response.length==0){
				alert("暂时没有账户数据，你可以选择添加账户");
			}else{
				listData = response;
				setTable(listData);
			}
		}
	});	
}

/**
 * 向服务器发送投顾组管理获取投顾组列表请求，获取结果
 */
function getListData()
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/group/get",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{adminName:adminName,adminPassword:adminPassword},
		cache:false,
		async:false,
		success:function(response){
			if(response.length==0){
				
			}else{
				groupData = response;
				setOption(groupData);
			}
		}
	});	
}

/**
 * 向服务器发送投顾管理添加账户请求，并刷新表数据
 */
function addManagementData(name,fullName,password,groupID,permission)
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/manager/add",
        dataType: 'json',
		type:"post",
        data:{adminName:adminName,adminPassword:adminPassword,name:name,fullName:fullName,
        	password:password,groupID:groupID,permission:permission},
		success:function(response){
			if(response==1){
				alert("恭喜你，添加账户成功！");
				//将输入框置为默认的状态
				setAlertDefault();
				$('#adviser-group-alert').hide();
				getManagementData();
			}else{
				alert("对不起，添加账户失败！");
			}
		}
	});	
}

/**
 * 向服务器发送投顾管理添加账户请求，并刷新表数据
 */
function deleteManagementData()
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/manager/delete",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{adminName:adminName,adminPassword:adminPassword,id:id},
		cache:false,
		async:false,
		success:function(response){
			if(response==1){
				getManagementData();
			}else{
				alert("对不起，删除账户失败！");
			}
		}
	});	
}

/**
 * 向服务器发送投顾管理添加账户请求，并刷新表数据
 */
function changeManagementData(id,name,fullName,password,groupID,permission)
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/manager/change",
        dataType: 'json',
		type:"post",
        data:{adminName:adminName,adminPassword:adminPassword,id:id,name:name,fullName:fullName,
        	password:password,groupID:groupID,permission:permission},
		success:function(response){
			if(response==1){
				alert("恭喜你，修改账户成功！");
				getManagementData();
				$('#adviser-group-alert').hide();
			}else{
				alert("对不起，修改账户失败！");
			}
		}
	});	
}

