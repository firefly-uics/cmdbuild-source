(function() {

	Ext.define('CMDBuild.view.management.common.widgets.email.CMEmailWindowFileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMManageEmailController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		fileName: undefined,

		/**
		 * @property {Object}
		 */
		referredEmail: null,

		frame: true,
		layout: {
			type: 'hbox',
			align: 'middle'
		},
		margin: 5,

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				items: [
					{
						xtype: 'panel',
						bodyCls: 'x-panel-body-default-framed',
						border: false,
						html: this.fileName,
						frame: false,
						flex: 1,
					},
					{
						xtype: 'button',
						iconCls: 'delete',

						handler: function() {
							me.delegate.onCMEmailWindowRemoveAttachmentButtonClick(me);
						}
					}
				]
			});

			this.callParent(arguments);
		},

		removeFromEmailWindow: function() {
			this.ownerCt.remove(this);
		}
	});

})();