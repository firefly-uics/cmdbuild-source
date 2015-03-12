(function() {

	Ext.define('CMDBuild.view.management.common.widgets.manageEmail.emailWindow.ViewWindow', {
		extend: 'CMDBuild.PopupWindow',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

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

			this.attachmentContainer = Ext.create('CMDBuild.view.management.common.widgets.manageEmail.attachments.MainContainer', {
				height: '30%',
				region: 'south',
				readOnly: true
			});

			this.formPanel = Ext.create('Ext.panel.Panel', {
				region: 'center',
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
						flex: 1,
						html: this.delegate.record.get(CMDBuild.core.proxy.CMProxyConstants.BODY)
					}
				]
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
					Ext.create('CMDBuild.buttons.CloseButton', {
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