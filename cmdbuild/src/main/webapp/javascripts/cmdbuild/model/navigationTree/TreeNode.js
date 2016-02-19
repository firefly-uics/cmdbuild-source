(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.navigationTree.TreeNode', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.DESCRIPTION, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.DOMAIN_MODEL, type: 'auto', defaultValue: {} }, // {CMDBuild.model.navigationTree.Domain}
			{ name: CMDBuild.core.constants.Proxy.ENTRY_TYPE, type: 'auto', defaultValue: {} }, // {CMDBuild.model.navigationTree.Class}
			{ name: CMDBuild.core.constants.Proxy.FILTER, type: 'string' }
		]
	});

})();
