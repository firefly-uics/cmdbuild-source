(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.Lookup.typeComboStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.Lookup.parentComboStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int' }
		]
	});

	Ext.define('CMDBuild.model.Lookup.gridStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Active', type: 'string' },
			{ name: 'Code', type: 'string' },
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'string' },
			{ name: 'Notes', type: 'string' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int' },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

	Ext.define('CMDBuild.model.Lookup.fieldStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', defaultValue: '' },
			{ name: 'Number', type: 'int' },
			{ name: 'ParentId', type: 'int' },
			{ name: 'TranslationUuid', type: 'string' }
		]
	});

})();
