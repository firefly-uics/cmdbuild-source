(function () {

	Ext.define('CMDBuild.proxy.common.field.translatable.Translatable', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.proxy.index.Json',
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.localization.translation.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.localization.translation.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOCALIZATION, parameters, true);
		}
	});

})();
