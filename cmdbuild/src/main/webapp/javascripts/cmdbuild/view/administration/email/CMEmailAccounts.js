(function() {

	var tr = CMDBuild.Translation.administration.email.accounts; // Path to translation

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define('CMDBuild.view.administration.email.CMEmailAccounts', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = Ext.create('Ext.Button', {
				iconCls: 'add',
				text: tr.add,
				handler: function() {
					me.delegate.cmOn('onAddButtonClick');
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
				tbar: [this.addButton],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();