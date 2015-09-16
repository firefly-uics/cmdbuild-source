(function() {

	Ext.define('CMDBuild.model.widget.customForm.Capabilities', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ADD_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.DELETE_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.IMPORT_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.MODIFY_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY, type: 'boolean' }
		]
	});

})();