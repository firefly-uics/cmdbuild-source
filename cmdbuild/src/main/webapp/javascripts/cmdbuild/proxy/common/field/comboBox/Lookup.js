(function () {

	Ext.define('CMDBuild.proxy.common.field.comboBox.Lookup', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.comboBox.lookup.Value',
			'CMDBuild.proxy.index.Json'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 * @param {Boolean} parameters.active
		 * @param {Boolean} parameters.short
		 * @param {String} parameters.type
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function (parameters) {
			parameters: Ext.isObject(parameters) ? parameters : {};
			parameters.active = Ext.isBoolean(parameters.active) ? parameters.active : true;
			parameters.short = Ext.isBoolean(parameters.short) ? parameters.short : true;

			// Error handling
				if (!Ext.isString(parameters.type) || Ext.isEmpty(parameters.type))
					return _error('getStoreTypes(): unmanaged type parameter', this, parameters.type);
			// END: Error handling

			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.LOOKUP, {
				autoLoad: true,
				model: 'CMDBuild.model.common.field.comboBox.lookup.Value',
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.lookup.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: parameters,
				},
				sorters: [
					{ property: 'Number', direction: 'ASC' },
					{ property: 'Description', direction: 'ASC' }
				]
			});
		}
	});

})();
