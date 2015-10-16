(function() {

	Ext.define('CMDBuild.core.proxy.common.field.filter.advanced.window.Window', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.Utils',
			'CMDBuild.model.common.field.filter.advanced.Filter'
		],

		singleton: true,

		/**
		 * Returns a store with the filters for a given group
		 *
		 * @return {Ext.data.Store}
		 */
		getGroupStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.Filter',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				proxy: {
					url: CMDBuild.core.proxy.Index.filter.groupStore,
					type: 'ajax',
					reader: {
						root: 'filters',
						type: 'json',
						totalProperty: 'count'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();