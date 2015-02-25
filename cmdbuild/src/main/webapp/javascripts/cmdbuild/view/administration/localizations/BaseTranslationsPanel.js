(function() {

	Ext.define('CMDBuild.view.administration.localizations.BaseTranslationsPanel', {
		extend: 'Ext.panel.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.BaseTranslations}
		 */
		delegate: undefined,

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

				layout: {
					type: 'vbox',
					align:'stretch'
				},

				defaults: {
					labelWidth: CMDBuild.LABEL_WIDTH,
					maxWidth: CMDBuild.MEDIUM_FIELD_WIDTH
				},

				items: [
					{
						xtype: 'combobox',
						fieldLabel: '@@ Default language',
						name: '@@ language',
						valueField: CMDBuild.core.proxy.CMProxyConstants.NAME,
						displayField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

						forceSelection: true,
						editable: false,

						store: CMDBuild.core.proxy.Localizations.getLanguagesStore(),
						queryMode: 'local',
					},
					{
						xtype: 'checkbox',
						fieldLabel: '@@ Show language choice',
						name: '@@ languageprompt'
					}
				]
			});

			this.enabledLanguagesFieldset = Ext.create('Ext.form.FieldSet', {
				title: '@@ Enabled languages',
				overflowY: 'auto',

				items: [
					Ext.create('CMDBuild.view.administration.localizations.panels.LanguagesGrid')
				]
			});

			Ext.apply(this, {
				items: [this.languageFieldset, this.enabledLanguagesFieldset]
			});

			this.callParent(arguments);
		}
	});

})();