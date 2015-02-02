(function() {

	Ext.define('CMDBuild.view.administration.localizations.TranslationCheckbox', {
		extend: 'Ext.form.FieldContainer',

		/**
		 * @cfg {String}
		 */
		name: undefined,

		/**
		 * @cfg {String}
		 */
		image: undefined,

		layout: 'hbox',
		padding: '0 0 0 5',
		width: 222,

		initComponent: function() {
			this.checkbox = Ext.create('Ext.form.field.Checkbox', {
				fieldLabel: this.language,
				labelWidth: CMDBuild.LABEL_WIDTH,
				name: this.name,
				submitValue: false
			});

			this.translationsFlags = Ext.create('Ext.form.field.Display', {
				iconCls: this.image,
				width: 22,
				scope: this,

				renderer: function() {
						return '<div style="background-repeat:no-repeat;background-position:center;" class="' + this.image + '">&#160;</div>';
				}
			});

			this.items = [this.translationsFlags, this.checkbox];

			this.callParent(arguments);
		},

		setValue: function(value) {
			this.checkbox.setValue(value);
		},

		getValue: function() {
			return this.checkbox.getValue();
		}
	});

})();