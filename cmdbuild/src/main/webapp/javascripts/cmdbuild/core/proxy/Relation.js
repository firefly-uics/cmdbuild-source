(function () {

	Ext.define('CMDBuild.core.proxy.Relation', {

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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getAlreadyRelatedCards: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.getAlreadyRelatedCards });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		removeDetail: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.removeDetail });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.relations.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.RELATION, parameters, true);
		}
	});

})();
