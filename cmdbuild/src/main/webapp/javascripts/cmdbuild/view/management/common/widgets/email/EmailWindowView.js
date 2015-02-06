(function() {

	Ext.define('CMDBuild.view.management.common.widgets.email.EmailWindowView', {
		extend: 'CMDBuild.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.CMManageEmailController}
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
		 * @property {Ext.panel.Panel}
		 */
		formPanel: undefined,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.email}
		 */
		record: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.viewEmail,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			// Buttons configuration
				this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
					iconCls: 'clone',
					text: CMDBuild.Translation.composeFromTemplate,
					disabled: true,

					handler: function() {
						this.showMenu();
					},

					menu: Ext.create('Ext.menu.Menu', {
						items: []
					})
				});
			// END: Buttons configuration

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

								disabled: true
							}
						]
					}),
					Ext.create('Ext.button.Button', {
						margin: '0 0 0 5',
						text: CMDBuild.Translation.add_attachment_from_dms,

						disabled: true
					})
				]
			});

			this.attachmentPanelsContainer = Ext.create('Ext.container.Container', {
				autoScroll: true,
				flex: 1
			});

			this.formPanel = Ext.create('Ext.panel.Panel', {
				frame: false,
				border: false,
				padding: '5',
				flex: 3,
				bodyCls: 'x-panel-body-default-framed',

				layout: {
					type: 'vbox',
					align: 'stretch' // Child items are stretched to full width
				},

				defaults: {
					labelAlign: 'right'
				},

				items: [
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.ID,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID)
					},
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY)
					},
					{
						xtype: 'hidden',
						name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
						value: this.record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT)
					},
					{
						xtype: 'panel',
						autoScroll: true,
						frame: true,
						border: true,
						flex: 1,
						html: this.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT)
					}
				]
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: this.fillFromTemplateButton
					}
				],
				items: [this.formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer],
				buttons: [
					Ext.create('CMDBuild.buttons.CloseButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowAbortButtonClick');
						}
					})
				]
			});

			this.callParent(arguments);

			var attachments = this.record.getAttachmentNames();

			for (var i = 0; i < attachments.length; ++i) {
				var attachmentName = attachments[i];

				this.addAttachmentPanel(attachmentName, this.record);
			}
		},

		addAttachmentPanel: function(fileName, emailRecord) {
			this.attachmentPanelsContainer.add(
				Ext.create('CMDBuild.view.management.common.widgets.email.CMEmailWindowFileAttacchedPanel', {
					fileName: fileName,
					referredEmail: emailRecord,
					delegate: this.delegate
				})
			);

			this.attachmentPanelsContainer.doLayout();
		}
	});

})();