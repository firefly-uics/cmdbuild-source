(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.UserInterface', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.userInterface.getGroupUiConfiguration
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.userInterface.saveGroupUiConfiguration
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();