(function() {

	Ext.define('CMDBuild.core.proxy.lookup.Lookup', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.lookup.GridStore',
			'CMDBuild.model.lookup.ParentComboStore'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		disable: function(parameters, disable) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.disable });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		enable: function(parameters, disable) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.enable });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getParentStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.ParentComboStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.getParentList,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					}
				},
				sorters: [
					{ property: 'ParentDescription', direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.lookup.GridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					actionMethods: 'POST' // Lookup types can have UTF-8 names not handled correctly
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		setOrder: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.setOrder });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP, parameters, true);
		}
	});

})();