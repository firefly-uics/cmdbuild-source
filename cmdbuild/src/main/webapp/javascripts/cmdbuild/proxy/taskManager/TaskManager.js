(function () {

	Ext.define('CMDBuild.proxy.taskManager.TaskManager', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.model.taskManager.Grid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.TASK_MANAGER, {
				autoLoad: false,
				model: 'CMDBuild.model.taskManager.Grid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.taskManager.readAll,
					reader: {
						type: 'json',
						root: 'response.elements'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TYPE, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		start: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.start });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		stop: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.taskManager.stop });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.TASK_MANAGER, parameters, true);
		}
	});

})();
