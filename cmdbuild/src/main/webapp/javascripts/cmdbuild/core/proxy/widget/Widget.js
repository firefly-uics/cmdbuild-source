(function() {

	Ext.define('CMDBuild.core.proxy.widget.Widget', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @return {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() { // TODO: waiting for refactor (CRUD)
//			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WIDGET, {
//				autoLoad: false,
//				model: 'CMDBuild.model.filter.group.Store',
//				proxy: {
//					type: 'ajax',
//					url: CMDBuild.core.proxy.Index.widget.readAll,
//					reader: {
//						type: 'json',
//						root: CMDBuild.core.constants.Proxy.FILTERS
//					}
//				},
//				sorters: [
//					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
//				]
//			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * TODO: temporary
		 */
		setSorting: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.setSorting });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		}
	});

})();