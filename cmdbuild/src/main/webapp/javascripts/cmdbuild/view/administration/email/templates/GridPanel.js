(function() {

	Ext.define('CMDBuild.view.administration.email.templates.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Main}
		 */
		delegate: undefined,

		border: false,
		cls: 'cmborderbottom',
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
						flex: 3
					},
					{
						dataIndex: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						text: CMDBuild.Translation.subject,
						flex: 2
					}
				],
				store: CMDBuild.core.proxy.EmailTemplates.getStore()
			});

			this.callParent(arguments);
		},

		listeners: {
			itemdblclick: function(grid, record, item, index, e, eOpts) {
				this.delegate.cmfg('onEmailTemplatesItemDoubleClick');
			},

			select: function(row, record, index) {
				this.delegate.cmfg('onEmailTemplatesRowSelected');
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