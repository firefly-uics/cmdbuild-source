(function() {

	Ext.define('CMDBuild.core.proxy.CMProxyTasks', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.model.CMModelTasks'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: this.getUrl(parameters.type).post,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				callback: parameters.callback
			});
		},

		/**
		 * @return {Ext.data.Store} store
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
		 * @return {Ext.data.Store} store
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
					property: CMDBuild.core.constants.Proxy.TYPE,
					direction: 'ASC'
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: this.getUrl(parameters.type).remove,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				callback: parameters.callback
			});
		},

		/**
		 * @param {Object} parameters
		 */
		start: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: this.getUrl('all').start,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				callback: parameters.callback
			});
		},

		/**
		 * @param {Object} parameters
		 */
		stop: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: this.getUrl('all').stop,
				params: parameters.params,
				scope: parameters.scope,
				success: parameters.success,
				callback: parameters.callback
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.Ajax.request({
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
		 * @param {String} type
		 *
		 * @return {String} url address
		 */
		getUrl: function(type) {
			switch (type) {
				case 'all':
					return CMDBuild.core.proxy.Index.tasks;

				case 'connector':
					return CMDBuild.core.proxy.Index.tasks.connector;

				case 'email':
					return CMDBuild.core.proxy.Index.tasks.email;

				case 'event':
					return CMDBuild.core.proxy.Index.tasks.event;

				case 'event_asynchronous':
					return CMDBuild.core.proxy.Index.tasks.event.asynchronous;

				case 'event_synchronous':
					return CMDBuild.core.proxy.Index.tasks.event.synchronous;

				case 'workflow':
					return CMDBuild.core.proxy.Index.tasks.workflow;

				default:
					throw 'CMProxyTasks error: url type not recognized';
			}
		},

		/**
		 * Connector specific proxies
		 */
			/**
			 * @return {Ext.data.Store} store
			 */
			getDbTypes: function() {
				return Ext.create('Ext.data.Store', {
					autoLoad: true,
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
			 * @return {Ext.data.SimpleStore} store
			 */
			getDeletionTypes: function() {
				var tr = CMDBuild.Translation.administration.tasks.taskConnector.deletionTypes;

				var store = Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
					data: [
						[tr.deleteCard, CMDBuild.core.constants.Proxy.DELETE_CARD],
						[tr.changeStatus, CMDBuild.core.constants.Proxy.CHANGE_STATUS]
					]
				});

				return store;
			},

			/**
			 * @return {Object} store
			 */
			getClassStore: function() {
				return _CMCache.getClassesStore();
			},

			/**
			 * @return {Ext.data.SimpleStore} store
			 */
			// TODO: implement real server call
			getSourceStore: function() {
//				return Ext.create('Ext.data.SimpleStore', {
//					fields: [CMDBuild.core.constants.Proxy.NAME],
//					data: [
//						['SourceName1'],
//						['SourceName2'],
//						['SourceName3']
//					]
//				});

				return null;
			},

			/**
			 * @return {Ext.data.SimpleStore} store
			 */
			// TODO: implement real server call
			getSourceAttributeNames: function(viewName) {
//				return Ext.create('Ext.data.SimpleStore', {
//					fields: [CMDBuild.core.constants.Proxy.NAME],
//					data: [
//						['SourceAttributeName1'],
//						['SourceAttributeName2'],
//						['SourceAttributeName3']
//					]
//				});

				return null;
			},

		/**
		 * Event specific proxies
		 */
			/**
			 * Returns synchronous event phases
			 *
			 * @return {Ext.data.SimpleStore} store
			 */
			getPhases: function() {
				var tr = CMDBuild.Translation.administration.tasks.taskEvent.eventPhases;

				return Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
					data: [
						[tr.afterCreate, CMDBuild.core.constants.Proxy.PHASE_AFTER_CREATE],
						[tr.afterUpdate, CMDBuild.core.constants.Proxy.PHASE_AFTER_UPDATE],
						[tr.beforeUpdate, CMDBuild.core.constants.Proxy.PHASE_BEFORE_UPDATE],
						[tr.beforeDelete, CMDBuild.core.constants.Proxy.PHASE_BEFORE_DELETE]
					]
				});
			},

		/**
		 * Workflow specific proxies
		 */
			/**
			 * Used from Processes -> Task Manager tab to get all processes by workflow name
			 *
			 * @return {Ext.data.Store} store
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
						property: CMDBuild.core.constants.Proxy.TYPE,
						direction: 'ASC'
					}
				});
			},

			/**
			 * @return {Ext.data.Store} store
			 */
			getStoreAllWorkflow: function() {
				var processes = _CMCache.getProcesses();
				var store = Ext.create('Ext.data.Store', {
					autoLoad: true,
					fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
					data: []
				});

				for (var key in processes) {
					var obj = processes[key];

					if (!obj.raw.superclass) {
						// Building object to add to store using proxy costants
						var bufferStoreObj = {};

						bufferStoreObj[CMDBuild.core.constants.Proxy.NAME] = _CMCache.getEntryTypeNameById(obj.raw.id);
						bufferStoreObj[CMDBuild.core.constants.Proxy.DESCRIPTION] = obj.raw.text;

						store.add(bufferStoreObj);
					}
				}

				return store;
			},

			/**
			 * @param {Object} parameters
			 */
			getWorkflowAttributes: function(parameters) {
				CMDBuild.core.Ajax.request({
					method: 'POST',
					url: CMDBuild.core.proxy.Index.attribute.read,
					params: parameters.params,
					scope: parameters.scope,
					success: parameters.success,
					callback: parameters.callback
				});
			}
	});

})();