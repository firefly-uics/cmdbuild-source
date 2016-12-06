(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.taskManager.task.email.Email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.CRON_EXPRESSION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.FILTER_FUNCTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FILTER_SUBJECT, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.FILTER_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARSING_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PARSING_KEY_END, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARSING_KEY_INIT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARSING_VALUE_END, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE, type: 'boolean' },
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

			if (!data[CMDBuild.core.constants.Proxy.ATTACHMENTS_ACTIVE])
				delete data[CMDBuild.core.constants.Proxy.ATTACHMENTS_CATEGORY];

			switch (data[CMDBuild.core.constants.Proxy.FILTER_TYPE]) {
				case CMDBuild.core.constants.Proxy.FUNCTION: {
					delete data[CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS];
					delete data[CMDBuild.core.constants.Proxy.FILTER_SUBJECT];
				} break;

				case CMDBuild.core.constants.Proxy.REGEX: {
					delete data[CMDBuild.core.constants.Proxy.FILTER_FUNCTION];
				} break;

				case CMDBuild.core.constants.Proxy.NONE:
				default: {
					delete data[CMDBuild.core.constants.Proxy.FILTER_FROM_ADDRESS];
					delete data[CMDBuild.core.constants.Proxy.FILTER_FUNCTION];
					delete data[CMDBuild.core.constants.Proxy.FILTER_SUBJECT];
				}
			}

			if (Ext.isString(mode) && mode == 'create')
				delete data[CMDBuild.core.constants.Proxy.ID];

			if (!data[CMDBuild.core.constants.Proxy.NOTIFICATION_ACTIVE])
				delete data[CMDBuild.core.constants.Proxy.NOTIFICATION_EMAIL_TEMPLATE];

			if (!data[CMDBuild.core.constants.Proxy.PARSING_ACTIVE]) {
				delete data[CMDBuild.core.constants.Proxy.PARSING_KEY_END];
				delete data[CMDBuild.core.constants.Proxy.PARSING_KEY_INIT];
				delete data[CMDBuild.core.constants.Proxy.PARSING_VALUE_END];
				delete data[CMDBuild.core.constants.Proxy.PARSING_VALUE_INIT];
			}

			if (!data[CMDBuild.core.constants.Proxy.REJECT_NOT_MATCHING])
				delete data[CMDBuild.core.constants.Proxy.REJECTED_FOLDER];

			if (!data[CMDBuild.core.constants.Proxy.WORKFLOW_ACTIVE]) {
				delete data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES];
				delete data[CMDBuild.core.constants.Proxy.WORKFLOW_CLASS_NAME];
			} else {
				data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES] = Ext.encode(data[CMDBuild.core.constants.Proxy.WORKFLOW_ATTRIBUTES]);
			}

			return data;
		}
	});

})();
