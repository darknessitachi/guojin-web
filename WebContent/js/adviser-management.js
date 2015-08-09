var table_title = {
		"id":"投顾编号",
		"name":"投顾名称",
		"belong-group":"所属组名称",
		"allow-num":"允许管理账户数",
		"current-num":"当前管理账户数",
		"create-time":"创建日期",
		"state":"状态"
};

var groupID = 0;
//操作类型，1表示操作是来自添加按钮，2表示操作来自编辑按钮
var operateFlag = 1;
//数据条目的id
var id;
//数据条目在表格中所对应的条目编号
var num;
/**
 * 此变量用于缓存从数据库查询到的数据，方便编辑操作
 */
var listData;
/**
 * 此变量用于缓存组数据
 */
var groupData;
//此变量用于标识用户是否选择了表格中的数据条目
var clickFlag = false;

$(function(){
	//添加按钮的响应事件
	$('#adviser-add').click(function(){
		setAlertDefault();
		$('#adviser-group-alert').show();
		getListData();
		operateFlag = 1;
	});
	
	//删除按钮的响应事件
	$('#adviser-delete').click(function(){
		if(confirm("确定要删除该数据吗？"))
		{
			deleteManagementData();
		}
	});
	
	//编辑投顾信息按钮的响应函数
	$('#adviser-edit').click(function(){
		if(clickFlag){
			$('#adviser-group-alert').show();
			getListData();
			operateFlag = 2;
			setAlertData();
		}else{
			alert("对不起，你还未选择需要编辑的条目！");
		}
	});
	
	//停用投顾按钮的响应事件
	$('#adviser-stop').click(function(){
		if(clickFlag){
			if(listData[num].permission<4){
				alert("该投顾已经停用！");
			}else{
				changeManagementData(listData[num].id,listData[num].name,listData[num].fullName,null
						,groupID,listData[num].permission-4);
			}
		}else{
			alert("对不起，你还未选择需要编辑的条目！");
		}
	});
	
	//启用投顾按钮的响应事件
	$('#adviser-start').click(function(){
		if(clickFlag){
			if(listData[num].permission>=4){
				alert("该投顾已经启用！");
			}else{
				changeManagementData(listData[num].id,listData[num].name,listData[num].fullName,null
						,groupID,listData[num].permission+4);
			}
		}else{
			alert("对不起，你还未选择需要编辑的条目！");
		}
	});
	
	//添加组弹出框中的保存按钮的响应事件
	$('#adviser-management-save-button').click(function(){
		var name = $('#adviser-management-input-name').val();
		var num = $('#adviser-management-input-num').val();
		var account = $('#adviser-management-input-account').val();
		var pwd = $('#adviser-management-input-pwd').val();
		var repwd = $('#adviser-management-input-repwd').val();
		
		if(checkName('#adviser-management-input-name',name)&&checkNum('#adviser-management-input-num',num)&&
				checkName('#adviser-management-input-account',account)&&
				checkPassWord('#adviser-management-input-pwd',pwd)&&
				checkPassWord('#adviser-management-input-repwd',repwd)){
			if(pwd!=repwd){
				$('#adviser-management-input-pwd').attr("type","text");
				$('#adviser-management-input-pwd').val(input_pwd_text["pwd_equal"]);
				$('#adviser-management-input-repwd').val('');
				$('#adviser-management-input-pwd').css('color','#999');
			}else{
				//下拉选择组编号
				var selectIndex = $('#adviser-management-select')[0].selectedIndex;
				//表示是否选择添加风控设置权限
				var riskControlFlag = $('#adviser-management-riskcontrol').prop('checked');
				//表示是否选择强制平仓权限
				var positionFlag = $('#adviser-management-position').prop('checked');
				var permission = 4;
				if(riskControlFlag){
					permission = permission + 1;
				}
				if(positionFlag){
					permission = permission + 2;
				}
				if(operateFlag==1){
					addManagementData(account,name,repwd,groupID,permission);
				}else if(operateFlag==2){
					changeManagementData(id,account,name,repwd,groupID,permission);
				}
			}
		}
	});
	
	getManagementData();
//	setTable();
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
	html= html + "<th>"+ table_title["id"] +"</th>";
	html= html + "<th>"+ table_title["name"] +"</th>";
	html= html + "<th>"+ table_title["belong-group"] +"</th>";
	html= html + "<th>"+ table_title["allow-num"] +"</th>";
	html= html + "<th>"+ table_title["current-num"] +"</th>";
//	html= html + "<th>"+ table_title["create-time"] +"</th>";
	html= html + "<th>"+ table_title["state"] +"</th>";
	html = html + "</tr>";
 
	for(var i=0; i<data.length;i++)//填充数据到表格
	{
		html = html + "<tr>";
		html = html + "<td><input type=\"radio\" name=\"adviser-management-radio\" id=\""+data[i].id+"-"+i+"\"/></td>";
		html = html + "<td>"+data[i].name+"</td>";
		html = html + "<td>"+data[i].fullName+"</td>";
		html = html + "<td>"+data[i].groupName+"</td>";
		html = html + "<td>"+data[i].max+"</td>";
		html = html + "<td>"+data[i].now+"</td>";
//		html = html + "<td>2014-09-23</td>";
		if(data[i].permission<4){
			html = html + "<td>停用</td>";
		}else{
			html = html + "<td>启用</td>";
		}
		html = html + "</tr>";
	}
	$('#table-table').html(html);  //关联到表格  <tbody id = "table-table" >
	
	$("tr td input[type='radio']").click(function(){
		id = $(this).attr("id").split('-')[0];
		num = $(this).attr("id").split('-')[1];
		clickFlag = true;
	});
	
}

/**
 * 此方法用于设置下拉选择组的
 */
function setOption(data){
	var html = "";
	groupID = data[0].id;
	for (var v = 0; v < data.length; v++) {
		html = html + "<option id=\""+data[v].id+"\"  onclick=\"optionClick( this )\">"+data[v].name+"</option>";
	}
	$('#adviser-management-select').html(html);
	
}
/**
 * 点击下拉投顾组选项的响应事件
 * @param t
 */
function optionClick(t){
	groupID = $(t).attr("id");
}

/**
 * 此方法用于适应过滤条件第一选择的IE适配
 */
function simOptionClick4IE(){  
    var evt=window.event  ;  
    var selectObj=evt?evt.srcElement:null;  
    // IE Only  
    if (evt && selectObj &&  evt.offsetY && evt.button!=2  
        && (evt.offsetY > selectObj.offsetHeight || evt.offsetY<0 ) ) {  
              
            // 记录原先的选中项  
            var oldIdx = selectObj.selectedIndex;  
  
            setTimeout(function(){  
                var option=selectObj.options[selectObj.selectedIndex];  
                // 此时可以通过判断 oldIdx 是否等于 selectObj.selectedIndex  
                // 来判断用户是不是点击了同一个选项,进而做不同的处理.  
                optionClick(option);
  
            }, 60);  
    }  
} 

/**
 * 设置添加弹出框的默认显示
 */
function setAlertDefault(){
	$('#adviser-management-input-num').val('');
	$('#adviser-management-input-name').val('');
	$('#adviser-management-input-account').val('');
	$('#adviser-management-input-pwd').val('');
	$('#adviser-management-input-repwd').val('');
	$('#adviser-management-select')[0].selectedIndex = 0;
	$('#adviser-management-riskcontrol').prop('checked',false);
	$('#adviser-management-position').prop('checked',false);
	setAlertColor();
}
/**
 * 此方法用于设置弹出框文本的颜色为黑色
 */
function setAlertColor(){
	$('#adviser-management-input-num').css('color','#000');
	$('#adviser-management-input-num').css('color','#000');
	$('#adviser-management-input-account').css('color','#000');
	$('#adviser-management-input-pwd').css('color','#000');
	$('#adviser-management-input-repwd').css('color','#000');
}

/**
 * 设置添加弹出框的编辑数据显示
 */
function setAlertData(){
	setAlertColor();
	$('#adviser-management-input-num').val(listData[num].max);
	$('#adviser-management-input-name').val(listData[num].fullName);
	$('#adviser-management-input-account').val(listData[num].name);
	$('#adviser-management-input-pwd').val('');
	$('#adviser-management-input-repwd').val('');
	for (var v = 0; v < groupData.length; v++) {
		if(listData[num].groupName==groupData[v].name){
			$('#adviser-management-select')[0].selectedIndex = v;
			break;
		}
	}
	switch (listData[num].permission) {
	case 7:
		$('#adviser-management-riskcontrol').prop('checked',true);
		$('#adviser-management-position').prop('checked',true);
		break;
	case 6:
		$('#adviser-management-riskcontrol').prop('checked',false);
		$('#adviser-management-position').prop('checked',true);
		break;
	case 5:
		$('#adviser-management-riskcontrol').prop('checked',true);
		$('#adviser-management-position').prop('checked',false);
		break;
	case 4:
		$('#adviser-management-riskcontrol').prop('checked',false);
		$('#adviser-management-position').prop('checked',false);
		break;
	default:
		break;
	}
}