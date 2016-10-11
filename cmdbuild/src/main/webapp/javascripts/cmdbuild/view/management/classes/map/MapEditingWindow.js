(function() {

	var ICON_CLS = {
		"POINT" : "mapFeaturePoint",
		"LINESTRING" : "mapFeatureLine",
		"POLYGON" : "mapFeaturePolygon"
	};

	Ext.define("CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate", {
		addFeatureButtonHasBeenToggled : Ext.emptyFn,
		removeFeatureButtonHasBeenClicked : Ext.emptyFn,
		geoAttributeMenuItemHasBeenClicked : Ext.emptyFn
	});

	/**
	 * @class CMDBuild.view.management.map.MapEditingWindow
	 * @extends Ext.Window
	 * 
	 * This window looks like an external toolbar (Photoshop style) After the
	 * creation is possible to say to the window to show the tools for a
	 * specific geometry type
	 * 
	 * The window can be shown only if the owner panel is visible
	 */
	Ext.define("CMDBuild.view.management.map.CMMapEditingToolsWindow",
			{
				extend : "Ext.Window",

				editingControls : {},
				layers : {},

				translation : CMDBuild.Translation,

				mixins : {
					delegable : "CMDBuild.core.CMDelegable"
				},

				owner : {
					getPosition : function() {
						return [ 0, 0 ];
					}
				},

				constructor : function() {
					this.mixins.delegable.constructor.call(this,
							"CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate");
					this.callParent(arguments);
				},

				initComponent : function() {

					this.closable = false;
					this.hideBorders = true;
					this.resizable = false;
					this.frame = false;
					this.interactionDocument.observe(this);

					this.layout = {
						type : 'hbox',
						padding : '2',
						align : 'stretch'
					};

					this.geoAttrMenuButton = new Ext.Button({
						text : this.translation.management.modcard.gis.geo_attributes,
						menu : new Ext.menu.Menu({
							items : []
						})
					});

					this.addButton = new Ext.Button({
						text : this.translation.add,
						creationControl : undefined,
						iconCls : 'add',
						enableToggle : false,
						allowDepress : true,
						disabled : true,
						scope : this,
						handler : function(button) {
							this.callDelegates("addFeatureButtonHasBeenToggled");
							button.disable();
						}
					});

					this.removeButton = new Ext.Button({
						text : this.translation.remove,
						iconCls : 'delete',
						scope : this,
						disabled : true,
						handler : function() {
							this.callDelegates("removeFeatureButtonHasBeenClicked");
						}
					});

					this.callParent(arguments);
				},

				refresh : function() {
					// refreshing the edit combo that has to be filled
					// with the layers postgis that are owned by the
					// class
					var card = this.interactionDocument.getCurrentCard();
					if (!card) {
						return;
					}
					var currentClassName = card.className;
					var currentCardId = card.cardId;
					var me = this;
					this.interactionDocument.getAllLayers(function(layers) {
						me.refreshAllLayers(layers, currentClassName, currentCardId);
					}, this);
				},

				refreshAllLayers : function(layers, currentClassName, currentCardId) {
					if (!currentCardId) {
						return;
					}
					this.removeAllLayerBinding();
					for (var i = 0; i < layers.length; i++) {
						var layer = layers[i];
						var visible = this.interactionDocument.isVisible(layer, currentClassName, currentCardId);
						if (visible && !this.interactionDocument.isGeoServerLayer(layer)) {
							this.addLayer(layer);
						}
					}
				},
				onRender : function() {
					this.callParent(arguments);
					this.interactionDocument.setNoZoom(true);
					this.add([ this.geoAttrMenuButton, this.addButton, this.removeButton ]);
				},

				show : function() {
					if (this.geoAttrMenuButton.menu.items.length > 0) {
						this.interactionDocument.setEditing(true);
						this.callParent(arguments);

						var firstItemOfMenu = this.geoAttrMenuButton.menu.items.first();
						if (firstItemOfMenu) {
							this.onAddMenuitemSelect.call(this, firstItemOfMenu);
						}
					}
				},
				closeAllEditings : function() {
					this.interactionDocument.setEditing(false);
					var card = this.interactionDocument.getCurrentCard();
					var currentClassName = card.className;
					for ( var key in this.layers) {
						var geoLayer = this.interactionDocument.getGeoLayerByName(key);
						if (!geoLayer) {
							continue;
						}
						var adapter = geoLayer.get("adapter");
						if (adapter && adapter.closeAllEditings) {
							adapter.closeAllEditings();
						}
					}
				},
				addLayer : function(layer) {
					if (layer && !this.interactionDocument.isGeoServerLayer(layer)) {
						var card = this.interactionDocument.getCurrentCard();
						var currentClassName = card.className;
						if (!this.layers[layer.name] && layer.masterTableName === currentClassName) {
							this.layers[layer.name] = this.geoAttrMenuButton.menu.add({
								iconCls : ICON_CLS[layer.type],
								text : layer.description,
								geoType : layer.type,
								name : layer.name,
								scope : this,
								handler : this.onAddMenuitemSelect,
								layer : layer
							});
						}
					}
				},

				removeAllLayerBinding : function() {
					this.geoAttrMenuButton.menu.removeAll(true);
					this.layers = {};
				},
				onAddMenuitemSelect : function(item) {
					this.geoAttrMenuButton.setText(item.text);
					this.geoAttrMenuButton.setIconCls(item.iconCls);
					var currentCard = this.interactionDocument.getCurrentCard();
					var layer = this.interactionDocument.getMapPanel().getLayerByClassAndName(currentCard.className,
							item.text);
					var feature = this.searchFeature(layer, item.text, currentCard.cardId);
					if (currentCard.cardId == -1)
						feature = null;
					if (feature !== null) {
						this.removeButton.enable();
						this.addButton.disable();
					} else {
						this.removeButton.disable();
						this.addButton.enable();
					}

					this.callDelegates("geoAttributeMenuItemHasBeenClicked", item);
				},
				searchFeature : function(layer, description, cardId) {
					if (!layer || !layer.getSource()) {
						return null;
					}
					var features = layer.getSource().getFeatures();
					for (var i = 0; i < features.length; i++) {
						if (features[i].get("master_card") == cardId) {
							return features[i];
						}
					}
					return null;
				}

			});

})();