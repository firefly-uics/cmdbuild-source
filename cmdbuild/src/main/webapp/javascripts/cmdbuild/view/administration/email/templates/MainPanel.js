(function() {

	Ext.define('CMDBuild.view.administration.email.templates.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Main}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addButton: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.templates.GridPanel}
		 */
		grid: undefined,

		border: true,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.email + ' - ' + CMDBuild.Translation.templates,

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.email.templates.GridPanel', {
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.templates.FormPanel', {
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Add', {
								text: CMDBuild.Translation.addTemplate,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailTemplatesAddButtonClick');
								}
							})
						]
					})
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();
