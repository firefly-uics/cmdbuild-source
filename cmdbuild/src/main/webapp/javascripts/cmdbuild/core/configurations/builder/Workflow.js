(function () {

	Ext.define('CMDBuild.core.configurations.builder.Workflow', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.core.configurations.builder.Workflow'
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
		 */
		constructor: function (configurationObject) {
			Ext.apply(this, configurationObject); // Apply configurations

			Ext.ns('CMDBuild.configuration');

			CMDBuild.core.proxy.core.configurations.builder.Workflow.read({
				loadMask: false,
				scope: this.scope || this,
				success: function (response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

					CMDBuild.configuration.workflow = Ext.create('CMDBuild.model.core.configurations.builder.Workflow', decodedResponse); // Configuration model
				},
				callback: this.callback
			});
		}
	});

})();