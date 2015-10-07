(function() {

	Ext.define('CMDBuild.core.proxy.group.privileges.CustomPages', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.group.privileges.GridRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			var store =  Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.group.privileges.GridRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.privileges.customPages.read, // TODO: implementation
					reader: {
						type: 'json',
						root: 'privileges'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
			return store;
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.privileges.customPages.update, // TODO: implementation
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();