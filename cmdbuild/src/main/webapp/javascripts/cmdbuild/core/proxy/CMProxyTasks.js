(function() {

	Ext.require('CMDBuild.model.CMModelTasks');

	Ext.define('CMDBuild.core.proxy.CMProxyTasks', {
		statics: {

			/**
			 * @param (Object) parameters
			 */
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

			/**
			 * @return (Object) store
			 */
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

			/**
			 * @return (Object) store
			 */
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
						property: CMDBuild.core.proxy.CMProxyConstants.TYPE,
						direction: 'ASC'
					}
				});
			},

			/**
			 * @param (Object) parameters
			 */
			remove: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: this.getUrl(parameters.type).delete,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @param (Object) parameters
			 */
			start: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: this.getUrl('all').start,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @param (Object) parameters
			 */
			stop: function(parameters) {
				CMDBuild.Ajax.request({
					method: 'POST',
					url: this.getUrl('all').stop,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			},

			/**
			 * @param (Object) parameters
			 */
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

			/**
			 * To transform type in server call url
			 *
			 * @param (String) type
			 *
			 * @return (Var) url address
			 */
			getUrl: function(type) {
				switch (type) {
					case 'all':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks;

					case 'connector':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.connector;

					case 'email':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.email;

					case 'event':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event;

					case 'event_asynchronous': // TODO
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event.asynchronous;

					case 'event_synchronous':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.event.synchronous;

					case 'workflow':
						return CMDBuild.core.proxy.CMProxyUrlIndex.tasks.workflow;

					default:
						throw 'CMProxyTasks error: url type not recognized';
				}
			},

			/**
			 * Connector specific proxies
			 */
				/**
				 * @return (Object) store
				 */
				// TODO: setup string as proxyConstant
				getDbTypes: function() {
					return Ext.create('Ext.data.Store', {
						autoLoad: false,
						model: 'CMDBuild.model.CMModelTasks.connector.availableSqlSources',
						proxy: {
							type: 'ajax',
							url: this.getUrl('connector').getSqlSources,
							reader: {
								type: 'json',
								root: 'response'
							}
						}
					});
				},

				/**
				 * @return (Object) store
				 */
				getDeletionTypes: function() {
					var tr = CMDBuild.Translation.administration.tasks.taskConnector.deletionTypes;

					return Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.VALUE],
						data: [
							{ description: tr.logic, value: 'logic' },
							{ description: tr.notLogic, value: 'notLogic' }
						]
					});
				},

				/**
				 * @return (Object) store
				 */
				getClassStore: function() {
					return _CMCache.getClassesStore();
				},

				/**
				 * @return (Object) store
				 */
				// TODO: delete or implement real server call
				getSourceStore: function() {
					return Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
						data: [
							{ name: 'SourceName1' },
							{ name: 'SourceName2' },
							{ name: 'SourceName3' }
						]
					});
				},

				/**
				 * @return (Object) store
				 */
				// TODO: delete or implement real server call
				getSourceAttributeNames: function(viewName) {
					return Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.NAME],
						data: [
							{ name: 'SourceAttributeName1' },
							{ name: 'SourceAttributeName2' },
							{ name: 'SourceAttributeName3' }
						]
					});
				},

			/**
			 * Event specific proxies
			 */
				/**
				 * Returns synchronous event phases
				 *
				 * @return (Object) store
				 */
				getPhases: function() {
					var tr = CMDBuild.Translation.administration.tasks.taskEvent.eventPhases;

					return Ext.create('Ext.data.Store', {
						autoLoad: true,
						fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.VALUE],
						data: [
							{ description: tr.afterCreate, value: 'afterCreate' },
							{ description: tr.afterUpdate, value: 'afterUpdate' },
							{ description: tr.beforeUpdate, value: 'beforeUpdate' },
							{ description: tr.beforeDelete, value: 'beforeDelete' }
						]
					});
				},

			/**
			 * Workflow specific proxies
			 */
				/**
				 * Used from Processes -> Task Manager tab to get all processes by workflow name
				 *
				 * @return (Object) store
				 */
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
							property: CMDBuild.core.proxy.CMProxyConstants.TYPE,
							direction: 'ASC'
						}
					});
				},

				/**
				 * @return (Object) store
				 */
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
						fields: [CMDBuild.core.proxy.CMProxyConstants.NAME, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
						data: data,
						autoLoad: true
					});
				},

				/**
				 * @param (Object) parameters
				 */
				getWorkflowAttributes: function(parameters) {
					CMDBuild.Ajax.request({
						method: 'POST',
						url: CMDBuild.core.proxy.CMProxyUrlIndex.attribute.read,
						params: parameters.params,
						scope: parameters.scope,
						success: parameters.success,
						callback: parameters.callback
					});
				}
		}
	});

})();