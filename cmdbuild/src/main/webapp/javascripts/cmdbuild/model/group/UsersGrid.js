(function() {

	Ext.define('CMDBuild.model.group.UsersGrid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'userid',  type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'string', mapping: 'userid' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.proxy.Constants.USERNAME, type: 'string' }
		]
	});

})();