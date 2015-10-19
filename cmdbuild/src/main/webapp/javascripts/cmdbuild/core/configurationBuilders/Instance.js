(function() {

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
		fullInit: true,

		statics: {
			/**
			 * Rebuild configuration object
			 *
			 * @param {Object} dataObject
			 */
			build: function(dataObject) {
				if (!Ext.isEmpty(dataObject[CMDBuild.core.constants.Proxy.DATA]))
					dataObject = dataObject[CMDBuild.core.constants.Proxy.DATA];

				CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE] = Ext.create('CMDBuild.model.configuration.instance.Instance', dataObject);
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function() {
				if (CMDBuild.core.configurationBuilders.Instance.isValid())
					delete CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE];
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function() {
				return !Ext.isEmpty(CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE]);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.fullInit
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.INSTANCE] = Ext.create('CMDBuild.model.configuration.instance.Instance'); // Instance configuration object

			if (this.fullInit)
				CMDBuild.core.proxy.configuration.GeneralOptions.read({
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						CMDBuild.core.configurationBuilders.Instance.build(decodedResponse);
					},
					callback: this.callback
				});
		}
	});

})();