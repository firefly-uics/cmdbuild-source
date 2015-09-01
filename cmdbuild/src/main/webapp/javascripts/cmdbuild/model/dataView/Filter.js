(function() {

	Ext.define('CMDBuild.model.dataView.Filter', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SOURCE_CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'FILTER' } // Filter type
		]
	});

})();