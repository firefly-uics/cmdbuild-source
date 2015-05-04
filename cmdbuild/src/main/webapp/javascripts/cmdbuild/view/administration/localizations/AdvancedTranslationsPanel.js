(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.AdvancedTranslations}
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

			this.importExportFieldset = Ext.create('Ext.panel.Panel', {
				bodyCls: 'cmgraypanel-nopadding',
				overflowY: 'auto',
				frame: false,
				border: false,

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
				items: [this.translationGridLanguagesFieldset, this.importExportFieldset],
				buttons: [
					Ext.create('CMDBuild.buttons.SaveButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onAdvancedSaveButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onAdvancedAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();