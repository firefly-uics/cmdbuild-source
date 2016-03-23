(function() {

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

		statics: {
			/**
			 * Rebuild configuration object
			 *
			 * @param {Object} dataObject
			 * @param {Function} callback
			 */
			build: function(dataObject, callback) {
				callback = callback || Ext.emptyFn;

				if (!Ext.isEmpty(dataObject[CMDBuild.core.constants.Proxy.DATA]))
					dataObject = dataObject[CMDBuild.core.constants.Proxy.DATA];

				CMDBuild.core.proxy.Bim.readRootLayer({
					loadMask: false,
					success: function(response, options, decodedResponse) {
						dataObject[CMDBuild.core.constants.Proxy.ROOT_CLASS] = decodedResponse[CMDBuild.core.constants.Proxy.ROOT];

						CMDBuild.configuration[CMDBuild.core.constants.Proxy.BIM] = Ext.create('CMDBuild.model.configuration.bim.Bim', dataObject);
					},
					callback: callback
				});
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

			CMDBuild.core.proxy.configuration.Bim.read({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.configurationBuilders.Bim.build(decodedResponse, this.callback);
				}
			});
		}
	});

})();