(function() {

	Ext.define('CMDBuild.model.common.tabs.email.SelectedEntity', {
		extend: 'Ext.data.Model',

		require: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.ENTITY, type: 'auto' }, // Class or Activity object
			{ name: CMDBuild.core.proxy.Constants.ID, type: 'int', useNull: true }
		]
	});

})();