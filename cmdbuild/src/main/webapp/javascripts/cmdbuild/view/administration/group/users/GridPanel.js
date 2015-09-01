(function() {

	Ext.define('CMDBuild.view.administration.group.users.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.group.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Users}
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
						dataIndex: CMDBuild.core.proxy.Constants.USERNAME,
						flex: 1
					},
					{
						text: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.proxy.Constants.DESCRIPTION,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.group.Users.getGroupsUserStore()
			});

			this.callParent(arguments);
		}
	});

})();