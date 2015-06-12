(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedPanel', {
		extend: 'Ext.form.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.localizations.Advanced}
		 */
		delegate: undefined,

		translationGridLanguagesFieldset: undefined, // TODO
		importExportFieldset: undefined, // TODO
		importPanel: undefined, // TODO
		exportPanel: undefined, // TODO

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
									this.delegate.cmfg('onLocalizationsAdvancedSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onLocalizationsAdvancedAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.translationGridLanguagesFieldset = Ext.create('Ext.form.FieldSet', {
						title: '@@ Languages to show in table',
						overflowY: 'auto',
						padding: '0 5',

						items: [
							Ext.create('CMDBuild.view.administration.localizations.common.LanguagesGrid')
						]
					}),
					this.importExportFieldset = Ext.create('Ext.panel.Panel', {
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