(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.user;

	Ext.define('CMDBuild.view.administration.user.CMUserGrid', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			// TODO: Require CMDBuild.ServiceProxy.group class
		],

		border: false,
		frame: false,

		initComponent : function() {
			Ext.apply(this, {
				columns: [
					{
						header: tr.username,
						dataIndex: 'username',
						flex: 1
					},
					{
						header: tr.description,
						dataIndex: 'description',
						flex: 1
					}
				],
				store: CMDBuild.ServiceProxy.group.getUserStoreForGrid()
			});

			this.callParent(arguments);
		},

		listeners: {
			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function() {
				this.store.load({
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