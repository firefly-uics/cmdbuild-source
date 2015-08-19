(function() {

	Ext.define('CMDBuild.model.common.filter.cql.Cql', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.CONTEXT, type: 'auto' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.EXPRESSION, type: 'string' }
		]
	});

})();