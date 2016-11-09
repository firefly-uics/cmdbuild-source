(function () {

	Ext.define('CMDBuild.model.common.field.comboBox.searchable.StoreRecord', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', useNull: true }
		]
	});

})();
