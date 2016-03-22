(function () {

	Ext.define('CMDBuild.core.proxy.lookup.Type', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.lookup.Type'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.type.create });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP_TYPE, parameters, true);
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getStore: function () {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.lookup.Type',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.lookup.type.readAll,
					reader: {
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
		read: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.type.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP_TYPE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.type.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP_TYPE, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.lookup.type.update });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.LOOKUP_TYPE, parameters, true);
		}
	});

})();
