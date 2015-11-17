(function() {

	Ext.define('CMDBuild.core.proxy.dataView.Filter', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.dataView.filter.GridStore',
			'CMDBuild.model.dataView.filter.SourceClass'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.filter.create });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.DATA_VIEW, {
				autoLoad: false,
				model: 'CMDBuild.model.dataView.filter.GridStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.dataView.filter.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.VIEWS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * Creates store with Classes, Processes andDashboards
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreSourceClass: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.dataView.filter.SourceClass',
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
					function(record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != 'Class';
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.NAME, direction: 'ASC' }
				],

				listeners: {
					load: function(store, records, successful, eOpts) { // Add Dashboards items
						CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DASHBOARD, {
							url: CMDBuild.core.proxy.Index.dashboard.readAll,
							scope: this,
							success: function(response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.RESPONSE][CMDBuild.core.constants.Proxy.DASHBOARDS];

								if (!Ext.Object.isEmpty(decodedResponse))
									Ext.Object.each(decodedResponse, function(id, dashboardObject, myself) {
										if (!Ext.Object.isEmpty(dashboardObject)) {
											dashboardObject[CMDBuild.core.constants.Proxy.ID] = id;
											dashboardObject[CMDBuild.core.constants.Proxy.TEXT] = dashboardObject[CMDBuild.core.constants.Proxy.DESCRIPTION];

											store.add(dashboardObject);
										}
									}, this);
							}
						});
					}
				}
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.filter.read });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.filter.remove });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.dataView.filter.update });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.DATA_VIEW, parameters, true);
		}
	});

})();