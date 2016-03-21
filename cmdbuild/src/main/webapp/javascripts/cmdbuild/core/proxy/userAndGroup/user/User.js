(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.user.User', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.user.DefaultGroup',
			'CMDBuild.model.userAndGroup.user.User'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.user.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		disable: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.user.disable });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.USER, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.User',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: 'username', direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreDefaultGroup: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.USER, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.DefaultGroup',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.getGroupList,
					reader: {
						type: 'json',
						root: 'result'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
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
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.user.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.user.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		}
	});

})();
