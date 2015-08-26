(function() {

	Ext.define('CMDBuild.view.administration.groups.users.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.groups.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.groups.Users}
		 */
		delegate: undefined,

		border: true,
		frame: false,
		flex: 1,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						text: CMDBuild.Translation.username,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.groups.Users.getGroupsUserStore()
			});

			this.callParent(arguments);
		}
	});

})();