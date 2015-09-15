(function() {

	Ext.define('CMDBuild.core.proxy.domain.Properties', {

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.domain.ClassesStore'
		],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getCardinalityStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.Constants.NAME, CMDBuild.core.proxy.Constants.VALUE],
				data: [
					['1:1', '1:1'],
					['1:N', '1:N'],
					['N:1', 'N:1'],
					['N:N', 'N:N']
				]
			});
		},

		/**
		 * @returns {Ext.data.Store}
		 */
		getClassesStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.domain.ClassesStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.read,
					reader: {
						type: 'json',
						root: 'classes'
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters simple classes
						return record.get(CMDBuild.core.proxy.Constants.TABLE_TYPE) != CMDBuild.Constants.cachedTableType.simpletable;
					}
				],
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.TEXT, direction: 'ASC' }
				]
			});
		}
	});

})();