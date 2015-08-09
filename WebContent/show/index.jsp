<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>国金期货-测试页面</title>
</head>
<body>
	<div id="k2">eeeeeee</div>
	
	<script type="text/javascript">
		window.load=loading();
		function loading(){
			var s=window.parent.document.getElementById("k1");
			var param=<%=pageContext.findAttribute("obj")%>;
			var k2=<%=pageContext.getAttribute("obj")%>;
			var k3=<%=pageContext.getAttributesScope("obj")%>;
			var k4=<%=pageContext.getRequest().getAttribute("obj")%>;
			var k5=${obj};
			document.getElementById("k2").innerHTML=<%=request.getAttribute("obj")%>;
		};
	</script>
</body>
</html>