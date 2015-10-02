(function() {

	Ext.define('CMDBuild.model.common.field.comboBox.searchable.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.READ_ONLY_SEARCH_WINDOW, type: 'Boolean', defaultValue: true }
		]
	});

})();