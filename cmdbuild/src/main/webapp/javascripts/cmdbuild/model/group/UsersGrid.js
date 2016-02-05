(function() {

	Ext.define('CMDBuild.model.group.UsersGrid', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'userid',  type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string', mapping: 'userid' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.proxy.CMProxyConstants.USERNAME, type: 'string' }
		]
	});

})();