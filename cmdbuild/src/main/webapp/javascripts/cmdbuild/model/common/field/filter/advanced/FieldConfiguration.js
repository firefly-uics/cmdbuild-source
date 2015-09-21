(function() {

	Ext.define('CMDBuild.model.common.field.filter.advanced.FieldConfiguration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENABLED_PANELS, type: 'auto', defaultValue: ['attribute', 'relation', 'function'] },
			{ name: CMDBuild.core.constants.Proxy.TARGET_CLASS_FIELD, type: 'auto' } // Target class field pointer
		]
	});

})();