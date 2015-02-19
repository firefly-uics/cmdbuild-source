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

		/**
		 * @property {Ext.container.Container}
		 */
		attachmentPanelsContainer: undefined,

		/**
		 * @property {Ext.container.Container}
		 */
		attachmentButtonsContainer: undefined,

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

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

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

			// Fill from template button store configuration
			this.setLoading(true);
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
					this.setLoading(false);
				}
			});

			this.attachmentButtonsContainer = Ext.create('Ext.container.Container', {
				layout: {
					type: 'hbox',
					padding: '0 5'
				},

				items: [
					Ext.create('Ext.form.Panel', {
						frame: false,
						border: false,
						bodyCls: 'x-panel-body-default-framed',

						items: [
							{
								xtype: 'filefield',
								name: 'file',
								buttonText: CMDBuild.Translation.attachfile,
								buttonOnly: true,

								listeners: {
									change: function(field, value) {
										var form = this.up('form').getForm();

										me.delegate.onCMEmailWindowAttachFileChanged(me, form, me.delegate.record); // TODO: use cmOn and this
									}
								}
							}
						]
					}),
					Ext.create('Ext.button.Button', {
						margin: '0 0 0 5',
						text: CMDBuild.Translation.add_attachment_from_dms,

						handler: function() {
							me.delegate.onAddAttachmentFromDmsButtonClick(me, me.delegate.record); // TODO: use cmOn and this
						}
					})
				]
			});

			this.attachmentPanelsContainer = Ext.create('Ext.container.Container', {
				autoScroll: true,
				flex: 1,

				getFileNames: function() {
					var names = [];

					this.items.each(function(i) {
						names.push(i.fileName);
					});

					return names;
				}
			});

			this.formPanel = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowEditForm', {
				delegate: this.delegate
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
				items: [this.formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer],
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