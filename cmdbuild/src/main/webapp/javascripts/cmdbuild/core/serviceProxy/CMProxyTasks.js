(function() {

	Ext.define('CMDBuild.core.serviceProxy.CMProxyTasks', {
		statics: {
			create: function(parameters) {},

			get: function() {},

			getStore: function(type) {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelTasks.grid',
					proxy: {
						type: 'ajax',
						url: this.getUrl(type).getStore,
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

			update: function(parameters) {},

			// Other proxy functions extra CRUD
			getUrl: function(type) {
				switch (type) {
					case 'email':
						return CMDBuild.ServiceProxy.url.tasks.email;

					case 'event':
						return CMDBuild.ServiceProxy.url.tasks.event;

					case 'workflow':
						return CMDBuild.ServiceProxy.url.tasks.workflow;

					default:
						return CMDBuild.ServiceProxy.url.tasks;
				}
			},

			getWorkflowsStore: function() {
				var processes = _CMCache.getProcesses(),
					data = [];

				for (var key in processes) {
					var obj = processes[key];

					if (obj.raw.superclass)
						continue;

					data.push({
						id: obj.raw.id,
						description: obj.raw.text
					});
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.ID, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: data,
					autoLoad: true
				});
			},

			getWorkflowAttributes: function(parameters) {
				Ext.Ajax.request({
					url: CMDBuild.ServiceProxy.url.attribute.read,
					params: parameters.params,
					success: parameters.success
				});
			}
		}
	});

})();