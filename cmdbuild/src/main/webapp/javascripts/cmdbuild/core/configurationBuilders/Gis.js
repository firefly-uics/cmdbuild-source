(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Gis', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Gis',
			'CMDBuild.core.proxy.Gis'
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

				CMDBuild.core.proxy.Gis.readTreeNavigation({
					loadMask: false,
					success: function(response, options, decodedResponse) {
						dataObject[CMDBuild.core.constants.Proxy.CARD_BROWSER_BY_DOMAIN_CONFIGURATION] = {
							geoServerLayersMapping: decodedResponse[CMDBuild.core.constants.Proxy.GEO_SERVER_LAYERS_MAPPING],
							root: decodedResponse[CMDBuild.core.constants.Proxy.ROOT]
						};

						CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS] = Ext.create('CMDBuild.model.configuration.gis.Gis', dataObject);
					},
					callback: callback
				});
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function() {
				if (CMDBuild.core.configurationBuilders.Gis.isValid())
					delete CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS];
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function() {
				return !Ext.isEmpty(CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS]);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configuration

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.GIS] = Ext.create('CMDBuild.model.configuration.gis.Gis'); // Configuration object

			CMDBuild.core.proxy.configuration.Gis.read({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.configurationBuilders.Gis.build(decodedResponse, this.callback);
				}
			});
		}
	});

})();