(function() {
	Ext.define("CMDBuild.view.management.classes.map.thematism.configurationSteps.ConfigureIndividualLayout", {
		extend : "Ext.form.Panel",
		itemId : "configureIndividualLayout",
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
			Ext.apply(this, {
				items : [ {
					xtype : "textfield",
					fieldLabel : "@@ Individual",
					name : 'layerName',
					allowBlank : false
				} ],
				buttons : [ {
					text : '@@ Cancel',
					handler : function() {
						me.parentWindow.close();
					}
				}, {
					text : '@@ Previous',
					handler : function() {
						var form = this.up('form').getForm();
						me.parentWindow.previous(me.itemId);
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
			callback.apply(callbackScope, []);
		}
	});
})();
