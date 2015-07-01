(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.DataViews.filter', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SOURCE_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'FILTER' } // Filter type
		]
	});

	Ext.define('CMDBuild.model.DataViews.sql', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SOURCE_FUNCTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'SQL' } // Filter type
		]
	});

})();