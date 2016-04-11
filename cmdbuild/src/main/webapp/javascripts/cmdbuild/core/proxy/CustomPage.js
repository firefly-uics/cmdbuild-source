(function () {

	Ext.define('CMDBuild.core.proxy.CustomPage', {

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
		readForCurrentUser: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.customPage.readForCurrentUser });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CUSTOM_PAGE, parameters);
		}
	});

})();
