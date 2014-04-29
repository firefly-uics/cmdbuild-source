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
						property: CMDBuild.ServiceProxy.parameter.TYPE,
						direction: 'ASC'
					}
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
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks;

					case 'connector': // TODO
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.connector;

					case 'email':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.email;

					case 'event': // TODO
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event;

					case 'event_asynchronous': // TODO
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event;

					case 'event_synchronous': // TODO
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event;

					case 'workflow':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.workflow;

					default:
						throw 'CMProxyTasks error: url type not recognized';
				}
			},

			/**
			 * Connector specific proxies
			 */
			getConnectorOperations: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.VALUE],
					data: [
						{ name: 'New cards', value: 'NewCards' },
						{ name: 'Edited cards', value: 'EditedCards' },
						{ name: 'Deleted not matching cards', value: 'DeletedNotMatchingCards' }
					]
				});
			},

			getDbTypes: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.VALUE],
					data: [
						{ name: 'MySQL', value: 'mysql' },
						{ name: 'Oracle', value: 'oracle' },
						{ name: 'PostgreSQL', value: 'postgresql' },
						{ name: 'SQLServer', value: 'sqlserver' }
					]
				});
			},
// TODO: future implementation
//			getFunctionStore: function() {
//				return Ext.create('Ext.data.Store', {
//					autoLoad: true,
//					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
//					data: [
//						{ name: 'Function1', description: 'Function 1' },
//						{ name: 'Function2', description: 'Function 2' },
//						{ name: 'Function3', description: 'Function 3' }
//					]
//				});
//			},

			getViewStore: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						{ name: 'ViewName1', description: 'View name 1' },
						{ name: 'ViewName2', description: 'View name 2' },
						{ name: 'ViewName3', description: 'View name 3' }
					]
				});
			},

			getViewAttributeNames: function(viewName) {
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.NAME, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						{ name: 'ViewAttributeName1', description: 'View attribute name 1' },
						{ name: 'ViewAttributeName2', description: 'View attribute name 2' },
						{ name: 'ViewAttributeName3', description: 'View attribute name 3' }
					]
				});
			},

			/**
			 * Event specific proxies
			 */
			getPhases: function(viewName) {
				var tr = CMDBuild.Translation.administration.tasks;

				return Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.ServiceProxy.parameter.DESCRIPTION, CMDBuild.ServiceProxy.parameter.VALUE],
					data: [
						{ description: tr.taskEvent.afterCreate, value: 'afterCreate' },
						{ description: tr.taskEvent.afterUpdate, value: 'afterUpdate' },
						{ description: tr.taskEvent.beforeUpdate, value: 'beforeUpdate' },
						{ description: tr.taskEvent.beforeDelete, value: 'beforeDelete' }
					]
				});
			},

			/**
			 * Workflow specific proxies
			 */
			// Used from Processes -> Task Manager tab to get all processes by workflow name
			getStoreByWorkflow: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: false,
					model: 'CMDBuild.model.CMModelTasks.grid.workflow',
					proxy: {
						type: 'ajax',
						url: this.getUrl('workflow').getStoreByWorkflow,
						reader: {
							type: 'json',
							root: 'response'
						}
					},
					sorters: {
						property: CMDBuild.ServiceProxy.parameter.TYPE,
						direction: 'ASC'
					}
				});
			},

			getStoreAllWorkflow: function() {
				var processes = _CMCache.getProcesses();
				var data = [];

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