(function() {

	Ext.define('CMDBuild.model.common.filter.cql.Cql', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CONTEXT, type: 'auto', defaultValue: {} },
			{ name: CMDBuild.core.constants.Proxy.EXPRESSION, type: 'string' }
		]
	});

})();