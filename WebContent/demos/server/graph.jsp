<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<link rel="stylesheet" type="text/css" href="../js/ext-3.4.0/resources/css/ext-all.css" />
<script type="text/javascript" src="../js/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../js/ext-3.4.0/ext-all.js"></script>
<script type="text/javascript">
Ext.onReady(function(){ 
	Ext.Ajax.request({
		   url: '/SiteTreeUI/CrawlJSon',    // where you wanna post
		   method: 'POST',
		   success: function(resp){
			  var jsonResp = Ext.util.JSON.decode(resp.responseText);
			  Ext.Msg.alert("Info", resp.responseText);
		   },   // function called on success
		   failure: function(resp){
			  alert("Error:"+resp.responseText);
			  Ext.Msg.alert("Error", resp.responseText);			  
			  location.href = "/SiteTreeUI/server/main.jsp";
		   }
		   //,params: { id: jsonResp.id }  
		});
});
</script>
<title>Graph Result Page</title>
</head>
<body>
<h1>Done!</h1>
</body>
</html>