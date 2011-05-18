/**
 * Base class for CMDBuild viewports
 * 
 * @class CMDBuild.MainViewport
 * @extends Ext.Viewport
 */
CMDBuild.MainViewport = Ext.extend(Ext.Viewport, {
	colorsConst: CMDBuild.Constants.colors.blue,
	defaults: {
		collapsible: false,
		animCollapse: false,
		animate: false
	},
	/**
	 * @cfg {***********************} modules
	 * Modules included in the application
	 */
	modules: [],
	/**
	 * @cfg {***********************} trees
	 * Trees included in the application
	 */
	trees: [],
	controllerType: "MainViewportController",
	initComponent : function() {
		Ext.apply(this, {
				layout: 'border',
				frame: true,
				border: false,
				listeners: {
					render: function() {
						var controllerType = this.controllerType;						
						new CMDBuild[controllerType]({viewport:this});
					}
				},
				items:[{
					frame: false,
					border: false,
		        	region:'north',
		        	style: {'border-bottom': '1px '+this.colorsConst.border+' solid'},
		        	id: 'header_panel',
		            contentEl: 'header',
		            height: 45
		        },{
		        	frame: false,
					border: true,
					region:'west',
					collapseMode: 'mini',
		            id:'menu_accordion_panel',            
		            split: true,		            
		            width: 200,
		            layout:'accordion',
		            activeOnTop: false,
		            items: this.trees,
		            margins: '4 0 4 4',
			        items: this.trees
		        },{
		    		frame: false,
					border: false,
					id: 'content_panel',
					region: 'center',
					layout: 'card',
					activeItem: 0,
					items: this.modules,
					margins: '4 4 4 0'
				},{
		        	frame: false,
					border: false,
			        region : "south",
			        id: 'footer_panel',
			        contentEl: "footer",
			        style: {'border-top': '1px '+this.colorsConst.border+' solid'},
					height: 16
		        }]
			});
		CMDBuild.MainViewport.superclass.initComponent.call(this);
		Ext.getDom('header').style.display = "block";
		Ext.getDom('footer').style.display = "block";
	},

	getTreePanels: function() {
		return this.trees;
	},
	
	getModByName: function(name) {
		for (var i=0, l=this.modules.length; i<l; ++i) {
			var m = this.modules[i];
			if (typeof m != "undefined" && m.NAME == name) {
				return m;
			}
		}
		return null;
	},
	
	getTreeByName: function(name) {
		for (var i=0, l=this.trees.length; i<l; ++i) {
			var t = this.trees[i];
			if (typeof t != "undefined" && t.NAME == name) {
				return t;
			}
		}
		return null;
	}
});
Ext.reg('mainviewport', CMDBuild.MainViewport);