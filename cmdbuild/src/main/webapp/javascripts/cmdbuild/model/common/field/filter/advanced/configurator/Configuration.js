(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.configurator.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_PANELS, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'auto', defaultValue: {} }
		]
	});

})();
