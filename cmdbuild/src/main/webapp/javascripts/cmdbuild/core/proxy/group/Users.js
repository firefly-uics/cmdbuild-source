(function() {

	Ext.define('CMDBuild.core.proxy.group.Users', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.group.UsersGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getGroupsUserStore: function(parameters) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.group.UsersGrid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.CMProxyUrlIndex.group.users.getGroupUserList,
					reader: {
						type: 'json',
						root: 'users'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.USERNAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.group.users.saveGroupUserList,
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