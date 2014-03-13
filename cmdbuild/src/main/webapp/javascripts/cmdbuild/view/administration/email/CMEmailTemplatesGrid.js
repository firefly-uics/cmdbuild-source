(function() {

	Ext.define('CMDBuild.view.administration.email.CMEmailTemplatesGrid', {
		extend: 'Ext.grid.Panel',

		delegate: undefined,

		border: false,
		frame: false,
		cls: 'cmborderbottom',

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
						header: CMDBuild.Translation.description_,
						flex: 3
					},
					{
						dataIndex: CMDBuild.ServiceProxy.parameter.SUBJECT,
						header: CMDBuild.Translation.subject,
						flex: 2
					}
				],
				store: CMDBuild.core.serviceProxy.CMProxyConfigurationEmailTemplates.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			select: function(row, record, index) {
				this.delegate.cmOn('onRowSelected', {
					'row': row,
					'record': record,
					'index': index
				}, null);
			},

			/**
			 * Event to load store on view display and first row selection as CMDbuild standard
			 */
			viewready: function() {
				var me = this;

				this.store.load({
					callback: function() {
						me.getSelectionModel().select(0, true);
					}
				});
			}
		}
	});

})();