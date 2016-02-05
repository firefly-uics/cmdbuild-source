(function() {

	Ext.define('CMDBuild.model.configuration.userInterface.DisabledModules', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'bulkupdate', type: 'boolean' },
			{ name: 'exportcsv', type: 'boolean' },
			{ name: 'importcsv', type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CHANGE_PASSWORD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DASHBOARD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DATA_VIEW, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PROCESS, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.REPORT, type: 'boolean' }
		]
	});

})();