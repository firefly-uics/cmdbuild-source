(function() {

	Ext.define('CMDBuild.core.buttons.iconized.Print', {
		extend: 'Ext.button.Split',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {Object}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		mode: undefined,

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

		iconCls: 'print',
		text: CMDBuild.Translation.common.buttons.print,

		initComponent: function() {
			Ext.apply(this, {
				menu: Ext.create('Ext.menu.Menu'),

				handler: function(button, e) {
					if (!button.isDisabled())
						button.showMenu();
				},
			});

			this.callParent(arguments);

			switch (this.mode) {
				case 'legacy':
					return this.buildLegacyMenu();

				default:
					return this.buildMenu();
			}
		},

		buildLegacyMenu: function() {
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
		},

		buildMenu: function() {
			Ext.Array.forEach(this.formatList, function(format, i, allFormats) {
				this.menu.add({
					text: CMDBuild.Translation.common.buttons.as + ' ' + format.toUpperCase(),
					iconCls: format,
					format: format,
					scope: this,

					handler: function(button, e) {
						this.delegate.cmfg('onButtonPrintClick', button.format);
					}
				});
			}, this);
		}
	});

})();