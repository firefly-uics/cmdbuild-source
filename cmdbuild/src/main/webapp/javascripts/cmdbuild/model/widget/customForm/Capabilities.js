(function() {

	Ext.define('CMDBuild.model.widget.customForm.Capabilities', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ADD_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.DELETE_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.IMPORT_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.MODIFY_DISABLED, type: 'boolean' },
			{ name: CMDBuild.core.proxy.Constants.READ_ONLY, type: 'boolean' }
		]
	});

})();