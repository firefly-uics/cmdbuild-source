(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.management.workflow.PreviousActivity', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVITY_SUBSET_ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.INSTANCE_ID, type: 'int', type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.WORKFLOW_NAME, type: 'string' }
		]
	});

})();
