(function() {

	Ext.define('CMDBuild.model.lookup.ParentComboStore', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'ParentDescription', type: 'string' },
			{ name: 'ParentId', type: 'int' }
		]
	});

})();