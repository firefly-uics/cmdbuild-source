(function() {

	Ext.define('CMDBuild.view.administration.dataView.sql.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.dataView.Sql'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Sql}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.constants.Proxy.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.constants.Proxy.SOURCE_FUNCTION,
						text: CMDBuild.Translation.dataSource,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.dataView.Sql.getStore()
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

			// Event to load store on view display and first row selection as CMDBuild standard
			viewready: function() {
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