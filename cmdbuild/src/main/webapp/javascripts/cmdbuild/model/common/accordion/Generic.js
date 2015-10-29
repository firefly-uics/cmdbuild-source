(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.accordion.Generic', {
		extend: 'Ext.data.TreeModel',

		fields: [
			{ name: 'cmIndex', type: 'int' },
			{ name: 'cmName', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' }, // Text alias
			{ name: CMDBuild.core.constants.Proxy.ENTITY_ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'string'},
			{ name: CMDBuild.core.constants.Proxy.NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.PARENT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.SECTION_HIERARCHY, type: 'auto' }, // Service parameter used on multilevel accordions
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' }
		]
	});

})();