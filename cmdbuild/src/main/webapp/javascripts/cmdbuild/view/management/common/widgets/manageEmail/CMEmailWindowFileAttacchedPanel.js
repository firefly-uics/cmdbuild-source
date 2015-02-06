(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowFileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
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
							this.delegate.onCMEmailWindowRemoveAttachmentButtonClick(me); // TODO: use cmOn and this
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