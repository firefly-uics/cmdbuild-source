(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.ImportPanel', {
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
		border: false,
		buttonAlign: 'center',
		encoding: 'multipart/form-data',
		fileUpload: true,
		frame: false,
		monitorValid: true,

		initComponent: function() {
			Ext.apply(this, {
				items: [
					{
						name: '@@ importSection',
						fieldLabel: '@@ Import section',
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

						store: CMDBuild.core.proxy.Localizations.getSectionsStore(),
						queryMode: 'local'
					},
					{
						name: '@@ importFormat',
						fieldLabel: '@@ Format',
						displayField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

						value: 'csv', // Default value

						store: CMDBuild.core.proxy.Localizations.getFileFormatStore(),
						queryMode: 'local'
					},
					{
						xtype: 'filefield',
						name: '@@ importFile',
						fieldLabel: CMDBuild.Translation.csvFile,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false
					},
					{
						name: '@@ importSeparator',
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
					text: '@@ Import',
					scope: this,

					handler: function() {
						this.delegate.cmOn('onImportButtonClick');
					}
				}],
			});

			this.callParent(arguments);
		}
	});

})();
