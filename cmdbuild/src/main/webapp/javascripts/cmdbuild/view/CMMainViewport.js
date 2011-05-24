(function() {
	Ext.define("CMDBuild.view.CMMainViewport", {
		extend: "Ext.Viewport",
		hideMode: "offsets",
		layout: 'border',
		renderTo: Ext.getBody(),
		cmPanels: [],
		cmAccordions: [],
		controllerType: "MainViewportController",

		initComponent : function() {
			this.cmAccordions = Ext.create("Ext.panel.Panel", {
				region: 'west',
				split: true,
				layout: "accordion",
				items: this.cmAccordions,
				width: 200,
				hideMode: "offsets"
			});

			this.cmPanels = new Ext.panel.Panel({
				region: 'center',
				layout: "card",
				items: this.cmPanels,
				hideMode: "offsets"
			});

			this.items = [this.cmAccordions,this.cmPanels]
			CMDBuild.view.CMMainViewport.superclass.initComponent.call(this, arguments);
		},
		/*
		 * Take a function as parameter
		 * iterate over the cmAccordions and call the given
		 * function with the current accordion as parameter
		 */
		foreachAccordion: function(fn, scope) {
			if (typeof fn == "undefined") {
				throw "CMMainViewport.foreachAccordion must have a function as parameter";
			}
			this.cmAccordions.items.each(fn, scope)
		},
		/*
		 * Take a function as parameter
		 * iterate over the cmPanels and call the given
		 * function with the current accordion as parameter
		 */
		foreachPanel: function(fn, scope) {
			if (typeof fn == "undefined") {
				throw "CMMainViewport.foreachPanel must have a function as parameter";
			}
			this.cmPanels.items.each(fn, scope)
		},
		/*
		 * Search in the cmPanels the given name
		 * and bring it to front
		 */
		bringTofrontPanelByCmName: function(cmName, params) {
			var p = this.cmPanels.items.findBy(function(panel) {
				return panel.cmName == cmName;
			});

			if ((typeof p.afterBringToFront == "function" && p.afterBringToFront()) 
					|| typeof p.afterBringToFront == "undefined") {

				this.cmPanels.layout.setActiveItem(p.id);
				p.fireEvent("CM_iamtofront", params);
			}
		},

		deselectAccordionByName: function(cmName) {
			var a = this.cmAccordions.items.findBy(function(accordion) {
				return accordion.cmName == cmName;
			});

			var sm = a.getSelectionModel();
			sm.deselect(sm.getSelection());
		}
	});
})();


//Ext.define("CMDBuild.MainViewport", {
//	extend: "Ext.Viewport",
//
//	colorsConst: CMDBuild.Constants.colors.blue,
//	defaults: {
//		collapsible: false,
//		animCollapse: false,
//		animate: false
//	},
//	/**
//	 * @cfg {***********************} modules
//	 * Modules included in the application
//	 */
//	modules: [],
//	/**
//	 * @cfg {***********************} trees
//	 * Trees included in the application
//	 */
//	trees: [],
//	controllerType: "MainViewportController",
//	initComponent : function() {
//		Ext.apply(this, {
//				layout: 'border',
//				frame: true,
//				border: false,
//				listeners: {
//					render: function() {
////						var controllerType = this.controllerType;
////						new CMDBuild[controllerType]({viewport:this});
//					}
//				},
//				items:[{
//					frame: false,
//					border: false,
//		        	region:'north',
//		        	style: {'border-bottom': '1px '+this.colorsConst.border+' solid'},
//		        	id: 'header_panel',
//		            contentEl: 'header',
//		            height: 45
//		        },{
//		        	frame: false,
//					border: true,
//					region:'west',
//					collapseMode: 'mini',
//		            id:'menu_accordion_panel',            
//		            split: true,		            
//		            width: 200,
//		            layout:'accordion',
//		            activeOnTop: false,
//		            items: this.trees,
//		            margins: '4 0 4 4',
//			        items: this.trees
//		        },{
//		    		frame: false,
//					border: false,
//					id: 'content_panel',
//					region: 'center',
//					layout: 'card',
//					activeItem: 0,
//					items: this.modules,
//					margins: '4 4 4 0'
//				},{
//		        	frame: false,
//					border: false,
//			        region : "south",
//			        id: 'footer_panel',
//			        contentEl: "footer",
//			        style: {'border-top': '1px '+this.colorsConst.border+' solid'},
//					height: 16
//		        }]
//			});
//		CMDBuild.MainViewport.superclass.initComponent.call(this);
//		Ext.getDom('header').style.display = "block";
//		Ext.getDom('footer').style.display = "block";
//	},
//
//	getTreePanels: function() {
//		return this.trees;
//	},
//	
//	getModByName: function(name) {
//		for (var i=0, l=this.modules.length; i<l; ++i) {
//			var m = this.modules[i];
//			if (typeof m != "undefined" && m.NAME == name) {
//				return m;
//			}
//		}
//		return null;
//	},
//	
//	getTreeByName: function(name) {
//		for (var i=0, l=this.trees.length; i<l; ++i) {
//			var t = this.trees[i];
//			if (typeof t != "undefined" && t.NAME == name) {
//				return t;
//			}
//		}
//		return null;
//	}
//});