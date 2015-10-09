(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.userAndGroup.group.userInterface.DisabledModules', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'bulkupdate', type: 'boolean' },
			{ name: 'exportcsv', type: 'boolean' },
			{ name: 'importcsv', type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CHANGE_PASSWORD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CLASS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.CUSTOM_PAGES, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DASHBOARD, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DATA_VIEW, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.PROCESS, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.REPORT, type: 'boolean' }
		]
	});

})();