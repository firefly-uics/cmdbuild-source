(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.privileges.Classes', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.group.privileges.GridRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.group.privileges.GridRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.privileges.classes.read,
					reader: {
						type: 'json',
						root: 'privileges'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readUIConfiguration: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.classes.loadClassUiConfiguration
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		setRowAndColumn: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.classes.setRowAndColumnPrivileges
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.classes.update
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		updateUIConfiguration: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.privileges.classes.saveClassUiConfiguration
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();