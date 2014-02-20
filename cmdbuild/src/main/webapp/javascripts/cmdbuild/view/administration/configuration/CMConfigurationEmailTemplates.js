(function() {

	var tr = CMDBuild.Translation.administration.setup.email.templates; // Path to translation

	Ext.define('CMDBuild.view.administration.configuration.CMConfigurationEmailTemplates', {
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

			this.grid = Ext.create('CMDBuild.view.administration.configuration.CMConfigurationEmailTemplatesGrid', {
				region: 'center'
			});

			this.form = Ext.create('CMDBuild.view.administration.configuration.CMConfigurationEmailTemplatesForm', {
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