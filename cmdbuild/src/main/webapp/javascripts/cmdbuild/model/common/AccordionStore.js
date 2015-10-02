(function() {

	Ext.define('CMDBuild.model.common.AccordionStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: 'cmIndex', type: 'int' },
			{ name: 'cmName', type: 'auto' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto' }, // Service parameter used on multilevel accordions
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();