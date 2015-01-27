(function() {

	Ext.define('CMDBuild.view.administration.localizations.BaseTranslationsPanel', {
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
			this.languageFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Language configuration',
				overflowY: 'auto',

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
					anchor: '100%'
				},

				items: [
					{
						fieldLabel: '@@ Default language',
						xtype: 'combobox',
						name: 'language',
						valueField: 'name',
						displayField: 'value',
						forceSelection: true,
						editable: false,

						store: CMDBuild.core.proxy.Localizations.getLanguagesStore(),
						queryMode: 'local',
					},
					{
						fieldLabel: '@@ Show language choice',
						xtype: 'checkbox',
						name: 'languageprompt'
					}
				]
			});

			this.enabledLanguages = Ext.create('CMDBuild.view.administration.localizations.EnabledLanguagesGrid');

			this.enabledLanguagesFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Enabled languages',
				overflowY: 'auto',

				items: [this.enabledLanguages]
			});

			Ext.apply(this, {
				items: [this.languageFieldset, this.enabledLanguagesFieldset]
			});

			this.callParent(arguments);
		}
	});

})();