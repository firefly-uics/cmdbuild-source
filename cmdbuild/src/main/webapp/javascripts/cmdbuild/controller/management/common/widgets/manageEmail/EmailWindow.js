(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {

		requires: [
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates',
			'CMDBuild.model.EmailTemplates'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Main}
		 */
		widgetController: undefined,

		/**
		 * Used as flag to avoid pop-up spam
		 *
		 * @cfg {Boolean}
		 */
		isAdvicePrompted: false,

		/**
		 * @property {CMDBuild.model.widget.ManageEmail.email}
		 */
		record: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Mixed} emailWindows
		 */
		view: undefined,

		/**
		 * @property {Object}
		 */
		widgetConf: undefined,

		/**
		 * @cfg {String}
		 */
		windowMode: 'create',

		/**
		 * @cfg {Array}
		 */
		windowModeAvailable: ['confirm','create', 'edit', 'reply', 'view'],

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Main} configObject.parentDelegate
		 * @param {CMDBuild.model.widget.ManageEmail.email} record
		 * @param {String} configObject.windowMode
		 */
		constructor: function(configObject) {
			var enableAttachmentSetup = true;
			var windowClassName = null;

			Ext.apply(this, configObject); // Apply config

			if (Ext.Array.contains(this.windowModeAvailable, this.windowMode)) {
				switch (this.windowMode) {
					case 'confirm': {
						windowClassName = 'CMDBuild.view.management.common.widgets.manageEmail.EmailWindowConfirmRegeneration';
						enableAttachmentSetup = false;
					} break;

					case 'view': {
						windowClassName = 'CMDBuild.view.management.common.widgets.manageEmail.EmailWindowView';
					} break;

					default: { // Default window class to build
						windowClassName = 'CMDBuild.view.management.common.widgets.manageEmail.EmailWindowEdit';
					}
				}

				this.view = Ext.create(windowClassName, {
					delegate: this
				});
			}

			// No attachment panel setup on confirm regeneration window
			if (enableAttachmentSetup) {
				var attachments = this.record.getAttachmentNames();

				for (var i = 0; i < attachments.length; ++i) {
					var attachmentName = attachments[i];

					this.addAttachmentPanel(attachmentName, this.record);
				}
			}
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onEmailChange':
					return this.onEmailChange();

				case 'onEmailWindowAbortButtonClick':
					return this.onEmailWindowAbortButtonClick();

				case 'onEmailWindowConfirmButtonClick':
					return this.onEmailWindowConfirmButtonClick();

				case 'onFillFromTemplateButtonClick':
					return this.onFillFromTemplateButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {String} fileName
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
		 */
		addAttachmentPanel: function(fileName, emailRecord) {
			this.view.attachmentPanelsContainer.add(
				Ext.create('CMDBuild.view.management.common.widgets.manageEmail.EmailWindowFileAttacchedPanel', {
					fileName: fileName,
					referredEmail: emailRecord,
					delegate: this
				})
			);

			this.view.attachmentPanelsContainer.doLayout();
		},

		/**
		 * @return {Mixed}
		 */
		getView: function() {
			return this.view;
		},

		/**
		 * @return {Boolean}
		 */
		isKeepSynchronizationChecked: function() {
			return this.view.formPanel.keepSynchronizationCheckbox.getValue();
		},

		/**
		 * @return {Boolean}
		 */
		isPromptSynchronizationChecked: function() {
			return this.record.get(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION);
		},

		/**
		 * @param {CMDBuild.model.widget.ManageEmail.template} record
		 */
		loadFormValues: function(record) {
_debug('### loadFormValues');
			var me = this;
			var xaVars = Ext.apply({}, record.getData(), record.get(CMDBuild.core.proxy.CMProxyConstants.VARIABLES));

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: me.widgetController.clientForm,
				xaVars: xaVars,
				serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars()
			});

			this.templateResolver.resolveTemplates({
				attributes: Ext.Object.getKeys(record.getData()),
				callback: function(values) {
_debug('values', values);
					var setValueArray = [];
					var content = values[CMDBuild.core.proxy.CMProxyConstants.BODY];

					if (me.windowMode == 'reply') {
						setValueArray.push({
							id: CMDBuild.core.proxy.CMProxyConstants.BODY,
							value: content + '<br /><br />' + me.record.get(CMDBuild.core.proxy.CMProxyConstants.BODY)
						});
					} else {
						setValueArray.push(
							{
								id: CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
								value: values[CMDBuild.core.proxy.CMProxyConstants.ACCOUNT]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.TO,
								value: values[CMDBuild.core.proxy.CMProxyConstants.TO]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.FROM,
								value: values[CMDBuild.core.proxy.CMProxyConstants.FROM]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.CC,
								value: values[CMDBuild.core.proxy.CMProxyConstants.CC]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.BCC,
								value: values[CMDBuild.core.proxy.CMProxyConstants.BCC]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
								value: values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.BODY,
								value: content
							}
						);
					}

					me.view.formPanel.getForm().setValues(setValueArray);
					me.record = Ext.create('CMDBuild.model.widget.ManageEmail.email', values); // Updates also record property
				}
			});
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
		 */
		onAddAttachmentFromDmsButtonClick: function(emailWindow, emailRecord) {
_debug('onAddAttachmentFromDmsButtonClick', this.record);
			Ext.create('CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker', {
				title: CMDBuild.Translation.choose_attachment_from_db,
				emailRecord: emailRecord,
				emailWindow: emailWindow,
				delegate: this
			}).show();
		},

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
		 * @param {Object} form
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
		 */
		onCMEmailWindowAttachFileChanged: function(emailWindow, form, emailRecord) {
			if (emailRecord.isNew()) {
				var params = {};
				var temporaryId = emailRecord.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID);

				if (!Ext.isEmpty(temporaryId))
					params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID] = temporaryId;

				CMDBuild.core.proxy.widgets.ManageEmail.addAttachmentFromNewEmail(form, {
					params: params,
					success: function(fp, o) {
						emailRecord.set(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID, o.result[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID]);
						emailWindow.addAttachmentPanel(o.result.fileName, emailRecord);
					}
				});
			} else {
				CMDBuild.core.proxy.widgets.ManageEmail.addAttachmentFromExistingEmail(form, {
					params: {
						emailId: emailRecord.getId()
					},
					success: function(fp, o) {
						emailWindow.addAttachmentPanel(o.result.fileName, emailRecord);
					}
				});
			}
		},

		/**
		 * @param {Object} attachmentPanel
		 */
		onCMEmailWindowRemoveAttachmentButtonClick: function(attachmentPanel) {
			var emailRecord = attachmentPanel.referredEmail;
			var params = {
				fileName: attachmentPanel.fileName
			};

			if (emailRecord.isNew()) {
				params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID] = emailRecord.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID);

				CMDBuild.core.proxy.widgets.ManageEmail.removeAttachmentFromNewEmail({
					params: params,
					success: function(response, options ,decodedResponse) {
						attachmentPanel.removeFromEmailWindow();
					}
				});
			} else {
				params.emailId = emailRecord.getId();

				CMDBuild.core.proxy.widgets.ManageEmail.removeAttachmentFromExistingEmail({
					params: params,
					success: function(response, options ,decodedResponse) {
						attachmentPanel.removeFromEmailWindow();
					}
				});
			}
		},

		/**
		 * Change event management to catch email content edit
		 */
		onEmailChange: function() {
_debug('onEmailChange');
			if (!this.isAdvicePrompted && this.isKeepSynchronizationChecked()) {
				this.isAdvicePrompted = true;

				CMDBuild.Msg.warn(null, '@@ E-mail changed and \"Auto synchronization\" active. <br />If \"Auto synchronization\" some change could be missed.');
			}
		},

		/**
		 * Destroy email window object
		 */
		onEmailWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Updates record object adding id (time in milliseconds), Description and attachments array and adds email record to grid store
		 */
		onEmailWindowConfirmButtonClick: function() {
_debug('### onEmailWindowConfirmButtonClick');
			if (this.view.formPanel.getNonValidFields().length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
			} else {
				var formValues = this.view.formPanel.getForm().getValues();
				var attachments = this.view.attachmentPanelsContainer.getFileNames();
_debug('formValues', formValues);
				// Apply formValues to record object
				for (var key in formValues)
					this.record.set(key, formValues[key]);

				this.record.set(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, attachments);
				this.record.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, this.widgetController.getActivityId());
_debug('this.record', this.record);
				if (Ext.isEmpty(this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID))) {
					this.parentDelegate.addRecord(this.record);
				} else {
					this.parentDelegate.editRecord(this.record);
				}

				if (!Ext.Object.isEmpty(this.templateResolver))
					this.widgetController.bindLocalDepsChangeEvent(this.record, this.templateResolver, this.widgetController);

				this.onEmailWindowAbortButtonClick();
			}
		},

		/**
		 * Using FillFromTemplateButton I put only tempalteName in emailObject and get template data to fill email form
		 *
		 * @param {String} templateName
		 */
		onFillFromTemplateButtonClick: function(templateName) {
_debug('onFillFromTemplateButtonClick', this.record);
			this.view.setLoading(true);
			CMDBuild.core.proxy.EmailTemplates.get({
				scope: this,
				params: {
					name: templateName
				},
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, '@@ ManageEmail EmailWindow controller error: get template call failure', false);
				},
				success: function(response, options, decodedResponse) {
					this.loadFormValues(Ext.create('CMDBuild.model.widget.ManageEmail.template', decodedResponse.response));
					this.record.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, templateName); // Bind templateName to email record
				},
				callback: function(options, success, response) {
					this.view.setLoading(false);
				}
			});
		},

		// As CMDMSAttachmentPicker Delegate
			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 * @param {String} classId
			 */
			onCMDMSAttachmentPickerClassDidSelected: function(dmsAttachmentPicker, classId) {
				var className = _CMCache.getEntryTypeNameById(classId);

				dmsAttachmentPicker.cmState.setClassName(className);
				dmsAttachmentPicker.updateCardGridForClassId(classId);
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker,
			 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord,
			 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow,
			 */
			onCMDMSAttachmentPickerOKButtonClick: function(dmsAttachmentPicker, emailRecord, emailWindow) {
				var me = this;
				var data = dmsAttachmentPicker.cmState.getData();

				if (data && data.length == 0)
					return;

				var encodedAttachments = Ext.JSON.encode(data);
				var params = {
					attachments: encodedAttachments
				};

				if (emailRecord.isNew()) {
					var temporaryId = emailRecord.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID);

					if (!Ext.isEmpty(temporaryId))
						params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID] = temporaryId;

					CMDBuild.core.proxy.widgets.ManageEmail.copyAttachmentFromCardForNewEmail({
						params: params,
						success: function(fp, request, response) {
							emailRecord.set(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID, response[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY_ID]);
							me.updateAttachmentList(response.attachments, emailWindow, emailRecord);
							dmsAttachmentPicker.destroy();
						}
					});
				} else {
					params.emailId = emailRecord.getId();
					CMDBuild.core.proxy.widgets.ManageEmail.copyAttachmentFromCardForExistingEmail({
						params: params,
						success: function(fp, request, response) {
							me.updateAttachmentList(response.attachments, emailWindow, emailRecord);
							dmsAttachmentPicker.destroy();
						}
					});
				}
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 */
			onCMDMSAttachmentPickerCancelButtonClick: function(dmsAttachmentPicker) {
				dmsAttachmentPicker.destroy();
			},

			/**
			 * @param {Array} attachmentNames
			 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
			 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
			 */
			updateAttachmentList: function(attachmentNames, emailWindow, emailRecord) {
				if (Ext.isArray(attachmentNames))
					for (var i = 0; i < attachmentNames.length; ++i)
						emailWindow.addAttachmentPanel(attachmentNames[i], emailRecord);
			},

		// As CMCardGrid Delegate
			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 * @param {CMDBuild.view.management.common.CMCardGrid} attachmentGrid
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel} record
			 */
			onCMDMSAttachmentPickerCardDidSelected: function(dmsAttachmentPicker, attachmentGrid, record) {
				var className = record.get('IdClass_value');
				var cardId = record.get(CMDBuild.core.proxy.CMProxyConstants.ID);

				dmsAttachmentPicker.cmState.setCardId(cardId);
				dmsAttachmentPicker.loadAttachmentsForClassNameAndCardId(className, cardId);
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 * @param {String} fileName
			 * @param {Boolean} checked
			 */
			onCMDMSAttachmentPickerAttachmentCheckChange: function(dmsAttachmentPicker, fileName, checked) {
				if (checked) {
					dmsAttachmentPicker.cmState.check(fileName);
				} else {
					dmsAttachmentPicker.cmState.uncheck(fileName);
				}

				console.log(dmsAttachmentPicker.cmState.getData());
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentModel[]} records
			 */
			onCMDMSAttachmentPickerAttachmentsGridDidLoad: function(dmsAttachmentPicker, records) {
				dmsAttachmentPicker.cmState.syncSelection(records);
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker} dmsAttachmentPicker
			 */
			onCMDMSAttachmentPickerCardDidLoad: function(dmsAttachmentPicker) {
				dmsAttachmentPicker.cleanAttachmentGrid();
			}
	});

})();