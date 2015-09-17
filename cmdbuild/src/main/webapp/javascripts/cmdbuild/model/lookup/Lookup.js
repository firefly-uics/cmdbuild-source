(function() {

	Ext.define('CMDBuild.model.lookup.Lookup.fieldStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', defaultValue: '' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentId', type: 'int' },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.lookup.Lookup.gridStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Active', type: 'boolean', defaultValue: true },
			{ name: 'Code', type: 'string' },
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', useNull: true },
			{ name: 'Notes', type: 'string' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int', useNull: true },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.lookup.Lookup.parentComboStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int' }
		]
	});

})();