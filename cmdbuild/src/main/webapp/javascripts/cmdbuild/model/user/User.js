(function() {

	Ext.define('CMDBuild.model.user.User', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

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