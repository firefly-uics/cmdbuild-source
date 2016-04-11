(function () {

	Ext.define('CMDBuild.core.proxy.dashboard.Dashboard', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dashboard.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dashboard.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dashboard.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dashboard.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		updateColumns : function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dashboard.columns.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, parameters, true);
		}
	});

})();
