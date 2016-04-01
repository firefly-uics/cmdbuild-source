(function () {

	Ext.define('CMDBuild.core.configurationBuilders.Instance', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.GeneralOptions'
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
		enableServerCalls: true,

		/**
		 * @cfg {Object}
		 */
		scope: undefined,

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.enableServerCalls
		 * @param {Object} configurationObject.scope
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			if (this.enableServerCalls) {
				CMDBuild.core.proxy.configuration.GeneralOptions.read({
					loadMask: false,
					scope: this.scope || this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						CMDBuild.configuration.instance = Ext.create('CMDBuild.model.configuration.instance.Instance', decodedResponse);
					},
					callback: this.callback
				});
			} else { // Instance configuration model with defaults
				CMDBuild.configuration.instance = Ext.create('CMDBuild.model.configuration.instance.Instance');
			}
		}
	});

})();
