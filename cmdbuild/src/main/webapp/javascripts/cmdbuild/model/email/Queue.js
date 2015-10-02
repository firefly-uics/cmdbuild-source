(function() {

	Ext.define('CMDBuild.model.email.Queue', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.TIME, type: 'int' }
		]

	});

})();