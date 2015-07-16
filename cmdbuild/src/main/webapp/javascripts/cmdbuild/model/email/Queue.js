(function() {

	Ext.define('CMDBuild.model.email.Queue', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.TIME, type: 'int' }
		]

	});

})();