(function() {

	Ext.define('CMDBuild.model.widget.customForm.Capabilities', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ADD_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DELETE_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IMPORT_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.MODIFY_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY, type: 'boolean' }
		]
	});

})();