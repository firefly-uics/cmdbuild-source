(function () {

	Ext.define('CMDBuild.proxy.management.workflow.panel.tree.Tree', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.management.workflow.Node',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.proxy.management.workflow.panel.tree.Reader'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.TreeStore}
		 */
		getStore: function () {
			return Ext.create('Ext.data.TreeStore', {
				autoLoad: false,
				model: 'CMDBuild.model.management.workflow.Node',
				pageSize: CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.ROW_LIMIT),
				remoteSort: true,
				root: {
					expanded: true
				},
				proxy: {
					type: 'ajax',
					url: CMDBuild.proxy.index.Json.workflow.activity.readAll,
					reader: {
						type: 'workflowstore',
						totalProperty: CMDBuild.core.constants.Proxy.RESULTS
					}
				}
			});
		},

		/**
		 * @returns {Ext.data.ArrayStore}
		 */
		getStoreState: function () {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.VALUE],
				data: [
					[CMDBuild.Translation.open, 'open.running'],
					[CMDBuild.Translation.suspended, 'open.not_running.suspended'],
					[CMDBuild.Translation.completed, 'closed.completed'],
					[CMDBuild.Translation.aborted, 'closed.aborted'],
					[CMDBuild.Translation.all, 'all']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readAllWorkflow: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.readAll });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.WORKFLOW, parameters);
		},

		/**
		 * Get the position on the DB of the required card, considering the sorting and current filter applied on the grid
		 *
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		readPosition: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.proxy.index.Json.workflow.getPosition });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, parameters);
		}
	});

})();
