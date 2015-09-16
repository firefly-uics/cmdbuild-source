(function() {

	Ext.define('CMDBuild.view.administration.email.templates.TemplatesView', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Templates}
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

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.grid = Ext.create('CMDBuild.view.administration.email.templates.GridPanel', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.templates.FormPanel', {
				delegate: this.delegate,
				region: 'center'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.iconized.add.Add', {
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
