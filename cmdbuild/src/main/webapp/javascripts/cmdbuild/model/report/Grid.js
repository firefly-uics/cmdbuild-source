(function() {

	Ext.define('CMDBuild.model.report.Grid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.GROUPS, type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.QUERY, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TITLE, type: 'string' }, // Usually called name
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string', defaultValue: 'CUSTOM' }
		]
	});

})();