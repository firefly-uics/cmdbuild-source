(function() {

	Ext.define('CMDBuild.view.management.common.widgets.email.CMEmailWindow', {
		alternateClassName: 'CMDBuild.view.management.common.widgets.CMEmailWindow', // Legacy class name
		extend: 'CMDBuild.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyEmailTemplates'
		],

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
		 * @cfg {CMDBuild.view.management.common.widgets.CMEmailGrid}
		 */
		emailGrid: undefined,

		/**
		 * @cfg {Ext.button.Split}
		 */
		fillFromTemplateButton: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.email.EmailWindowForm}
		 */
		formPanel: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.grid}
		 */
		record: undefined,

		/**
		 * @property {Ext.data.Store}
		 */
		templatesStore: undefined,

		/**
		 * Possible values: create, edit, reply, view
		 *
		 * @cfg {String}
		 */
		windowMode: 'create',

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
				disabled: this.readOnly,

				handler: function() {
					this.showMenu();
				},

				menu: Ext.create('Ext.menu.Menu', {
					items: []
				})
			});
			// END: Buttons configuration

			// Fill from template button store configuration
			this.templatesStore = CMDBuild.core.proxy.CMProxyEmailTemplates.getStore();
			this.templatesStore.load({
				callback: function(records, operation, success) {
					if (records.length > 0) {
						for (var index in records) {
							var record = records[index];

							me.fillFromTemplateButton.menu.add({
								text: record.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
								templateId: record.get(CMDBuild.core.proxy.CMProxyConstants.ID),

								handler: function(button, e) {
									me.onFillFromTemplateButtonClick(button[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID]);
								}
							});
						}
					} else { // To disable button if the aren't templates
						me.fillFromTemplateButton.setDisabled(true);
					}
				}
			});

			this.attachmentPanelsContainer = this.buildAttachmentPanelsContainer();
			this.attachmentButtonsContainer = this.buildAttachmentButtonsContainer();

			this.formPanel = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowForm', {
				readOnly: this.readOnly,
				record: this.record
			});
			this.form = this.formPanel.getForm(); // To reach the basic form outside

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
				buttons: this.buildButtons()
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
		},

		/**
		 * @return {Ext.container.Container}
		 */
		buildAttachmentButtonsContainer: function() {
			var me = this;

			return Ext.create('Ext.container.Container', {
				layout: {
					type: 'hbox',
					padding: '0 5'
				},

				disabled: me.readOnly,

				items: [
					buildUploadForm(me),
					{
						xtype: 'button',
						margin: '0 0 0 5',
						text: CMDBuild.Translation.add_attachment_from_dms,

						handler: function() {
							me.delegate.onAddAttachmentFromDmsButtonClick(me, me.record);
						}
					}
				]
			});
		},

		/**
		 * @return {Ext.container.Container}
		 */
		buildAttachmentPanelsContainer: function() {
			var me = this;

			return Ext.create('Ext.container.Container', {
				autoScroll: true,
				flex: 1,
				disabled: me.readOnly,
				getFileNames: function() {
					var names = [];

					this.items.each(function(i) {
						names.push(i.fileName);
					});

					return names;
				}
			});
		},

		/**
		 * @return {Array} buttons
		 */
		buildButtons: function() {
			var buttons = [];

			if (this.readOnly) {
				buttons = [
					Ext.create('CMDBuild.buttons.CloseButton', {
						scope: this,

						handler: function() {
							this.delegate.cmOn('onEmailWindowAbortButtonClick');
						}
					})
				];
			} else {
				buttons = [
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
				];
			}

			return buttons;
		},

		/**
		 * Fills form with template data, but if window was in "reply" mode fills only email content with prepend function
		 *
		 * @param {CMDBuild.Management.TemplateResolver} templateResolver
		 * @param {Object} emailTemplatesData
		 */
		createEmailFromTemplate: function(templateResolver, emailTemplatesData) {
			var me = this;

			templateResolver.resolveTemplates({
				attributes: Ext.Object.getKeys(emailTemplatesData),
				callback: function(values) {
					var setValueArray = [];
					var content = values[CMDBuild.core.proxy.CMProxyConstants.BODY];

					if (me.windowMode == 'reply') {
						setValueArray.push({
							id: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
							value: content + '<br /><br />' + me.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT)
						});
					} else {
						setValueArray.push(
							{
								id: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
								value: values[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES,
								value: values[CMDBuild.core.proxy.CMProxyConstants.TO]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
								value: values[CMDBuild.core.proxy.CMProxyConstants.FROM]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES,
								value: values[CMDBuild.core.proxy.CMProxyConstants.CC]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
								value: values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
								value: content
							}
						);
					}

					me.form.setValues(setValueArray);
				}
			});
		},

		/**
		 * @param {Object} record - CMDBuild.model.CMModelEmailTemplates.grid raw value
		 */
		loadFormValues: function(record) {
			var me = this;

			var xaVars = Ext.apply({}, this.delegate.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], record);

			for (var key in record.variables)
				xaVars[key] = record.variables[key];

			var templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: me.formPanel.getForm(),
				xaVars: xaVars,
				serverVars: me.delegate.getTemplateResolverServerVars()
			});

			this.createEmailFromTemplate(templateResolver, record);
		},

		/**
		 * @param {Int} templateId
		 */
		onFillFromTemplateButtonClick: function(templateId) {
			this.loadFormValues(
				this.templatesStore.getAt(
						this.templatesStore.find(CMDBuild.core.proxy.CMProxyConstants.ID, templateId)
				).raw
			);
		}
	});

	/**
	 * @param {Object} me - this
	 *
	 * @return {Ext.form.Panel}
	 */
	function buildUploadForm(me) {
		return Ext.create('Ext.form.Panel', {
			frame: false,
			border: false,
			bodyCls: 'x-panel-body-default-framed',
			items: [ {
				xtype: 'filefield',
				name: 'file',
				buttonText: CMDBuild.Translation.attachfile,
				buttonOnly: true,

				listeners: {
					change: function(field, value) {
						var form = this.up('form').getForm();

						me.delegate.onCMEmailWindowAttachFileChanged(me, form, me.record);
					}
				}
			}]
		});
	}

})();