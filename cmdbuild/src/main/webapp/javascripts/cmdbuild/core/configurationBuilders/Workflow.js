(function() {

	Ext.define('CMDBuild.core.configurationBuilders.Workflow', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.configuration.Workflow'
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

				CMDBuild.configuration[CMDBuild.core.constants.Proxy.WORKFLOW] = Ext.create('CMDBuild.model.configuration.workflow.Workflow', dataObject);
			},

			/**
			 * Invalidate configuration object
			 */
			invalid: function() {
				if (CMDBuild.core.configurationBuilders.Workflow.isValid())
					delete CMDBuild.configuration[CMDBuild.core.constants.Proxy.WORKFLOW];
			},

			/**
			 * @returns {Boolean}
			 */
			isValid: function() {
				return !Ext.isEmpty(CMDBuild.configuration[CMDBuild.core.constants.Proxy.WORKFLOW]);
			}
		},

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.configuration[CMDBuild.core.constants.Proxy.WORKFLOW] = Ext.create('CMDBuild.model.configuration.workflow.Workflow'); // Workflow configuration object

			CMDBuild.core.proxy.configuration.Workflow.read({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.core.configurationBuilders.Workflow.build(decodedResponse);
				},
				callback: this.callback
			});
		}
	});

})();