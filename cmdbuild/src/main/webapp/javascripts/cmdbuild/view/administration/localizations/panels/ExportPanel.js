(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.ExportPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslations}
		 */
		delegate: undefined,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		defaults: {
			xtype: 'combobox',
			labelWidth: CMDBuild.LABEL_WIDTH,
			maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH
		},

		bodyCls: 'cmgraypanel',
		bodyPadding: '0 0 32 0', // Hack to fix panel height to be same as left one
		border: false,
		buttonAlign: 'center',
		frame: false,

		initComponent: function() {
_debug('ExportPanel', this.delegate);
			Ext.apply(this, {
				items: [
					{
						name: '@@ exportSection',
						fieldLabel: '@@ Export section',
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

						store: CMDBuild.core.proxy.Localizations.getSectionsStore(),
						queryMode: 'local'
					},
					{
						name: '@@ exportFormat',
						fieldLabel: '@@ Format',
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

						value: 'csv', // Default value

						store: CMDBuild.core.proxy.Localizations.getFileFormatStore(),
						queryMode: 'local'
					},
					{
						name: '@@ exportSeparator',
						fieldLabel: CMDBuild.Translation.separator,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						maxWidth: 200,
						value: ';',
						editable: false,
						allowBlank: false,

						store: CMDBuild.core.proxy.Localizations.getCsvSeparatorStore(),
						queryMode: 'local'
					}
				],
				buttons: [{
					xtype: 'button',
					text: '@@ Export',
					scope: this,

					handler: function() {
						this.delegate.cmOn('onExportButtonClick');
					}
				}],
			});

			this.callParent(arguments);
		}
	});

})();
