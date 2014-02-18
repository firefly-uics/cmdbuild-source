(function() {

	var tr = CMDBuild.Translation.administration.setup.email, // Path to translation
		delegate = null; // Controller handler

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationEmail", {
		extend: "Ext.panel.Panel",

		title: tr.title,
		frame: false,
		border: true,
		layout: 'border',

		initComponent: function() {
			var me = this;

			this.addButton = new Ext.Button({
				iconCls: 'add',
				text: tr.add,
				handler: function() {
					me.delegate.cmOn('onAddButtonClick', me);
				}
			});

			this.emailGrid = new CMDBuild.view.administration.configuration.CMModConfigurationEmailGrid({
				region: 'center'
			});

			this.emailForm = new CMDBuild.view.administration.configuration.CMModConfigurationEmailForm({
				region: 'south',
				height: '70%'
			});

			Ext.apply(this, {
				tbar: [this.addButton],
				items: [this.emailGrid, this.emailForm]
			});

			this.callParent(arguments);
		}
	});

})();