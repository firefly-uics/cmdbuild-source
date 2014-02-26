(function() {

	Ext.define('CMDBuild.core.serviceProxy.CMProxyTasks', {
		statics: {
			create: function(parameters) {},

			get: function() {},

			getStore: function(type) {
				var url = null;

				switch (type) {
					case 'email':
						url = CMDBuild.ServiceProxy.url.tasks.getEmailStore;
					break;

					case 'event':
						url = CMDBuild.ServiceProxy.url.tasks.getEventStore;
					break;

					case 'workflow':
						url = CMDBuild.ServiceProxy.url.tasks.getWorkflowStore;
					break;

					default:
						url = CMDBuild.ServiceProxy.url.tasks.getStore;
				}

				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelTasks.grid',
					proxy: {
						type: 'ajax',
						url: url,
						reader: {
							type: 'json',
							root: 'response.elements'
						}
					},
					sorters: {
						property: 'type',
						direction: 'ASC'
					}
				});
			},

			remove: function(parameters) {},

			update: function(parameters) {}
		}
	});

})();