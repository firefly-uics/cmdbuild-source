(function() {

	Ext.define('CMDBuild.view.administration.localizations.ConfigurationsPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Configurations}
		 */
		delegate: undefined,

		languageFieldset: undefined, // TODO
		enabledLanguagesFieldset: undefined, // TODO
		importExportPanel: undefined, // TODO

		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.Constants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationsBaseSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationsBaseAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
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
							Ext.create('CMDBuild.view.common.field.LanguageCombo', {
								name: '@@ defaultLanguage',
								fieldLabel: '@@ Default language',
								labelWidth: CMDBuild.LABEL_WIDTH
							}),
							{
								xtype: 'checkbox',
								name: '@@ languagePrompt',
								fieldLabel: '@@ Show language choice'
							}
						]
					}),
					this.enabledLanguagesFieldset = Ext.create('Ext.form.FieldSet', {
						title: '@@ Enabled languages',
						overflowY: 'auto',

						items: [
							Ext.create('CMDBuild.view.administration.localizations.common.LanguagesGrid')
						]
					}),
					this.importExportPanel = Ext.create('Ext.panel.Panel', {
						bodyCls: 'cmgraypanel-nopadding',
						overflowY: 'auto',
						frame: false,
						border: false,

						layout: {
							type: 'hbox'
						},

						items: [
							{
								xtype: 'fieldset',
								title: '@@ Import',
								flex: 1,
								overflowY: 'auto',

								items: [
									this.importPanel = Ext.create('CMDBuild.view.administration.localizations.common.ImportPanel', {
										delegate: this.delegate
									})
								]
							},
							{ xtype: 'splitter' },
							{
								xtype: 'fieldset',
								title: '@@ Export',
								flex: 1,
								overflowY: 'auto',

								items: [
									this.exportPanel = Ext.create('CMDBuild.view.administration.localizations.common.ExportPanel', {
										delegate: this.delegate
									})
								]
							}
						]
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();