(function() {

	Ext.define('CMDBuild.view.administration.users.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Users'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.users.Users}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
						text: CMDBuild.Translation.username,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.Users.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onUserItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onUserRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function(panel, e0pts) {
				this.getStore().load({
					scope: this,
					callback: function(records, operation, success) {
						// Store load errors manage
						if (!success) {
							CMDBuild.core.Message.error(null, {
								text: CMDBuild.Translation.errors.unknown_error,
								detail: operation.error
							});
						}

						if (!this.getSelectionModel().hasSelection())
							this.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();