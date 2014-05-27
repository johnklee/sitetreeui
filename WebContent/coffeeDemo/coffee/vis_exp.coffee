
root = exports ? this

# Help with the placement of nodes
RadialPlacement = () ->
  # stores the key -> location values
  values = d3.map()
  # how much to separate each location by
  increment = 20
  # how large to make the layout
  radius = 0
  # where the center of the layout should be
  center = {"x":0, "y":0}
  # what angle to start at
  start = -120
  current = start
  maxDepth = 0

  # Given an center point, angle, and radius length,
  # return a radial position for that angle
  radialLocation = (center, angle, radius) ->
    x = (center.x + radius * Math.cos(angle * Math.PI / 180))
    y = (center.y + radius * Math.sin(angle * Math.PI / 180))
    {"x":x,"y":y}

  # Main entry point for RadialPlacement
  # Returns location for a particular key,
  # creating a new location if necessary.
  placement = (key) ->
    value = values.get(key)
    if !values.has(key)
      value = place(key)
    value

  # Gets a new location for input key
  place = (key, rad) ->
    value = radialLocation(center, current, rad)
    values.set(key,value)
    #console.log(value)
    current += increment
    value

  # Given a set of keys, perform some 
  # magic to create a two ringed radial layout.
  # Expects radius, increment, and center to be set.
  # If there are a small number of keys, just make
  # one circle.
  setKeys = (keys) ->
    # start with an empty values
    values = d3.map()
  
    # number of keys to go in first circle
    firstCircleCount = 360 / increment

    # if we don't have enough keys, modify increment
    # so that they all fit in one circle
    if keys.length < firstCircleCount
      increment = 360 / keys.length

    # set locations for inner circle
    firstCircleKeys = keys.slice(0,firstCircleCount)
    firstCircleKeys.forEach (k) -> place(k)

    # set locations for outer circle
    secondCircleKeys = keys.slice(firstCircleCount)

    # setup outer circle
    radius = radius + radius / 1.8
    increment = 360 / secondCircleKeys.length

    secondCircleKeys.forEach (k) -> place(k)

  setKeysEx = (nodedata) ->
    temp_radius = 0
    for d in [0..getMaxDepth(nodedata)]
      circleKeys = getKeysAtDepth(nodedata,d)
      increment = 360 / circleKeys.length
      if d == 0
        temp_radius = 0
      else if d == 1
        temp_radius = radius
      else
        temp_radius = 1.55*temp_radius
      console.log(temp_radius)
      circleKeys.forEach (k) -> place(k,temp_radius)

  getMaxDepth = (nodedata) ->
    max_depth = 1
    for n in nodedata
      if n.lvl > max_depth
        max_depth = n.lvl
    maxDepth = max_depth
    maxDepth
  
  getKeysAtDepth = (nodedata, depth) ->
    # nodesAtSameDepth is an array
    nodesAtSameDepth = (node for node in nodedata when node.lvl is "#{depth}")
    nodesAtSameDepth = (node.id for node in nodesAtSameDepth)
    nodesAtSameDepth
  
  placement.keys = (_) ->
    if !arguments.length
      return d3.keys(values)
    setKeys(_)
    placement

  placement.data = (_) ->
    if !arguments.length
      return d3.keys(values)
    setKeysEx(_)
    placement

  placement.center = (_) ->
    if !arguments.length
      return center
    center = _
    placement

  placement.radius = (_) ->
    if !arguments.length
      return radius
    radius = _
    placement

  placement.start = (_) ->
    if !arguments.length
      return start
    start = _
    current = start
    placement

  placement.increment = (_) ->
    if !arguments.length
      return increment
    increment = _
    placement

  return placement

Network = () ->
  # variables we want to access
  # in multiple places of Network
  width = 960
  height = 600
  # radial base radius & base increment
  r = 100
  inc = 18
  # allData will store the unfiltered data
  allData = []
  curLinksData = []
  curNodesData = []
  nodesMap = d3.map()
  linkByIndex = d3.map()
  invLinkByIndex = d3.map()
  # these will hold the svg groups for
  # accessing the nodes and links display
  nodesG = null;
  linksG = null;
  layersG = null;
  markersG = null;
  # these will point to the circles and lines
  # of the nodes and links
  node = null;
  link = null;
  layer = null;
  #marker = null;
  # variables to reflect the current settings
  # of the visualization
  layout = "radial"
  filter = "all"
  sort = "songs"
  # groupCenters will store our radial layout for
  # the group by artist layout.
  groupCenters = null;

  # our force directed layout
  force = d3.layout.force()
  # color function used to color nodes
  nodeColors = d3.scale.category20()
  # tooltip used to display details
  tooltip = Tooltip("vis-tooltip", 230)

  # charge used in artist layout
  charge = (node) -> -Math.pow(node.radius, 2.0) / 2

  # Starting point for network visualization
  # Initializes visualization and starts force layout
  network = (selection, data) ->
    # format our data
    allData = setupData(data)

    # create our svg and groups
    vis = d3.select(selection).append("svg")
      .attr("width", width)
      .attr("height", height)
      .call(d3.behavior.zoom().scaleExtent([1, 8]).on("zoom", zoom))
    layersG = vis.append("g").attr("id", "layers")
    linksG = vis.append("g").attr("id", "links")
    markersG = vis.append("defs").attr("id", "markers")
    nodesG = vis.append("g").attr("id", "nodes")

    # setup the size of the force environment
    force.size([width, height])

    #setLayout("force")
    #force.on("tick", radialTick).charge(charge)
    #setFilter("all")
    force.on("tick", radialTick)

    # perform rendering and start force layout
    update()

  # The update() function performs the bulk of the
  # work to setup our visualization based on the
  # current layout/sort/filter.
  #
  # update() is called everytime a parameter changes
  # and the network needs to be reset.
  update = () ->
    # filter data to show based on current filter settings.
    #curNodesData = filterNodes(allData.nodes)
    curNodesData = allData.nodes
    #curLinksData = filterLinks(allData.links, curNodesData)
    curLinksData = allData.links

    # sort nodes based on current sort and update centers for
    # radial layout
    ###
    if layout == "radial"
      artists = sortedArtists(curNodesData, curLinksData)
      updateCenters(artists)
    ###
    #originally expect an array
    #currently just passing all in, maybe truncate?
    groupCenters = RadialPlacement().center({"x":width/2, "y":height / 2})
        .radius(r).increment(inc).data(curNodesData)

    # reset nodes in force layout
    force.nodes(curNodesData)

    # enter / exit for nodes
    updateNodes()

    # always show links in force layout
    if layout == "force"
      force.links(curLinksData)
      updateLinks()
    else
      # reset links so they do not interfere with
      # other layouts. updateLinks() will be called when
      # force is done animating.
      force.links([])
      # if present, remove them from svg 
      if link
        link.data([]).exit().remove()
        link = null;

    # start me up
    force.start()

  # Public function to switch between layouts
  ###
  network.toggleLayout = (newLayout) ->
    force.stop()
    setLayout(newLayout)
    update()
  ###
  
  # Public function to switch between filter options
  ###
  network.toggleFilter = (newFilter) ->
    force.stop()
    setFilter(newFilter)
    update()
  ###
  
  # Public function to switch between sort options
  ###
  network.toggleSort = (newSort) ->
    force.stop()
    setSort(newSort)
    update()
  ###
  
  # Public function to update highlighted nodes
  # from search
  ###
  network.updateSearch = (searchTerm) ->
    searchRegEx = new RegExp(searchTerm.toLowerCase())
    node.each (d) ->
      element = d3.select(this)
      match = d.name.toLowerCase().search(searchRegEx)
      if searchTerm.length > 0 and match >= 0
        element.style("fill", "#F38630")
          .style("stroke-width", 2.0)
          .style("stroke", "#555")
        d.searched = true
      else
        d.searched = false
        element.style("fill", (d) -> nodeColors(d.artist))
          .style("stroke-width", 1.0)
  ###
  
  network.updateData = (newData) ->
    allData = setupData(newData)
    link.remove()
    node.remove()
    update()

  # called once to clean up raw data and switch links to
  # point to node instances
  # Returns modified data
  setupData = (data) ->
    # initialize circle radius scale
    countExtent = d3.extent(data.nodes, (d) -> d.lvl)
    circleRadius = d3.scale.sqrt().range([3, 12]).domain(countExtent)

    # id's -> node objects
    nodesMap  = mapNodes(data.nodes)
    # console.log(nodesMap)

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
      
    data.nodes.forEach (n) ->
      # set initial x/y to values within the width/height
      # of the visualization
      n.x = randomnumber=Math.floor(Math.random()*width)
      n.y = randomnumber=Math.floor(Math.random()*height)
      # add radius to the node so we can use it later
      # radius ~ node circle's size
      n.radius = circleRadius(2)
      # calculate subgraph collapsible
      n.collapsed = false;
      #n.subgraph = calcSubgraph(n.id)

    data

  # Helper function to map node id's to node objects.
  # Returns d3.map of ids -> nodes
  mapNodes = (nodes) ->
    nodesMap = d3.map()
    nodes.forEach (n) ->
      nodesMap.set(n.id, n)
    nodesMap

  # Helper function to find node's subgraph
  # Returns an array of node id's
  calcSubgraph = (nodeid) ->
    subgraph = []
    # check if indegree disqualifies this node
    if invLinkByIndex.has(nodeid)
      test = true;
      for i in invLinkByIndex.get(nodeid)
        test = test & checkShareParent(i,nodeid)
        if (test == false)
          # disqualified, discard any subgraph of this node
          return []
    # node qualified, check its subgraph
    if linkByIndex.has(nodeid)
      for i in linkByIndex.get(nodeid)
        if nodesMap.get(i).lvl > nodesMap.get(nodeid).lvl
          jQuery.union(subgraph,calcSubgraph(i))
    subgraph.push(nodeid)
    subgraph
  
  # Helper function to check if two node share same parent
  # parent must be either the collapse root or STRICTLY below its paths
  checkShareParent = (a, b) ->
    test = true;
    return test
  
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
      groupCenters = RadialPlacement().center({"x":width/2, "y":height / 2 - 100})
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
  
  # enter/exit display for nodes
  updateNodes = () ->
    node = nodesG.selectAll("circle.node")
      .data(curNodesData, (d) -> d.id)

    node.enter().append("circle")
      .attr("class", "node")
      .attr("cx", (d) -> d.x)
      .attr("cy", (d) -> d.y)
      .attr("r", (d) -> d.radius)
      .style("fill", (d) -> nodeColors(d.id))
      .style("stroke", (d) -> strokeFor(d))
      .style("stroke-width", 1.0)

    node.on("mouseover", showDetails)
      .on("mouseout", hideDetails)

    # when click node -> run onClick function
    node.on("click", onClick ) 

    node.on("dblclick", onDoubleClick ) 


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
    console.log(layers)
    layer = layersG.selectAll("circle")
      .data(layers)
    layer.enter().append("circle")
      .attr("fill","none")
      .attr("fill-opacity",0.0)
      .attr("stroke","gray")
      .attr("stroke-width","1px")
      .attr("stroke-dasharray","10,10")
      .attr("cx",width/2)
      .attr("cy",height/2)
      .attr("r",(d) -> d)
  
  # switches force to new layout parameters
  ###
  setLayout = (newLayout) ->
    layout = newLayout
    if layout == "force"
      force.on("tick", forceTick)
        .charge(-200)
        .linkDistance(50)
    else if layout == "radial"
      force.on("tick", radialTick)
        .charge(charge)
  ###
  
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
  radialTick = (e) ->
    node.each(moveToRadialLayout(e.alpha))

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
  moveToRadialLayout = (alpha) ->
    k = alpha * 0.1
    (d) ->
      centerNode = groupCenters(d.id)
      d.x += (centerNode.x - d.x) * k
      d.y += (centerNode.y - d.y) * k


  # Helper function that returns stroke color for
  # particular node.
  strokeFor = (d) ->
    d3.rgb(nodeColors(d.id)).darker().toString()

  # Mouseover tooltip function
  showDetails = (d,i) ->
    content = '<p class="main">' + d.name + '</span></p>'
    content += '<hr class="tooltip-hr">'
    content += '<p class="main">' + "level = " + d.lvl + '</span></p>'
    content += '<hr class="tooltip-hr">'
    content += '<p class="main">' + "Id = " + d.id + '</span></p>'
    content += '<hr class="tooltip-hr">'  
    content += '<p class="main">' + "cordination:" + '(' + d.x + ',' + d.y + ')' + '</span></p>'      
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
    node.style("stroke", (n) ->
      if (n.searched or neighboring(d, n)) then "#555" else strokeFor(n))
      .style("stroke-width", (n) ->
        if (n.searched or neighboring(d, n)) then 2.0 else 1.0)
  
    # highlight the node being moused over
    d3.select(this).style("stroke","black")
      .style("stroke-width", 2.0)

  # Mouseout function
  hideDetails = (d,i) ->
    tooltip.hideTooltip()
    # watch out - don't mess with node if search is currently matching
    node.style("stroke", (n) -> if !n.searched then strokeFor(n) else "#555")
      .style("stroke-width", (n) -> if !n.searched then 1.0 else 2.0)
    if link
      link.attr("stroke", "#ddd")
        .attr("stroke-opacity", 0.8)
        .attr("marker-end", "url(#end)")


  onClick = (d,i) ->
    if !(d.collapsed)
      # calculate subgraph and store in
      console.log("not collapsed")
      
    
  onDoubleClick = (d,i) ->
    # base node.url load corresponding page

    # open new tab & load page 
      window.open "http://stackoverflow.com", "_blank" 
    # overide current page
      #window.location.replace("http://stackoverflow.com"); 
      
  zoom = (d,i,e) ->
    node.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    link.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    layer.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    #alert("zooming")

  # Final act of Network() function is to return the inner 'network()' function.
  return network

# Activate selector button
activate = (group, link) ->
  d3.selectAll("##{group} a").classed("active", false)
  d3.select("##{group} ##{link}").classed("active", true)

$ ->
  myNetwork = Network()

  ###
  d3.selectAll("#layouts a").on "click", (d) ->
    newLayout = d3.select(this).attr("id")
    activate("layouts", newLayout)
    myNetwork.toggleLayout(newLayout)

  d3.selectAll("#filters a").on "click", (d) ->
    newFilter = d3.select(this).attr("id")
    activate("filters", newFilter)
    myNetwork.toggleFilter(newFilter)

  d3.selectAll("#sorts a").on "click", (d) ->
    newSort = d3.select(this).attr("id")
    activate("sorts", newSort)
    myNetwork.toggleSort(newSort)

  $("#song_select").on "change", (e) ->
    songFile = $(this).val()
    d3.json "data/#{songFile}", (json) ->
      myNetwork.updateData(json)
  
  $("#search").keyup () ->
    searchTerm = $(this).val()
    myNetwork.updateSearch(searchTerm)
  ###
  
  d3.json "data/sitehierarchy.json", (json) ->
    myNetwork("#vis", json)
