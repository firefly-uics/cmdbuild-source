(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.attachments.FileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Attachments}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		fileName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

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
						disabled: this.readOnly,
						scope: this,

						handler: function(button, e) {
							this.delegate.cmfg('onAttachmentRemoveButtonClick', this);
						}
					}
				]
			});

			this.callParent(arguments);
		}
	});

})();