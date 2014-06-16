sitetreeui
==========

SD 2014 Project - SiTree

This is the UI of SiTree.
Usage: Import this into Eclipse.
Copy the folder 'phantomJS' into C:\

Launch Tomcat Server and go to localhost:8080/SiteTreeUI/

Structure
---------

WebContent
|__ css
|__ demos (This is for experiment and sandbox environment)
	|___ coffeeDemo (experimenting with Coffeescript)
	|___ d3 (stuff related to d3.js and mashup with extJS)
		|___ mashup: extend extJS panel to hold d3.js SVG
		|___ mashup_combo: do layout
		|___ prototype: final prototype
	|___ extjsDemo (stuff related to extJS)
	|___ ...
|___js
|___sitetreeui (holds the release version coffeescript)
|___index.html

Usage
-----

Enter URL like http://google.com/
Wait for crawling and data to load.

2014.06.15
