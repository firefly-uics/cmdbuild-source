(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowEdit', {
		extend: 'CMDBuild.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow}
		 */
		delegate: undefined,

		attachmentContainer: undefined,

//		/**
//		 * @property {Ext.container.Container}
//		 */
//		attachmentPanelsContainer: undefined,
//
//		/**
//		 * @property {Ext.container.Container}
//		 */
//		attachmentButtonsContainer: undefined,

		/**
		 * @cfg {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.manageEmail.EmailWindowEditForm}
		 */
		formPanel: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		templatesStore: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.composeEmail,

		layout: 'border',

		initComponent: function() {
			var me = this;
_debug('EmailWindowEdit', this.delegate);
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

			// Fill from template button store configuration
			// TODO implementare questa parte nel controller
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.EmailTemplates.getAll({
				scope: this,
				success: function(response, options, decodedResponse) {
					var templatesArray = decodedResponse.response.elements;

					if (templatesArray.length > 0) {
						// Sort templatesArray by description ascending
						Ext.Array.sort(templatesArray, function(item1, item2) {
							if (item1[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] < item2[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION])
								return -1;

							if (item1[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] > item2[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION])
								return 1;

							return 0;
						});

						Ext.Array.forEach(templatesArray, function(template, index, allItems) {
							this.fillFromTemplateButton.menu.add({
								text: template[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
								templateName: template[CMDBuild.core.proxy.CMProxyConstants.NAME],

								handler: function(button, e) {
									me.delegate.cmOn('onFillFromTemplateButtonClick', button[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_NAME]);
								}
							});
						}, this);
					} else { // To disable button if the aren't templates
						this.fillFromTemplateButton.setDisabled(true);
					}
				},
				callback: function(options, success, response) {
					CMDBuild.LoadMask.get().hide();
				}
			});

			this.formPanel = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowEditForm', {
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
