(function() {
	var TRUE = "1";

	Ext.define("CMDBuild.view.management.workflow.widgets.CMLinkCardsGrid", {
		extend: "CMDBuild.view.management.common.CMCardGrid",

		initComponent: function() {
			if (this.multiSelect) {
				this.selType = "checkboxmodel"
			}

			this.callParent(arguments);
		},

		syncSelections: function(s) {
			var sm = this.getSelectionModel();
			sm.suspendEvents();
			sm.clearSelections();
			sm.resumeEvents();

			if (!this.noSelect && s) {
				var store = this.getStore();
				for (var i = 0, l = s.length; i<l; ++i) {
					var cardId = s[i];
					this.selectByCardId(cardId);
				}
			}
		},

		selectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().select(recIndex, true);
			}
		},

		deselectByCardId: function(cardId) {
			var recIndex = this.getStore().find("Id", cardId);
			if (recIndex >= 0) {
				this.getSelectionModel().deselect(recIndex, true);
			}
		}
	});

	Ext.define("CMDBuild.view.management.workflow.widgets.CMLinkCards", {
		extend: "Ext.panel.Panel",
		constructor: function(c) {
			this.widgetConf = c.widget;
			this.activity = c.activity.raw || c.activity.data;
			this.clientForm = c.clientForm;

			this.callParent([this.widgetConf]); // to apply the conf to the panel
		},

		setModel: function(m) {
			this.model = m;
		},

		initComponent: function() {
			var c = this.widgetConf,
				selType = this.NoSelect==TRUE ? "rowmodel" : "checkboxmodel",
				multipleSelect = this.NoSelect!=TRUE && !this.SingleSelect;

			this.grid = new CMDBuild.view.management.workflow.widgets.CMLinkCardsGrid({
				autoScroll : true,
				filterSubcategory : c.identifier,
				selType: selType,
				multiSelect: multipleSelect,
				noSelect: this.NoSelect,
				hideMode: "offsets",
				region: "center",
				border: false
			});

			Ext.apply(this, {
				items: [this.grid],
				layout: "border",
				border: false,
				frame: false
			});

			this.callParent(arguments);

			this.grid.getSelectionModel().on("select", function(sm, s) {
				this.fireEvent("select", s.get("Id"));
			}, this);

			this.grid.getSelectionModel().on("deselect", function(sm, s) {
				this.fireEvent("deselect", s.get("Id"));
			}, this);

			this.grid.on("beforeload", onBeforeLoad, this)
			this.grid.on("load", onLoad, this);
		},

		cmActivate: function() {
			this.mon(this.ownerCt, "cmactive", function() {
				this.ownerCt.bringToFront(this);
			}, this, {single: true});

			this.ownerCt.cmActivate();
		},

		updateGrid: function(classId, cqlParams) {
			this.grid.CQL = cqlParams;
			this.grid.updateStoreForClassId(classId);
		},

		getTemplateResolver: function() {
			return this.templateResolver || new CMDBuild.Management.TemplateResolver({
				clientForm: this.clientForm,
				xaVars: this.widgetConf,
				serverVars: this.activity
			});
		},

		syncSelections: function(s) {
			if (this.model) {
				this.grid.syncSelections(this.model.getSelections());
			}
		}
	});

	function onBeforeLoad() {
		this.model.freeze();
	}

	function onLoad() {
		this.model.defreeze();
		this.syncSelections();
	}

//CMDBuild.Management.LinkCards = Ext.extend(CMDBuild.Management.BaseExtendedAttribute, {
//	singleSelect: false,
//	templateResolverIsBusy: false,
//	map: null,
//	
//	// override BaseExtendedAttribute
//	initialize: function(extAttrDef) {
//		this.outputName = extAttrDef.outputName;
//		this.singleSelect = extAttrDef.SingleSelect ? true : false;
//		this.noSelect = extAttrDef.NoSelect ? true : false;
//		
//		buildCardListGrid.call(this, extAttrDef);
//		buildMapPanel.call(this, extAttrDef);
//		buildController.call(this);
//		updateMap.call(this, extAttrDef);
//		
//		return buildConfiguration.call(this);
//	},
//	// override BaseExtendedAttribute
//	onExtAttrShow: function(extAttr) {
//		this.fireEvent("CM_show", extAttr);
//	},
//	// override BaseExtendedAttribute
//	onSave: function() {
//		this.fireEvent("CM_save");
//	},
//	// override BaseExtendedAttribute
//	isBusy: function() {
//		return this.templateResolverIsBusy || CMDBuild.Management.LinkCards.superclass.isBusy.call(this);
//	},
//	
//	hasMap: function() {
//		return this.map != null;
//	},
//	
//	getMap: function() {
//		return this.map;
//	},
//
//	initGrid: function(classId, cardReqParams) {
//		if (this.cardGrid.isFirstLoad) {
//			var grid = this.cardGrid;
//			CMDBuild.Management.FieldManager.loadAttributes(
//				classId
//				, function(classAttrs) {
//					var eventParams = {
//						classId : classId,
//						classAttributes : classAttrs
//					};
//					grid.initForClass(eventParams, cardReqParams);
//				});
//		} else {
//			this.cardGrid.loadCards();
//		}
//	}
//});
//
//function buildCardListGrid(extAttrDef) {
//	this.cardGrid = new CMDBuild.Management.LinkCards.LinkCardsCardGrid({
//		subscribeToEvents : false,
//		autoScroll : true,
//		noSelect : this.noSelect,
//		filterSubcategory : this.identifier,
//		singleSelect: this.singleSelect
//	});
//}
//
//function buildMapPanel(extAttrDef) {
//	var theMapIsToSet = (extAttrDef.Map == "enabled" && CMDBuild.Config.gis.enabled);
//	if (theMapIsToSet) {
//		buildMap.call(this, extAttrDef);
//		this.mapButton = new Ext.Button({
//			text: CMDBuild.Translation.management.modcard.tabs.map,
//			iconCls: 'map',
//			scope: this,
//			handler: function() {
//				this.fireEvent("CM_toggle_map");
//			}
//		});
//		
//		this.mapPanel = new Ext.Panel({
//			xtype: "panel",
//			layout: "border",
//			frame: false,
//			border: false,
//			items: [this.map, this.layersTree],
//			baseCls: CMDBuild.Constants.css.bottom_border_blue
//	    });
//		
//		
//	}
//}
//
//function buildMap(extAttrDef) {
//	var map = CMDBuild.Management.MapBuilder.buildMap();
//	
//	this.getMap = function () {
//		return map;
//	};
//	
//	var zoomSlider = new GeoExt.ZoomSlider({
//        map: map,
//        vertical: true,
//        height: 100,
//        x: 10,
//        y: 20,
//        aggressive: false,                                                                                                                                                   
//        plugins: new GeoExt.ZoomSliderTip({
//            template: "<div>Zoom Level: {zoom}</div>"
//        })
//    });
//	
//	var lon = extAttrDef.StartMapWithLongitude || CMDBuild.Config.gis['center.lon'];
//	var lat = extAttrDef.StartMapWithLatitude || CMDBuild.Config.gis['center.lat'];
//	var center = new OpenLayers.LonLat(lon,lat);
//	
//	this.map = new GeoExt.MapPanel({
//		map: map,
//	    center: center.transform(new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
//	    zoom: extAttrDef.StartMapWithZoom || CMDBuild.Config.gis.initialZoomLevel,
//	    region: 'center',
//	    border: false,
//		items: [zoomSlider]
//	});
//	
//	this.layersTree = new Ext.tree.TreePanel({
//		collapsible: true,
//		collapsed: false,
//		margins: "0 0 0 0",
//		cmargins: "5 5 0 5",
//		autoScroll: true,
//		region: "east",
//		split: true,
//		width: 200,
//		loader: new Ext.tree.TreeLoader({
//            // applyLoader has to be set to false to not interfer with loaders
//            // of nodes further down the tree hierarchy
//            applyLoader: false
//        }),
//        root: {	            
//            expanded: true,
//            children: [{
//    	        nodeType: "gx_baselayercontainer",
//    	        expanded: true,
//    	        layerStore: this.map.layers
//    	    },{
//    	        nodeType: "gx_overlaylayercontainer",
//    	        expanded: true,
//    	        layerStore: this.map.layers
//    	    }]
//        },
//        rootVisible: false,
//	    border: false
//	});		
//}
//
//function updateMap(extAttrDef) {
//	if (this.hasMap()) {
//		this.getMap().update({
//			geoAttributes: CMDBuild.GeoUtils.getGeoAttributes(extAttrDef.ClassId),
//			classId: extAttrDef.ClassId
//		}, withEditLayer = false);
//	}
//}
//
//function buildController() {
//	var controller = new CMDBuild.Management.LinkCardsController(this);
//	this.getController = function() {
//		return controller;
//	};
//	
//	if (this.hasMap()) {
//		controller.buildCardListMapController(view=this.getMap(), ownerController=this);
//	}
//}
//
//function buildConfiguration() {
//	var widgetConfiguration = {};
//	
//	if (this.map) {
//		widgetConfiguration  = {
//			layout: "card",
//			hideMode: "offsets",
//			activeItem: 0,
//			skipTitle: true,
//			items: [this.cardGrid, this.mapPanel],
//			tbar: ['->', this.mapButton]
//		};
//	} else {
//		widgetConfiguration = {
//			layout : 'fit',
//			items : [ this.cardGrid ]
//		};
//	}
//	
//	return widgetConfiguration;	
//}
//
//Ext.reg("linkCards", CMDBuild.Management.LinkCards);
})();