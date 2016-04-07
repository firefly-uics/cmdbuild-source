(function () {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Email', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.tabs.email.Email'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.post
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.EMAIL, {
				autoLoad: false,
				model: 'CMDBuild.model.common.tabs.email.Email',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.email.getStore,
					reader: {
						root: 'response',
						type: 'json'
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: {
					property: CMDBuild.core.constants.Proxy.STATUS,
					direction: 'ASC'
				},
				groupField: CMDBuild.core.constants.Proxy.STATUS
			});
		},

		/**
		 * @param {Object} parameters
		 */
		isEmailEnabledForCard: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.enabled
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.remove
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.put
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.EMAIL, parameters, true);
		}
	});

})();
