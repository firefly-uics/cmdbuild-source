(function() {

	Ext.define('CMDBuild.ServiceProxy.tasks', {
		statics: {
			create: function(parameters) {},

			get: function() {},

			getStore: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.tasks.grid',
					proxy: {
						type: 'ajax',
						url: CMDBuild.ServiceProxy.url.tasks.getStore,
						reader: {
							type: 'json',
							root: 'response'
						}
					},
					sorters: {
						property: 'type',
						direction: 'ASC'
					}
				});
			},

			// TODO: to implement for dynamic columns object build with ExtJs grid column configuration
			getStoreColumns: function() {},

			remove: function(parameters) {},

			update: function(parameters) {}
		}
	});

})();