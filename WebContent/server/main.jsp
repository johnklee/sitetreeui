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
	var progressBar;
	var task;
	Ext.MessageBox.prompt('提示', '輸入目標 URL(s):', callBack, this, false);
	
	//function stopTask(){Ext.TaskManager.stop(task);}
	function aftSend(response)
	{
		//Ext.Msg.alert("Info", "TaskID="+response.responseText)
		var jsonResp = Ext.util.JSON.decode(response.responseText); 		
		if(jsonResp.id.length>0)
		{			
			task = Ext.TaskMgr.start({  
	            run:function(){  
	            	Ext.Ajax.request({
	         		   url: '/SiteTreeUI/CrawlStatus',    // where you wanna post
	         		   method: 'POST',
	         		   success: function(resp){
	         			  var jsonStat = Ext.util.JSON.decode(resp.responseText);
	         			  if(jsonStat.stat>0)
	         			  {
	         				 percentage = jsonStat.stat/3;  
	     	                 progressText = Math.ceil(percentage*100) +'%' + ' ('+jsonStat.desc+")";  
	     	                 progressBar.updateProgress(percentage, progressText);  
	         		      }
	         			  else if(jsonStat.stat==-1)
	         			  {
	         				 alert("Error: "+jsonResp.desc);
	         				 Ext.Msg.alert("Error", jsonResp.desc);
	         				 progressBar.hide();
	         				 location.href = "/SiteTreeUI/server/main.jsp";
	         		      }
	         			  else
	         			  {
	         				 progressBar.hide();
		         			 //stopTask();
		         			 location.href = "/SiteTreeUI/server/graph.jsp";
	         			  }
	         		   },   // function called on success
	         		   failure: function(resp){
	         			  alert("Fail: "+resp.responseText);
	         			  Ext.Msg.alert("Error", resp.responseText);
	         			  progressBar.hide();
	         			  location.href = "/SiteTreeUI/server/main.jsp";
	         		   },
	         		   params: { id: jsonResp.id }  // your json data
	         		});
	            	
	                /* count++;  
	                if(count > 10)  
	                {  
	                    progressBar.hide();  
	                }                     
	                percentage = count/10;  
	                progressText = percentage * 100 + '%';  
	                progressBar.updateProgress(percentage, progressText);  */ 
	            },  
	            interval: 1000  
	        });
		}
		else
		{
			alert("ID:"+jsonResp.id+", URL:"+jsonResp.url+", Desc:"+jsonResp.desc);
			Ext.Msg.alert("Error", jsonResp.desc);
			progressBar.hide();
		}
	}
	
	function failSend()
	{
		Ext.Msg.alert("Error", "Fail to send request!");
	}
	
    function callBack(id, msg)  
    {  
    	//Ext.Msg.alert("Test", '單擊的按鈕ID是: '+id+'\n'+"輸入內容是: "+msg);
    	progressBar = new Ext.ProgressBar({  
            text: '處理中...',  
            width: 300,              
            // 在 id 為 'ProgressBar1' 的 div 元素後追加一個進度條組件  
            applyTo: 'ProgressBar1'  
            // applyTo 可以接受元素 ID 或元素本身做為參數, 如下也是正確寫法  
            // applyTo: document.getElementById('ProgressBar')  
        });
    	
    	Ext.Ajax.request({
    		   url: '/SiteTreeUI/Crawl',    // where you wanna post
    		   method: 'POST',
    		   success: aftSend,   // function called on success
    		   failure: failSend,
    		   params: { urls: msg }  // your json data
    	});     	    	
    } 
});
</script>
<title>Server Module Main Page</title>
</head>
<body>
<div id='ProgressBar1'></div> 
<!--<table><tr><td id='ProgressBar1'></td></tr></table>-->
</body>
</html>