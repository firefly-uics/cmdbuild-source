(function() {

	var tr = CMDBuild.Translation.administration.setup.email.accounts; // Path to translation

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailAccounts', {
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

			this.grid = Ext.create('CMDBuild.view.administration.configuration.CMConfigurationEmailAccountsGrid', {
				region: 'center'
			});

			this.form = Ext.create('CMDBuild.view.administration.configuration.CMConfigurationEmailAccountsForm', {
				region: 'south',
				height: '70%'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.grid, this.form]
			});

			this.callParent(arguments);
		}
	});

})();