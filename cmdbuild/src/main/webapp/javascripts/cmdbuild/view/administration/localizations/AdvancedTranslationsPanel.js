(function() {

	Ext.define('CMDBuild.view.administration.localizations.AdvancedTranslationsPanel', {
		extend: 'Ext.panel.Panel',

		border: false,
		layout: 'vbox',
		frame: true,

		initComponent: function() {
			Ext.apply(this, {
				items: [Ext.create('Ext.form.field.Text'),Ext.create('Ext.form.field.Text'),Ext.create('Ext.form.field.Text'),Ext.create('Ext.form.field.Text')]
			});

			this.callParent(arguments);
		}
	});

})();