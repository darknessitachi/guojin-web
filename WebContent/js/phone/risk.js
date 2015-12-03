//全局textbox配置
$.extend($.fn.textbox.defaults,{
	missingMessage : "此输入框不能为空",
	required: true,
    width : 80
});
$.extend($.fn.timespinner.defaults,{
	missingMessage : "此输入框不能为空"
});

var risk_config = {
	warn_indicator : '<div><input name="warnCheck#" type="checkbox"/><b>预警线</b><input disabled="disabled" name="warnRadio#" type="radio" value="1"checked="checked"/>比例&nbsp;<input disabled="disabled" name="warnRadio#" type="radio" value="2"/>金额&nbsp;&nbsp;<input id="warnVal#"/>&nbsp;<span id="warnSuffix#">%</span></div>',
	close_indicator : '<div><input id="closeCheck#" name="closeCheck#" type="checkbox" /><b>平仓线</b><input id="closeRadioFst#" disabled="disabled" name="closeRadio#" type="radio" value="1"checked="checked" />比例&nbsp;<input id="closeRadioSec#" disabled="disabled" name="closeRadio#" type="radio" value="2"/>金额&nbsp;&nbsp;<input id="closeVal#"/>&nbsp;<span id="closeSuffix#">%</span></div>',
	close_type : '<div><b>平仓方式</b><input disabled="disabled" id="closeTypeRadio1#" name="closeTypeRadio#" type="radio" value="1" />全平&nbsp;<input disabled="disabled" id="closeTypeRadio2#" name="closeTypeRadio#" type="radio" value="2" checked="checked"/>百分比&nbsp;&nbsp;<input id="closeTypeVal#"/>&nbsp;<span id="closeTypeSuffix#">%</span></div>',
	simple_warn_indi : '<div><input name="warnCheck#" type="checkbox"/><b>预警线</b>&nbsp;&nbsp;&nbsp;&nbsp;<input id="warnVal#"/>&nbsp;<span id="warnSuffix#">%</span></div>',
	simple_close_indi : '<div><input id="closeCheck#" name="closeCheck#" type="checkbox" /><b>平仓线</b>&nbsp;&nbsp;&nbsp;&nbsp;<input id="closeVal#"/>&nbsp;<span id="closeSuffix#">%</span></div>',
	variety_input : '<div><b>品种或合约号</b>&nbsp;&nbsp;<input id="varietyVal#"/></div>',
	time_bucket : '<div><b>时间段</b>&nbsp;<input id="startTimeLimitVal#" name="startTimeLimitVal#"/>&nbsp;—&nbsp;<input id="endTimeLimitVal#" name="endTimeLimitVal#"/></div>',
	time_bucket_limit : '<div><input id="timeBucketCheck1#" type="checkbox" /><b>时间限制1</b><input id="startTimeBucketVal1#" />&nbsp;—&nbsp;<input id="endTimeBucketVal1#" />&nbsp;&nbsp;&nbsp;&nbsp;<input disabled="disabled" id="timeBucketRadio1#" name="timeBucketRadio1#" type="radio" value="1" checked="checked" />警告&nbsp;<input disabled="disabled" id="timeBucketRadio1close#" name="timeBucketRadio1#" type="radio" value="2" />平仓&nbsp;&nbsp; </div> <div><input id="timeBucketCheck2#" type="checkbox" /><b>时间限制2</b><input id="startTimeBucketVal2#" />&nbsp;—&nbsp;<input id="endTimeBucketVal2#" />&nbsp;&nbsp;&nbsp;&nbsp;<input disabled="disabled" id="timeBucketRadio2#" name="timeBucketRadio2#" type="radio" value="1" checked="checked" />警告&nbsp;<input disabled="disabled" id="timeBucketRadio2close#" name="timeBucketRadio2#" type="radio" value="2" />平仓&nbsp;&nbsp; </div> <div><input id="timeBucketCheck3#" type="checkbox" /><b>时间限制3</b><input id="startTimeBucketVal3#" />&nbsp;—&nbsp;<input id="endTimeBucketVal3#" />&nbsp;&nbsp;&nbsp;&nbsp;<input disabled="disabled" id="timeBucketRadio3#" name="timeBucketRadio3#" type="radio" value="1" checked="checked" />警告&nbsp;<input disabled="disabled" id="timeBucketRadio3close#" name="timeBucketRadio3#" type="radio" value="2" />平仓&nbsp;&nbsp; </div>',
	//风控显示内容配置
	risk_show_html_config : function(type){
		switch (type) {
		case "1":
			return [risk_config.warn_indicator,risk_config.close_indicator,risk_config.close_type];
		case "2" : 
			return [risk_config.time_bucket,risk_config.warn_indicator,risk_config.close_indicator,risk_config.close_type];
		case "3" : 
			return [risk_config.time_bucket_limit];
		case "4" : 
			return [risk_config.variety_input,risk_config.warn_indicator,risk_config.close_indicator,risk_config.close_type];
		case "5" : 
			return [risk_config.simple_warn_indi,risk_config.simple_close_indi,risk_config.close_type];
		case "6" : 
			return [risk_config.simple_warn_indi,risk_config.simple_close_indi,risk_config.close_type];
		}
	},
	getSelector : function(idx){
		var html = '<div>风控方式：' + 
				'<select id="selector_' + idx + '" class="risk_selector">' + 
				'<option value="1">权益监控</option>' + 
				'<option value="2">自定义时间段权益监控</option>' + 
				'<option value="3">自定义时间段限制开仓监控</option>' + 
				'<option value="4">品种及合约资金监控</option>' + 
				'<option value="5">日内资金回撤监控</option>' + 
				'<option value="6">劣后资金监控</option>' + 
				'</select>' + 
				'</div>';
		return $(html);
	},
	//警告div的绑定
	warningDivBanding : function(jqDivDom){
		//validatebox绑定
		jqDivDom.find("input[id*='warnVal']").textbox({
//			disabled: true,
		    validType: ['percent[1,5]'],
		    prompt:'百分比'
		});
		jqDivDom.find("input[id*='warnVal']").textbox("disable");
		//checkbox改变
		jqDivDom.find("input[name*='warnCheck']").change(function(){
			if($(this).prop("checked")){
				$(this).nextAll().prop("disabled",false);
				$(this).parent().find("input[id*='warnVal']").textbox("enable");
			}else{
				$(this).nextAll().prop("disabled",true);
				$(this).parent().find("input[id*='warnVal']").textbox("disable");
			}
		});
		//radio改变
		jqDivDom.find("input[name*='warnRadio']").change(function(){
			if($(this).prop("checked")){
				if($(this).val() == "1"){//比例
					$(this).nextAll("input[id*='warnVal']").textbox({
						required:true,
					    prompt:'百分比',
					    validType: ['percent[1,5]']
					});
					$(this).nextAll("span[id*='Suffix']").text("%");
				}else if($(this).val() == "2"){//金额
					$(this).nextAll("input[id*='warnVal']").textbox({
						required:true,
					    prompt:'金额',
					    validType: ['integer[1,10]']
					});
					$(this).nextAll("span[id*='Suffix']").text("元");
				}
			}
		});
	},
	//平仓div的绑定
	closeDivBanding : function(jqDivDom){
		//validatebox绑定
		jqDivDom.find("input[id*='closeVal']").textbox({
		    validType: ['percent[1,5]'],
		    prompt:'百分比'
		});
		jqDivDom.find("input[id*='closeVal']").textbox("disable");
		jqDivDom.find("input[id*='closeTypeVal']").textbox({
			validType: ['percent[1,5]'],
		    prompt:'百分比'
		});
		jqDivDom.find("input[id*='closeTypeVal']").textbox("disable");
		//checkbox改变
		jqDivDom.find("input[name*='closeCheck']").change(function(){
			if($(this).prop("checked")){
				$(this).nextAll().prop("disabled",false);
				$(this).parent().parent().find("input[id*='closeType']").prop("disabled",false);
				$(this).parent().parent().find("input[id*='closeVal']").textbox("enable");
				$(this).parent().parent().find("input[id*='closeTypeVal']").textbox("enable");
			}else{
				$(this).nextAll().prop("disabled",true);
				$(this).parent().parent().find("input[id*='closeType']").prop("disabled",true);
				$(this).parent().parent().find("input[id*='closeVal']").textbox("disable");
				$(this).parent().parent().find("input[id*='closeTypeVal']").textbox("disable");
			}
		});
		//radio改变
		jqDivDom.find("input[name*='closeRadio']").change(function(){
			if($(this).prop("checked")){
				if($(this).val() == "1"){//比例
					$(this).nextAll("input[id*='closeVal']").textbox({
						required:true,
					    prompt:'百分比',
					    validType: ['percent[1,5]']
					});
					$(this).nextAll("span[id*='Suffix']").text("%");
				}else if($(this).val() == "2"){//金额
					$(this).nextAll("input[id*='closeVal']").textbox({
						required:true,
					    prompt:'金额',
					    validType: ['integer[1,10]']
					});
					$(this).nextAll("span[id*='Suffix']").text("元");
				}
			}
		});
		jqDivDom.find("input[name*='closeTypeRadio']").change(function(){
			if($(this).prop("checked")){
				if($(this).val() == "2"){//百分比平仓
					$(this).nextAll("span[id*='Suffix']").show();
					$(this).nextAll("[id*='closeTypeVal']").textbox("enable");
					$(this).nextAll("input[id*='closeTypeVal']").textbox({
						required:true,
					    prompt:'百分比',
					    validType: ['percent[1,5]']
					});
				}else if($(this).val() == "1"){//全平
					$(this).nextAll("span[id*='Suffix']").hide();
					$(this).nextAll("[id*='closeTypeVal']").textbox("disable");
					$(this).nextAll("input[id*='closeTypeVal']").textbox("reset");
					$(this).nextAll("input[id*='closeTypeVal']").textbox({
					    prompt:'不填'
					});
				}
			}
		});
	},
	//时间段div的绑定
	timeLimitDivBanding : function(jqDivDom){
		jqDivDom.find("input[name*='TimeLimit']").timespinner({
		    required: true,
		    width : 80
		});
	},
	//时间区间限制div的绑定
	timeBucketDivBanding : function(jqDivDom){
		jqDivDom.find("input[id*='timeBucketCheck']").change(function(){
			if($(this).prop("checked")){
				$(this).nextAll().prop("disabled",false);
				$(this).parent().find("input[id*='TimeBucket']").timespinner("enable");
			}else{
				$(this).nextAll().prop("disabled",true);
				$(this).parent().find("input[id*='TimeBucket']").timespinner("disable");
			}
		});
		jqDivDom.find("input[id*='TimeBucket']").timespinner({
		    required: true,
//		    disabled : true,
		    width : 80
		});
		jqDivDom.find("input[id*='TimeBucket']").timespinner("disable");
	},
	//品种div的绑定
	varietyDivBanding : function(jqDivDom){
		jqDivDom.find("input[id*='variety']").textbox({
		    required: true,
		    validType: ['variety[1,6]']
		});
	}
};

var risk_main = {
	//风控指标队列
	indicators : [],
	//索引
	idx : 1,
	//添加风控指标
	addRiskIndicator : function(regex,type,icon){
		$('#risk_accordion').accordion('add',{
			id : "risk_panel_" + risk_main.idx,
			title:'风控指标'+risk_main.idx,
			iconCls : (icon == undefined ? 'icon-pencil' : icon),
			content:'<div id="risk_selector_' + risk_main.idx + '" class="risk_selector"></div><div id="risk_setting_panel_' + risk_main.idx + '" class="risk_setting_panel"></div>'
		});
		var divDomObj = $("#risk_selector_"+ risk_main.idx).parent();
		var indicator = new RiskIndicatorDiv(divDomObj,risk_main.idx,regex,(type == undefined || type == null ? "1" : type));
		risk_main.indicators.push(indicator);
		indicator.init();
		risk_main.idx++;
	},
	//删除风控指标
	removeRiskIndicator : function(){
		var pp = $('#risk_accordion').accordion('getSelected');
		if (pp){
			var index = $('#risk_accordion').accordion('getPanelIndex',pp);
			$('#risk_accordion').accordion('remove',index);
			risk_main.indicators.splice(index, 1);
		}
	},
	//根据regex生成风控指标
	initByRegexes : function(regexes,types){
		if(regexes!= undefined && regexes != null && regexes.length > 0){
			for(var i = 0 ; i < regexes.length; i++){
				risk_main.addRiskIndicator(regexes[i],types[i],'icon-ok');
			}
		}
	},
	//校验 并 根据html转化regex
	beforeSaveAction : function(){
		var valid = true;
		if(risk_main.indicators.length == 0){
//			$.messager.alert("提示","不能提交空表单");
			return true;
		}
		//验证提交权限
		//---------------------
		//close
		var closeAble = Math.floor((permission-4*Math.floor(permission/4))/2);
		//risk
		var riskSaveAble = Math.floor(permission-4*Math.floor(permission/4)-2*Math.floor((permission-4*Math.floor(permission/4))/2));
		if(riskSaveAble == "1"){
			if(closeAble == "0"){
				if($("input:enabled:checked[id*='close']").size() > 0){
					$.messager.alert("提示","您不具有设置平仓的权限，请去掉平仓的相关设置,再进行保存！或要求管理员为您开通此权限！");
					return false;
				}
			}
		}else{
			$.messager.alert("提示","您不具有设置风控的权限，无法提交成功！如需保存，请要求管理员为您开通此权限！");
			return false;
		}
		//---------------------
		for(var i = 0 ; i < risk_main.indicators.length ; i++){
			if(risk_main.indicators[i].coreObj.validate()){
				risk_main.indicators[i].coreObj.parse();
			}else{
				valid = false;
			}
		}
		return valid;
	},
	saveData : function(){
		if(risk_main.beforeSaveAction()){
			var criteria = "";
			var type ="";
			for(var i = 0 ; i < risk_main.indicators.length; i++){
				criteria += risk_main.indicators[i].coreObj.regex;
				type += risk_main.indicators[i].type;
				if(i  < risk_main.indicators.length -1){
					criteria += "|";
					type += "|";
				}
			}
//			alert(criteria);
//			alert(type);
			$.ajax({
				url:basePath+"configure/change",
		        dataType: 'json',
				type:"get",
				timeout:15000,//15秒
		        data:{token:tokenVal,clientID:clientIDVal,type:type,criteria:criteria},
				cache:false,
				success:function(response){
					if(response==0){
						$.messager.alert("提示","设置失败!");
					}else{
						$.messager.show({
							title:'提示',
							msg:'设置成功!',
							timeout:1000,
							style:{
								top:50
							}
						});
					}
				},
				error:function(){
					$.messager.alert("提示","数据提交错误，请检查网络 或 联系管理员");
				},
				beforeSend:function(){
					$.messager.progress();
		        },
		        complete: function() {
		        	$.messager.progress('close');
		        }
			});
		}else{
			$.messager.alert("提示","表单有误");
		}
	},
	collapsePanel : function(idx){
		$("#risk_panel_" + idx).panel("collapse");
		//$('#risk_accordion').accordion('getPanel',idx-1).panel("collapse");
	},
	expandPanel : function(idx){
		$("#risk_panel_" + idx).panel("expand");
		//$('#risk_accordion').accordion('getPanel',idx-1).panel("expand");
	},
	errorMarkPanel : function(idx){
		//$('#risk_accordion').accordion('getPanel',idx-1).parent().find(".panel-icon").removeClass("icon-ok").addClass("icon-no");
		$("#risk_panel_" + idx).parent().find(".panel-icon").removeClass("icon-ok").addClass("icon-no");
	},
	correctMarkPanel : function(idx){
		//$('#risk_accordion').accordion('getPanel',idx-1).parent().find(".panel-icon").removeClass("icon-no").addClass("icon-ok");
		$("#risk_panel_" + idx).parent().find(".panel-icon").removeClass("icon-no").addClass("icon-ok");
	}
};

//指标对象---反映单个指标 ---【selector + indicators】
var RiskIndicatorDiv = function(divDomObj,idx,regex,type){
	//self
	var that = this;
	//regex
	this.regex = regex;
	//type
	this.type = type;
	//div的dom
	this.divDomObj = divDomObj;
	//选择下拉对象
	this.jqueryTypeSelector;
	//索引
	this.idx = idx;
	//初始化
	this.init = function(){
		//核心风控指标对象
		that.genCoreObject(that.type);
		//获取select对象 并绑定事件
		that.jqueryTypeSelector = risk_config.getSelector(that.idx);
		//渲染selector
		that.divDomObj.find(".risk_selector").empty().append(that.jqueryTypeSelector);
		$("#selector_" + that.idx).val(that.type);
		that.jqueryTypeSelector.change(that.changeRiskType);
		//渲染下面的风控指标详情
		that.coreObj.render();
		that.jqueryTypeSelector.change();
	};
	//改变风控类型
	this.changeRiskType = function(){
		var type = that.jqueryTypeSelector.find("option:selected").val();
		var text = that.jqueryTypeSelector.find("option:selected").text();
		var panelOrgiTitle = $("#risk_panel_" + idx).panel("options").title;
		var intervalStr = "  ";
		if(panelOrgiTitle.indexOf(intervalStr) > -1){
			panelOrgiTitle = panelOrgiTitle.substring(0,panelOrgiTitle.indexOf(intervalStr));
		}
		$("#risk_panel_" + idx).panel("setTitle",panelOrgiTitle+intervalStr+"【"+text+"】");
		that.genCoreObject(type);
		that.type = type;
		//渲染下面的风控指标详情
		that.coreObj.render();
	};
	//根据类型来声场风控指标对象
	this.genCoreObject = function(type){
		switch (type+"") {
		case "1" : 
			that.coreObj = new AllDayRisk(that.divDomObj,that.idx, that.regex);
			break;
		case "2" :
			that.coreObj = new AllDayTimeLimitRisk(that.divDomObj,that.idx, that.regex);
			break;
		case "3" :
			that.coreObj = new TimeBucketLimitRisk(that.divDomObj,that.idx, that.regex);
			break;
		case "4" :
			that.coreObj = new VarietyRisk(that.divDomObj,that.idx, that.regex);
			break;
		case "5" :
			that.coreObj = new DrawdownRisk(that.divDomObj,that.idx, that.regex);
			break;
		case "6" :
			that.coreObj = new AfterPoorRisk(that.divDomObj,that.idx, that.regex);
			break;
		};
	};
};

//基类指标---其它指标集成它
function BaseRiskClass(jqueryDocObj,idx,regex){
	//风控文档对象
	this.risk_setting_panel = jqueryDocObj.find("div.risk_setting_panel");
	//索引
	this.idx = idx;
	//表达式
	this.regex = regex;
	//对象本身
	var that = this;
	//初始化
	//根据html获得jquery对象
	this.showConfig;
	//渲染
	this.render = function(){
		that.risk_setting_panel.empty();
		for(var i = 0 ; that.showConfig != null && i < that.showConfig.length ; i++){
			var html = that.showConfig[i].replaceAll("#", that.idx);
			//渲染jquery对象
			that.risk_setting_panel.append(html);
		}
		//生成指标说明
		that.risk_setting_panel.append("<div style=\"height:20px;width:30px;background:url(\'../js/jquery-easyui-1.4/themes/icons/help.png\') no-repeat center;\"></div>" +
				"<div style='width:100%;background:#FFFFFF;margin:2px;padding:3px;height:auto!important;min-height:40px;' align='left'>" +
				"<div id=\"msg" + that.idx + "\" style=\"width:1000px;line-height:25px;overflow:hidden;word-break:break-all;\"></div>" +
				"</div>");
		$("#msg" + that.idx).html(that.help_info);
		//绑定事件
		that.eventBanding();
		//如果表达式不为空 根据regex改变显示
		if(that.regex != undefined && that.regex != null){
			that.generate();
		}
	};
	//根据regex改变html中的值
	this.generate = function(){
		//overwrite in SubClass
	};
	//默认生成  让子类去调用
	this.defualtGenerate = function(){
		var valArray = that.regex.split("*");
		if(valArray[0] != "0"){
			that.risk_setting_panel.find("input[name*='warnCheck']").click();
			that.risk_setting_panel.find("input[name*='warnRadio'][value='" + valArray[0] + "']").click();
			that.risk_setting_panel.find("input[name*='warnRadio'][value='" + valArray[0] + "']").change();
			that.risk_setting_panel.find("input[id*='warnVal']").textbox("setValue",valArray[1]);
		}
		if(valArray[2] != "0"){
			that.risk_setting_panel.find("input[name*='closeCheck']").click();
			that.risk_setting_panel.find("input[name*='closeRadio'][value='" + valArray[2] + "']").click();
			that.risk_setting_panel.find("input[name*='closeRadio'][value='" + valArray[2] + "']").change();
			that.risk_setting_panel.find("input[id*='closeVal']").textbox("setValue",valArray[3]);
		}
		if(valArray[4] != "0"){
			that.risk_setting_panel.find("input[name*='closeTypeRadio'][value='" + valArray[4] + "']").click();
			that.risk_setting_panel.find("input[name*='closeTypeRadio'][value='" + valArray[4] + "']").change();
			if(valArray[4] == "2"){
				that.risk_setting_panel.find("input[id*='closeTypeVal']").textbox("setValue",valArray[5]);
			}
		}
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		//overwrite in SubClass
	};
	//默认转化 让子类去调用
	this.defualtParse = function(){
		var valArray = [];
		if(that.risk_setting_panel.find("input[name*='warnCheck']").prop("checked")){
			var warnRadio = that.risk_setting_panel.find("input[name*='warnRadio']:checked").val();
			valArray[0] = (warnRadio == undefined ? "1" : warnRadio);
			valArray[1] = that.risk_setting_panel.find("input[id*='warnVal']").val();
		}else{
			valArray[0] = 0;
			valArray[1] = 0;
		}
		if(that.risk_setting_panel.find("input[name*='closeCheck']").prop("checked")){
			var closeRadio = that.risk_setting_panel.find("input[name*='closeRadio']:checked").val();
			valArray[2] = (closeRadio == undefined ? "1" : closeRadio);
			valArray[3] = that.risk_setting_panel.find("input[id*='closeVal']").val();
		}else{
			valArray[2] = 0;
			valArray[3] = 0;
		}
		if(that.risk_setting_panel.find("input[name*='closeCheck']").prop("checked")){
			valArray[4] = that.risk_setting_panel.find("input[name*='closeTypeRadio']:checked").val();
			valArray[5] = that.risk_setting_panel.find("input[id*='closeTypeVal']").val() == "" ? "0" : that.risk_setting_panel.find("input[id*='closeTypeVal']").val();
		}else{
			valArray[4] = 0;
			valArray[5] = 0;
		}
		that.regex = RiskTool.arrayToRegex(valArray);
	};
	//根据风控文档对象校验
	this.validate = function(){
		//overwrite in SubClass
	};
	this.defaultValidate = function(){
		var validateTextBoxes = that.risk_setting_panel.find("input:enabled[id*='Val']");
		if(validateTextBoxes.size() == 0){
			risk_main.errorMarkPanel(that.idx);
			risk_main.expandPanel(that.idx);
			return false;
		}
		var vaild = true;
		for(var i = 0 ; i < validateTextBoxes.size() ; i++){
			if(!validateTextBoxes.eq(i).textbox("isValid")){
				vaild = false;
				break;
			}
		}
		if(vaild){
			risk_main.collapsePanel(that.idx);
			risk_main.correctMarkPanel(that.idx);
			return true;
		}else{
			risk_main.expandPanel(that.idx);
			risk_main.errorMarkPanel(that.idx);
			return false;
		}
	};
	//绑定事件
	this.eventBanding = function(){
		//overwrite in SubClass
	};
	//获得表达式
	this.getRegex = function(){
		if(that.validate()){
			that.parse();
			return that.regex;
		}
	};
};

//全天候
function AllDayRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br/>"+
					 "&nbsp;&nbsp;交易时间段内的权益监控，当触发平仓线时，按亏损最大顺序平仓。<br/>"+
					 "<b>风控指标</b>：<br/>"+
					 "&nbsp;&nbsp;预警线/平仓线（比例）：【动态权益/初始资金】小于设定值，发出报警/触发平仓；<br/>"+
					 "&nbsp;&nbsp;预警线/平仓线（金额）：【动态权益】小于设定值，发出报警/触发平仓。<br/>"+
					 "<b>平仓方式</b>：<br/>"+
					 "&nbsp;&nbsp;比例：【单次平仓上限资金/初始资金】，如：初始资金100000元，50%比例平仓，则单次平仓上限为50000元。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("1");
	//绑定事件
	this.eventBanding = function(){
		risk_config.warningDivBanding(that.risk_setting_panel);
		risk_config.closeDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		that.defualtGenerate();
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		that.defualtParse();
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

//全天候--分时段
function AllDayTimeLimitRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br/>"+
					 "&nbsp;&nbsp;只对设定时间端内进行监控，时间段以外无监控，当触发平仓线时，按亏损最大顺序平仓。<br/>"+
					 "<b>风控指标</b>：<br/>"+
					 "&nbsp;&nbsp;预警线/平仓线（比例）：【动态权益/初始资金】小于设定值，发出报警/触发平仓；<br/>"+
					 "&nbsp;&nbsp;预警线/平仓线（金额）：【动态权益】小于设定值，发出报警/触发平仓。<br/>"+
					 "<b>平仓方式</b>：<br/>"+
					 "&nbsp;&nbsp;比例：【单次平仓上限资金/初始资金】，如：初始资金100000元，50%比例平仓，则单次平仓上限为50000元。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("2");
	//绑定事件
	this.eventBanding = function(){
		risk_config.timeLimitDivBanding(that.risk_setting_panel);
		risk_config.warningDivBanding(that.risk_setting_panel);
		risk_config.closeDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		that.defualtGenerate();
		var valArray = that.regex.split("*");
		var startTime = RiskTool.getTimeStrByMs(valArray[6]);
		var endTime = RiskTool.getTimeStrByMs(valArray[7]);
		that.risk_setting_panel.find("[id*='startTimeLimitVal']").timespinner("setValue",startTime);
		that.risk_setting_panel.find("[id*='endTimeLimitVal']").timespinner("setValue",endTime);
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		//调用默认转化
		that.defualtParse();
		//读取时间段 继续拼接 regex
		var startTime = that.risk_setting_panel.find("[name*='startTimeLimitVal']").val();
		var endTime = that.risk_setting_panel.find("[name*='endTimeLimitVal']").val();
		var startTimeMs = RiskTool.getMsByTimeStr(startTime);
		var endTimeMs = RiskTool.getMsByTimeStr(endTime);
		if(startTimeMs >= endTimeMs){
			endTimeMs = endTimeMs + 3600*24*1000;
		}
		that.regex = that.regex+"*"+startTimeMs+"*"+endTimeMs;
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

//时间段限制
function TimeBucketLimitRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br />" +
					 "&nbsp;&nbsp;对设定时间段内进行监控，在设定时间段内不允许任何持仓。<br />" +
					 "<b>风控指标</b>：<br />" +
					 "&nbsp;&nbsp;警告：时间段内监控有任何仓位，则进行警告；<br />" +
					 "&nbsp;&nbsp;平仓：时间段内监控有任何仓位，则进行全部平仓。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("3");
	//绑定事件
	this.eventBanding = function(){
		risk_config.timeBucketDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		var valArray = that.regex.split("*");
		that.risk_setting_panel.find("[id*='timeBucketCheck']").each(function(i){
			if(valArray[i*3] != "0"){
				$(this).click();
				$(this).nextAll("input[name*='timeBucketRadio'][value='" + valArray[i*3] + "']").click();
				var startTime = RiskTool.getTimeStrByMs(valArray[i*3+1]);
				var endTime = RiskTool.getTimeStrByMs(valArray[i*3+2]);
				$(this).nextAll("[id*='startTimeBucketVal']").timespinner("setValue",startTime);
				$(this).nextAll("[id*='endTimeBucketVal']").timespinner("setValue",endTime);
			}
		});
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		var valArray = [];
		that.risk_setting_panel.find("[id*='timeBucketCheck']").each(function(i){
			if($(this).prop("checked")){
				var startTime = $(this).nextAll("[id*='startTimeBucketVal']").val();
				var endTime = $(this).nextAll("[id*='endTimeBucketVal']").val();
				valArray[i*3] = $(this).nextAll("input[name*='timeBucketRadio']:checked").val();
				var startTimeMs = RiskTool.getMsByTimeStr(startTime);
				var endTimeMs = RiskTool.getMsByTimeStr(endTime);
				if(startTimeMs >= endTimeMs){
					endTimeMs = endTimeMs + 3600*24*1000;
				}
				valArray[i*3+1] = startTimeMs;
				valArray[i*3+2] = endTimeMs;
			}else{
				valArray[i*3] = 0;
				valArray[i*3+1] = 0;
				valArray[i*3+2] = 0;
			}
		});
		that.regex = RiskTool.arrayToRegex(valArray);
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

//品种监控
function VarietyRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br />" +
					 "&nbsp;&nbsp;对交易品种进行监控，可以分别对品种或者合约进行监控。输入品种则表示对品种的监控，输入合约号则表示对指定合约进行监控。（<font color=\"red\">注意大小写，设置错误该指标不生效</font>）<br />"+
					 "<b>风控指标</b>：<br />" +
					 "&nbsp;&nbsp;预警线/平仓线（比例）：【品种动态权益/初始资金】大于设定值，发出报警/触发平仓；<br />" + 
					 "&nbsp;&nbsp;预警线/平仓线（金额）：【品种动态权益】大于设定值，发出报警/触发平仓。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("4");
	//绑定事件
	this.eventBanding = function(){
		risk_config.varietyDivBanding(that.risk_setting_panel);
		risk_config.warningDivBanding(that.risk_setting_panel);
		risk_config.closeDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		that.defualtGenerate();
		var valArray = that.regex.split("*");
		that.risk_setting_panel.find("[id*='variety']").textbox("setValue",valArray[6]);
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		//调用默认转化
		that.defualtParse();
		//品种
		var variety = that.risk_setting_panel.find("[id*='variety']").val();
		that.regex += "*" + variety;
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

//劣后资金监控
function AfterPoorRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br />" +
					 "&nbsp;&nbsp;对初始资金中的劣后资金部分进行监控，当触发平仓线时，按亏损最大顺序平仓。<br />" +
					 "<b>风控指标</b>：<br />" +
					 "&nbsp;&nbsp;预警线/平仓线（比例）：【(动态权益-初始资金+初始劣后资金)/初始劣后资金】小于设定值，发出报警/触发平仓。<br />" + 
					 "<b>平仓方式</b>：<br />" +
					 "&nbsp;&nbsp;比例：【单次平仓上限资金/初始资金】，如：初始资金100000元，50%比例平仓，则单次平仓上限为50000元。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("6");
	//绑定事件
	this.eventBanding = function(){
		risk_config.warningDivBanding(that.risk_setting_panel);
		risk_config.closeDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		that.defualtGenerate();
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		//调用默认转化
		that.defualtParse();
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

//资金回撤监控
function DrawdownRisk(jqueryDocObj,idx,regex){
	var that = this;
	this.help_info = "<b>指标描述</b>：<br />" +
					 "&nbsp;&nbsp;日内资金回撤监控，当触发平仓线时，按亏损最大顺序平仓。<br />" +
					 "<b>风控指标</b>：<br />" +
					 "&nbsp;&nbsp;预警线/平仓线（比例）：【(最高动态权益-当前动态权益)/最高动态权益】比例大于设定值，发出报警/触发平仓。<br />" +
					 "<b>平仓方式</b>：<br />" +
					 "&nbsp;&nbsp;比例：【单次平仓上限资金/初始资金】，如：初始资金100000元，50%比例平仓，则单次平仓上限为50000元。";
	BaseRiskClass.apply(this, arguments);
	//初始化
	//根据html获得jquery对象
	this.showConfig = risk_config.risk_show_html_config("5");
	//绑定事件
	this.eventBanding = function(){
		risk_config.warningDivBanding(that.risk_setting_panel);
		risk_config.closeDivBanding(that.risk_setting_panel);
	};
	//根据regex改变html中的值
	this.generate = function(){
		that.defualtGenerate();
	};
	//根据html中的内容生成regex值
	this.parse = function(){
		//调用默认转化
		that.defualtParse();
	};
	//根据风控文档对象校验
	this.validate = function(){
		return that.defaultValidate();
	};
};

String.prototype.replaceAll = function(reallyDo, replaceWith, ignoreCase) { 
    if (!RegExp.prototype.isPrototypeOf(reallyDo)) {  
        return this.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);  
    } else {  
        return this.replace(reallyDo, replaceWith);  
    }  
};  

//辅助工具类
var RiskTool = {
		/**
		 * 此方法用于将数据库读到的毫秒数转化为时间格式07:00
		 * @param time 数据库得到的时间毫秒数
		 * @returns {String}
		 */
		getTimeStrByMs :function (time){
			if(time >= 60*1000*60*24){
				time = time - 60*1000*60*24;
			}
			time = new Number(time);
			time = Math.floor(time/(60*1000));
			var minute = time%60;
			minute  = minute >10?minute :'0'+minute ;
			var hour = Math.floor(time/60);
			return hour + ':' + minute ;
		},
		/**
		 * 此方法用于将用户输入的时间字符串转化为毫秒数
		 * @param str 用户输入的字符串
		 * @returns {Number} 毫秒数
		 */
		getMsByTimeStr : function (str){
			var hour = new Number(str.split(':')[0]);
			var minute = new Number(str.split(':')[1]);
			return hour*60*60*1000 + minute*60*1000;
		},
		//数组转regex
		arrayToRegex : function(valArray){
			var regex = "";
			for(var i = 0 ; i < valArray.length ; i++){
				regex += valArray[i];
				if(i != valArray.length-1){
					regex += "*";
				}
			}
			return regex;
		}
};
