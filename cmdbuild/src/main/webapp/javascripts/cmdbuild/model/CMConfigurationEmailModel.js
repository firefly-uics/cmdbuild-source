(function() {

	Ext.define("CMDBuild.model.CMConfigurationEmailModel", {
		extend: "Ext.data.Model",

		fields: [
			{ name: 'isDefault', type: 'boolean' },
			{ name: 'name', type: 'string' },
			{ name: 'address', type: 'string' },
			{ name: 'isActive', type: 'boolean' }
		]
	});

})();