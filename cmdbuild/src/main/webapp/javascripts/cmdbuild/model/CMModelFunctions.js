(function() {

	Ext.define('CMDBuild.model.CMModelFunctions.list', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		]
	});

})();