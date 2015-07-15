(function() {

	Ext.define('CMDBuild.view.administration.email.templates.GridPanel', {
		extend: 'Ext.grid.Panel',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.email.Templates'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.email.templates.Templates}
		 */
		delegate: undefined,

		border: false,
		frame: false,

		initComponent: function() {
			Ext.apply(this, {
				columns: [
					{
						dataIndex: CMDBuild.core.proxy.Constants.NAME,
						text: CMDBuild.Translation.name,
						flex: 1
					},
					{
						dataIndex: CMDBuild.core.proxy.Constants.DESCRIPTION,
						text: CMDBuild.Translation.descriptionLabel,
						flex: 3
					},
					{
						dataIndex: CMDBuild.core.proxy.Constants.SUBJECT,
						text: CMDBuild.Translation.subject,
						flex: 2
					}
				],
				store: CMDBuild.core.proxy.email.Templates.getStore()
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