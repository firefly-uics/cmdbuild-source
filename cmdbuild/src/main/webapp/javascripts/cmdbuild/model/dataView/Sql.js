(function() {

	Ext.define('CMDBuild.model.dataView.Sql', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SOURCE_FUNCTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'SQL' } // Filter type
		]
	});

})();