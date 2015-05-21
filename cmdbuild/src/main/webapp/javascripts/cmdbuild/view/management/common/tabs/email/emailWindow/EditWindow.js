(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.EditWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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
		form: undefined,

		title: CMDBuild.Translation.composeEmail,

		layout: 'border',

		initComponent: function() {
			this.form = Ext.create('CMDBuild.view.management.common.tabs.email.emailWindow.EditForm', {
				delegate: this.delegate,
				region: 'center'
			});

			this.attachmentContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
				height: '30%',
				region: 'south'
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
								iconCls: 'clone',
								text: CMDBuild.Translation.composeFromTemplate,

								handler: function() {
									this.showMenu();
								},

								menu: Ext.create('Ext.menu.Menu', {
									items: []
								})
							})
						]
					}),
					Ext.create('Ext.toolbar.Toolbar', {
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
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailWindowConfirmButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.form, this.attachmentContainer]
			});

			this.callParent(arguments);
		}
	});

})();