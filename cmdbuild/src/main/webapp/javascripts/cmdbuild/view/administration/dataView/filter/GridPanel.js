(function() {

	Ext.define('CMDBuild.view.administration.dataView.filter.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.dataViews.Filter'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.dataView.Filter}
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
						dataIndex: _CMProxy.parameter.SOURCE_CLASS_NAME,
						text: CMDBuild.Translation.targetClass,
						flex: 1
					}
				],
				store: CMDBuild.core.proxy.dataViews.Filter.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onDataViewFilterItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onDataViewFilterRowSelected');
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