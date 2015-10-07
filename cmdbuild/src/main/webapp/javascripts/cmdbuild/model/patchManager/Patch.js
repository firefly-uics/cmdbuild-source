(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.patchManager.Patch', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CATEGORY,  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME,  type: 'string' }
		]
	});

})();