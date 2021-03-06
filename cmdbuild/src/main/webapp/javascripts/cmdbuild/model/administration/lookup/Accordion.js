(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.administration.lookup.Accordion', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: 'cmName', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' }, // Text alias
			{ name: CMDBuild.core.constants.Proxy.ENTITY_ID, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto', defaultValue: [] }, // Service parameter used on multilevel accordions
			{ name: CMDBuild.core.constants.Proxy.SELECTABLE, type: 'boolean', defaultValue: true }, // Property to enable/disable node selection
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();
