(function() {

	// TODO bind X button on top of window, to save update after click
	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define('CMDBuild.view.management.common.widgets.CMEmailWindowDelegate', {
		/**
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
		 * @param {Ext.form.Basic} form
		 * @param {CMDBuild.management.mail.Model} emailRecord
		 */
		onCMEmailWindowAttachFileChanged: function(emailWindow, form, emailRecord) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
		 * @param {CMDBuild.management.mail.Model} emailRecord
		 */
		onAddAttachmentFromDmsButtonClick: function(emailWindow, emailRecord) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
		 */
		onCMEmailWindowRemoveAttachmentButtonClick: function(emailWindow) {},

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} emailWindow
		 */
		beforeCMEmailWindowDestroy: function(emailWindow) {}
	});

	Ext.define('CMDBuild.view.management.common.widgets.CMEmailWindowFileAttacchedPanel', {
		extend: 'Ext.panel.Panel',

		// Configuration
			fileName: undefined,
			referredEmail: null,
			delegate: undefined,
		// END: Configuration

		frame: true,
		layout: {
			type: 'hbox',
			align: 'middle'
		},
		margin: 5,

		initComponent: function() {
			var me = this;

			Ext.apply(this, {
				items: [
					{
						bodyCls: 'x-panel-body-default-framed',
						border: false,
						html: this.fileName,
						frame: false,
						flex: 1,
					},
					{
						xtype: 'button',
						iconCls: 'delete',
						handler: function() {
							me.delegate.onCMEmailWindowRemoveAttachmentButtonClick(me);
						}
					}
				]
			});

			this.callParent(arguments);
		},

		removeFromEmailWindow: function() {
			this.ownerCt.remove(this);
		}
	});

	Ext.define('CMDBuild.view.management.common.widgets.CMEmailWindow', {
		extend: 'CMDBuild.PopupWindow',

		// Configuration
			emailGrid: undefined,
			readOnly: false,
			record: undefined,
			delegate: undefined,
		// END: Configuration

		buttonAlign: 'center',

		layout: {
			type: 'vbox',
			align: 'stretch'
		},

		title: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.compose,

		initComponent : function() {
			var me = this;

			var body = bodyBuild(me);
			this.attachmentPanelsContainer = buildAttachmentPanelsContainer(me);
			this.attachmentButtonsContainer = buildAttachmentButtonsContainer(me);
			this.formPanel = buildFormPanel(me, body);

			// To reach the basic form outside
			this.form = this.formPanel.getForm();

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
				buttons: buildButtons(me),
				items: [this.formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer],
				tbar: [this.fillFromTemplateButton],
			});

			this.delegate = this.delegate || Ext.create('CMDBuild.view.management.common.widgets.CMEmailWindowDelegate');

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
				Ext.create('CMDBuild.view.management.common.widgets.CMEmailWindowFileAttacchedPanel', {
					fileName: fileName,
					referredEmail: emailRecord,
					delegate: this.delegate
				})
			);

			this.attachmentPanelsContainer.doLayout();
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
	 * @return {Object} body
	 */
	function bodyBuild(me) {
		var body;

		if (me.readOnly) {
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
	}

	/**
	 * @param {Object} me - this
	 * @param {Object} body
	 *
	 * @return {Ext.form.FormPanel}
	 */
	function buildFormPanel(me, body) {
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
					name: CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESS,
					allowBlank: false,
					fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
					disabled: me.readOnly,
					vtype: me.readOnly ? null : 'multiemail',
					value: me.record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESS)
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
	}

	/**
	 * @param {Object} me - this
	 *
	 * @return {Array} buttons
	 */
	function buildButtons(me) {
		var buttons;

		if (me.readOnly) {
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
	}

	/**
	 * @param {Object} me - this
	 *
	 * @return {Ext.container.Container}
	 */
	function buildAttachmentButtonsContainer(me) {
		return Ext.create('Ext.container.Container', {
			layout: {
				type: 'hbox',
				padding: '0 5'
			},

			disabled: me.readOnly,
			items: [
				buildUploadForm(me)
				,
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
	}

	/**
	 * @param {Object} me - this
	 */
	function buildAttachmentPanelsContainer(me) {
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
	}

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
	 * @param {Array} errors
	 *
	 * @return {String} messages
	 */
	function htmlComposeMessage(errors) {
		var messages = '';

		for (var i = 0; i < errors.length; i++) {
			var msg = Ext.String.format('<p class="{0}">{1}</p>', CMDBuild.Constants.css.error_msg, errors[i]);

			messages += msg;
		}

		return messages;
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