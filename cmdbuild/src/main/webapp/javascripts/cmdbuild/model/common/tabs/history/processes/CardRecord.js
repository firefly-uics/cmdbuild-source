(function() {

	Ext.define('CMDBuild.model.common.tabs.history.processes.CardRecord', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ACTIVITY_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.BEGIN_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.proxy.Constants.CLASS_NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.END_DATE, type: 'date', dateFormat: 'd/m/Y H:i:s' },
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.IS_CARD, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.Constants.IS_RELATION, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.proxy.Constants.PERFORMERS, type: 'auto' },
			{
				name: CMDBuild.core.proxy.Constants.STATUS,
				type: 'auto',
				mapping: CMDBuild.core.proxy.Constants.STATUS + '.' + CMDBuild.core.proxy.Constants.DESCRIPTION
			},
			{ name: CMDBuild.core.proxy.Constants.USER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VALUES, type: 'auto' } // Historic card values
		]
	});

})();