(function() {

	Ext.define('CMDBuild.model.domain.ClassesStore', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TABLE_TYPE, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' } // FIXME: waiting for refactor (renamed as description on server side)
		]
	});

})();