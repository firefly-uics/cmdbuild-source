(function() {

	Ext.define('CMDBuild.core.proxy.Menu', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * Read the menu designed for this group. If there are no menu, a default menu is returned. If the configuration of the menu contains some node
		 * but the group has not the privileges to use it this method does not add it to the menu
		 *
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.menu.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * Read the items that are not added to the current menu configuration
		 *
		 * @param {Object} parameters
		 */
		readAvailableItems: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.menu.readAvailableItems });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * Read the full configuration designed for the given group.
		 *
		 * @param {Object} parameters
		 */
		readConfiguration: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.menu.readConfiguration });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.menu.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		save: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.menu.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.MENU, parameters, true);
		}
	});

})();
