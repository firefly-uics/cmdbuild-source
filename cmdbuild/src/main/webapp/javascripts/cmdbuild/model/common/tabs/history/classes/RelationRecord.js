(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.tabs.history.classes.RelationRecord', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BEGIN_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.constants.Proxy.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DESTINATION_DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DOMAIN, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.END_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.IS_CARD, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.IS_RELATION, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.USER, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.VALUES, type: 'auto' } // Historic relation values
		]
	});

})();