(function() {

	Ext.define('CMDBuild.model.common.AccordionStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'cmIndex', type: 'int' },
			{ name: 'cmName', type: 'string' },
			{ name: 'sourceFunction', type: 'auto' },
			{ name: 'viewType', type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FILTER, type: 'auto' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.PARENT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' }
		]
	});

})();