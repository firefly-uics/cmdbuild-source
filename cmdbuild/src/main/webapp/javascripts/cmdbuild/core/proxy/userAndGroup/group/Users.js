(function () {

	Ext.define('CMDBuild.core.proxy.userAndGroup.group.Users', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.userAndGroup.group.UsersGrid'
		],

		singleton: true,

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getGroupsUserStore: function (parameters) {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.UNCACHED, {
				autoLoad: false,
				model: 'CMDBuild.model.userAndGroup.group.UsersGrid',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.group.users.getGroupUserList,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.USERS
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.USERNAME, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		update: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.group.users.saveGroupUserList });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.GROUP, parameters, true);
		}
	});

})();

