(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.event.asynchronous.Asynchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE, type: 'string' }
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

			if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.FILTER])) {
				delete data[CMDBuild.core.constants.Proxy.FILTER];
			} else {
				data[CMDBuild.core.constants.Proxy.FILTER] = Ext.encode(data[CMDBuild.core.constants.Proxy.FILTER]);
			}

			if (Ext.isString(mode) && mode == 'create')
				delete data[CMDBuild.core.constants.Proxy.ID];

			if (!data[CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE]) {
				delete data[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT];
				delete data[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE];
			}

			return data;
		}
	});

})();
