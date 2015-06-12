(function() {

	Ext.define('CMDBuild.view.administration.localizations.BasePanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Base}
		 */
		delegate: undefined,

		languageFieldset: undefined, // TODO
		enabledLanguagesFieldset: undefined, // TODO

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
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationsBaseSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
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
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();