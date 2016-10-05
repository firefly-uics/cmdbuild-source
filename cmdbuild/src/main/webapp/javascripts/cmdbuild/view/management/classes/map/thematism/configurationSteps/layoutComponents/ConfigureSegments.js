(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.layoutComponents.ConfigureSegments", {
		extend : "Ext.panel.Panel",
		itemId : "configureSegments",
		xtype : "form",
		layout : "anchor",

		parentWindow : undefined,
		interactionDocument : undefined,

		/**
		 * @returns {Void}
		 * 
		 * @override
		 */
		initComponent : function() {
			var me = this;
			Ext.apply(this, {
				items : [ {
					xtype : "textfield",
					fieldLabel : "@@ Segments",
					name : 'segmentsConfiguration',
					allowBlank : true
				} ]
			});
			this.callParent(arguments);
		},
		defaults : {
			anchor : "100%"
		},
		getLayoutConfiguration : function() {
			return this.parentWindow.getLayoutConfiguration();
		},
		init : function() {
			var layoutConfiguration = this.getLayoutConfiguration();
			this.parentWindow.initForm(this, layoutConfiguration);
		},
		loadComponents : function(callback, callbackScope) {
			this.init();
			callback.apply(callbackScope, []);
		}
	});
})();
