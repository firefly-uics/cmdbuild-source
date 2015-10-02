(function() {

	Ext.define('CMDBuild.model.dataView.SqlView', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.INPUT, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.OUTPUT, type: 'auto', defaultValue: [] },
			{ name: CMDBuild.core.constants.Proxy.SOURCE_FUNCTION, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();