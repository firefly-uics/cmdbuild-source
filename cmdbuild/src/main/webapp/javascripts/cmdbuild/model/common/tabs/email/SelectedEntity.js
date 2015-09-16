(function() {

	Ext.define('CMDBuild.model.common.tabs.email.SelectedEntity', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ENTITY, type: 'auto' }, // Class or Activity object
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true }
		]
	});

})();