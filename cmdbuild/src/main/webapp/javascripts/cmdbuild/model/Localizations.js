(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.Localizations.translation', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TAG, type: 'string' },
		]
	});

})();