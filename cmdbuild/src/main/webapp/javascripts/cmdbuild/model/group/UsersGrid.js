(function() {

	Ext.define('CMDBuild.model.group.UsersGrid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: 'userid',  type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string', mapping: 'userid' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.constants.Proxy.USERNAME, type: 'string' }
		]
	});

})();