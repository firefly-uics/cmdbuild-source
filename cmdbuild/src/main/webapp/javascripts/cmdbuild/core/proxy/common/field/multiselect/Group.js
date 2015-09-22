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
					url: CMDBuild.core.proxy.Index.group.getGroupList,
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
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		}
	});

})();