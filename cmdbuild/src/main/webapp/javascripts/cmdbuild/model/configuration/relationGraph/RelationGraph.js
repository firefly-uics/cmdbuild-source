(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.configuration.relationGraph.RelationGraph', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.BASE_LEVEL, type: 'int', defaultValue: 1, useNull: true },
			{ name: CMDBuild.core.constants.Proxy.CLUSTERING_THRESHOLD, type: 'int', defaultValue: 5, useNull: true },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean', defaultValue: false },
			{ name: CMDBuild.core.constants.Proxy.EXTENSION_MAXIMUM_LEVEL, type: 'int', defaultValue: 5, useNull: true }
		]
	});

})();