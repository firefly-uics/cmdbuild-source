(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.configurator.Configuration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DISABLED_FEATURES, type: 'auto', defaultValue: [] }, // Managed values: 'inputParameter'
			{ name: CMDBuild.core.constants.Proxy.DISABLED_PANELS, type: 'auto', defaultValue: [] } // Managed values: 'attributes', 'functions', 'relations'
		]
	});

})();
