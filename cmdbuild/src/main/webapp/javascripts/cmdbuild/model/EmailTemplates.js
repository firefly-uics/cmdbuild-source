(function() {

	Ext.define('CMDBuild.model.EmailTemplates.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' }
		]

	});

	Ext.define('CMDBuild.model.EmailTemplates.singleTemplate', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.BCC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CC, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TEMPORARY, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TO, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VARIABLES, type: 'auto' },
		]
	});

	Ext.define('CMDBuild.model.EmailTemplates.variablesWindow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VALUE, type: 'string' }
		]
	});

})();