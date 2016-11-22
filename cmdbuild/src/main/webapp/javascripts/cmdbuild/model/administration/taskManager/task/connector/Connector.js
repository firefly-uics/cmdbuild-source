(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.connector.Connector', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTE_MAPPING, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CLASS_MAPPING, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.DATASOURCE_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE_ERROR, type: 'string' }
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
			data[CMDBuild.core.constants.Proxy.ATTRIBUTE_MAPPING] = Ext.encode(data[CMDBuild.core.constants.Proxy.ATTRIBUTE_MAPPING]);
			data[CMDBuild.core.constants.Proxy.CLASS_MAPPING] = Ext.encode(data[CMDBuild.core.constants.Proxy.CLASS_MAPPING]);
			data[CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION] = Ext.encode(data[CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION]);

			if (Ext.isEmpty(data[CMDBuild.core.constants.Proxy.DATASOURCE_TYPE]) || data[CMDBuild.core.constants.Proxy.DATASOURCE_TYPE] == 'off') {
				delete data[CMDBuild.core.constants.Proxy.DATASOURCE_CONFIGURATION];
				delete data[CMDBuild.core.constants.Proxy.DATASOURCE_TYPE];
			}

			if (Ext.isString(mode) && mode == 'create')
				delete data[CMDBuild.core.constants.Proxy.ID];

			if (!data[CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE]) {
				delete data[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_ACCOUNT];
				delete data[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE_ERROR];
			}

			return data;
		}
	});

})();
