(function() {

	Ext.define("CMDBuild.view.administration.localizations.TranslationCheckbox", {
		extend: "Ext.container.Container",
		layout: "hbox",
		padding: "0 0 0 5",
		width: 200,
		name : 'no name',
		allowBlank : false,
		vtype : '',
		setValue: function(value) {
			this.check.setValue(value);
		},
		getValue: function() {
			return this.check.getValue();
		},
		initComponent : function() {
			var me = this;
			this.check = new Ext.form.field.Checkbox( {
				fieldLabel : me.language,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name : me.name,
				submitValue: false
			});
			this.width += 22;
			this.translationsButton = new Ext.form.field.Display( {
				iconCls: me.image,
				renderer : function(){
						return '<div style="background-repeat:no-repeat;background-position:center;" class="' + me.image + '">&#160;</div>';
				},
				width: 22
			});
			this.items = [this.translationsButton, this.check];
			this.callParent(arguments);
		}
	});

})();