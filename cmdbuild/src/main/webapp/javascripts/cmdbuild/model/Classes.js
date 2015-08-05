(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.Classes.domainsTreePanel', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ENABLED, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' }
		]
	});

})();