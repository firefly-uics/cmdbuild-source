(function() {

	var ICON_CLS = {
		"POINT" : "mapFeaturePoint",
		"LINESTRING" : "mapFeatureLine",
		"POLYGON" : "mapFeaturePolygon"
	};

	Ext.define("CMDBuild.view.management.map.CMMapEditingToolsWindowDelegate",
			{
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
	Ext
			.define(
					"CMDBuild.view.management.map.CMMapEditingToolsWindow",
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
							this.mixins.delegable.constructor
									.call(this,
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

							this.geoAttrMenuButton = new Ext.Button(
									{
										text : this.translation.management.modcard.gis.geo_attributes,
										menu : new Ext.menu.Menu({
											items : []
										})
									});

							this.addButton = new Ext.Button({
								text : this.translation.add,
								creationControl : undefined,
								iconCls : 'add',
								enableToggle : true,
								allowDepress : true,
								disabled : true,
								scope : this,
								toggleHandler : function(button, state) {
									this.callDelegates(
											"addFeatureButtonHasBeenToggled",
											state);
								}
							});

							this.removeButton = new Ext.Button(
									{
										text : this.translation.remove,
										iconCls : 'delete',
										scope : this,
										disabled : true,
										handler : function() {
											this
													.callDelegates("removeFeatureButtonHasBeenClicked");
										}
									});

							this.callParent(arguments);
						},

						// add the buttons on render
						// to allow the window to resize it
						// automatically
						refresh : function() {
							// refreshing the edit combo that has to be filled
							// with the layers postgis that are owned by the
							// class
							var currentClassId = (Ext
									.isEmpty(_CMCardModuleState.entryType)) ? undefined
									: _CMCardModuleState.entryType.getId();
							if (!currentClassId) {
								return;
							}
							var currentClassName = (Ext
									.isEmpty(_CMCardModuleState.entryType)) ? undefined
									: _CMCardModuleState.entryType.getName();
							var currentCardId = (Ext
									.isEmpty(_CMCardModuleState.card)) ? undefined
									: _CMCardModuleState.card.raw.Id;// getId();
							var me = this;
							this.interactionDocument.getAllLayers(function(
									layers) {
								me.refreshAllLayers(layers, currentClassId,
										currentClassName, currentCardId);
							}, this);
						},

						refreshAllLayers : function(layers, currentClassId,
								currentClassName, currentCardId) {
							if (!currentCardId) {
								return;
							}
							this.removeAllLayerBinding();
							for (var i = 0; i < layers.length; i++) {
								var layer = layers[i];
								var visible = this.interactionDocument
										.isVisible(layer, currentClassName,
												currentCardId);
								if (visible
										&& !this.interactionDocument
												.isGeoServerLayer(layer)) {
									this.addLayer(layer);
								}
							}
						},
						onRender : function() {
							this.callParent(arguments);

							this.add([ this.geoAttrMenuButton, this.addButton,
									this.removeButton ]);
						},

						show : function() {
							if (this.geoAttrMenuButton.menu.items.length) {
								this.callParent(arguments);
								if (this.geoAttrMenuButton.menu.items.length > 0) {

									this.callParent(arguments);

									var firstItemOfMenu = this.geoAttrMenuButton.menu.items
											.first();
									if (firstItemOfMenu) {
										this.onAddMenuitemSelect.call(this,
												firstItemOfMenu);
									}

								}
							}
						},

						addLayer : function(layer) {
							if (layer
									&& !this.interactionDocument
											.isGeoServerLayer(layer)) {
								if (!this.layers[layer.name]) {
									this.layers[layer.name] = this.geoAttrMenuButton.menu
											.add({
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
//							this.geoAttrMenuButton.setText(item.text);
//							this.geoAttrMenuButton.setIconCls(item.iconCls);
//							this.addButton.enable();
//							this.addButton.toggle(false);
							var layer = this.interactionDocument.getMapPanel().getLayerByName(item.text);
							var feature = this.searchFeature(layer, item.text, _CMCardModuleState.card.raw.Id);
							if (feature !== null) {
								this.removeButton.enable();
								this.addButton.disable();
							}
							else {
								this.removeButton.disable();
								this.addButton.enable();
							}

							this.callDelegates(
									"geoAttributeMenuItemHasBeenClicked", item);
						},
						searchFeature : function(layer, description, cardId) {
							var features = layer.getSource().getFeatures();//ById(cardId);
							for (var i = 0; i < features.length; i++) {
								if (features[i].get("master_card") == cardId) {
									return features[i];
								}
							}
							return null;
						}

					});

})();