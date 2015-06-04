(function() {

	Ext.define('CMDBuild.view.administration.dataViews.sql.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.dataViews.Sql'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataViews.Sql}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					{
						dataIndex: _CMProxy.parameter.SOURCE_FUNCTION,
						text: CMDBuild.Translation.dataSource,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.dataViews.Sql.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onDataViewSqlItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onDataViewSqlRowSelected');
			},

			// Event to load store on view display and first row selection as CMDbuild standard
			viewready: function() {
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