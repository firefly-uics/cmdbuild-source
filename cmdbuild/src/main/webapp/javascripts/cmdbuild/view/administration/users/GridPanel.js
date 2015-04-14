(function() {

	Ext.define('CMDBuild.view.administration.users.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.users.Main}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent : function() {
			Ext.apply(this, {
				columns: [
					{
						header: CMDBuild.Translation.username,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
						flex: 1
					},
					{
						header: CMDBuild.Translation.descriptionLabel,
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.Users.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmOn('onItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmOn('onUserRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function(panel, e0pts) {
				this.getStore().load({
					scope: this,
					callback: function() {
						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();