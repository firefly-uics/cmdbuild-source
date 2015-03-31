(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyConstants');

	Ext.define('CMDBuild.model.Users.defaultGroup', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: 'isdefault', type: 'boolean' }
		]
	});

	Ext.define('CMDBuild.model.Users.single', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'defaultgroup', type: 'int', useNull: true },
			{ name: 'userid', type: 'int', useNull: true },
			{ name: 'username', type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EMAIL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE, type: 'boolean', defaultValue: true }
		]
	});

})();