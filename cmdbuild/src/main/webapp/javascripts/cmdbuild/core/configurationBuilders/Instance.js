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

		statics: {
			/**
			 * Rebuild configuration object
			 *
			 * @param {Object} dataObject
			 */
			build: function (dataObject) {
				if (!Ext.isEmpty(dataObject[CMDBuild.core.constants.Proxy.DATA]))
					dataObject = dataObject[CMDBuild.core.constants.Proxy.DATA];

				CMDBuild.configuration.instance = Ext.create('CMDBuild.model.configuration.instance.Instance', dataObject);
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function () {
				if (CMDBuild.core.configurationBuilders.Instance.isValid())
					delete CMDBuild.configuration.instance;
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function () {
				return !Ext.isEmpty(CMDBuild.configuration.instance);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 * @param {Boolean} configuration.enableServerCalls
		 *
		 * @override
		 */
		constructor: function (configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');
			CMDBuild.configuration.instance = Ext.create('CMDBuild.model.configuration.instance.Instance'); // Instance configuration object

			if (this.enableServerCalls)
				CMDBuild.core.proxy.configuration.GeneralOptions.read({
					loadMask: false,
					scope: this,
					success: function (response, options, decodedResponse) {
						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

						CMDBuild.core.configurationBuilders.Instance.build(decodedResponse);
					},
					callback: this.callback
				});
		}
	});

})();
