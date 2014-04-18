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

<title>Insert title here</title>
</head>
<body>
<div id="chart"></div>
</body>
<script type="text/javascript">  
Ext.onReady(function(){
	Ext.Msg.alert('提示','我不會停止程序執行.'); 
	Ext.define('EB.view.content.SingleView', {
	    extend : 'Ext.panel.Panel',
	    alias : 'widget.singleview',

	    layout : 'fit',

	    title : 'single view',

	    initComponent : function() {
	        this.callParent(arguments);
	    },

	    onRender : function() {
	        var me = this;

	        me.doc = Ext.getDoc();
	        me.callParent(arguments);

	        me.drawMap();
	    },

	    drawMap : function() {
	    	var width = 960,
	        height = 500;

	    var color = d3.scale.category20();

	    var force = d3.layout.force()
	        .charge(-120)
	        .linkDistance(30)
	        .size([width, height]);

	    var svg = d3.select("#chart").append("svg")
	        .attr("width", width)
	        .attr("height", height);

	    // "miserables.json"
	    d3.json("/SiteTreeUI/FDGraph", function(error, graph) {
	      force
	          .nodes(graph.nodes)
	          .links(graph.links)
	          .start();

	      var link = svg.selectAll(".link")
	          .data(graph.links)
	          .enter().append("line")
	          .attr("class", "link")
	          .style("stroke-width", function(d) { return Math.sqrt(d.value); });

	      var node = svg.selectAll(".node")
	          .data(graph.nodes)
	          .enter().append("circle")
	          .attr("class", "node")
	          .attr("r", 5)
	          .style("fill", function(d) { return color(d.group); })
	          .call(force.drag);

	      node.append("title")
	          .text(function(d) { return d.name; });

	      force.on("tick", function() {
	        link.attr("x1", function(d) { return d.source.x; })
	            .attr("y1", function(d) { return d.source.y; })
	            .attr("x2", function(d) { return d.target.x; })
	            .attr("y2", function(d) { return d.target.y; });

	        node.attr("cx", function(d) { return d.x; })
	            .attr("cy", function(d) { return d.y; });
	      });
	    });
	    }

		});  
});
</script>
</html>