(function() {

	Ext.define('CMDBuild.model.common.attributes.Metadata', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.KEY, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.VALUE, type: 'string' }
		]
	});

})();