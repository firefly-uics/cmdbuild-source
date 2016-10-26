(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.generic.Generic', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CONTEXT, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACTIVE, type: 'boolean', defaultValue: true }, // Fixed value until refactor
			{ name: CMDBuild.core.constants.Proxy.EMAIL_TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.REPORT_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_EXTENSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REPORT_PARAMETERS, type: 'auto', defaultValue: {} }
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

			if (Ext.Object.isEmpty(data[CMDBuild.core.constants.Proxy.CONTEXT])) {
				delete data[CMDBuild.core.constants.Proxy.CONTEXT];
			} else {
				data[CMDBuild.core.constants.Proxy.CONTEXT] = Ext.encode({ client: data[CMDBuild.core.constants.Proxy.CONTEXT] }); // FIXME: multiple sub-context predisposition
			}

			if (Ext.isString(mode) && mode == 'create')
				delete data[CMDBuild.core.constants.Proxy.ID];

			if (!data[CMDBuild.core.constants.Proxy.REPORT_ACTIVE]) {
				delete data[CMDBuild.core.constants.Proxy.REPORT_EXTENSION];
				delete data[CMDBuild.core.constants.Proxy.REPORT_NAME];
				delete data[CMDBuild.core.constants.Proxy.REPORT_PARAMETERS];
			} else {
				data[CMDBuild.core.constants.Proxy.REPORT_PARAMETERS] = Ext.encode(data[CMDBuild.core.constants.Proxy.REPORT_PARAMETERS]);
			}

			return data;
		}
	});

})();
