(function () {

	Ext.define('CMDBuild.core.proxy.gis.GeoAttribute', {

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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.gis.geoAttribute.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		modify: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.gis.geoAttribute.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.gis.geoAttribute.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters, true);
		}
	});

})();
