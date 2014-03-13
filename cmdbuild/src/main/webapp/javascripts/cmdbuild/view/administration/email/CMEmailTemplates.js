(function() {

	var tr = CMDBuild.Translation.administration.setup.email.templates; // Path to translation

	Ext.require('CMDBuild.core.serviceProxy.CMProxyConfigurationEmailTemplates');

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplates', {
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

			this.grid = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesGrid', {
				region: 'center'
			});

			this.form = Ext.create('CMDBuild.view.administration.email.CMEmailTemplatesForm', {
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