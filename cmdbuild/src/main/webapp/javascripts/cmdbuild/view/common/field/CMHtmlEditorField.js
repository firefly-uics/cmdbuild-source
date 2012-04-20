(function() {
Ext.define("CMDBuild.view.common.field.CMHtmlEditorField", {
	extend: "Ext.form.field.HtmlEditor",
	enableExpand: true, // to have a button that increase the height

	initComponent: function() {
		// set the defaultValue to empty string,
		// because the Ext default value has encoding problems
		// when used in some query
		this.defaultValue = "";
		this.callParent();
	},

	createToolbar: function() {
		var me = this;

		this.callParent(arguments);

		if (this.enableExpand) {
			this.toolbar.add("->");
			this.toolbar.add({
				iconCls: "expand",
				handler: function() {
					var conf = me.initialConfig,
						htmlField = new CMDBuild.view.common.field.CMHtmlEditorField(Ext.apply(conf, {
							hideLabel: true,
							resizable: false,
							enableExpand: false,
							region: "center"
						})),

						popup = new CMDBuild.PopupWindow({
							title: conf.fieldLabel,
							items: [{
								xtype: "panel",
								layout: "border",
								border: false,
								frame: false,
								items: htmlField
							}],
							buttonAlign: "center",
							buttons: [{
								text: CMDBuild.Translation.common.btns.confirm,
								handler: function() {
									me.setValue(htmlField.getValue());
									popup.destroy();
								}
							}, {
								text: CMDBuild.Translation.common.btns.abort,
								handler: function() {
									popup.destroy();
								}
							}]
						}).show();

						htmlField.setValue(me.getValue());
				}
			});
		}
	}
});

})();