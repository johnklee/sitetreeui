Ext.onReady ->
  
  # global
  progressMask = undefined;
  pollingTask = undefined;
  polling = undefined; # polling clock function
  
  visPanel = undefined;
  ig = undefined; # this holds the graph itself
  colorLegend = undefined; # color tooltip
  eastPanel = undefined;
  searchPanel = undefined;
  analysisPanel = undefined;
  viewport = undefined;
  
  rooturl = undefined; # remember user input url
  colorLegendHTML = undefined; # store color legend for tooltip
  resultStore = new Ext.data.JsonStore(
  
    # store configs
    autoDestroy: true
    storeId: "resultStore"
    
    # reader configs
    root: "result"
    idProperty: "id"
    fields: [
      { name: "Id", mapping: "id" }
      { name: "Score", mapping: "score" }
      { name: "URL", mapping: "url" }
    ]
    
  )
  analysisStore = new Ext.data.JsonStore(
    autoDestroy: true
    storeId: "analysisStore"
    root: "analysis"
    idProperty: "rule"
    fields: [
      { name: "Rule", mapping: "rule" }
      { name: "Score", mapping: "score" }
      { name: "Description", mapping: "description" }
    ]
  )
  
  
  # functions
  
  showWelcome = () ->
    Ext.Msg.prompt "Welcome to SiTree!", "Enter URL:", initialize
  
  initialize = (btn, url) ->
    if btn is "ok"
      rooturl = url
      progressMask = Ext.Msg.progress("Processing...","Sending URL...")
      Ext.Ajax.request
        url:'/SiteTreeUI/Crawl'
        method:'POST'
        success:pollStatus
        failure:failURLSend
        params:
            urls: url
      
    return
  
  
  failURLSend = () ->
    progressMask.hide()
    Ext.Msg.alert("Error","Failed to send URL!")   
  
  
  pollStatus = (response) ->
    jsonResp = Ext.util.JSON.decode(response.responseText)    
    if jsonResp.id.length > 0
      polling =
        run: () ->
          Ext.Ajax.request
            url:'/SiteTreeUI/CrawlStatus'
            method:'POST'
            success:updateProgress
            failure:failProgress
            params:
              id: jsonResp.id
        interval: 1000
      Ext.TaskMgr.start(polling)
    else
      alert("ID:"+jsonResp.id+", URL:"+jsonResp.url+", Desc:"+jsonResp.desc)
      progressMask.hide()
      showWelcome()
    return

   
  updateProgress = (response) ->
    jsonStat = Ext.util.JSON.decode(response.responseText)
    if jsonStat.stat > 0
      percentage = jsonStat.stat/3
      progressText = Math.ceil(percentage*100) + '% (' + jsonStat.desc + ')'
      progressMask.updateProgress(percentage, progressText)
    else if jsonStat.stat == -1
      progressMask.hide()
      alert("Error: "+jsonStat.desc)
      showWelcome()
    else 
      # processing done, d3 can get data
      Ext.TaskMgr.stop(polling)
      visPanel.setup()
      viewport.doLayout()
    
  
  failProgress = (response) ->
    alert("Fail: "+resp.responseText)
    progressMask.hide()
    Ext.Msg.alert("Error", resp.responseText);
    showWelcome()
  
  sendKeyword = (rooturl, keyword) ->
      Ext.Ajax.request
        url:"/SiteTreeUI/search"
        method:"POST"
        success: setupResultStore
        failure: failResultStoreSetup
        params:
          url: rooturl
          keyword: keyword
  
  setupResultStore = (response) ->
    searchdata = JSON.parse( response.responseText )
    resultStore.loadData(searchdata)
    #console.log(resultStore.getById(3))
    
  failResultStoreSetup = (response) ->
    alert("failed at getting search results")
  
  
  # Abstract Layout class
  # Returns placement for nodes
  class Layout
    values: d3.map()
    dummy: 'test'
    setKeys: (nodedata) ->
    getPlacement: (key) ->
    data: (input) ->
      if !arguments.length
        return d3.keys(values)
      @setKeys(input)
      return @
  
  # Concrete Layout: Radial
  # Place nodes in rings
  class radialLayout extends Layout
    _center: {"x":0, "y":0}
    _radius: 1
    _start: -120
    current: -120
    increment: 0
    type: 'radial'
    
    # Given an center point, angle, and radius length,
    # return a radial position for that angle
    radialLocation = (center, angle, radius) ->
      x = (center.x + radius * Math.cos(angle * Math.PI / 180))
      y = (center.y + radius * Math.sin(angle * Math.PI / 180))
      {"x":x,"y":y}

    # Main entry point for radialLayout
    # Returns location for a particular key,
    # creating a new location if necessary.
    getPlacement: (key) ->
      value = @values.get(key)
      value

    # Gets a new location for input key
    place: (key, rad) ->
      #console.log(@_center, @current, rad)
      value = radialLocation(@_center, @current, rad)
      @values.set(key,value)
      #console.log(value)
      @current += @increment
      value
    
    setKeys: (nodedata) ->
      temp_radius = 0
      for d in [0..@getMaxDepth(nodedata)]
        circleKeys = @getKeysAtDepth(nodedata,d)
        @increment = 360 / circleKeys.length
        @current = @_start
        if d == 0
          temp_radius = 0
        else if d == 1
          temp_radius = @_radius
        else
          temp_radius = 1.55*temp_radius
        T = @
        circleKeys.forEach (k) -> T.place(k, temp_radius)
      #console.log(@values)

    getMaxDepth: (nodedata) ->
      max_depth = 1
      for n in nodedata
        if n.lvl > max_depth
          max_depth = n.lvl
      maxDepth = max_depth
      maxDepth
    
    getKeysAtDepth: (nodedata, depth) ->
      # nodesAtSameDepth is an array of id's
      nodesAtSameDepth = (node for node in nodedata when node.lvl is depth)
      nodesAtSameDepth = (node.id for node in nodesAtSameDepth)
      nodesAtSameDepth
    
    center: (_) ->
      if !arguments.length
        return @_center
      @_center = _
      return @
    radius: (_) ->
      if !arguments.length
        return @_radius
      @_radius = _
      return @
    start: (_) ->
      if !arguments.length
        return @_start
      @_start = _
      return @
 
  
  
  # The BIG function: d3.js graph 
  graphPanel = Ext.extend(Ext.Panel,
    
    setup: () ->
      progressMask.updateProgress(1,"Rendering...")
      if !d3.select('#sitreeui_ig').empty()
        # Clean up
        d3.select('#sitreeui_ig').remove()
        jQuery('#tooltipHolder').empty()
        ig = undefined;
      
      ig = @Graph()
      d3.json "/SiteTreeUI/CrawlJSon", (error, json) ->
        if (error)
          progressMask.hide()
          alert("Error retrieving, abort!")
          showWelcome()
        else
          ig("#customd3", json)
      return

    
    # Draw the graph!
    Graph: () ->
      # variables we want to access
      # in multiple places of Graph
      width = @superclass().getInnerWidth.call this
      height = @superclass().getInnerHeight.call this
      # radial base radius
      r = 120
      previousHighlight = null;
      # allData will store the unfiltered data
      allData = []
      curLinksData = []
      curNodesData = []
      nodesMap = d3.map()
      linkByIndex = d3.map()
      invLinkByIndex = d3.map()
      # these will hold the SVG groups for
      # accessing the nodes and links display
      nodesG = null;
      linksG = null;
      layersG = null;
      markersG = null;
      # these will point to the d3 SVG elements
      # of nodes links and circles
      node = null;
      link = null;
      layer = null;
      #marker = null;
      # variables to reflect the current settings
      # of the visualization
      layout = undefined;
      # groupCenters will store our radial layout for
      # the group by artist layout.
      #groupCenters = null;

      # our force directed layout
      force = d3.layout.force()
      # color function used to color nodes
      nodeColors = d3.scale.category20()
      # tooltip used to display details
      tooltip = Tooltip("tooltipHolder", "vis-tooltip", 230)
      # charge used in artist layout
      #charge = (node) -> -Math.pow(node.radius, 2.0) / 2

      # Starting point for graph visualization
      # Initializes visualization and starts force layout
      graph = (selection, data) ->
        # format our data
        allData = setupData(data)
        
        # create our svg and groups
        vis = d3.select(selection).append("svg")
          .attr("width", width)
          .attr("height", height)
          .attr("id", 'sitreeui_ig')
          .call(d3.behavior.zoom().scaleExtent([1, 8]).on("zoom", zoom))
        layersG = vis.append("g").attr("id", "layers")
        linksG = vis.append("g").attr("id", "links")
        markersG = vis.append("defs").attr("id", "markers")
        nodesG = vis.append("g").attr("id", "nodes")

        # setup the size of the force environment
        force.size([width, height])
        # set default layout radial
        setLayout( new radialLayout )
        #setFilter("all")
        # Perform rendering and start force layout
        update()

      
      # called ONCE to clean up raw data and switch links to
      # point to node instances
      # Returns modified data
      setupData = (data) ->
        # initialize circle radius scale
        countExtent = d3.extent(data.nodes, (d) -> d.lvl)
        circleRadius = d3.scale.sqrt().range([16,4]).domain(countExtent)

        # id's -> node objects
        nodesMap  = mapNodes(data.nodes)
        #console.log(nodesMap)

        # switch links to point to node objects instead of id's
        data.links.forEach (l) ->
          l.source = nodesMap.get(l.source)
          l.target = nodesMap.get(l.target)
          
          # linkByIndex is used for link sorting
          if linkByIndex.has(l.source.id)
            temp = linkByIndex.get(l.source.id)
            temp.push(l.target.id) # this returns NOT the array itself!
            linkByIndex.set(l.source.id, temp)
          else
            linkByIndex.set(l.source.id, [l.target.id])
            
          # invLinkByIndex is used to find indegree
          if invLinkByIndex.has(l.target.id)
            temp = invLinkByIndex.get(l.target.id)
            temp.push(l.source.id) # this returns NOT the array itself!
            invLinkByIndex.set(l.target.id, temp)
          else
            invLinkByIndex.set(l.target.id, [l.source.id])
        
        contentTypeColorMap = d3.map()
        data.nodes.forEach (n) ->
          # set initial x/y to values within the width/height
          # of the visualization
          n.x = randomnumber=Math.floor(Math.random()*width)
          n.y = randomnumber=Math.floor(Math.random()*height)
          # set radius to the node ~ node circle's size
          n.radius = circleRadius(n.lvl)
          # calculate subgraph collapsible
          #n.subgraph = calcSubgraph(n.id)
          # set searched toggle
          n.searched = false;
          # keep track of all contentTypes
          if !contentTypeColorMap.has(n.contentType)
            contentTypeColorMap.set(n.contentType, nodeColors(n.contentType))
          
        colorLegendHTML = convertLegendtoHTML(contentTypeColorMap)
        #console.log(colorLegendHTML)
          
        data
      
      
      # Switches force to new layout
      setLayout = (newLayout) ->
        layout = newLayout
   
   
      # The update() function performs the bulk of the
      # work to setup our visualization based on the
      # current layout/sort/filter.
      #
      # update() is called everytime a parameter changes
      # and the graph needs to be reset.
      update = () ->
        # filter data to show based on current filter settings.
        #curNodesData = filterNodes(allData.nodes)
        curNodesData = allData.nodes
        #console.log(curNodesData)
        #curLinksData = filterLinks(allData.links, curNodesData)
        curLinksData = allData.links
        
        # Set layout parameters
        # TODO: uncouple this from d3.js?
        if layout.type == "radial"
          # curNodesData expects an array
          # TODO: currently just passing all in, maybe truncate?
          layout.center({"x":width/2, "y":height/2})
            .radius(r).data(curNodesData)
          force.on("tick", tickFunc)
        else
          alert('Tick for this layout not implemented!')
              
        # reset nodes in force layout
        force.nodes(curNodesData)
        # enter / exit for nodes
        updateNodes()
        
        # updateLinks() will be called when
        # force is done animating

        # finally hide progress
        progressMask.hide()
        # start me up
        force.start()

      # Public function to switch between layouts
      ###
      graph.toggleLayout = (newLayout) ->
        force.stop()
        setLayout(newLayout)
        update()
      ###
      
      # Public function to switch between filter options
      ###
      graph.toggleFilter = (newFilter) ->
        force.stop()
        setFilter(newFilter)
        update()
      ###
      
      # Public function to switch between sort options
      ###
      graph.toggleSort = (newSort) ->
        force.stop()
        setSort(newSort)
        update()
      ###
      
      # Public function to highlight node
      graph.highlight = (nodeid) ->
        nodeid = (if typeof nodeid is "string" then Number(nodeid) else nodeid)
        ig.resetHighlight()
        node.each (d) ->
          if d.id == nodeid
            n = d3.select(this)
            n.style("fill", "red")
             .style("stroke-width", 5.0)
             .style("stroke", "#555")
            previousHighlight = nodeid
            d.searched = true;
      
      # Public function to reset highlight
      graph.resetHighlight = () ->
        if previousHighlight != null
          node.each (d) ->
            if d.id == previousHighlight
              n = d3.select(this)
              n.style("fill", (d) -> nodeColors(d.contentType))
               .style("stroke", (d) -> strokeFor(d))
               .style("stroke-width", 1.0)
              d.searched = false;
              previousHighlight = null;
      
      
      # Helper function to map node id's to node objects.
      # Returns d3.map of ids -> nodes
      mapNodes = (nodes) ->
        nodesMap = d3.map()
        nodes.forEach (n) ->
          nodesMap.set(n.id, n)
        nodesMap

      # Helper function that turns color map to html
      # for colorLegend tooltip
      convertLegendtoHTML = (map) ->
        html = ''
        map.forEach (k,v) ->
          html += '<p>' + k + '</p>'
          html += '<div class="clrlgnd" style="background-color:'
          html += v + ';"> ^ </div>'
        html
        
      
      # Helper function to find node's subgraph
      # Returns an array of node id's
      # Algorithm version 1, DROPPED
      calcSubgraph = (nodeid) ->
        
        # Helper function to check if two node share same parent
        # parent must be either the collapse root or STRICTLY below its paths
        checkShareParent = (a, b) ->
          test = true;
          return test
        
        subgraph = []
        # check if indegree disqualifies this node
        if invLinkByIndex.has(nodeid)
          test = true;
          for i in invLinkByIndex.get(nodeid)
            test = test & checkShareParent(i,nodeid)
            if (test == false)
              # disqualified, discard any subgraph of this node
              # BUG: what if other nodes connect to this node's subgraph?
              return []
        # node qualified, check its subgraph
        if linkByIndex.has(nodeid)
          for i in linkByIndex.get(nodeid)
            if nodesMap.get(i).lvl > nodesMap.get(nodeid).lvl
              jQuery.union(subgraph,calcSubgraph(i))
        subgraph.push(nodeid) # lastly add itself
        subgraph
        
      # Find nodes to include under nodeid in **current** graph
      # Note: when expanding expand every node
      #       some missing links must be expanded manually by other nodes
      # Algorithm version 2, DROPPED
      getCurrentSubgraph = (nodeid) ->
        # Phase 1: get all nodes strictly under
        dfs = getLinkRecursive(nodeid)
        # Phase 2: remove nodes that are connected by nodes outside dfs
        subgraph = dfs.slice(0)
        for node in dfs
          if invLinkByIndex.has(node)
            for parent in invLinkByIndex.get(node)
              if parent not in dfs
                # linked by node outside dfs, remove
                subgraph.splice( jQuery.inArray(parent,subgraph), 1 )
                # BUG: now how to stop exploring this node's subgraph?
      
      
      # Helper function that returns an associative array
      # with counts of unique attr in nodes
      # attr is value stored in node, like 'artist'
      nodeCounts = (nodes, attr) ->
        counts = {}
        nodes.forEach (d) ->
          counts[d[attr]] ?= 0
          counts[d[attr]] += 1
        counts

      # Given two nodes a and b, returns true if
      # there is a link between them.
      # Uses linkByIndex initialized in setupData
      neighboring = (a, b) ->
        cond1 = false;
        cond2 = false;
        if linkByIndex.has(a.id)
          cond1 = true if (linkByIndex.get(a.id).indexOf(b.id) != -1)
        if linkByIndex.has(b.id)
          cond2 = true if (linkByIndex.get(b.id).indexOf(a.id) != -1)
        cond1 or cond2
        #linkByIndex[a.id + "," + b.id] or linkByIndex[b.id + "," + a.id]

      # Removes nodes from input array
      # based on current filter setting.
      # Returns array of nodes
      ###
      filterNodes = (allNodes) ->
        filteredNodes = allNodes
        if filter == "popular" or filter == "obscure"
          playcounts = allNodes.map((d) -> d.playcount).sort(d3.ascending)
          cutoff = d3.quantile(playcounts, 0.5)
          filteredNodes = allNodes.filter (n) ->
            if filter == "popular"
              n.playcount > cutoff
            else if filter == "obscure"
              n.playcount <= cutoff

        filteredNodes
      ###
        
      # Returns array of artists sorted based on
      # current sorting method.
      ###
      sortedArtists = (nodes,links) ->
        artists = []
        if sort == "links"
          counts = {}
          links.forEach (l) ->
            counts[l.source.artist] ?= 0
            counts[l.source.artist] += 1
            counts[l.target.artist] ?= 0
            counts[l.target.artist] += 1
          # add any missing artists that dont have any links
          nodes.forEach (n) ->
            counts[n.artist] ?= 0

          # sort based on counts
          artists = d3.entries(counts).sort (a,b) ->
            b.value - a.value
          # get just names
          artists = artists.map (v) -> v.key
        else
          # sort artists by song count
          counts = nodeCounts(nodes, "artist")
          artists = d3.entries(counts).sort (a,b) ->
            b.value - a.value
          artists = artists.map (v) -> v.key

        artists
      ###
      
      ###
      updateCenters = (artists) ->
        if layout == "radial"
          groupCenters = radialLayout().center({"x":width/2, "y":height / 2 - 100})
            .radius(300).increment(18).keys(artists)
      ###
      
      # Removes links from allLinks whose
      # source or target is not present in curNodes
      # Returns array of links
      ###
      filterLinks = (allLinks, curNodes) ->
        curNodes = mapNodes(curNodes)
        allLinks.filter (l) ->
          curNodes.get(l.source.id) and curNodes.get(l.target.id)
      ###
      
      # Helper function that returns stroke color for
      # particular node.
      strokeFor = (d) ->
        d3.rgb(nodeColors(d.contentType)).darker().toString()

      # enter/exit display for nodes
      updateNodes = () ->
        node = nodesG.selectAll("circle.node")
          .data(curNodesData, (d) -> "#{d.id}")
          
        node.enter().append("circle")
          .attr("class", "node")
          .attr("cx", (d) -> d.x)
          .attr("cy", (d) -> d.y)
          .attr("r", (d) -> d.radius)
          .style("fill", (d) -> nodeColors(d.contentType))
          .style("stroke", (d) -> strokeFor(d))
          .style("stroke-width", 1.0)

        node.on("mouseover", showDetails)
          .on("mouseout", hideDetails)
          .on("mousemove", trackDetails)
          .on("click", onClick )
          .on("dblclick", onDoubleClick ) 

        node.exit().remove()

      # enter/exit display for links
      updateLinks = () ->
        markersG.selectAll("marker").data(["end"])
          .enter().append("marker")    
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .attr("fill", "#ddd")
          .append("path")
            .attr("d", "M0,-5L10,0L0,5");
            
        markersG.selectAll("marker_h").data(["end_h"])
          .enter().append("marker")    
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .attr("fill", "#555")
          .append("path")
            .attr("d", "M0,-5L10,0L0,5");
      
        link = linksG.selectAll("line.link")
          .data(curLinksData, (d) -> "#{d.source.id}_#{d.target.id}")
        link.enter().append("line")
          .attr("class", "link")
          .attr("stroke", "#ddd")
          .attr("stroke-opacity", 0.8)
          .attr("x1", (d) -> d.source.x)
          .attr("y1", (d) -> d.source.y)
          .attr("x2", (d) -> d.target.x)
          .attr("y2", (d) -> d.target.y)
          .attr("marker-end","url(#end)")

        link.exit().remove()
      
      drawCircles = () ->
        # we assume the nodes are ordered in ASC depth order
        curCenterX = curNodesData[0].x
        curCenterY = curNodesData[0].y
        tally = 0
        layers = []
        for node in curNodesData
          if node.lvl > tally
            [a,b] = [node.x - curCenterX, node.y - curCenterY]
            layers.push(Math.sqrt(Math.pow(a,2) + Math.pow(b,2)))
            tally = node.lvl
        #console.log(layers)
        layer = layersG.selectAll("circle")
          .data(layers)
        layer.enter().append("circle")
          .attr("fill","none")
          .attr("fill-opacity",0.0)
          .attr("stroke","lightgray")
          .attr("stroke-width","1px")
          .attr("stroke-dasharray","5,5")
          .attr("cx",width/2)
          .attr("cy",height/2)
          .attr("r",(d) -> d)

      
      # switches filter option to new filter
      ###
      setFilter = (newFilter) ->
        filter = newFilter
      ###
      
      # switches sort option to new sort
      ###
      setSort = (newSort) ->
        sort = newSort
      ###
      
      # tick function for force directed layout
      ###
      forceTick = (e) ->
        node
          .attr("cx", (d) -> d.x)
          .attr("cy", (d) -> d.y)

        link
          .attr("x1", (d) -> d.source.x)
          .attr("y1", (d) -> d.source.y)
          .attr("x2", (d) -> d.target.x)
          .attr("y2", (d) -> d.target.y)
      ###
      
      # tick function for radial layout
      # TODO: drawCircles is a post-processing procedure
      #       but it's creation is tightly integrated to d3.js
      #       How to decouple and delegate?
      tickFunc = (e) ->
        node.each(moveToLayout(e.alpha))

        node
          .attr("cx", (d) -> d.x)
          .attr("cy", (d) -> d.y)

        if e.alpha < 0.03
          force.stop()
          drawCircles()
          updateLinks()

      # Adjusts x/y for each node to
      # push them towards appropriate location.
      # Uses alpha to dampen effect over time.
      moveToLayout = (alpha) ->
        k = alpha * 0.1
        (d) ->
          centerNode = layout.getPlacement(d.id)
          d.x += (centerNode.x - d.x) * k
          d.y += (centerNode.y - d.y) * k

      # Mouseover tooltip function
      showDetails = (d,i) ->
        content = '<p class="main"><i>' + d.contentType + '</i><br>'
        content += d.title + '</p>'
        content += '<hr class="tooltip-hr">'
        content += '<p class="main">' + "level = " + d.lvl + '</p>'
        content += '<hr class="tooltip-hr">'
        content += '<p class="main">' + "Id = " + d.id + '</p>'
        #content += '<hr class="tooltip-hr">'  
        #content += '<p class="main">' + "cordination:" + '(' + d.x + ',' + d.y + ')' + '</span></p>'        
        
        tooltip.showTooltip(content,d3.event)

        # higlight connected links
        if link
          link.attr("stroke", (l) ->
            if l.source == d or l.target == d then "#555" else "#ddd"
            )
            .attr("stroke-opacity", (l) ->
              if l.source == d or l.target == d then 1.0 else 0.5
            )
            .attr("marker-end", (l) ->
              if l.source == d or l.target == d then "url(#end_h)" else "url(#end)"
            )

          # link.each (l) ->
          #   if l.source == d or l.target == d
          #     d3.select(this).attr("stroke", "#555")

        # highlight connected links' markers
        
        # highlight neighboring nodes
        # watch out - don't mess with node if search is currently matching
        tempd = d # node.each has to use 'd' as parameter...
        node.each (d) ->
          if (!d.searched and neighboring(tempd, d))
            neighbor = d3.select(this)
            neighbor.style("stroke", "#555")
                    .style("stroke-width", 2.0)
        # highlight the node being moused over
        if !d.searched
          d3.select(this).style("stroke","black").style("stroke-width", 2.0)

      # Mouseout function
      hideDetails = (d,i) ->
        tooltip.hideTooltip()
        # watch out - don't mess with node if search is currently matching
        node.each (d) ->
          if !d.searched
            neighbor = d3.select(this)
            neighbor.style("stroke", strokeFor(d))
                    .style("stroke-width", 1.0)
        if link
          link.attr("stroke", "#ddd")
            .attr("stroke-opacity", 0.8)
            .attr("marker-end", "url(#end)")


      trackDetails = (d,i) ->
        tooltip.updatePosition(d3.event)
      
      onClick = (d,i) ->
        analysisStore.loadData(d)
        
      onDoubleClick = (d,i) ->
        window.open d.url, "_blank"
          
      zoom = (d,i,e) ->
        node.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        link.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        layer.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        #alert("zooming")

      # Final act of Graph() function is to return the inner 'graph()' function.
      return graph
  
  )
  # close My.D3panel
  
  # Custom tooltip that activates only on click
  clickTooltip = Ext.extend(Ext.ToolTip,
    initTarget: (target) ->
      t = undefined
      if t = Ext.get(target)
        if @target
          tg = Ext.get(@target)
          @mun tg, "click", @onTargetOver, this
          
          #this.mun(tg, 'mouseover', this.onTargetOver, this);
          @mun tg, "mouseout", @onTargetOut, this
          @mun tg, "mousemove", @onMouseMove, this
        @mon t,
          
          #mouseover: this.onTargetOver,
          click: @onTargetOver
          mouseout: @onTargetOut
          mousemove: @onMouseMove
          scope: this

        @target = t
      @anchorTarget = @target  if @anchor
      return
      
  )
  
  visPanel = new graphPanel(
    title: 'SiTree Interactive Graph'
    region : 'center'
    contentEl: 'customd3'
    tbar: [
      {
        xtype: "button"
        text: "Change URL"
        handler: ->
          Ext.Msg.prompt "Welcome to SiTree!", "Enter URL:", initialize
      }
      { xtype: "tbfill" }
      {
        xtype: "button"
        id: "disp_legend"
        text: "Display legend"
        handler: ->
          colorLegend.show()
          colorLegend.update(colorLegendHTML)
          # update() can be called only after tooltip has rendered
      }
    ]
  )
  
  colorLegend = new clickTooltip(
    target: 'disp_legend'
    anchor: 'right'
    autoHide: false
    autoWidth: true
    closable: true
    html: null
  )   
  
  searchPanel = new Ext.grid.GridPanel(
    title: "Search & Result Panel"
    width: '100%'
    flex: 1
    frame: true
    ds: resultStore
    columns: [
      { header: "Node Id", width: 50, dataIndex: "Id" }
      # dataIndex corresponds to 'name' NOT 'mapping'
      { header: "Score", width: 80, sortable: true, dataIndex: "Score" }
      { header: "URL", width: 200, dataIndex: "URL" }
    ]
    sm: new Ext.grid.RowSelectionModel(
      singleSelect: true
      listeners:
        rowselect: (sm, row, rec) ->
          ig.highlight(rec.id)
          return
    )
    tbar: [
      {
        xtype: "textfield"
        id: 'keyword_input'
        hideLabel: false
        listeners:
          specialkey: (f, e) ->
            if e.getKey() is e.ENTER
              keyword = this.getValue()
              sendKeyword rooturl,keyword
            return
      }
      {
        xtype: "button"
        text: "Search"
        handler: ->
          keyword = Ext.getCmp('keyword_input').getValue()
          #Ext.Msg.alert "result", "input:" + keyword
          sendKeyword rooturl,keyword
          return
      }
    ]
    bbar: [
      { xtype:"tbfill" }
      {
        xtype: "button"
        text: "Reset highlight"
        handler: ->
          ig.resetHighlight()
          return
      }
    ]
  )
  
  analysisPanel = new Ext.grid.GridPanel(
    title: "Analysis Scores"
    width: '100%'
    flex: 1
    frame: true
    ds: analysisStore
    columns: [
      { header: "Rule", dataIndex: "Rule", width: 80}
      { header: "Score", dataIndex: "Score", width: 50}
      { header: "Description", dataIndex: "Description", width: 300}
    ]
  )
  
  eastPanel = new Ext.Panel(
    width: '30%'
    height: '100%'
    layout: 'vbox'
    region: 'east'
    frame: true
    items: [
      searchPanel
      analysisPanel
    ]
  )

  viewport = new Ext.Viewport(
    layout: 'border'
    autoshow: true;
    items: [
      visPanel
      eastPanel
    ]
  )
  
  showWelcome()
  return
