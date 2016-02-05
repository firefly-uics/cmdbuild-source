(function() {

	Ext.define('CMDBuild.core.configurationBuilders.UserInterface', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.Group'
		],

		/**
		 * Build user interface configuration model
		 */
		constructor: function() {
			if (!Ext.isEmpty(CMDBuild) && !Ext.isEmpty(CMDBuild.configuration)) {
				CMDBuild.core.proxy.group.Group.getUIConfiguration({
					scope: this,
					success: function(result, options, decodedResult) {
						decodedResult = decodedResult.response;

						CMDBuild.configuration.userInterface = Ext.create('CMDBuild.model.configuration.userInterface.UserInterface', decodedResult);
					}
				});
			} else {
				_error('CMDBuild or CMDBuild.configuration objects is empty', this);
			}
		}
	});

})();