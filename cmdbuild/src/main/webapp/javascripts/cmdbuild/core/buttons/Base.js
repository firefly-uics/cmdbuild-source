(function() {

	Ext.define('CMDBuild.core.buttons.Base', {
		extend: 'Ext.button.Button',

		/**
		 * Default text value if not specified
		 *
		 * @cfg {String}
		 */
		textDefault: undefined,

		initComponent: function() {
			// Button minWidth setup
			if (Ext.isEmpty(this.iconCls))
				Ext.apply(this, {
					minWidth: 75
				});

			// Apply defaultText property if no defined text and tooltip properties
			if (Ext.isEmpty(this.text) && Ext.isEmpty(this.tooltip))
				Ext.apply(this, {
					text: this.textDefault
				});

			this.callParent(arguments);
		}
	});

})();