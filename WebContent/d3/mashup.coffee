Ext.onReady ->


  # global 
  toolbar = undefined;
  vis = undefined;
  viewport = undefined;
  rooturl = undefined;
  searchdata = undefined;


    # example of custom renderer function
  change = (val) ->
    if val > 0
      return "" + val + ""
    else return "" + val + ""  if val < 0
    val
  
  # example of custom renderer function
  pctChange = (val) ->
    if val > 0
      return "" + val + "%"
    else return "" + val + "%"  if val < 0
    val

  testData = [
    [
      "3m Co"
      71.72
      0.02
      0.03
      "9/1 12:00am"
    ]
    [
      "Alcoa Inc"
      29.01
      0.42
      1.47
      "9/1 12:00am"
    ]
    [
      "Altria Group Inc"
      83.81
      0.28
      0.34
      "9/1 12:00am"
    ]
  ]
  ds = new Ext.data.Store(reader: new Ext.data.ArrayReader({}, [
    {
      name: "company"
    }
    {
      name: "price"
      type: "float"
    }
    {
      name: "change"
      type: "float"
    }
    {
      name: "pctChange"
      type: "float"
    }
    {
      name: "lastChange"
      type: "date"
      dateFormat: "n/j h:ia"
    }
  ]))
  ds.loadData testData


  colModel = new Ext.grid.ColumnModel([
    {
      id: "company"
      header: "Company"
      width: 160
      sortable: true
      locked: false
      dataIndex: "company"
    }
    {
      header: "Price"
      width: 75
      sortable: true
      renderer: Ext.util.Format.usMoney
      dataIndex: "price"
    }
    {
      header: "Change"
      width: 75
      sortable: true
      renderer: change
      dataIndex: "change"
    }
    {
      header: "% Change"
      width: 75
      sortable: true
      renderer: pctChange
      dataIndex: "pctChange"
    }
    {
      header: "Last Updated"
      width: 85
      sortable: true
      renderer: Ext.util.Format.dateRenderer("m/d/Y")
      dataIndex: "lastChange"
    }
  ])  

  
  store = new Ext.data.JsonStore(

    autoDestroy: true
    url: "/SiteTreeUI/search"
    storeId: "SearchStore"
    autoLoad: true
    idProperty: "message"
    root: "message"
    fields: ["message"]
    #proxy: new Ext.data.MemoryProxy(searchdata),
    #proxy: new Ext.data.ScriptTagProxy(url: "http://extjs.com/forum/topics-browse-remote.php")
  )

  # functions 
  helloWorld = ->
    Ext.Msg.alert "Info", "Another Hello World!"
    return
  onMenuItem = (item) ->
    Ext.Msg.alert "Info", item.text
    return
  initialize = (btn, text) ->
    if btn is "ok"
      rooturl = text
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
 

  loadDataFromServer = (url , keyword) ->
    oReq = new XMLHttpRequest()
    oReq.onload = reqListener
    oReq.open "get", "/SiteTreeUI/search?url=" + url + "&keyword=" + keyword , true
    oReq.send()
    return

  reqListener = ->
    console.log @responseText
    Ext.Msg.alert "result", @responseText
    #searchPanel.update(@responseText)
    searchdata = JSON.parse( @responseText )
    #alert(searchdata.message)
    Ext.getCmp('lb1').update("Message:" + searchdata.message )
    Ext.getCmp('lb2').update("Error:" + searchdata.error)
  
    return



  searchButton = new Ext.Button(
    colspan: 1
    text: "Search"
    handler: ->
      keyword = Ext.getCmp('msg').getValue()
      #Ext.Msg.alert "result", "input:" + keyword
      loadDataFromServer(rooturl, keyword)
      return
  )



 

  searchPanel = new Ext.Panel(
    region: "east"
    title: "Search ＆ Result Panel"
    width: 300
    frame: true
    layout:'table'
    layoutConfig:
      columns:2
      row:4
    items: [
      {
        id:'msg'
        columns:1
        fieldLabel: 'Name'
        xtype: "textfield"
        hideLabel: false
        flex: 2
        listeners:
          specialkey: (f, e) ->
            if e.getKey() is e.ENTER
              keyword = this.getValue()
              loadDataFromServer(rooturl, keyword)
            return
      }
      searchButton
      {
          id:'lb1'
          colspan:2
          xtype: 'label',
          forId: 'myFieldId',
          text: '',
          margins: '0 0 0 10'
          listeners:
            render: (c) ->
              c.getEl().on "click", (->
                alert(searchdata.message)
                return
              ), c
      }
      {
          id:'lb2'
          colspan:2
          xtype: 'label',
          forId: 'myFieldId',
          text: '',
          margins: '0 0 0 10'
          listeners:
            render: (c) ->
              c.getEl().on "click", (->
                alert(searchdata.error)
                return
              ), c 
      }
      {
          id: 'gp'
          colspan: 3
          xtype: 'grid' 
          ds: ds
          cm: colModel
          columnWidth: 0.6
          sm: new Ext.grid.RowSelectionModel(
            singleSelect: true
            listeners:
              rowselect: (sm, row, rec) ->
                alert("this is row:" + row)
                return
          )

          autoExpandColumn: "company"
          height: 350
          title: "Company Data"
          border: true
      }
    ]
  )

  




  vis = new Myd3panel(
    title: 'in-page d3 demo'
    region : 'center'
    contentEl: 'customd3'
  )


  viewport = new Ext.Viewport(
    layout: 'border'
    autoshow: true;
    items: [
      vis
      searchPanel
    ]
  )
  Ext.Msg.prompt "Welcome to SiTree!", "Enter URL:", initialize

 # store.load params:
 #   start: 0
 #   limit: 25

  return
