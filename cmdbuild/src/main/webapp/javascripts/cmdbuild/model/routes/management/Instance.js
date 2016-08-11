(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.routes.management.Instance', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.INSTANCE_IDENTIFIER, type: 'int', useNull: true }, // Process instance ID
			{ name: CMDBuild.core.constants.Proxy.PROCESS_IDENTIFIER, type: 'string' } // Process name
		]
	});

})();
