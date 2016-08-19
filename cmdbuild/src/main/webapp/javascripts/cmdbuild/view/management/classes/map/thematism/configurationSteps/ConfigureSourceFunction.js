(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureSourceFunction", {
		extend : "CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureFunction",
		itemId : "configureSourceFunction",

		strategiesStore : undefined,
		comboStrategies : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			this.strategiesStore = Ext.create("Ext.data.Store", {
				fields : [ "name", "type" ],
				data : []
			});
			this.comboStrategies = Ext.create("Ext.form.field.ComboBox", {
				fieldLabel : "@@ Choose Strategy *",
				store : this.strategiesStore,
				queryMode : "local",
				displayField : "description",
				name : "currentStrategy",
				valueField : "value",
				allowBlank : false
			});
			Ext.apply(this, {
				items : [ this.comboStrategies ]
			});
			this.callParent(arguments);
		},
		loadStrategies : function(callback, callbackScope) {
			var strategiesStore = Ext.create("Ext.data.Store", {
				fields : [ "description", "value" ],
				autoLoad : false,
				data : []
			});
			this.interactionDocument.getFunctionStrategies(function(strategies) {
				// a single strategy contains also the
				// parameters but for now
				// are irrelevant
				for ( var key in strategies) {
					var strategy = strategies[key];
					strategiesStore.add({
						"description" : strategy.description,
						"value" : strategy
					});
				}
				this.comboStrategies.store.loadData(strategiesStore.getRange(), false);
				callback.apply(callbackScope, this);
			}, this);
		}
	});
})();
