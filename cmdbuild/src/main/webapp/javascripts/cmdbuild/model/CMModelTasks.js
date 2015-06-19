(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' }
		]
	});

	// Model used from Processes -> Task Manager tab
	Ext.define('CMDBuild.model.CMModelTasks.grid.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'}
		]
	});

	/*
	 * Models for single tasks get proxy calls
	 */
	Ext.define('CMDBuild.model.CMModelTasks.singleTask.connector', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.Constants.ATTRIBUTE_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.CLASS_MAPPING, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.DATASOURCE_CONFIGURATION, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.DATASOURCE_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_TEMPLATE_ERROR, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.ATTACHMENTS_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.ATTACHMENTS_CATEGORY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.FILTER_FROM_ADDRESS, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.FILTER_SUBJECT, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.INCOMING_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PARSING_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.PARSING_KEY_END, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PARSING_KEY_INIT, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PARSING_VALUE_END, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PARSING_VALUE_INIT, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PROCESSED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.REJECTED_FOLDER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.REJECT_NOT_MATCHING, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_asynchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_synchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.GROUPS, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.PHASE, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean'},

			{ name: CMDBuild.core.proxy.Constants.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.core.proxy.Constants.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	/*
	 * Inner tasks models
	 */
	// Connector
		Ext.define('CMDBuild.model.CMModelTasks.connector.availableSqlSources', { // Step 3 type field store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.Constants.KEY, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.classLevel', { // Step 4 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.CREATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.Constants.UPDATE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.Constants.DELETE, type: 'boolean', defaultValue: true },
				{ name: CMDBuild.core.proxy.Constants.DELETE_TYPE, type: 'string' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.attributeLevel', { // Step 5 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.CLASS_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.SOURCE_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.SOURCE_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.IS_KEY, type: 'boolean' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.referenceLevel', { // Step 6 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.DOMAIN_NAME, type: 'string' }
			]
		});

	// Workflow form
		Ext.define('CMDBuild.model.CMModelTasks.common.workflowForm', {
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
				{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' }
			]
		});

})();