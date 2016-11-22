(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTES_PRIVILEGES, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_FEATURES, type: 'auto', defaultValue: [] }, // Managed values: 'inputParameter'
			{ name: CMDBuild.core.constants.Proxy.MODE, type: 'string', defaultValue: 'field' } // Managed values: 'field', 'grid'
		]
	});

})();
