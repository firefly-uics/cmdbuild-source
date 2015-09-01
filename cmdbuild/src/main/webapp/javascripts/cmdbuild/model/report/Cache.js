(function() {

	Ext.define('CMDBuild.model.report.Cache', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.GROUP, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' }
		]
	});

})();