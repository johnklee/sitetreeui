<%@ page language="java" contentType="text/html; charset=UTF8" pageEncoding="UTF8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF8">
<script type="text/javascript" src="../js/ext-3.4.0/adapter/ext/ext-base.js"></script>
<script type="text/javascript" src="../js/ext-3.4.0/ext-all.js"></script>
<script src="http://d3js.org/d3.v3.min.js"></script>
<link rel="stylesheet" type="text/css" href="../js/ext-3.4.0/resources/css/ext-all.css" />
<style>

.node {
  stroke: #fff;
  stroke-width: 1.5px;
}

.link {
  stroke: #999;
  stroke-opacity: .6;
}

</style>
<script type="text/javascript">  
    Ext.onReady(function(){  
        Ext.BLANK_IMAGE_URL = '../js/ext-3.4.0/resources/images/default/s.gif';  
        /* new Ext.Panel({  
            title:'面板標題(header)',  
            tbar : ['頂端工具欄(top toolbars)'],  
            bbar : ['底端工具欄(bottom toolbars)'],  
            height:800,  
            width:800,  
            frame:true,  
            applyTo :'panel',              
            bodyStyle:'background-color:#FFFFFF',  
            autoLoad :{url:"test.html" ,scripts:true},
            tools : [  
                {id:'toggle'},  
                {id:'close'},  
                {id:'maximize'}  
            ],  
            buttons:[  
                new Ext.Button({  
                    text:'面板底部(footer)'  
                })  
            ]  
        })  */ 
        
        var win = new Ext.Window({  
            el: 'window',  
            layout: 'fit',  
            title: 'Force-Directed Graph',  
            closable: true,  
            closeAction: 'hide',  
            width: 500,  
            height: 500,  
            items: [{}],  
            autoLoad :{url:"test.html" ,scripts:true},
            buttons: [{  
                text: 'Button',  
            }]  
        });  
          
        win.show(); 
    });  
</script>
<title>Force Direct Graph</title>
<!-- https://github.com/mbostock/d3/wiki/Force-Layout -->
</head>
<body>
<a href="../">Back</a>
<div id="panel"></div>
<div id="window"></div>
</body>

</html>