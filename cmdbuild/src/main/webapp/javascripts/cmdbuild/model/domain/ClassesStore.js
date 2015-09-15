(function() {

	Ext.define('CMDBuild.model.domain.ClassesStore', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TABLE_TYPE, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string' } // FIXME: waiting for refactor (renamed as description on server side)
		]
	});

})();