(function() {

	Ext.define('CMDBuild.model.report.Grid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.GROUPS, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.QUERY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TITLE, type: 'string' }, // Usually called name
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();