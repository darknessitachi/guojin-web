<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.*"%>
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
	 
	 <script src="js/adviser-list.js" charset="utf-8"></script>
	 <script src="js/adviser-list-request.js" charset="utf-8"></script>
	 <script src="js/util.js" charset="utf-8"></script>
	</head>
	<body>
	<h1>投顾列表</h1>
	<div class="adviser-list-table-info">
		<a href="#" class="need-float">
			<div id="adviser-list-add">
				<img src="images/adviser-list-add-image.png"></img> <span>添加</span>
			</div>
		</a> 
		<a href="#" class="need-float">
			<div id="adviser-list-edit">
				<img src="images/adviser-list-edit-image.png"></img> <span>编辑</span>
			</div>
		</a>
		<a href="#" class="need-float">
			<div id="adviser-list-delete">
				<img src="images/check.png"></img> <span>删除</span>
			</div>
		</a>
	</div>
	
	<table id="table-table" style="margin-left: 10px;margin-top: 40px;width:80%;">
	</table>

	<div class="popur-box border-radius-3 box-shadow" id="adviser-group-alert">
		<div class="popur-header">
			<span>投顾组</span> <span class="popur-close-btn">X</span>
		</div>
		<ul>
			<li>组名称</li>
			<li><input type="text" id="adviser-list-input-name"
				onfocus="checkNameFocus(this)"
				onblur="checkNameBlur(this)" /></li>
			<li>允许投顾数</li>
			<li><input type="text" id="adviser-list-input-num" style="color: #999;" onfocus="checkNumFocus(this)"
				onblur="checkNumBlur(this)" /></li>
			<li><div class="button btn-blue-border border-radius-3"
					id="adviser-list-save-button">保存</div></li>
		</ul>
	</div>

</body>
</html>