(function() {

	// TODO bind X button on top of window, to save update after click
	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	var reader = CMDBuild.management.model.widget.ManageEmailConfigurationReader;
	var fields = reader.FIELDS;

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
			var formPanel = buildFormPanel(me, body);

			// To reach the basic form outside
			this.form = formPanel.getForm();

			var btnTemplates = {
				iconCls: 'clone',
				xtype: 'splitbutton',
				text: CMDBuild.Translation.composeFromTemplate,
				disabled: this.readOnly,
				menu: Ext.create('Ext.menu.Menu', {
					items: [],

					listeners: {
						click: function(menu, item, e, eOpts) {
							var record = me.selectedDataStore.getAt(item.index);

							loadFormValues(me, me.form, record.raw);
						}
					}
				})
			};

			this.selectedDataStore = CMDBuild.core.proxy.CMProxyEmailTemplates.getStore();

			this.selectedDataStore.load({
				params: {},
				callback: function(templates) {
					for (var i = 0; i < templates.length; i++) {
						var tmp = templates[i];

						btnTemplates.menu.add({
							text: tmp.get(CMDBuild.core.proxy.CMProxyConstants.NAME) + ' - ' + tmp.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION),
							index: i
						});
					}
				}
			});

			Ext.apply(this, {
				buttons: buildButtons(me),
				items: [formPanel, this.attachmentButtonsContainer, this.attachmentPanelsContainer],
				tbar: [btnTemplates],
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
				html: me.record.get(fields.CONTENT),
				autoScroll: true,
				flex: 1
			});
		} else {
			body = Ext.create('Ext.form.field.HtmlEditor', {
				name: fields.CONTENT,
				fieldLabel: 'Content',
				hideLabel: true,
				enableLinks: false,
				enableSourceEdit: false,
				enableFont: false,
				value: me.record.get(fields.CONTENT),
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
					name: fields.FROM_ADDRESS,
					fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.fromfld,
					disabled: me.readOnly,
					value: me.record.get(fields.FROM_ADDRESS)
				},
				{
					xtype: me.readOnly ? 'displayfield' : 'textfield',
					name: fields.TO_ADDRESS,
					fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld,
					disabled: me.readOnly,
					value: me.record.get(fields.TO_ADDRESS)
				},
				{
					xtype: me.readOnly ? 'displayfield' : 'textfield',
					name: fields.CC_ADDRESS,
					fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld,
					disabled: me.readOnly,
					value: me.record.get(fields.CC_ADDRESS)
				},
				{
					xtype: me.readOnly ? 'displayfield' : 'textfield',
					name: fields.SUBJECT,
					fieldLabel: CMDBuild.Translation.management.modworkflow.extattrs.manageemail.subjectfld,
					disabled: me.readOnly,
					value: me.record.get(fields.SUBJECT)
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
						var valueTo = me.form.getValues()[fields.TO_ADDRESS];
						var valueCC = me.form.getValues()[fields.CC_ADDRESS];

						if (controlAddresses(valueTo, valueCC)) {
							me.save = true;
							// Destroy call an event after(!) the destruction of the window the event saves the values of the form. For save the values
							// only if are correct we have to put this boolean that is valdid only on the confirm button
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
	 * @param {String} valueTo
	 * @param {String} valueCC
	 *
	 * @return {Boolean}
	 */
	function controlAddresses(valueTo, valueCC) {
		var toStr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail.tofld;
		var ccStr = CMDBuild.Translation.management.modworkflow.extattrs.manageemail.ccfld;
		var strError = CMDBuild.Translation.error;
		var errors = [];

		errors = getAddressErrors(strError + ' ' + toStr + ': ', valueTo, errors);

		if (Ext.String.trim(valueCC) != '')
			errors = getAddressErrors(strError + ' ' + ccStr + ': ', valueCC, errors);

		if (errors.length > 0) {
			var messages = htmlComposeMessage(errors);

			CMDBuild.Msg.error(null, messages , false);

			return false;
		}

		return true;
	}

	/**
	 * @param {String} str
	 * @param {String} value
	 * @param {Array} errors
	 *
	 * @return  {Array} errors
	 */
	function getAddressErrors(str, value, errors) {
		var ar = value.split(',');

		for (var i = 0; i < ar.length; i++) {
			var s = Ext.String.trim(ar[i]);

			if (!(/^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$/.test(s)))
				errors.push(str + ': ' + s);

		}

		return errors;
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
	 * @param {CMDBuild.view.management.common.widgets.CMEmailWindow} me - this
	 * @param {Ext.form.FormPanel} form
	 * @param {Object} record
	 */
	function loadFormValues(me, form, record) {
		var xavars = Ext.apply({}, me.delegate.reader.templates(me.delegate.widgetConf), record);

		for (var key in record.variables)
			xavars[key] = record.variables[key];

		var templateResolver = new CMDBuild.Management.TemplateResolver({
			clientForm: clientForm,
			xaVars: xavars,
			serverVars: me.delegate.getTemplateResolverServerVars()
		});

		_createEmailFromTemplate(templateResolver, record, form);
	}

	/**
	 * @param {CMDBuild.Management.TemplateResolver} templateResolver
	 * @param {Object} emailTemplatesData
	 * @param {Ext.form.FormPanel} form
	 */
	function _createEmailFromTemplate(templateResolver, emailTemplatesData, form) {
		templateResolver.resolveTemplates({
			attributes: Ext.Object.getKeys(emailTemplatesData),
			callback: function onTemlatesWereSolved(values) {
				form.setValues([
					{
						id: fields.TO_ADDRESS,
						value: values.to
					},
					{
						id: fields.FROM_ADDRESS,
						value: values.from
					},
					{
						id: fields.CC_ADDRESS,
						value: values.cc
					},
					{
						id: fields.SUBJECT,
						value: values.subject
					},
					{
						id: fields.CONTENT,
						value: values.body
					}
				]);
			}
		});
	}

})();