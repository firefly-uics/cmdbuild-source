(function() {

	Ext.define('CMDBuild.model.common.tabs.history.Attribute', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ATTRIBUTE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.CHANGED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.INDEX, type: 'int', useNull: true }
		]
	});

})();