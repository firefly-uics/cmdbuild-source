(function() {

	Ext.require('CMDBuild.core.proxy.Constants');

	Ext.define('CMDBuild.model.email.Templates.grid', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.SUBJECT, type: 'string' }
		]

	});

	Ext.define('CMDBuild.model.email.Templates.singleTemplate', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.BCC, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.BODY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.CC, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DEFAULT_ACCOUNT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DELAY, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FROM, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.KEEP_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.SUBJECT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TO, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VARIABLES, type: 'auto' }
		]
	});

	Ext.define('CMDBuild.model.email.Templates.variablesWindow', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.proxy.Constants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' }
		]
	});

})();