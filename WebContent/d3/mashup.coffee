Ext.onReady ->
  
  # global 
  toolbar = undefined;
  vis = undefined;
  viewport = undefined;
  
  # functions 
  helloWorld = ->
    Ext.Msg.alert "Info", "Another Hello World!"
    return
  onMenuItem = (item) ->
    Ext.Msg.alert "Info", item.text
    return
  initialize = (btn, text) ->
    if btn is "ok"
      vis.urlinput text
      viewport.doLayout()
    return
    
  # PANEL 1: d3.js example 
  Myd3panel = Ext.extend(Ext.Panel,
    contructor: (config) ->
      Myd3panel.superclass.constructor.call this, config
      return
	
    listeners:
      afterrender: ->

      
    #raise event
    loadConfig: ->
 
    
    urlinput: (url) ->
      mask = Ext.Msg.wait("Loading...")
      @drawMap url, mask
      return

    
    drawMap: (url, mask) ->
      width = @superclass().getWidth.call this
      height = @superclass().getHeight.call this
      color = d3.scale.category20()
      force = d3.layout.force().charge(-500).linkDistance(30).size([
        width
        height
      ])
      target = d3.select("#customd3")
      svg = target.append("svg").attr("width", width).attr("height", height)
      
      #var svg = d3.select("body").append("svg").attr("width", width).attr("height", height);
      #var svg = d3.select('#' + this.id + '-innerCt').append("svg").attr("width", width).attr("height", height);
      console.log url + " is expected to be sent to servlet"
      d3.json "/SiteTreeUI/FDGraph", (error, graph) ->
        force.nodes(graph.nodes).links graph.links
        
        # demo: append stuff to html div id=result
        # result is used as content source by ExtJS panel
        jQuery('#result').append(JSON.stringify(graph))
        
        mask.hide()
        force.start()
        link = svg.selectAll(".link").data(graph.links).enter().append("line").attr("class", "link").style("stroke-width", (d) ->
          Math.sqrt d.value
        )
        node = svg.selectAll(".node").data(graph.nodes).enter().append("circle").attr("class", "node").attr("r", 5).style("fill", (d) ->
          color d.group
        ).call(force.drag)
        node.append("title").text (d) ->
          d.name

        force.on "tick", ->
          link.attr("x1", (d) ->
            d.source.x
          ).attr("y1", (d) ->
            d.source.y
          ).attr("x2", (d) ->
            d.target.x
          ).attr "y2", (d) ->
            d.target.y

          node.attr("cx", (d) ->
            d.x
          ).attr "cy", (d) ->
            d.y

          return

        return

      return
  )
  # close drawMap
  # close My.D3panel
  
  vis = new Myd3panel(
    title: 'in-page d3 demo'
    region : 'center'
    contentEl: 'customd3'
  )
  panel = new Ext.Panel(
  	title : 'commands & search panel'
  	region : 'east'
  	contentEl : 'cspanel'
  	width : '20%'
  )
  viewport = new Ext.Viewport(
    layout: 'border'
    autoshow: true;
    items: [
      vis
      panel
    ]
  )
  Ext.Msg.prompt "Welcome to SiTree!", "Enter URL:", initialize
  return
