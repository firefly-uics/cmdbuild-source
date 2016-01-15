(function() {

	Ext.define('CMDBuild.core.configurationBuilders.RelationGraph', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.RelationGraph'
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

				CMDBuild.configuration[CMDBuild.core.constants.Proxy.GRAPH] = Ext.create('CMDBuild.model.configuration.relationGraph.RelationGraph', dataObject);
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function() {
				if (CMDBuild.core.configurationBuilders.RelationGraph.isValid())
					delete CMDBuild.configuration[CMDBuild.core.constants.Proxy.GRAPH];
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function() {
				return !Ext.isEmpty(CMDBuild.configuration[CMDBuild.core.constants.Proxy.GRAPH]);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.GRAPH] = Ext.create('CMDBuild.model.configuration.relationGraph.RelationGraph'); // RelationGraph configuration object

			CMDBuild.core.proxy.configuration.RelationGraph.read({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.configurationBuilders.RelationGraph.build(decodedResponse);
				},
				callback: this.callback
			});
		}
	});

})();