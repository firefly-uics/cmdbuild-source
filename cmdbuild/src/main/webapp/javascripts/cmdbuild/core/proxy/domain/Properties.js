(function() {

	Ext.define('CMDBuild.core.proxy.domain.Properties', {

		requires: [
			'CMDBuild.core.constants.Global',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.domain.ClassesStore'
		],

		singleton: true,

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getCardinalityStore: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.VALUE],
				data: [
					['1:1', '1:1'],
					['1:N', '1:N'],
					['N:1', 'N:1'],
					['N:N', 'N:N']
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getClassesStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.domain.ClassesStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters simple classes
						return record.get(CMDBuild.core.constants.Proxy.TABLE_TYPE) != CMDBuild.core.constants.Global.getTableTypeSimpleTable();
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		}
	});

})();
