(function() {

	Ext.define('CMDBuild.model.CMModelTasks.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.TYPE, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'string'}
		]
	});

	Ext.define('CMDBuild.model.CMModelTasks.singleTask.workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.ServiceProxy.parameter.ID, type: 'int' },
			{ name: CMDBuild.ServiceProxy.parameter.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.ServiceProxy.parameter.ACTIVE, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.ATTRIBUTES, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.CLASS_NAME, type: 'string'},
			{ name: CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION, type: 'string'}
		]
	});

})();