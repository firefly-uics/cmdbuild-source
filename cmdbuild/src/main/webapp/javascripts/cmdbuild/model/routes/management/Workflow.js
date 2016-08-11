(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.routes.management.Workflow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLIENT_FILTER, type: 'auto', defaultValue: {} }, // Process name
			{ name: CMDBuild.core.constants.Proxy.FORMAT, type: 'string', defaultValue: CMDBuild.core.constants.Proxy.PDF }, // Print format
			{ name: CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER, type: 'string' }
		]
	});

})();
