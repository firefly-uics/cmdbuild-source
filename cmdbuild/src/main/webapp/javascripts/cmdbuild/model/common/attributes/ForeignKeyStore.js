(function() {

	Ext.define('CMDBuild.model.common.attributes.ForeignKeyStore', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'Id', type: 'string' },
			{ name: 'Description', type: 'string' }
		]
	});

})();