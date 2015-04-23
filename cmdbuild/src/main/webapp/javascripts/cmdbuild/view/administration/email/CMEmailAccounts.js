(function() {

	var tr = CMDBuild.Translation.administration.email.accounts;

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define('CMDBuild.view.administration.email.CMEmailAccounts', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.administration.email.CMEmailAccountsController}
		 */
		delegate: undefined,

		border: false,
		frame: false,
		layout: 'border',

		initComponent: function() {
			this.addButton = Ext.create('Ext.Button', {
				iconCls: 'add',
				text: tr.add,
				scope: this,
				handler: function() {
					this.delegate.cmOn('onAddButtonClick');
				}
			});

			this.grid = Ext.create('CMDBuild.view.administration.email.CMEmailAccountsGrid', {
				delegate: this.delegate,
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.CMEmailAccountsForm', {
				delegate: this.delegate,
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