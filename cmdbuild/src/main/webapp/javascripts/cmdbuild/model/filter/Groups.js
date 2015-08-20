(function() {

	Ext.define('CMDBuild.model.filter.Groups', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.CONFIGURATION, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ENTRY_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEMPLATE, type: 'boolean', defaultValue: true } // Client managed filters are true
		]
	});

})();