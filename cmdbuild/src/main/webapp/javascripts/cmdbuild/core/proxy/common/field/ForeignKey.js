(function() {

	Ext.define('CMDBuild.core.proxy.common.field.ForeignKey', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
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
				pageSize: parseInt(CMDBuild.Config.cmdbuild.referencecombolimit),
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.card.getListShort,
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
		}
	});

})();