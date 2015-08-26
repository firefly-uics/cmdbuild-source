(function() {

	Ext.define('CMDBuild.core.proxy.groups.Users', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.model.groups.UsersGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getGroupsUserStore: function(parameters) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.groups.UsersGrid',
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
				method: 'POST',
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