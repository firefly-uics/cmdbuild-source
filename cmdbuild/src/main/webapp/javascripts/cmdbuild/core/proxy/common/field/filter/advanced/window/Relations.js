(function() {

	Ext.define('CMDBuild.core.proxy.common.field.filter.advanced.window.Relations', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.common.field.filter.advanced.window.relations.DestinationEditorStore',
			'CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getDestinationStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: false,
				model: 'CMDBuild.model.common.field.filter.advanced.window.relations.DestinationEditorStore',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
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
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getDomainStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				model: 'CMDBuild.model.common.field.filter.advanced.window.relations.DomainGrid',
				data: [],

				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DOMAIN, direction: 'ASC' }
				]
			});
		}
	});

})();
