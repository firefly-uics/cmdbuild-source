(function() {

	Ext.define('CMDBuild.core.proxy.User', {

		requires: [
			'CMDBuild.core.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.user.DefaultGroup',
			'CMDBuild.model.user.User'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.create
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		disable:function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.disable
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.user.User',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.user.readAll,
					reader: {
						type: 'json',
						root: 'rows'
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
				model: 'CMDBuild.model.user.DefaultGroup',
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
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.read
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.user.update
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.USER, parameters, true);
		}
	});

})();