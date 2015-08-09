<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="zh-cn">
    <head>
	 <meta charset="utf-8">
     <meta http-equiv="x-ua-compatible" content="IE=edge" />
     <meta http-equiv="pragma" content="no-cache">
     <meta name="viewport" content="width=device-width, initial-scale=1.0">
	 <title>国金期货</title>
	 <link rel="stylesheet" href="css/common.css" charset="utf-8">
	 <link rel="stylesheet" href="css/bootstrap.css" charset="utf-8">
	 <link rel="stylesheet" href="css/adviser-list.css" charset="utf-8">
	 <script src="js/jquery.min.js" charset="utf-8"></script>
	 <script src="js/common.js" charset="utf-8"></script>
	 
	 <script src="js/adviser-management.js" charset="utf-8"></script>
	 <script src="js/adviser-management-request.js" charset="utf-8"></script>
	 <script src="js/util.js" charset="utf-8"></script>
	</head>
	<body>
	<h1>投顾列表</h1>
	<div class="adviser-list-table-info">
		<a href="#" class="need-float">
			<div id="adviser-add">
				<img src="images/adviser-list-add-image.png"></img> <span>添加</span>
			</div>
		</a> 
		<a href="#"  class="need-float">
			<div id="adviser-edit">
				<img src="images/adviser-list-edit-image.png"></img> <span>编辑</span>
			</div>
		</a>
		<a href="#"  class="need-float">
			<div id="adviser-stop">
				<img src="images/stop.png"></img> <span>停用投顾</span>
			</div>
		</a>
		<a href="#"  class="need-float">
			<div id="adviser-start">
				<img src="images/start.png"></img> <span>启用投顾</span>
			</div>
		</a>
		<a href="#" class="need-float">
			<div id="adviser-delete">
				<img src="images/check.png"></img> <span>删除</span>
			</div>
		</a>
	</div>
	
	<table id="table-table" style="margin-left: 10px;margin-top: 40px;width:80%;">
	</table>

	<div class="popur-box border-radius-3 box-shadow" id="adviser-group-alert">
		<div class="popur-header">
			<span>投顾</span> <span class="popur-close-btn">X</span>
		</div>
		<table style="border-spacing: 6px;">
			<tbody>
				<tr>
					<td><span>投顾账号</span></td>
					<td><input type="text" id="adviser-management-input-account"
					      onfocus="checkNameFocus(this)" onblur="checkNameBlur(this)"/></td>
					<td><span>投顾姓名</span></td>
					<td><input type="text" id="adviser-management-input-name"
					      onfocus="checkNameFocus(this)" onblur="checkNameBlur(this)"/></td>
				</tr>
				<tr>
					<td><span>投顾密码</span></td>
					<td><input type="password" id="adviser-management-input-pwd"
					     onfocus="checkPwdFocus(this)" onblur="checkPwdBlur(this)"/></td>
					<td><span>重复投顾密码</span></td>
					<td><input type="password" id="adviser-management-input-repwd"
					     onfocus="checkPwdFocus(this)" onblur="checkPwdBlur(this)"/></td>
				</tr>
				<tr>
					<td><span>允许管理账户数</span></td>
					<td><input type="text" id="adviser-management-input-num"
					      onfocus="checkNumFocus(this)" onblur="checkNumBlur(this)"/></td>
					<td><span>所属组</span></td>
					<td>
					    <select style="width:95%;height:30px;margin-left:5px;margin-top:5px;" id="adviser-management-select" onclick="simOptionClick4IE()">
					    </select>
					</td>
				</tr>
				<tr>
					<td><span>允许操作</span></td>
					<td><input type="checkbox" id="adviser-management-riskcontrol"/>添加风控指标</td>
					<td><input type="checkbox" id="adviser-management-position"/>强制平仓</td>
					<td></td>
				</tr>
			</tbody>
		</table>
		<ul>
			<li><div class="button btn-blue-border border-radius-3"
					id="adviser-management-save-button">保存</div></li>
		</ul>
	</div>

</body>
</html>