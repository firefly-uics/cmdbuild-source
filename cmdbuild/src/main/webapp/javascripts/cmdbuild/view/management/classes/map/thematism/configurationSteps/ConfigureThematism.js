(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureThematism", {
		extend : "Ext.form.Panel",
		itemId : "configureThematism",
		xtype : "form",
		layout : "anchor",

		layersStore : undefined,
		parentWindow : undefined,
		interactionDocument : undefined,
		comboLayers : undefined,
		
		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			this.layersStore = Ext.create("Ext.data.Store", {
				fields : [ "name", "type" ],
				data : []
			});
			this.comboLayers = Ext.create("Ext.form.field.ComboBox", {
				fieldLabel : "@@ Choose Layer *",
				store : this.layersStore,
				name : "sourceLayer",
				queryMode : "local",
				displayField : "name",
				valueField : "name",
				allowBlank : false
			});
			Ext.apply(this, {
				items : [ {
					xtype : "textfield",
					fieldLabel : "@@ Layer name *",
					name : 'layerName',
					allowBlank : false
				}, {
					xtype : "radiogroup",
					fieldLabel : "@@ Analysis type",
					vertical : true,
					border : true,
					items : [ {
						boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.RANGES_ANALYSIS),
						name : "analysis",
						inputValue : CMDBuild.gis.constants.layers.RANGES_ANALYSIS
					}, {
						boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS),
						name : "analysis",
						inputValue : CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS,
						checked : true
					}, {
						boxLabel : getAnalysisDescription(CMDBuild.gis.constants.layers.DENSITY_ANALYSIS),
						name : "analysis",
						inputValue : CMDBuild.gis.constants.layers.DENSITY_ANALYSIS
					} ]
				}, {
					xtype : "radiogroup",
					fieldLabel : "@@ Source type",
					columns : 1,
					vertical : true,
					border : true,
					items : [ {
						checked : true,
						boxLabel : "@@ from table",
						name : "source",
						inputValue : CMDBuild.gis.constants.layers.TABLE_SOURCE
					}, {
						boxLabel : "@@ from function",
						name : "source",
						inputValue : CMDBuild.gis.constants.layers.FUNCTION_SOURCE,
					} ]
				}, this.comboLayers ],
				buttons : [ {
					text : '@@ Cancel',
					handler : function() {
						me.parentWindow.close();
					}
				}, {
					text : '@@ Advance',
					formBind : true, // only enabled once the form is valid
					disabled : true,
					handler : function() {
						var form = this.up('form').getForm();
						me.parentWindow.advance(me.itemId, form.getValues());
					}
				} ],
			});
			this.callParent(arguments);
		},
		defaults : {
			anchor : "100%"
		},
		loadComponents : function(callback, callbackScope) {
			this.loadLayers(function() {
				this.comboLayers.select(this.comboLayers.getStore().getAt(0));
				callback.apply(callbackScope, []);
			}, this);
		},
		loadLayers : function(callback, callbackScope) {
			var card = this.interactionDocument.getCurrentCard();
			if (!card) {
				callback.apply(callbackScope, this);
				return;
			}
			var layersStore = Ext.create("Ext.data.Store", {
				fields : [ "name", "type" ],
				autoLoad : false,
				data : []
			});
			var currentClassName = card.className;
			var currentCardId = card.cardId;
			var me = this;
			this.interactionDocument.getAllLayers(function(layers) {
				for (var i = 0; i < layers.length; i++) {
					var layer = layers[i];
					var visible = me.interactionDocument.isVisible(layer, currentClassName, currentCardId);
					if (visible && !me.interactionDocument.isGeoServerLayer(layer)) {
						layersStore.add({
							"name" : layer.name,
							"type" : layer.type
						});
					}
				}
				this.layersStore.loadData(layersStore.getRange(), false);
				callback.apply(callbackScope, this);
			}, this);
		}
	});
	function getAnalysisDescription(analysisType) {
		switch (analysisType) {
		case CMDBuild.gis.constants.layers.RANGES_ANALYSIS:
			return "@@ Ranges";
		case CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS:
			return "@@ Puntual";
		case CMDBuild.gis.constants.layers.DENSITY_ANALYSIS:
			return "@@ Density";
		}
		return "@@ Puntual";
	}
	function getSourceDescription(sourceType) {
		switch (sourceType) {
		case CMDBuild.gis.constants.layers.TABLE_SOURCE:
			return "@@ Table";
		case CMDBuild.gis.constants.layers.FUNCTION_SOURCE:
			return "@@ Function";
		}
		return "@@ Table";
	}
})();
