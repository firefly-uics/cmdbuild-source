(function() {

	Ext.define('CMDBuild.model.group.StartingClass', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION,  type: 'string', mapping: 'text' }, // TODO: waiting for refactor
			{ name: CMDBuild.core.proxy.Constants.ID,  type: 'int', useNull: true },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TEXT,  type: 'string' }
		]
	});

})();