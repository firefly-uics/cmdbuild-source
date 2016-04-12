(function () {

	/**
	 * @deprecated (old cache system is dismissed)
	 */
	Ext.define('CMDBuild.core.proxy.Cache', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.cache.CMReferenceStoreModel'
		],

		singleton: true,

		/**
		 * @param {Boolean} isOneTime
		 * @param {Object} baseParams
		 *
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStoreReference: function (isOneTime, baseParams) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: !isOneTime,
				model: 'CMDBuild.cache.CMReferenceStoreModel',
				isOneTime: isOneTime,
				baseParams: baseParams, //retro-compatibility,
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.card.readAllShort,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS,
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					},
					extraParams: baseParams
				},
				sorters: [
					{ property: 'Description', direction: 'ASC' }
				]
			});
		}
	});

})();
