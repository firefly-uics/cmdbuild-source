(function() {

	Ext.define('CMDBuild.view.administration.localizations.BaseTranslationsPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Localizations'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.BaseTranslations}
		 */
		delegate: undefined,

		bodyCls: 'cmgraypanel-nopadding',
		border: false,
		buttonAlign: 'center',
		frame: false,
		overflowY: 'auto',
		region: 'center',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
_debug('CMDBuild.Config', CMDBuild.Config);
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
						fieldLabel: '@@ Default language',
						labelWidth: CMDBuild.LABEL_WIDTH,
						name: '@@ defaultLanguage'
					}),
					{
						xtype: 'checkbox',
						fieldLabel: '@@ Show language choice',
						name: '@@ languagePrompt'
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
				items: [this.languageFieldset, this.enabledLanguagesFieldset],
				buttons: [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onBaseSaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onBaseAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();