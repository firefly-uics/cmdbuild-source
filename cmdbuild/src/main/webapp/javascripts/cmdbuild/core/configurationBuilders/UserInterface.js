(function() {

	Ext.define('CMDBuild.core.configurationBuilders.UserInterface', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.group.Group'
		],

		constructor: function() {
			Ext.ns('CMDBuild.configuration');

			CMDBuild.core.proxy.userAndGroup.group.Group.getUIConfiguration({
				scope: this,
				success: function(response, options, decodedResponse) {
					decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE];

					CMDBuild.configuration.userInterface = Ext.create('CMDBuild.model.configuration.userInterface.UserInterface', decodedResponse);
				}
			});
		}
	});

})();