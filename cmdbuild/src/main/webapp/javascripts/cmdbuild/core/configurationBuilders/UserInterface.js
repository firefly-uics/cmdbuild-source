(function() {

	Ext.define('CMDBuild.core.configurationBuilders.UserInterface', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		/**
		 * @cfg {Function}
		 */
		callback: Ext.emptyFn,

		/**
		 * @param {Object} configuration
		 * @param {Function} configuration.callback
		 */
		constructor: function(configuration) {
			Ext.apply(this, configuration); // Apply configuration

			Ext.ns('CMDBuild.configuration');

			CMDBuild.core.proxy.userAndGroup.group.Group.getUIConfiguration({
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					CMDBuild.configuration.userInterface = Ext.create('CMDBuild.model.configuration.userInterface.UserInterface', decodedResponse);
				},
				callback: this.callback
			});
		}
	});

})();