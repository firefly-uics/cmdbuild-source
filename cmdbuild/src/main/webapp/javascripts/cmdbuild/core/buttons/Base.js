(function() {

	Ext.define('CMDBuild.core.buttons.Base', {
		extend: 'Ext.button.Button',

		/**
		 * @cfg {String}
		 */
		classIcon: undefined,

		/**
		 * @cfg {Boolean}
		 */
		withIcon: false,

		minWidth: 75,

		initComponent: function() {
			if (this.withIcon && !Ext.isEmpty(this.classIcon))
				Ext.apply(this, {
					cls: this.classIcon
				});

			this.callParent(arguments);
		}
	});

})();