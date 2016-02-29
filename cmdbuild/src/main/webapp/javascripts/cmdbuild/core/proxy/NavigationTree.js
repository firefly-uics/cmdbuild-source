(function() {

	Ext.define('CMDBuild.core.proxy.NavigationTree', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.navigationTree.TargetClassStore'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTree.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreTargetClass: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.navigationTree.TargetClassStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function (record) { // Filters simple classes
						return record.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable();
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTree.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTree.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function (parameters, success) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTree.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.navigationTree.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.NAVIGATION_TREE, parameters, true);
		}
	});

})();