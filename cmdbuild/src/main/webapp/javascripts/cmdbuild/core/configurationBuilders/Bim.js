(function () {

	Ext.define('CMDBuild.core.configurationBuilders.Bim', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Bim',
			'CMDBuild.core.proxy.configuration.Bim'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * @cfg {Object}
		 */
		scope: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Function} configurationObject.callback
		 * @param {Object} configurationObject.scope
		 *
		 * @returns {Void}
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configuration

			Ext.ns('CMDBuild.configuration');

			CMDBuild.core.proxy.configuration.Bim.read({
				loadMask: false,
				scope: this,
				success: function (response, options, decodedResponse) {
					var bimConfigurationObject = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					if (!Ext.isEmpty(bimConfigurationObject[CMDBuild.core.constants.Proxy.DATA]))
						bimConfigurationObject = bimConfigurationObject[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.proxy.Bim.readRootLayer({
						loadMask: false,
						scope: this.scope || this,
						success: function (response, options, decodedResponse) {
							bimConfigurationObject[CMDBuild.core.constants.Proxy.ROOT_CLASS] = decodedResponse[CMDBuild.core.constants.Proxy.ROOT];

							CMDBuild.configuration.bim = Ext.create('CMDBuild.model.configuration.bim.Bim', bimConfigurationObject); // Configuration model
						},
						callback: this.callback
					});
				}
			});
		}
	});

})();
