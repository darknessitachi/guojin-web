/**
 * 此js文件用于提供页面一些共用的方法以及一些特殊的方法
 */

/**
 * 此方法用于得到cookie
 */
function getCookie(c_name)
{
	if (document.cookie.length>0)
	{
		c_start=document.cookie.indexOf(c_name + "=");
		if (c_start!=-1)
		{ 
			c_start=c_start + c_name.length+1 ;
			c_end=document.cookie.indexOf(";",c_start);
			if (c_end==-1) c_end=document.cookie.length;
			return unescape(document.cookie.substring(c_start,c_end));
		} 
	}
	return "";
}
/**
 * 此方法用于设在cookie
 * @param c_name 名称
 * @param value  值
 * @param expiredays 过期天数
 */
function setCookie(c_name,value,expiredays)
{
	var exdate=new Date();
	exdate.setDate(exdate.getDate()+expiredays);
	//escape()函数可对字符串进行编码，这样就可以在所有的计算机上读取该字符串。
	document.cookie=c_name+ "=" +escape(value)+
	((expiredays==null) ? "" : ";expires="+exdate.toGMTString());
}

/**
 * 如果 cookie 已设置，则显示到界面。
 */
//function checkCookie()
//{
//	var name = getCookie('name');
//	var pwd = getCookie('pwd');
//	if (username!=null && username!=""){
//		
//	}
//}
