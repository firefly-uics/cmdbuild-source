(function() {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.Users', {

		requires: [
			'CMDBuild.core.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.userAndGroup.group.UsersGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store}
		 */
		getGroupsUserStore: function(parameters) {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.userAndGroup.group.UsersGrid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.group.users.getGroupUserList,
					reader: {
						type: 'json',
						root: 'users'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.USERNAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			Ext.apply(parameters, {
				url: CMDBuild.core.proxy.Index.group.users.saveGroupUserList
			});

			CMDBuild.core.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();