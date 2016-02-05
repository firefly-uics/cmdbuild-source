(function() {

	Ext.define('CMDBuild.model.common.tabs.history.processes.CardRecord', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.BEGIN_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.END_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_CARD, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.IS_RELATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.CMProxyConstants.PERFORMERS, type: 'auto' },
			{
				name: CMDBuild.core.proxy.CMProxyConstants.STATUS,
				type: 'auto',
				mapping: CMDBuild.core.proxy.CMProxyConstants.STATUS + '.' + CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION
			},
			{ name: CMDBuild.core.proxy.CMProxyConstants.USER, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.VALUES, type: 'auto' } // Historic card values
		]
	});

})();