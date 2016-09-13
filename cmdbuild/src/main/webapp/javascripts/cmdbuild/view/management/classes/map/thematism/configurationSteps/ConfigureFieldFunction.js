(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFieldFunction", {
		extend : "CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction",
		requires : [ 'CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction' ],
		itemId : "configureFieldFunction",

		strategiesStore : undefined,
		comboStrategies : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			this.attributesStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value", "type" ],
				data : []
			});
			this.strategiesStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value" ],
				data : []
			});
			this.comboAttributes = Ext.create("Ext.form.field.ComboBox", {
				fieldLabel : "@@ Choose Attribute *",
				store : this.attributesStore,
				name : "attribute",
				queryMode : "local",
				displayField : "description",
				valueField : "value",
				allowBlank : false
			});
			this.comboStrategies = Ext.create("Ext.form.field.ComboBox", {
				fieldLabel : "@@ Choose Field Strategy *",
				store : this.strategiesStore,
				name : "currentStrategy",
				queryMode : "local",
				displayField : "description",
				valueField : "value",
				allowBlank : false
			});
			Ext.apply(this, {
				items : [ this.comboAttributes, this.comboStrategies ],
			});
			this.callParent(arguments);
		},
		loadStrategies : function(callback, callbackScope) {
			this.loadFieldStrategies(function() {
				this.loadAttributes(function() {
					callback.apply(callbackScope, []);
				}, this);
			}, this);
		},
		loadFieldStrategies : function(callback, callbackScope) {
			var strategiesStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value" ],
				autoLoad : false,
				data : []
			});
			this.interactionDocument.getFieldStrategies(function(strategies) {
				for ( var key in strategies) {
					var strategy = strategies[key];
					strategiesStore.add({
						"description" : strategy.description,
						"value" : strategy.description
					});
				}
				this.comboStrategies.store.loadData(strategiesStore.getRange(), false);
				callback.apply(callbackScope, this);
			}, this);
		},
		loadAttributes : function(callback, callbackScope) {
			var card = this.interactionDocument.getCurrentCard();
			if (!card) {
				return;
			}
			var attributesStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value", "type" ],
				autoLoad : false,
				data : []
			});
			var currentClassName = card.className;
			var type = _CMCache.getEntryTypeByName(currentClassName);
			var currentClassId = type.get("id");
			var me = this;
			_CMCache.getAttributeList(currentClassId, function(attributes) {
				for (var i = 0; i < attributes.length; i++) {
					var attribute = attributes[i];
					attributesStore.add({
						"description" : attribute.description,
						"value" : attribute.name,
						"type" : attribute.type
					});
				}
				me.comboAttributes.store.loadData(attributesStore.getRange(), false);
				callback.apply(callbackScope, this);
			}, this);
		}

	});
})();
