(function () {

	Ext.define('CMDBuild.proxy.workflow.management.panel.form.tabs.Relations', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getAlreadyRelatedCards: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.relation.readAlreadyRelatedCards });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.relation.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.relation.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		}
	});

})();
