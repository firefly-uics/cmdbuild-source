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
				allowBlank : false,
				editable : false,
				triggerAction : "all"
			});
			Ext.apply(this, {
				items : [ {
					xtype : "textfield",
					fieldLabel : "@@ Layer name *",
					name : 'layerName',
					allowBlank : false
				}, {
					xtype : "radiogroup",
					name : "analysis",
					fieldLabel : "@@ Analysis type",
					vertical : true,
					border : true,
					items : getAnalysisItems(this.parentWindow)
				}, {
					xtype : "radiogroup",
					name : "source",
					fieldLabel : "@@ Source type",
					columns : 1,
					vertical : true,
					border : true,
					items : getSourceItems()
				}, this.comboLayers ],
				buttons : getButtons(this.parentWindow, this.itemId),
			});
			this.callParent(arguments);
		},
		defaults : {
			anchor : "100%"
		},
		loadComponents : function(callback, callbackScope) {
			this.loadLayers(function() {
				var thematismConfiguration = this.parentWindow.getThematismConfiguration();
				this.init();
				if (!thematismConfiguration.sourceLayer) {
					this.comboLayers.select(this.comboLayers.getStore().getAt(0));
				}
				callback.apply(callbackScope, []);
			}, this);
		},
		init : function() {
			var thematismConfiguration = this.parentWindow.getThematismConfiguration();
			this.parentWindow.initForm(this, thematismConfiguration);
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
					var visible = (layer.masterTableName === currentClassName);
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
	function getAnalysisItems(parentWindow) {
		return [ {
			boxLabel : parentWindow.getAnalysisDescription(CMDBuild.gis.constants.layers.RANGES_ANALYSIS),
			name : "analysis",
			inputValue : CMDBuild.gis.constants.layers.RANGES_ANALYSIS
		}, {
			boxLabel : parentWindow.getAnalysisDescription(CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS),
			name : "analysis",
			inputValue : CMDBuild.gis.constants.layers.PUNTUAL_ANALYSIS,
			checked : true
		}, {
			boxLabel : parentWindow.getAnalysisDescription(CMDBuild.gis.constants.layers.DENSITY_ANALYSIS),
			name : "analysis",
			inputValue : CMDBuild.gis.constants.layers.DENSITY_ANALYSIS
		} ];
	}
	function getSourceItems() {
		return [ {
			checked : true,
			boxLabel : "@@ from table",
			name : "source",
			inputValue : CMDBuild.gis.constants.layers.TABLE_SOURCE
		}, {
			boxLabel : "@@ from function",
			name : "source",
			inputValue : CMDBuild.gis.constants.layers.FUNCTION_SOURCE,
		} ];
	}

	/**
	 * @param
	 * {CMDBuild.view.management.classes.map.thematism.ThematismMainWindow}
	 * parentWindow
	 * @param {String}
	 *            itemId
	 * 
	 * @returns {Array} extjs items
	 */
	function getButtons(parentWindow, itemId) {
		return [ {
			text : '@@ Cancel',
			handler : function() {
				parentWindow.close();
			}
		}, {
			text : '@@ Advance',
			formBind : true,
			disabled : true,
			handler : function() {
				var form = this.up('form').getForm();
				parentWindow.advance(itemId, form.getValues());
			}
		} ];
	}

})();