(function () {

	Ext.define('CMDBuild.core.proxy.common.field.ForeignKey', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.common.attributes.ForeignKeyStore'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @return {Ext.data.Store}
		 */
		getStore: function (parameters) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: true,
				model: 'CMDBuild.model.common.attributes.ForeignKeyStore',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.card.readAllShort,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: parameters.extraParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				]
			});
		},

		/**
		 * @property {Object} parameters
		 *
		 * @returns {Void}
		 */
		readCard: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.card.read });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();
