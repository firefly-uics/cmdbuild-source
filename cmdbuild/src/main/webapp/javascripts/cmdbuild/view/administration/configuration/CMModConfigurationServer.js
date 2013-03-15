(function() {
	var tr = CMDBuild.Translation.administration.setup.server;

	Ext.define("CMDBuild.view.administration.configuration.CMModConfigurationServer", {
		extend: "CMDBuild.view.administration.configuration.CMBaseModConfiguration",
		title: tr.title,
		configFileName: 'server',
		
		constructor: function() {
			this.clearCacheButton = new Ext.button.Button({
				text : tr.clear_cache
			});
		
			this.clearProcesses = new Ext.button.Button({
				text : tr.servicesync
			});

			this.unlockAllCards = new Ext.button.Button({
				text : "@@ Sblocca tutte le card bloccate"
			});

			this.items = [{
				xtype : 'fieldset',
				title : tr.cache_management,
				autoHeight : true,
				items : [this.clearCacheButton]
			},
			{
				xtype : 'fieldset',
				title : tr.servicesync,
				autoHeight : true,
				layout : 'column',
				items : [this.clearProcesses]
			},
			{
				xtype : 'fieldset',
				title : "@@ Sblocca tutte le card bloccate",
				autoHeight : true,
				layout : 'column',
				items : [this.unlockAllCards]
			}
		]

			this.callParent(arguments);
		}
	});
})();