(function() {

	Ext.define('CMDBuild.core.proxy.email.Template', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.email.template.Store'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.templates.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Boolean} autoLoad
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function(autoLoad) {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: autoLoad || false,
				model: 'CMDBuild.model.email.template.Store',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.email.templates.readAll,
					reader: {
						type: 'json',
						root: 'response.elements'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
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

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.templates.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.templates.readAll });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.templates.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.templates.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		}
	});

})();