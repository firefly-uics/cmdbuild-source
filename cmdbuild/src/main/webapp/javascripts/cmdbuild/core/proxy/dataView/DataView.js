(function () {

	Ext.define('CMDBuild.core.proxy.dataView.DataView', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json'
		],

		singleton: true,

		/**
		 * Read all the data view available for the logged user
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.dataView.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters);
		}
	});

})();
