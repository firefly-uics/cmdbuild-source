(function() {

	Ext.define('CMDBuild.view.administration.userAndGroup.user.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.userAndGroup.user.User'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.userAndGroup.user.User}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.USERNAME,
						text: CMDBuild.Translation.username,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					Ext.create('Ext.ux.grid.column.Active', {
						dataIndex: CMDBuild.core.constants.Proxy.IS_ACTIVE,
						text: CMDBuild.Translation.active,
						width: 60,
						align: 'center',
						sortable: false,
						hideable: false,
						menuDisabled: true,
						fixed: true
					})
				],
				store: CMDBuild.core.proxy.userAndGroup.user.User.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onUserAndGroupUserItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onUserAndGroupUserRowSelected');
			}
		}
	});

})();