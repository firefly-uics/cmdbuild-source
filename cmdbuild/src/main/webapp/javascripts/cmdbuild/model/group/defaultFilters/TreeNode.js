(function() {

	Ext.define('CMDBuild.model.group.defaultFilters.TreeNode', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.Constants'],

		fields: [
			{ name: CMDBuild.core.proxy.Constants.DEFAULT_FILTER, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.NAME, type: 'string' },
			{ name: CMDBuild.core.proxy.Constants.TABLE_TYPE, type: 'string' }
		]
	});

})();