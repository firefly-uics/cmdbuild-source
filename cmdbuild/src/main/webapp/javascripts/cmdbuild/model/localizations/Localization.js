(function() {

	Ext.define('CMDBuild.model.localizations.Localization', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TAG, type: 'string' },
		]
	});

})();