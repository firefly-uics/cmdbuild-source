(function() {

	Ext.define('CMDBuild.model.localization.Localization', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION,  type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TAG, type: 'string' },
		]
	});

})();