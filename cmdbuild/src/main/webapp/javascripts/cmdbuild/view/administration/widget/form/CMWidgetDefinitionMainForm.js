Ext.define("CMDBuild.view.administration.widget.CMWidgetDefinitionForm", {
	extend: "Ext.form.Panel",

	mixins: ['CMDBuild.view.common.PanelFunctions'],

	layout: 'fit',

	initComponent: function() {
		var me = this;
		this.modifyButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.modify,
			iconCls: "modify",
			handler: function() {
				me.fireEvent("cm-modify");
			}
		});

		this.abortButton = new Ext.button.Button({
			text: CMDBuild.Translation.common.buttons.remove,
			iconCls: "delete",
			handler: function() {
				me.fireEvent("cm-remove");
			}
		});

		this.tbar = this.cmTBar = [this.modifyButton, this.abortButton];

		this.callParent(arguments);

		this.setDisabledModify(true, true, true);
	},

	reset: function() {
		this.removeAll();
	},

	/**
	 * Probably not necessary because PanelFunctions disable also non field elements
	 *
	 * @override
	 */
	enableModify: function(all) {
		this.setDisabledModify(false, true);

		this.items.each(function(item) {
			if (!Ext.isEmpty(item.enableNonFieldElements) && Ext.isFunction(item.enableNonFieldElements)) {
				item.enableNonFieldElements();
			}
		});
	},

	/**
	 * Probably not necessary because PanelFunctions disable also non field elements
	 *
	 * @override
	 */
	disableModify: function(enableCMTBar) {
		this.setDisabledModify(true);

		this.items.each(function(item) {
			if (!Ext.isEmpty(item.disableNonFieldElements) && Ext.isFunction(item.disableNonFieldElements)) {
				item.disableNonFieldElements();
			}
		});
	}
});