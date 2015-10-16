(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Bim', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		statics: {
			/**
			 * Rebuild configuration object
			 *
			 * @param {Object} dataObject
			 */
			build: function(dataObject) {
				if (!Ext.isEmpty(dataObject[CMDBuild.core.constants.Proxy.DATA]))
					dataObject = dataObject[CMDBuild.core.constants.Proxy.DATA];

				CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM] = Ext.create('CMDBuild.model.configuration.bim.Bim', dataObject);
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function() {
				if (CMDBuild.core.configurationBuilders.Bim.isValid())
					delete CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM];
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function() {
				return !Ext.isEmpty(CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM]);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configuration

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM] = Ext.create('CMDBuild.model.configuration.bim.Bim'); // BIM configuration object

			CMDBuild.core.proxy.Configuration.readBimConfiguration({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.configurationBuilders.Bim.build(decodedResponse);
				},
				callback: this.callback
			});
		}
	});

})();