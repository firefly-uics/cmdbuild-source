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
					allowBlank : false
				} ]
			});
			this.callParent(arguments);
		},
		defaults : {
			anchor : "100%"
		},
		loadComponents : function(callback, callbackScope) {
			callback.apply(callbackScope, []);
		}
	});
})();
