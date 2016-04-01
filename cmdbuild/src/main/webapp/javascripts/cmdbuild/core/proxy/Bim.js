(function () {

	Ext.define('CMDBuild.core.proxy.Bim', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readRootLayer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.bim.readRootLayer });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.BIM, parameters);
		}
	});

})();
