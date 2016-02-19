(function() {

	Ext.define('CMDBuild.core.proxy.widget.Widget', {

		requires: [
			'CMDBuild.core.interfaces.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.widget.DefinitionGrid'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @return {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WIDGET, {
				autoLoad: false,
				model: 'CMDBuild.model.widget.DefinitionGrid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.widget.readAllForClass,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.RESPONSE
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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * TODO: temporary
		 */
		setSorting: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.setSorting });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.widget.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WIDGET, parameters, true);
		}
	});

})();
