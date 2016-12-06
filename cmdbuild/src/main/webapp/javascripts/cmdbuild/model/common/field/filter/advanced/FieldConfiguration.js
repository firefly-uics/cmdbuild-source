(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.filter.advanced.FieldConfiguration', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DISABLED_PANELS, type: 'auto', defaultValue: [] }, // Managed values: 'attributes', 'relations', 'functions', 'columnPrivileges'
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD, type: 'auto' } // Target class field pointer
		]
	});

})();