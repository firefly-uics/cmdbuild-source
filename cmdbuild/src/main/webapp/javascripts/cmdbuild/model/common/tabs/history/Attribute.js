(function() {

	Ext.define('CMDBuild.model.common.tabs.history.Attribute', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ATTRIBUTE_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.CHANGED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.INDEX, type: 'int', useNull: true }
		]
	});

})();