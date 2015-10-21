(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.user.User', {

		requires: [
			'CMDBuild.core.cache.Cache',
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

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.create
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		disable:function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.disable
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.User',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					}
				},
				sorters: [
					{ property: 'username', direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getDefaultGroupStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.user.DefaultGroup',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.getGroupList,
					reader: {
						root: 'result',
						type: 'json'
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

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.read
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.update
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		}
	});

})();