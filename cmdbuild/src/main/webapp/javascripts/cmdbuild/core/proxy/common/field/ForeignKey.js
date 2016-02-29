(function() {

	Ext.define('CMDBuild.core.proxy.common.field.ForeignKey', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.attributes.ForeignKeyStore'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @return {Ext.data.Store}
		 */
		getStore: function(parameters) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.common.attributes.ForeignKeyStore',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.REFERENCE_COMBO_STORE_LIMIT),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.card.getListShort,
					reader: {
						type: 'json',
						root: 'rows',
						totalProperty: 'results'
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
		 */
		readCard: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.card.read,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || true,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();