(function() {

	Ext.define('CMDBuild.model.localization.Localization', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TAG, type: 'string' },
		]
	});

})();