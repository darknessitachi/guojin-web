/**
 * 该js文件是用于投顾组管理页面
 */
var table_title = {
		"id":"组编号",
		"name":"组名称",
		"belong-group":"所属组名称",
		"allow-num":"允许投顾数",
		"current-num":"当前投顾数",
		"create-time":"创建日期"
};

var id;
//操作类型，1表示操作是来自添加按钮，2表示操作来自编辑按钮
var operateFlag = 1;
/**
 * 此变量用于缓存从数据库查询到的数据，方便编辑操作
 */
var listData;

$(function(){
	//添加按钮的响应事件
	$('#adviser-list-add').click(function(){
		setAlertDefault();
		$('#adviser-group-alert').show();
		operateFlag = 1;
	});
	
	//编辑按钮响应事件
	$('#adviser-list-edit').click(function(){
		if(showEditPage()){
			$('#adviser-group-alert').show();
			operateFlag = 2;
		}else{
			alert("对不起，你还未选择需要编辑的条目！");
		}
	});
	//删除按钮响应事件
	$('#adviser-list-delete').click(function(){
		if(confirm("确定要删除该数据吗？"))
		{
			deleteListData();
		}
	});
	
	//添加组弹出框中的保存按钮的响应事件
	$('#adviser-list-save-button').click(function(){
		var name = $('#adviser-list-input-name').val();
		var num = $('#adviser-list-input-num').val();
		
		if(checkName('#adviser-list-input-name',name)&&checkNum('#adviser-list-input-num',num)){
			//将输入框置为默认的状态
			setAlertDefault();
			
			if(operateFlag==1){
				addListData(name,num);
			}else if(operateFlag==2){
				changeListData(id,name,num);
			}
			$('#adviser-group-alert').hide();
		}
	});
	
	getListData();
});


/**
 * 设置页面表格的内容
 */
function setTable(data)
{
//	alert("设置表格数据");
//	var state;//每条数据的状态
	var html = "<tr class=\"table-th-black\">"+
	"<th></th>";
	html= html + "<th>"+ table_title["name"] +"</th>";
	html= html + "<th>"+ table_title["allow-num"] +"</th>";
	html= html + "<th>"+ table_title["current-num"] +"</th>";
	html= html + "<th>"+ table_title["create-time"] +"</th>";
	html = html + "</tr>";
 
	for(var i=0; i<data.length;i++)//填充数据到表格
	{
		html = html + "<tr>";
		html = html + "<td><input type=\"radio\" name=\"adviser-list-radio\" id=\""+data[i].id+"\"/></td>";
		html = html + "<td>"+data[i].name+"</td>";
		html = html + "<td>"+data[i].max+"</td>";
		html = html + "<td>"+data[i].now+"</td>";
		html = html + "<td>"+data[i].createTime+"</td>";
		html = html + "</tr>";
	}
	$('#table-table').html(html);  //关联到表格  <tbody id = "table-table" >
	
	$('tr td input').click(function(){
		id = $(this).attr("id");
	});
	
}

/**
 * 编辑弹出框设置用户选择的条目的值
 * @returns {Boolean} true表示获取到用户的选择，false表示用户还未进行选择
 */
function showEditPage(){
	var flag = false;
	for (var v = 0; v < listData.length; v++) {
		if(listData[v].id==id){
			$('#adviser-list-input-name').val(listData[v].name);
			$('#adviser-list-input-num').val(listData[v].max);
			$('#adviser-list-input-num').css('color','#000');
			$('#adviser-list-input-name').css('color','#000');
			flag = true;
			break;
		}
	}
	return flag;
}
/**
 * 将输入框置为默认的状态
 */
function setAlertDefault(){
	$('#adviser-list-input-num').val('');
	$('#adviser-list-input-name').val('');
	$('#adviser-list-input-num').css('color','#000');
	$('#adviser-list-input-name').css('color','#000');
}

