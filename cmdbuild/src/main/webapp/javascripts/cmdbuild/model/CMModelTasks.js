(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		]
	});

	// Model used from Processes -> Task Manager tab
	Ext.define('CMDBuild.model.CMModelTasks.grid.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'}
		]
	});

	/*
	 * Models for single tasks get proxy calls
	 */
	Ext.define('CMDBuild.model.CMModelTasks.singleTask.connector', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTRIBUTE_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_CONFIGURATION, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATASOURCE_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE_ERROR, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS_CATEGORY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER_FROM_ADDRESS, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER_FUNCTION, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER_SUBJECT, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER_TYPE, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARSING_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARSING_KEY_END, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARSING_KEY_INIT, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARSING_VALUE_END, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PARSING_VALUE_INIT, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.REJECT_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_asynchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_synchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.FILTER, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.GROUPS, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.PHASE, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	/*
	 * Inner tasks models
	 */
	// Connector
		Ext.define('CMDBuild.model.CMModelTasks.connector.availableSqlSources', { // Step 3 type field store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.VALUE, type: 'string' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.classLevel', { // Step 4 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.CREATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.CMProxyConstants.UPDATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.CMProxyConstants.DELETE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.CMProxyConstants.DELETE_TYPE, type: 'string' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.attributeLevel', { // Step 5 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.SOURCE_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.IS_KEY, type: 'boolean' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.referenceLevel', { // Step 6 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.DOMAIN_NAME, type: 'string' }
			]
		});

	// Workflow form
		Ext.define('CMDBuild.model.CMModelTasks.common.workflowForm', {
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.CMProxyConstants.VALUE, type: 'string' }
			]
		});

})();