(function() {

	Ext.require('CMDBuild.model.CMModelTasks');

	Ext.define('CMDBuild.core.proxy.CMProxyTasks', {
		statics: {
			create: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: this.getUrl(parameters.type).post,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			get: function(type) {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelTasks.singleTask.' + type,
					proxy: {
						type: 'ajax',
						url: this.getUrl(type).get,
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

			getStoreByWorkflow: function() {
//				return Ext.create('Ext.data.Store', {
//					autoLoad: false,
//					model: 'CMDBuild.model.CMModelTasks.grid.workflow',
//					proxy: {
//						type: 'ajax',
//						url: this.getUrl('workflow').getStorebyWorkflow,
//						reader: {
//							type: 'json',
//							root: 'response.elements'
//						}
//					},
//					sorters: {
//						property: 'type',
//						direction: 'ASC'
//					}
//				});
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: ['id', 'description', 'active'],
					data: [
						{ 'id': 234, 'description': 'Workflow task 1', 'active': true },
						{ 'id': 123, 'description': 'Workflow task 2', 'active': true },
						{ 'id': 120, 'description': 'Workflow task 3', 'active': false },
						{ 'id': 1570, 'description': 'Workflow task 4', 'active': false },
						{ 'id': 456, 'description': 'Workflow task 5', 'active': false },
						{ 'id': 654, 'description': 'Workflow task 6', 'active': true }
					]
				});
			},

			remove: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: this.getUrl(parameters.type).delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			start: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: this.getUrl('all').start,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			stop: function(parameters) {
				CMDBuild.ServiceProxy.core.doRequest({
					method: 'POST',
					url: this.getUrl('all').stop,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			update: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: this.getUrl(parameters.type).put,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			getUrl: function(type) {
				switch (type) {
					case 'all':
						return CMDBuild.ServiceProxy.url.tasks;

					case 'email':
						return CMDBuild.ServiceProxy.url.tasks.email;

					case 'event':
						return CMDBuild.ServiceProxy.url.tasks.event;

					case 'workflow':
						return CMDBuild.ServiceProxy.url.tasks.workflow;

					default:
						throw 'CMProxyTasks error: url type not recognized';
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
						name: _CMCache.getEntryTypeNameById(obj.raw.id),
						description: obj.raw.text
					});
				}

				return Ext.create('Ext.data.Store', {
					fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
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