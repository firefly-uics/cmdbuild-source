(function () {

	Ext.define('CMDBuild.core.proxy.core.configurations.builder.UserInterface', {

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
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.group.getUiConfiguration });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CONFIGURATION, parameters);
		}
	});

})();
