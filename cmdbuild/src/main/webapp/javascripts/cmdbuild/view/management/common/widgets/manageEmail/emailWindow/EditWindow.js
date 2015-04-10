(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.emailWindow.EditWindow', {
		extend: 'CMDBuild.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer}
		 */
		attachmentContainer: undefined,

		/**
		 * @property {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.emailWindow.EditForm}
		 */
		formPanel: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.composeEmail,

		layout: 'border',

		initComponent: function() {
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

			this.formPanel = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.emailWindow.EditForm', {
				delegate: this.delegate,
				region: 'center'
			});

			this.attachmentContainer = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer', {
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
					}
				],
				items: [this.formPanel, this.attachmentContainer],
				buttons: [
					Ext.create('CMDBuild.buttons.ConfirmButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowConfirmButtonClick');
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);
		}
	});

})();