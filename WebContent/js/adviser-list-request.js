/**
 * 此文件用于投顾组管理页面的js代码请求数据的部分
 */

/**
 * 向服务器发送投顾组管理获取投顾组列表请求，获取结果
 */
function getListData()
{
//	alert(token);
	$.ajax({
		url:basePath+"admin/group/get",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
		dataType : "json",
//	    jsonp : "callback",
		type:"get",
        data:{adminName:adminName,adminPassword:adminPassword},
		//cache:false,
		//async:false,
		success:function(response){
			if(response.length==0){
				alert("暂时没有投顾组数据，你可以选择添加投顾组");
			}else{
				listData = response;
				setTable(listData);
			}
		}
	});	
}

/**
 * 向服务器发送投顾组管理添加投顾组请求
 */
function addListData(name,max)
{
//	alert(token);
	var data = {adminName:adminName,adminPassword:adminPassword,name:name,max:max};
	$.ajax({
		url:"admin/group/add",
        dataType: 'json',
		type:"post",
        data:data,
		success:function(response){
			if(response==1){
//				alert("恭喜你，添加投顾组成功！");
				getListData();
			}else{
				alert("对不起，添加投顾组失败！");
			}
		}
	});	
}

/**
 * 向服务器发送投顾组管理删除投顾组请求
 */
function deleteListData()
{
//	alert(token);
	$.ajax({
		url:"admin/group/delete",
		contentType:"application/x-www-form-urlencoded; charset=utf-8", 
        dataType: 'json',
		type:"get",
        data:{adminName:adminName,adminPassword:adminPassword,id:id},
		cache:false,
		async:false,
		success:function(response){
			if(response==1){
//				alert("恭喜你，删除投顾组成功！");
				getListData();
			}else{
				alert("对不起，删除投顾组失败！");
			}
		}
	});	
}

/**
 * 向服务器发送投顾组管理修改投顾组请求
 */
function changeListData(id,name,max)
{
	$.ajax({
		url:"admin/group/change",
        dataType: 'json',
		type:"post",
        data:{adminName:adminName,adminPassword:adminPassword,id:id,name:name,max:max},
		success:function(response){
			if(response==1){
//				alert("恭喜你，修改投顾组成功！");
				getListData();
			}else{
				alert("对不起，修改投顾组失败！");
			}
		}
	});	
}