(function() {

	Ext.define('CMDBuild.model.common.field.comboBox.searchable.Configuration', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.READ_ONLY_SEARCH_WINDOW, type: 'Boolean', defaultValue: true }
		]
	});

})();