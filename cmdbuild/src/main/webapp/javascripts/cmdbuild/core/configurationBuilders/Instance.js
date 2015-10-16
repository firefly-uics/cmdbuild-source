(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Instance', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * Enable or disable server calls (set as false within contexts where server calls aren't enabled)
		 *
		 * @cfg {Boolean}
		 */
		fullInit: true,

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.fullInit
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE] = Ext.create('CMDBuild.model.configuration.Instance'); // Instance configuration object

			if (this.fullInit)
				CMDBuild.core.proxy.Configuration.readMainConfiguration({
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE] = Ext.create('CMDBuild.model.configuration.Instance', decodedResponse);
					},
					callback: this.callback
				});
		}
	});

})();