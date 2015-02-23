(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowFileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Attachments}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		fileName: undefined,

		frame: true,
		margin: 5,

		layout: {
			type: 'hbox',
			align: 'middle'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					{
						xtype: 'panel',
						bodyCls: 'x-panel-body-default-framed',
						border: false,
						html: this.fileName,
						frame: false,
						flex: 1
					},
					{
						xtype: 'button',
						iconCls: 'delete',
						scope: this,

						handler: function() {
							this.delegate.cmOn('onRemoveAttachmentButtonClick', this);
						}
					}
				]
			});

			this.callParent(arguments);
		}
	});

})();