(function() {

	Ext.define('CMDBuild.view.administration.localization.ConfigurationPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.Constants'],

		/**
		 * @cfg {CMDBuild.controller.administration.localization.Configuration}
		 */
		delegate: undefined,

//		enabledLanguagesGrid = Ext.create('CMDBuild.view.administration.localization.common.LanguagesGrid
//		importPanel = Ext.create('CMDBuild.view.administration.localization.common.ImportPanel
//		exportPanel = Ext.create('CMDBuild.view.administration.localization.common.ExportPanel
//		defaultLanguageCombobox = Ext.create('CMDBuild.view.common.field.LanguageCombo
//		languagePromptCheckbox = Ext.create('Ext.form.field.Checkbox

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
									this.delegate.cmfg('onLocalizationConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationConfigurationAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
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
							this.defaultLanguageCombobox = Ext.create('CMDBuild.view.common.field.LanguageCombo', {
								name: CMDBuild.core.proxy.Constants.DEFAULT_LANGUAGE,
								fieldLabel: '@@ Default language',
								labelWidth: CMDBuild.LABEL_WIDTH
							}),
							this.languagePromptCheckbox = Ext.create('Ext.form.field.Checkbox', {
								name: CMDBuild.core.proxy.Constants.LANGUAGE_PROMPT,
								fieldLabel: '@@ Show language choice',
								labelWidth: CMDBuild.LABEL_WIDTH,
								inputValue: true,
								uncheckedValue: false
							})
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: '@@ Enabled languages',
						overflowY: 'auto',

						items: [
							this.enabledLanguagesGrid = Ext.create('CMDBuild.view.administration.localization.common.LanguagesGrid')
						]
					}),
					Ext.create('Ext.panel.Panel', {
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
									this.importPanel = Ext.create('CMDBuild.view.administration.localization.common.ImportPanel', {
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
									this.exportPanel = Ext.create('CMDBuild.view.administration.localization.common.ExportPanel', {
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