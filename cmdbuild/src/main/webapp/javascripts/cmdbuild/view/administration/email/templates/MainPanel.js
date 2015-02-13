(function() {

	var tr = CMDBuild.Translation.administration.email.templates;

	Ext.define('CMDBuild.view.administration.email.templates.MainPanel', {
		extend: 'Ext.panel.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.email.CMEmailTemplatesController}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.button.Button}
		 */
		addButton: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplatesForm}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.administration.email.CMEmailTemplatesGrid}
		 */
		grid: undefined,

		border: true,
		frame: false,
		layout: 'border',
		title: CMDBuild.Translation.administration.email.title + ' - ' + tr.title,

		initComponent: function() {
			this.addButton = Ext.create('Ext.button.Button', {
				iconCls: 'add',
				text: tr.add,
				scope: this,

				handler: function() {
					this.delegate.cmOn('onAddButtonClick');
				}
			});

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
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.addButton]
					}
				],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();