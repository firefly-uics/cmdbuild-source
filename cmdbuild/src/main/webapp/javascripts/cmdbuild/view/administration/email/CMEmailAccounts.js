(function() {

	var tr = CMDBuild.Translation.administration.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.email.CMEmailAccounts', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		border: true,
		frame: false,
		layout: 'border',
		title: tr.title,

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
				region: 'north',
				split: true,
				height: '30%'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.CMEmailAccountsForm', {
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