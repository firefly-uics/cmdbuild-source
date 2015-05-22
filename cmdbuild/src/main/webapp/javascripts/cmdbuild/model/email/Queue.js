(function() {

	Ext.define('CMDBuild.model.email.Queue', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.TIME, type: 'int' }
		]

	});

})();