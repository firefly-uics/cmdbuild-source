(function() {

	Ext.define('CMDBuild.view.administration.localizations.panels.ImportFieldSet', {
		extend: 'Ext.form.FieldSet',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		title: '@@ Import',
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
					Ext.create('CMDBuild.view.administration.localizations.panels.LanguagesGrid')
				]
			});

			Ext.apply(this, {
				items: [this.translationGridLanguagesFieldset, this.importExportFieldset]
			});

			this.callParent(arguments);
		}
	});

})();