(function() {

	Ext.define('CMDBuild.model.menu.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: 'referencedClassName', type: 'string' },
			{ name: 'referencedElementId', type: 'string' },
			{ name: 'uuid', type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.FOLDER_TYPE, type: 'string' }, // Used to get the folder of the available items
			{ name: CMDBuild.core.proxy.Constants.INDEX, type: 'int' },
			{ name: CMDBuild.core.proxy.Constants.TEXT, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TYPE, type: 'string' }
		]
	});

})();