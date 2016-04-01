(function () {

	Ext.define('CMDBuild.core.configurationBuilders.RelationGraph', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.RelationGraph'
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
			Ext.apply(this, configurationObject); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.core.proxy.configuration.RelationGraph.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration.graph = Ext.create('CMDBuild.model.configuration.relationGraph.RelationGraph', decodedResponse); // Configuration model
				},
				callback: this.callback
			});
		}
	});

})();