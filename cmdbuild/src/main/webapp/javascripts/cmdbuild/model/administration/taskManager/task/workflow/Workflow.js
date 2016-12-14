(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.workflow.Workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME, type: 'string' }
		],

		/**
		 * Filters and formats model data to avoid to send useless properties
		 *
		 * @param {String} mode ['create', 'update']
		 *
		 * @returns {Object} data
		 */
		getSubmitData: function (mode) {
			var data = this.getData();

			if (Ext.isString(mode) && mode == 'create')
				delete data[CMDBuild.core.constants.Proxy.ID];

			if (Ext.Object.isEmpty(data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES])) {
				delete data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES];
			} else {
				data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES] = Ext.encode(data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES]);
			}

			return data;
		}
	});

})();
