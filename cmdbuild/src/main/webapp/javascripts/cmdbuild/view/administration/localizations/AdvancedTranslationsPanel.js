(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel', {
		extend: 'Ext.panel.Panel',

		requires: [
//			'CMDBuild.core.proxy.CMProxyConstants' // TODO
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslations}
		 */
		delegate: undefined,

		border: false,
		frame: true,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			this.translationGridLanguagesFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Languages to show in table',
				overflowY: 'auto',
				padding: '0 5',

				items: [
					Ext.create('CMDBuild.view.administration.localizations.panels.LanguagesGrid')
				]
			});

			this.importPanel = Ext.create('CMDBuild.view.administration.localizations.panels.ImportPanel', {
				delegate: this.delegate
			});
			this.exportPanel = Ext.create('CMDBuild.view.administration.localizations.panels.ExportPanel', {
				delegate: this.delegate
			});

			this.importExportFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Import/Export',
				overflowY: 'auto',
				padding: '0 5',

				layout: 'hbox',

				items: [
					{
						xtype: 'fieldset',
						title: '@@ Import',
						flex: 1,
						overflowY: 'auto',

						items: [this.importPanel]
					},
					{ xtype: 'splitter' },
					{
						xtype: 'fieldset',
						title: '@@ Export',
						flex: 1,
						overflowY: 'auto',

						items: [this.exportPanel]
					}
				]
			});

			Ext.apply(this, {
				items: [this.translationGridLanguagesFieldset, this.importExportFieldset]
			});

			this.callParent(arguments);
		}
	});

})();
