(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		border: false,
		layout: 'vbox',
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
					Ext.create('CMDBuild.view.administration.localizations.LanguagesGrid')
				]
			});

			this.importExportFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Languages to show in table',
				overflowY: 'auto',
				padding: '0 5',

				items: [
					Ext.create('CMDBuild.view.administration.localizations.panels.ImportFieldSet'),
					Ext.create('CMDBuild.view.administration.localizations.panels.ExportFieldSet')
				]
			});

			Ext.apply(this, {
				items: [this.translationGridLanguagesFieldset, this.importExportFieldset]
			});

			this.callParent(arguments);
		}
	});

})();