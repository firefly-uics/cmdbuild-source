(function() {

	Ext.define('CMDBuild.model.Domain', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: 'descr_1', type: 'string' },
			{ name: 'descr_2', type: 'string' },
			{ name: 'idClass1', type: 'int', useNull: true },
			{ name: 'idClass2', type: 'int', useNull: true },
			{ name: 'md_label', type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID,type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_MASTER_DETAIL, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' }
		]
	});

})();