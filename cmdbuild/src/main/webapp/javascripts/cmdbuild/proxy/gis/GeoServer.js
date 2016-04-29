(function () {

	Ext.define('CMDBuild.proxy.gis.GeoServer', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		createLayer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.create });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store} store
		 */
		getStore: function () {
			var store =  CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.GIS, {
				model: 'GISLayerModel',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.gis.geoServer.layer.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.LAYERS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.INDEX, direction: 'ASC' }
				]
			});

			var reload = function(o) {
				if (o) {
					this.nameToSelect = o.nameToSelect;
				}
				this.reload();
			};

			_CMEventBus.subscribe('cmdb-modified-geoserverlayers', reload, store);

			return store;
		},

		/**
		 * Remove layer
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GIS, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		updateLayer: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.gis.geoServer.layer.update });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
