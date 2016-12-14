(function () {

	Ext.define('CMDBuild.proxy.administration.taskManager.task.Generic', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.administration.taskManager.Grid',
			'CMDBuild.model.administration.taskManager.task.generic.Account',
			'CMDBuild.model.administration.taskManager.task.generic.Template',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.generic.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.TASK_MANAGER, {
				autoLoad: false,
				model: 'CMDBuild.model.administration.taskManager.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.taskManager.generic.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE + '.' + CMDBuild.core.constants.Proxy.ELEMENTS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TYPE, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreAccount: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: true,
				model: 'CMDBuild.model.administration.taskManager.task.generic.Account',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.email.account.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE + '.' + CMDBuild.core.constants.Proxy.ELEMENTS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreTemplate: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: true,
				model: 'CMDBuild.model.administration.taskManager.task.generic.Template',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.email.template.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE + '.' + CMDBuild.core.constants.Proxy.ELEMENTS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.generic.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.generic.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.generic.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		}
	});

})();
