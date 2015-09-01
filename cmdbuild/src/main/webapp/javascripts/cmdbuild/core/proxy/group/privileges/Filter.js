(function() {

	Ext.define('CMDBuild.core.proxy.group.privileges.Filter', {

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.group.privileges.GridRecord'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.group.privileges.GridRecord',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.privileges.filter.read,
					reader: {
						type: 'json',
						root: 'privileges'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.privileges.filter.update,
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