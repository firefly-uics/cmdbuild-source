(function () {

	Ext.define('CMDBuild.proxy.workflow.management.panel.tree.Tree', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.model.workflow.management.Node',
			'CMDBuild.proxy.index.Json',
			'CMDBuild.proxy.workflow.management.panel.tree.Reader'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.TreeStore}
		 */
		getStore: function () {
			return Ext.create('Ext.data.TreeStore', {
				autoLoad: false,
				model: 'CMDBuild.model.workflow.management.Node',
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
					[CMDBuild.Translation.management.modworkflow.statuses['open.running'], 'open.running'],
					[CMDBuild.Translation.management.modworkflow.statuses['open.not_running.suspended'], 'open.not_running.suspended'],
					[CMDBuild.Translation.management.modworkflow.statuses['closed.completed'], 'closed.completed'],
					[CMDBuild.Translation.management.modworkflow.statuses['closed.aborted'], 'closed.aborted'],
					[CMDBuild.Translation.management.modworkflow.statuses['all'], 'all']
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
