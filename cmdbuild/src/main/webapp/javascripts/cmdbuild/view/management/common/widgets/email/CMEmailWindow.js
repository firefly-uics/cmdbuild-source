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
		 * @cfg {CMDBuild.view.management.common.widgets.CMEmailGrid}
		 */
		emailGrid: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: false,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.grid}
		 */
		record: undefined,

		buttonAlign: 'center',
		title: CMDBuild.Translation.composeEmail,

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		initComponent: function() {
			var me = this;
			var body = this.bodyBuild();

			this.attachmentPanelsContainer = this.buildAttachmentPanelsContainer();
			this.attachmentButtonsContainer = this.buildAttachmentButtonsContainer();
			this.formPanel = this.buildFormPanel(body);
			this.form = this.formPanel.getForm(); // To reach the basic form outside

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
									me.onFillFromTemplateButtonClick(button.templateId);
								}
							});
						}
					} else { // To disable button if the aren't templates
						me.fillFromTemplateButton.setDisabled(true);
					}
				}
			});

			Ext.apply(this, {
				buttons: this.buildButtons(),
				items: [this.formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer],
				tbar: [this.fillFromTemplateButton]
			});

			this.callParent(arguments);

			fixIEFocusIssue(this, body);

			var attachments = this.record.getAttachmentNames();

			for (var i = 0; i < attachments.length; ++i) {
				var attachmentName = attachments[i];

				this.addAttachmentPanel(attachmentName, this.record);
			}

			this.on('beforedestroy', function () {
				if (this.save)
					this.delegate.beforeCMEmailWindowDestroy(this);
			}, this);

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
		 * @return {Mixed} body
		 */
		bodyBuild: function() {
			var me = this;
			var body = null;

			if (this.readOnly) {
				body = Ext.create('Ext.panel.Panel', {
					frame: true,
					border: true,
					html: me.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT),
					autoScroll: true,
					flex: 1
				});
			} else {
				body = Ext.create('CMDBuild.view.common.field.CMHtmlEditorField', {
					name: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
					hideLabel: true,
					enableFont: false,
					value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT),
					flex: 1
				});
			}

			return body;
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
			var me = this;
			var buttons = [];

			if (this.readOnly) {
				buttons = [
					Ext.create('CMDBuild.buttons.CloseButton', {
						handler: function() {
							me.destroy();
						}
					})
				];
			} else {
				buttons = [
					Ext.create('CMDBuild.buttons.ConfirmButton', {
						scope: me,

						handler: function() {
							var valueTo = me.form.getValues()[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESS];
							var valueCC = me.form.getValues()[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESS];

							if (me.getNonValidFormFields().length > 0) {
								CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
							} else {
								me.save = true;
								// Destroy call an event after(!) the destruction of the window the event saves the values of the form. For save the values
								// only if are correct we have to put this boolean that is valid only on the confirm button
								me.destroy();
								me.save = false;
							}
						}
					}),
					Ext.create('CMDBuild.buttons.AbortButton', {
						handler: function() {
							me.destroy();
						}
					})
				];
			}

			return buttons;
		},

		/**
		 * @param {Object} body
		 *
		 * @return {Ext.form.FormPanel}
		 */
		buildFormPanel: function(body) {
			var me = this;

			return Ext.create('Ext.form.FormPanel', {
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
						name: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.ACCOUNT)
					},
					{
						xtype: 'displayfield',
						name: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
						disabled: me.readOnly,
						vtype: me.readOnly ? null : 'multiemail',
						value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS)
					},
					{
						xtype: me.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
						disabled: me.readOnly,
						vtype: me.readOnly ? null : 'multiemail',
						value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES)
					},
					{
						xtype: me.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESS,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
						disabled: me.readOnly,
						vtype: me.readOnly ? null : 'multiemail',
						value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESS)
					},
					{
						xtype: me.readOnly ? 'displayfield' : 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						allowBlank: false,
						fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
						disabled: me.readOnly,
						value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT)
					},
					body
				]
			});
		},

		/**
		 * TODO: would be better to build formPanel extending CMDBUild.view.common.CMFormFunctions as all other CMSDBuild forms so this function will be useless
		 *
		 * @return {Array} data
		 */
		getNonValidFormFields: function() {
			var data = [];

			this.formPanel.cascade(function(item) {
				if (item
					&& (item instanceof Ext.form.Field)
					&& !item.disabled
				) {
					if (!item.isValid()) {
						data.push(item);
					}
				}
			});

			return data;
		},


		/**
		 * @param {Object} record - CMDBuild.model.CMModelEmailTemplates.grid raw value
		 */
		loadFormValues: function(record) {
			var me = this;

			var xavars = Ext.apply({}, this.delegate.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES], record);

			for (var key in record.variables)
				xavars[key] = record.variables[key];

			var templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: xavars,
				serverVars: me.delegate.getTemplateResolverServerVars()
			});

			_createEmailFromTemplate(templateResolver, record, this.form);
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

	/**
	 * @param {Object} me - this
	 * @param {Object} body
	 */
	function fixIEFocusIssue(me, body) {
		// Sometimes on IE the HtmlEditor is not able to take the focus after the mouse click. With this call it works. The reason is currently unknown.
		if (Ext.isIE) {
			me.mon(body, 'render', function() {
				try {
					body.focus();
				} catch (e) {}
			}, me);
		}
	}

	/**
	 * @param {CMDBuild.Management.TemplateResolver} templateResolver
	 * @param {Object} emailTemplatesData
	 * @param {Ext.form.FormPanel} form
	 */
	function _createEmailFromTemplate(templateResolver, emailTemplatesData, form) {
		templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(emailTemplatesData),
			callback: function(values) {
				form.setValues([
					{
						id: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
						value: values[CMDBuild.core.proxy.CMProxyConstants.DEFAULT_ACCOUNT]
					},
					{
						id: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESS,
						value: values.to
					},
					{
						id: CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
						value: values.from
					},
					{
						id: CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESS,
						value: values.cc
					},
					{
						id: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
						value: values.subject
					},
					{
						id: CMDBuild.core.proxy.CMProxyConstants.CONTENT,
						value: values.body
					}
				]);
			}
		});
	}

})();