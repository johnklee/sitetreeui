Ext.onReady(function() {
		/* global */
		var toolbar;
		var vis;
		var viewport;
		
		/* functions */
		function helloWorld() {
			Ext.Msg.alert("Info", "Another Hello World!");
		}
		function onMenuItem(item) {
			Ext.Msg.alert('Info', item.text);
		}
		function initialize(btn,text){
			if (btn == 'ok'){
				vis.urlinput(text);
				viewport.doLayout();
			}
		}
		
		/* TOOLBAR */
		
		/*
		 var infoMenu = new Ext.menu.Menu({
		 items : [ {
		 text : 'ComboBox',
		 handler : onMenuItem,
		 icon : 'images/combobox.png',
		 menu : new Ext.menu.Menu({
		 items : [ {
		 text : '遠程數據源的組合框',
		 handler : function() {
		 window.location = 'extjsDemo/e3-9.html';
		 }
		 } ]
		 })
		 } ]
		 });
		 toolbar.add({
		 text : 'ExtJS Demo',
		 menu : infoMenu,
		 iconCls : 'demo'
		 });
		 */
		/* PANEL 1: d3.js example */
		Myd3panel = Ext.extend(Ext.Panel, {

			contructor : function(config) {
				Myd3panel.superclass.constructor.call(this, config);
			},
			
			/*
			initComponent : function() {
				Myd3panel.superclass.initComponent.apply(this, arguments);
			},
			*/
			
			listeners : {
				afterrender : function(){
					//this.drawMap();
				}	
			},

			/*
			onRender : function() {
				Myd3panel.superclass.onRender.apply(this, arguments);
				//this.drawMap();
			},
			*/

			loadConfig : function() {
				//raise event
			},
			
			urlinput : function(url){
				mask = Ext.Msg.wait("Loading...");
				this.drawMap(url, mask);
				//mask.hide();
			},

			drawMap : function(url, mask) {
				var width = 960, height = 500;
				var color = d3.scale.category20();
				var force = d3.layout.force().charge(-120).linkDistance(30)
						.size([ width, height ]);

				var target = d3.select("#customd3");
				var svg = target.append("svg").attr("width", width).attr("height",height);
		        //var svg = d3.select("body").append("svg").attr("width", width).attr("height", height);
				//var svg = d3.select('#' + this.id + '-innerCt').append("svg").attr("width", width).attr("height", height);
				
				console.log(url+" is expected to be sent to servlet");
				d3.json("/SiteTreeUI/FDGraph", function(error, graph) {
					force.nodes(graph.nodes).links(graph.links);
					console.log("finished loading");
					mask.hide();
					force.start();

					var link = svg.selectAll(".link").data(graph.links).enter()
							.append("line").attr("class", "link").style(
									"stroke-width", function(d) {
										return Math.sqrt(d.value);
									});

					var node = svg.selectAll(".node").data(graph.nodes).enter()
							.append("circle").attr("class", "node")
							.attr("r", 5).style("fill", function(d) {
								return color(d.group);
							}).call(force.drag);

					node.append("title").text(function(d) {
						return d.name;
					});

					force.on("tick", function() {
						link.attr("x1", function(d) {
							return d.source.x;
						}).attr("y1", function(d) {
							return d.source.y;
						}).attr("x2", function(d) {
							return d.target.x;
						}).attr("y2", function(d) {
							return d.target.y;
						});

						node.attr("cx", function(d) {
							return d.x;
						}).attr("cy", function(d) {
							return d.y;
						});
					});
				});
			} // close drawMap
		}); // close My.D3panel

		toolbar = new Ext.Toolbar();
		toolbar.addButton([ {
			text : 'Another Hello World',
			handler : helloWorld
		} ]);
		vis = new Myd3panel({
			title : "in-page d3 demo"
		});
		viewport = new Ext.Viewport({
			layout : 'Auto',
			autoshow : true,
			items : [ toolbar, vis ]
		});
		Ext.Msg.prompt("Welcome to SiTree!", "Enter URL:", initialize);
		
	});