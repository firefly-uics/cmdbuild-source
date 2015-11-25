(function() {

	Ext.define('CMDBuild.core.proxy.widgets.Workflow', {

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.widget.workflow.TargetWorkflow'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.ArrayStore}
		 *
		 * @administration
		 */
		getStoreSelectionType: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.NAME, CMDBuild.core.constants.Proxy.DESCRIPTION],
				data: [
					[CMDBuild.core.constants.Proxy.NAME, CMDBuild.Translation.byName ],
					[CMDBuild.core.constants.Proxy.CQL, CMDBuild.Translation.byCql]
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 *
		 * @administration
		 */
		getStoreTargetWorkflow: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.WORKFLOW, {
				autoLoad: true,
				model: 'CMDBuild.model.widget.workflow.TargetWorkflow',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						active: true,
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters classes and processes witch are superclasses
						return (
							record.get(CMDBuild.core.constants.Proxy.TYPE) == 'processclass'
							&& !record.get('superclass')
						);
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @administration
		 */
		readStartActivity: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.workflow.getStartActivity });

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		}
	});

})();