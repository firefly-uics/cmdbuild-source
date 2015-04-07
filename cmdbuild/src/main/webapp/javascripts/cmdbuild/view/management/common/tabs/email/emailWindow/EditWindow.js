(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.EditWindow', {
		extend: 'CMDBuild.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.attachments.MainContainer}
		 */
		attachmentContainer: undefined,

		/**
		 * @property {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.emailWindow.EditForm}
		 */
		formPanel: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.composeEmail,

		layout: 'border',

		initComponent: function() {
			var me = this;

			// Buttons configuration
				this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
					iconCls: 'clone',
					text: CMDBuild.Translation.composeFromTemplate,

					handler: function() {
						this.showMenu();
					},

					menu: Ext.create('Ext.menu.Menu', {
						items: []
					})
				});
			// END: Buttons configuration

			this.formPanel = Ext.create('CMDBuild.view.management.common.tabs.email.emailWindow.EditForm', {
				delegate: this.delegate,
				region: 'center'
			});

			this.attachmentContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
				height: '30%',
				region: 'south'
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.fillFromTemplateButton]
					},
					{
						xtype: 'toolbar',
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Confirm', {
								handler: function(button, e) {
									me.delegate.cmfg('onEmailWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								handler: function(button, e) {
									me.delegate.cmfg('onEmailWindowAbortButtonClick');
								}
							})
						]
					}
				],
				items: [this.formPanel, this.attachmentContainer]
			});

			this.callParent(arguments);
		}
	});

})();