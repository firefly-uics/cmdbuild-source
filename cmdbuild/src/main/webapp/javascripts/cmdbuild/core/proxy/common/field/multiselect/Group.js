(function() {

	Ext.define('CMDBuild.core.proxy.common.field.multiselect.Group', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.field.multiselect.Group'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.common.field.multiselect.Group',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.group.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.GROUPS
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters active groups only
						return record.get(CMDBuild.core.constants.Proxy.IS_ACTIVE);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();