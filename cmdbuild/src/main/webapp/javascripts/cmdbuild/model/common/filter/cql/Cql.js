(function() {

	Ext.define('CMDBuild.model.common.filter.cql.Cql', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.CONTEXT, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.proxy.Constants.EXPRESSION, type: 'string' }
		]
	});

})();