(function() {

	Ext.define('CMDBuild.core.buttons.Print',  {
		alternateClassName: 'CMDBuild.PrintMenuButton', // Legacy class name TODO eliminare tutti
		extend: 'Ext.button.Split',

		iconCls: 'print',
		text: CMDBuild.Translation.common.buttons.print,

		/**
		 * Supported formats
		 *
		 * @cfg {Array}
		 * */
		formatList: [
			CMDBuild.core.proxy.CMProxyConstants.CSV,
			CMDBuild.core.proxy.CMProxyConstants.ODT,
			CMDBuild.core.proxy.CMProxyConstants.PDF,
			CMDBuild.core.proxy.CMProxyConstants.RTF
		],

		initComponent: function() {
			Ext.apply(this, {
				menu: Ext.create('Ext.menu.Menu'),
				handler: function(button, e) {
					if (!this.isDisabled())
						this.showMenu();
				},
			});

			this.callParent(arguments);

			this.buildMenu();
		},

		buildMenu: function() {
			var me = this;

			Ext.Array.forEach(this.formatList, function(format, i, allFormats) {
				this.menu.add({
					text: CMDBuild.Translation.common.buttons.as + ' ' + format.toUpperCase(),
					iconCls: format,
					format: format,

					handler: function(button, e) {
						me.fireEvent('click', this.format);
					}
				});
			}, this);
		}
	});

})();