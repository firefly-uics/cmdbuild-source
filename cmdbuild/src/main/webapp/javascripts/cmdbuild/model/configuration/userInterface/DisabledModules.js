(function() {

	/**
	 * TODO: waiting for server refactor (properties rename)
	 */
	Ext.define('CMDBuild.model.configuration.userInterface.DisabledModules', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: 'bulkupdate', type: 'boolean' },
			{ name: 'exportcsv', type: 'boolean' },
			{ name: 'importcsv', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CHANGE_PASSWORD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DASHBOARD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DATA_VIEW, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REPORT, type: 'boolean' }
		]
	});

})();