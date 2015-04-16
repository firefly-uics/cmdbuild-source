(function() {

	Ext.define('CMDBuild.view.management.common.tabs.email.emailWindow.ViewWindow', {
		extend: 'CMDBuild.PopupWindow',

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
		 * @property {Ext.panel.Panel}
		 */
		formPanel: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.viewEmail,

		layout: 'border',

		initComponent: function() {
			// Buttons configuration
				this.fillFromTemplateButton = Ext.create('Ext.button.Split', {
					iconCls: 'clone',
					text: CMDBuild.Translation.composeFromTemplate,
					disabled: true,

					menu: Ext.create('Ext.menu.Menu', {
						items: []
					})
				});
			// END: Buttons configuration

			this.attachmentContainer = Ext.create('CMDBuild.view.management.common.tabs.email.attachments.MainContainer', {
				height: '30%',
				region: 'south',
				readOnly: true
			});

			// Use real field to translate values
			this.delayField = Ext.create('CMDBuild.view.common.field.delay.Delay', {
				value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.DELAY),
			});

			this.formPanel = Ext.create('Ext.panel.Panel', {
				region: 'center',
				frame: false,
				border: false,
				padding: '0 5',
				bodyCls: 'x-panel-body-default-framed',

				layout: {
					type: 'vbox',
					align: 'stretch' // Child items are stretched to full width
				},

				defaults: {
					labelAlign: 'right',
					labelWidth: CMDBuild.LABEL_WIDTH
				},

				items: [
					{
						xtype: 'checkbox',
						fieldLabel: CMDBuild.Translation.keepSync,
						readOnly: true,
						name: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION)
					},
					{
						xtype: 'displayfield',
						fieldLabel: CMDBuild.Translation.delay,
						readOnly: true,
						name: CMDBuild.core.proxy.CMProxyConstants.DELAY,
						value: this.delayField.getRawValue()
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.FROM,
						fieldLabel: CMDBuild.Translation.from,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO,
						fieldLabel: CMDBuild.Translation.to,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.TO)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC,
						fieldLabel: CMDBuild.Translation.cc,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.CC)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.BCC,
						fieldLabel: CMDBuild.Translation.bcc,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.BCC)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						fieldLabel: CMDBuild.Translation.subject,
						value: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT)
					},
					{
						xtype: 'panel',
						autoScroll: true,
						frame: true,
						border: true,
						margin: '1 0', // Fixes a bug that hides bottom border
						flex: 1,
						html: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.BODY)
					}
				]
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [this.fillFromTemplateButton]
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
							Ext.create('CMDBuild.core.buttons.Close', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onEmailWindowAbortButtonClick');
								}
							})
						]
					})
				],
				items: [this.formPanel, this.attachmentContainer]
			});

			this.callParent(arguments);
		}
	});

})();