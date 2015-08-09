/**
 * 向服务器发送持仓管理上表格获取数据的请求
 */
function getPositionsData()
{
	$('#positions-management-table').datagrid({    
	    url:basePath+'positions/summary?token='+tokenVal, 
	    columns:[[ 
	        {field:'investorID',title:'账户',width:50},    
	        {field:'number',title:'合约数',width:50,
	        	formatter: function(value,rowData,index){
	        		if(rowData.number<0){
	        			return 0;
	        		}else{
	        			return rowData.number;
	        		}
	        	}
	        },    
	        {field:'money',title:'持仓资金',width:50,
	        	formatter: function(value,rowData,index){
	        		return formatNumberMoney(rowData.money);
	        	}
	        }    
	    ]],
	    onSelect: function(rowIndex, rowData){
	    	if(typeof(rowData)!='undefined'){
	    		getPositionsDetailData(rowData.clientID);
	    	}
		},
	    onLoadSuccess : function(){
	    	$('#positions-management-table').datagrid("selectRow",0);
	    }
	});  
	$('#positions-management-table').datagrid('reload');
	
}

/**
 * 向服务器发送持仓管理下表格获取数据的请求
 */
function getPositionsDetailData(id)
{
	$.get(basePath+'positions/detail?token='+tokenVal+"&clientID="+id,
		function(data){
		var gridData = data.list;
		$('#positions-management-table01').datagrid({    
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
		        			return '买';
		        		}else if(rowData.contractType==3){
		        			return '卖';
		        		}
		        	}
		        },   
		        {field:'totalPositions',title:'总持仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.totalPositions);
		        	}
		        },   
		        {field:'lastPositions',title:'昨仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.lastPositions);
		        	}
		        },   
		        {field:'nowPositions',title:'今仓',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.nowPositions);
		        	}
		        },   
		        {field:'usablePositions',title:'可平量',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.usablePositions);
		        	}
		        },   
		        {field:'avePrice',title:'持仓均价',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.avePrice);
		        	}
		        },   
		        {field:'profit',title:'持仓盈亏',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.profit);
		        	}
		        },   
		        {field:'deposit',title:'占用保证金',width:50,
		        	formatter: function(value,rowData,index){
		        		return formatNumberMoney(rowData.deposit);
		        	}
		        }    
		    ]]    
		});  
		$('#positions-management-table01').datagrid('reload');
	},'json');
}

/**
 * 此方法用于根据数字的不同返回不同的数据，如果数字小于0则返回0.00，如果数字不小于0则返回保留小数两位的数据
 * @param data 需要处理的数字
 * @returns 返回的数据
 */
function formatNumberMoney(data){
	if(data<0){
		return '0.00';
	}else{
		return handleDigital(data);
	}
}

