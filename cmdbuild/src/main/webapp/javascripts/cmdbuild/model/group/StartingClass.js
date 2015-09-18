(function() {

	Ext.define('CMDBuild.model.group.StartingClass', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,  type: 'string', mapping: 'text' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID,  type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEXT,  type: 'string' }
		]
	});

})();