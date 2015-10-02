(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.UserInterface', {

		requires: [
			'CMDBuild.core.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.userInterface.getGroupUiConfiguration
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.userInterface.saveGroupUiConfiguration
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();