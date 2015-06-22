(function() {

	Ext.define('CMDBuild.model.Domain', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'descr_1', type: 'string' },
			{ name: 'descr_2', type: 'string' },
			{ name: 'idClass1', type: 'int', useNull: true },
			{ name: 'idClass2', type: 'int', useNull: true },
			{ name: 'md_label', type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.CARDINALITY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID,type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.IS_MASTER_DETAIL, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' }
		]
	});

})();