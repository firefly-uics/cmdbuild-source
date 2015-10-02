(function() {

	Ext.define('CMDBuild.model.menu.TreeStore', {
		extend: 'Ext.data.TreeModel',

		requires: ['CMDBuild.core.constants.Proxy'],

		fields: [
			{ name: 'referencedClassName', type: 'string' },
			{ name: 'referencedElementId', type: 'string' },
			{ name: 'uuid', type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.FOLDER_TYPE, type: 'string' }, // Used to get the folder of the available items
			{ name: CMDBuild.core.constants.Proxy.INDEX, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.TEXT, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		]
	});

})();