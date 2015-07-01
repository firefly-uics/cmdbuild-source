(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.Users.defaultGroup', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: 'isdefault', type: 'boolean' }
		]
	});

	Ext.define('CMDBuild.model.Users.single', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'defaultgroup', type: 'int', useNull: true },
			{ name: 'userid', type: 'int', useNull: true },
			{ name: 'username', type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.EMAIL, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.IS_ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.PRIVILEGED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.SERVICE, type: 'boolean', defaultValue: false }
		]
	});

})();