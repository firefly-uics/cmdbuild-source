(function() {

	Ext.define('CMDBuild.model.common.tabs.email.SelectedEntity', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ENTITY, type: 'auto' }, // Class or Activity object
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'int', useNull: true }
		]
	});

})();