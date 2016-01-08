(function() {

	Ext.define('CMDBuild.model.Cache', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DATE, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.PARAMETERS, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.RESPONSE, type: 'auto', defaultValue: {} }
		]
	});

})();