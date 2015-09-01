(function() {

	Ext.define('CMDBuild.model.group.userInterface.DisabledModules', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'bulkupdate', type: 'boolean' },
			{ name: 'exportcsv', type: 'boolean' },
			{ name: 'importcsv', type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CHANGE_PASSWORD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.CLASS, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.DASHBOARD, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.DATA_VIEW, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.PROCESS, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.REPORT, type: 'boolean' }
		]
	});

})();