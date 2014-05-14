(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'}
		]
	});

	// Model used from Processes -> Task Manager tab
	Ext.define('CMDBuild.model.CMModelTasks.grid.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'}
		]
	});

	/*
	 * Models for single tasks get proxy calls
	 */
	Ext.define('CMDBuild.model.CMModelTasks.singleTask.connector', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTRIBUTE_MAPPING, type: 'auto'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.email', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY, type: 'int'},
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.PARSING_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.PARSING_KEY_END, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.PARSING_KEY_INIT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.PARSING_VALUE_END, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.PARSING_VALUE_INIT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.event_synchronous', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_ACCOUNT, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.FILTER, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.GROUPS, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.PHASE, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'boolean'},
			{ name: CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES, type: 'auto'},
			{ name: CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME, type: 'string'}
		]
	});

	/*
	 * Inner tasks models
	 */
	// Connector
		Ext.define('CMDBuild.model.CMModelTasks.connector.classLevel', { // Step 4 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.VIEW_NAME, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.IS_MAIN, type: 'boolean' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.attributeLevel', { // Step 5 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.CLASS_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.VIEW_NAME, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.VIEW_ATTRIBUTE, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.IS_KEY, type: 'boolean' }
			]
		});

		Ext.define('CMDBuild.model.CMModelTasks.connector.referenceLevel', { // Step 6 grid store
			extend: 'Ext.data.Model',

			fields: [
				{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string' },
				{ name: CMDBuild.ServiceProxy.parameter.DOMAIN_NAME, type: 'string' }
			]
		});
})();