(function() {

	Ext.define('CMDBuild.model.common.field.filter.advanced.window.functions.Function', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' }
		]
	});

})();