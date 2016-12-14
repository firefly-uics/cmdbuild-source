(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.common.field.comboBox.lookup.Value', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: 'Description', type: 'string' },
			{ name: 'Id', type: 'int', useNull: true },
			{ name: 'Number', type: 'int', useNull: true },
			{ name: 'ParentId', type: 'int', useNull: true }
		]
	});

})();
