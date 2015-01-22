(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.CMManageEmailController', {
		extend: 'CMDBuild.controller.management.common.widgets.CMWidgetController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.widgets.ManageEmail'
		],

		mixins: {
			observable: 'Ext.util.Observable',
			attachmentPickerDelegate: 'CMDBuild.view.management.common.widgets.CMDMSAttachmentPickerDelegate'
		},

		statics: {
			WIDGET_NAME: '.ManageEmail'
		},

		/**
		 * @cfg {Array}
		 */
		TEMPLATE_FIELDS: [
			CMDBuild.core.proxy.CMProxyConstants.ACCOUNT,
			CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES,
			CMDBuild.core.proxy.CMProxyConstants.CONDITION,
			CMDBuild.core.proxy.CMProxyConstants.CONTENT,
			CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS,
			CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH,
			CMDBuild.core.proxy.CMProxyConstants.SUBJECT,
			CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID,
			CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES
		],

		/**
		 * @property {CMDBuild.model.CMActivityInstance}
		 */
		card: undefined,

		/**
		 * @property {Ext.form.Basic}
		 */
		clientForm: undefined,

		/**
		 * Shorthand to view grid
		 *
		 * @property {CMDBuild.view.management.common.widgets.CMEmailGrid}
		 */
		emailGrid: undefined,

		/**
		 * @property {Object} variables
		 */
		emailTemplatesData: undefined,

		/**
		 * @property {Boolean}
		 */
		emailsWereGenerated: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.email.CMEmailWindow}
		 */
		emailWindow: undefined,

		/**
		 * Used from button to force regeneration
		 *
		 * @cfg {Boolean}
		 */
		forceRegeneration: false,

		/**
		 * @cfg {Boolean}
		 */
		gridStoreWasLoaded: false,

		/**
		 * Flag used to check first widget load time
		 *
		 * @cfg {Boolean}
		 */
		isAlreadyRegenerated: false,

		/**
		 * @cfg {CMDBuild.controller.management.common.CMWidgetManagerController}
		 */
		ownerController: undefined,

		/**
		 * @cfg {Boolean}
		 */
		readOnly: undefined,

		/**
		 * @property {CMDBuild.Management.TemplateResolver}
		 */
		templateResolver: undefined,

		/**
		 * @property {Array}
		 */
		templatesToRegenerate: [],

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.CMManageEmail} view
		 * @param {CMDBuild.controller.management.common.CMWidgetManagerController} ownerController
		 * @param {Object} widgetConf
		 * @param {Ext.form.Basic} clientForm
		 * @param {CMDBuild.model.CMActivityInstance} card
		 *
		 * @override
		 */
		constructor: function(view, ownerController, widgetConf, clientForm, card) {
			this.mixins.observable.constructor.call(this);

			this.callParent(arguments);

			// Generate templates id
			var emailtemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES];
			for (var index in emailtemplates) {
				var template = emailtemplates[index];

				template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID] = new Date().valueOf() + index;
			}

			this.emailTemplatesData = this.extractVariablesForTemplateResolver();
			this.readOnly = !this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.READ_ONLY];

			var xaVars = Ext.apply({}, this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.TEMPLATES] || {}, this.emailTemplatesData);

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: clientForm,
				xaVars: xaVars,
				serverVars: this.getTemplateResolverServerVars()
			});

			this.view.delegate = this;
			this.view.emailGrid.delegate = this;

			// ShortHands
			this.emailGrid = this.view.emailGrid;
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
				case 'onEmailDelete':
					return this.onEmailDelete(param);

				case 'onEmailEdit':
					return this.onEmailEdit(param);

				case 'onEmailReply':
					return this.onEmailReply(param);

				case 'onEmailView':
					return this.onEmailView(param);

				case 'onEmailWindowAbortButtonClick':
					return this.onEmailWindowAbortButtonClick();

				case 'onEmailWindowConfirmButtonClick':
					return this.onEmailWindowConfirmButtonClick();

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/*
		 * Resolve the template only if there are no draft mails, because the draft mails are saved from this step, and assume that
		 * the user has already modified the template for this step.
		 */
		addEmailFromTemplateIfNeeded: function() {
			this.checkTemplatesToRegenerate();

			if (
				(
					this.thereAreTemplates()
					&& !this.emailGrid.hasDraftEmails()
					&& !this.emailsWereGenerated
				)
				|| this.forceRegeneration
			) {
				this.createEmailFromTemplate();
			}
		},

		/**
		 * If the grid is already loaded add the emails generated from the templates (if there are templates, and if the email are not already generated).
		 * Otherwise, load the grid before.
		 *
		 * @override
		 */
		beforeActiveView: function() {
			var pi = _CMWFState.getProcessInstance();

			this.templatesToRegenerate = []; // Reset buffer variable

			if (!this.gridStoreWasLoaded) {
				this.view.setLoading(true);
				this.emailGrid.getStore().load({
					params: {
						ProcessId: pi.getId()
					},
					scope: this,
					callback: function(records, operation, success) {
						this.gridStoreWasLoaded = true;
						this.view.setLoading(false);
						this.addEmailFromTemplateIfNeeded();
					}
				});
			} else {
				this.addEmailFromTemplateIfNeeded();
			}
		},

		/**
		 * Builds templatesToRegenerate array with indexes of templates
		 */
		checkTemplatesToRegenerate: function() {
			var dirtyVariables = Ext.Object.getKeys(this.ownerController.view.mainView.getValues(false, true));

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in this.templateResolver.xaVars) {
				var variable = this.templateResolver.xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& typeof variable == 'string'
				) {
					for (var j in dirtyVariables)
						if (variable.indexOf('{client:' + dirtyVariables[j]) > -1)
							dirtyVariables.push(i);
				}
			}

			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			for (var i in this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES]) {
				var template = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES][i];

				if (!Ext.Object.isEmpty(template))
					for (var j in template) {
						var templateAttribute = template[j] || [];

						if (
							!Ext.isEmpty(templateAttribute)
							&& !Ext.isObject(templateAttribute)
							&& typeof templateAttribute == 'string'
						) {
							for (var y in dirtyVariables)
								if (templateAttribute.indexOf('{client:' + dirtyVariables[y]) > -1)
									this.templatesToRegenerate.push(template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID]);
						}
					}
			}
		},

		/**
		 * @return {Int}
		 */
		countTemplates: function() {
			var emailTemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES] || [];

			return emailTemplates.length;
		},

		createEmailFromTemplate: function() {
			if (!this.busy) {
				var me = this;
				var oldStore = CMDBuild.core.Utils.deepCloneStore(this.emailGrid.getStore()); // Old store backup

				this.busy = true;
				this.emailGrid.removeTemplatesFromStore();
				this.emailsWereGenerated = true;

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(me.emailTemplatesData),
					callback: function(values, ctx) {
						for (var i = 1; i <= me.countTemplates(); ++i) {

							var oldEmailVersionIndex = oldStore.findBy(function(item) {
								return item.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID) == values[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID + i];
							});

							// If regeneration is forced by field edit or if it's first load
							if (
								Ext.Array.contains(me.templatesToRegenerate, values[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID + i])
								|| !me.isAlreadyRegenerated // Regenerate on first time widget is loaded
								|| me.forceRegeneration
							) {
								var v = {};
								var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION + i];

								if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
									for (var j = 0; j < me.TEMPLATE_FIELDS.length; ++j) {
										var field = me.TEMPLATE_FIELDS[j];

										v[field] = values[field + i];
									}

									_msg('Email with subject "' + v[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

									me.emailGrid.addTemplateToStore(v);
								}
							} else if (oldEmailVersionIndex >= 0) { // Copy from old store
								var record = oldStore.getAt(oldEmailVersionIndex);
								record.dirty = true; // Force record in dirty state

								me.emailGrid.addToStoreIfNotInIt(record);
							}
						}

						me.templateResolver.bindLocalDepsChange(function() {
							if (me.emailsWereGenerated) {
								me.emailsWereGenerated = false;

								CMDBuild.Msg.warn(null, CMDBuild.Translation.management.modworkflow.extattrs.manageemail.mailsAreChanged);
							}
						});

						me.busy = false;
						me.isAlreadyRegenerated = true;
						me.forceRegeneration = false;

						me.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
					}
				});
			}
		},

		/**
		 * Extract the variables of each EmailTemplate, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @return {Object} variables
		 */
		extractVariablesForTemplateResolver: function() {
			var emailTemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES] || [];
			var variables = {};

			for (var i = 0; i < emailTemplates.length; ++i) {
				var t = emailTemplates[i].variables;

				for (var key in t)
					variables[key] = t[key];

				t = emailTemplates[i];

				for (var key in t)
					variables[key + (i + 1)] = t[key];
			}

			return variables;
		},

		/**
		 * Generates temporary id from time in milliseconds
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.grid} targetObject
		 */
		generateTemporaryId: function(targetObject) {
			if (
				Ext.isEmpty(targetObject.get(CMDBuild.core.proxy.CMProxyConstants.ID))
				|| targetObject.get(CMDBuild.core.proxy.CMProxyConstants.ID) == 0
			) {
				targetObject.set(CMDBuild.core.proxy.CMProxyConstants.ID, new Date().valueOf());
				targetObject.set(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY, true);
			}
		},

		/**
		 * @return {Object}
		 *
		 * @override
		 */
		getData: function(isAdvance) {
			return {
				Updated: this.getOutgoingEmails(true),
				Deleted: this.emailGrid.deletedEmails
			};
		},

		/**
		 * @return {Boolean} busy
		 *
		 * @override
		 */
		isBusy: function() {
			this.addEmailFromTemplateIfNeeded();

			return this.busy;
		},

		/**
		 * @return {Boolean}
		 *
		 * @override
		 */
		isValid: function() {
			return !(
				this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.REQUIRED]
				&& this.getOutgoingEmails().length == 0
			);
		},

		/**
		 * Removes New and Draft emails from grid store
		 */
		removeUnsentEmails: function() {
			var emailToRemove = [].concat(this.emailGrid.getNewEmails()).concat(this.emailGrid.getDraftEmails());

			for (var i = 0; i < emailToRemove.length; ++i)
				this.removeRecord(emailToRemove[i]);
		},

		/**
		 * @return {Boolean}
		 */
		thereAreTemplates: function() {
			return this.countTemplates() > 0;
		},

		/**
		 * @param {Array} attachmentNames
		 * @param {CMDBuild.view.management.common.widgets.email.CMEmailWindow} emailWindow
		 * @param {CMDBuild.model.widget.ManageEmail.grid} emailRecord
		 */
		updateAttachmentList: function(attachmentNames, emailWindow, emailRecord) {
			if (Ext.isArray(attachmentNames))
				for (var i = 0; i < attachmentNames.length; ++i)
					emailWindow.addAttachmentPanel(attachmentNames[i], emailRecord);
		},

		// As EmailGrid Delegate
			/**
			 * @param {Boolean} modifiedOnly
			 *
			 * @return {Array} outgoingEmails
			 */
			getOutgoingEmails: function(modifiedOnly) {
				var allOutgoing = modifiedOnly ? false : true;
				var outgoingEmails = [];
				var emails = this.emailGrid.getStore().getRange();

				for (var i = 0; i < emails.length; ++i) {
					var currentEmail = emails[i];

					// Avoid to send temporary Ids to server
					if (currentEmail.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY))
						delete currentEmail.data[CMDBuild.core.proxy.CMProxyConstants.ID];

					if (allOutgoing || currentEmail.dirty)
						outgoingEmails.push(currentEmail.data);
				}

				return outgoingEmails;
			},

			/**
			 * @param {CMDBuild.view.management.common.widgets.CMEmailGrid} emailGrid
			 * @param {CMDBuild.model.widget.ManageEmail.grid} emailRecord
			 */
			onAddEmailButtonClick: function(emailGrid, emailRecord) {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.CMEmailWindow', {
					emailGrid: emailGrid,
					delegate: this,
					record: emailRecord
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			onEmailDelete: function(record) {
				Ext.Msg.confirm(
					CMDBuild.Translation.common.confirmpopup.title,
					CMDBuild.Translation.common.confirmpopup.areyousure,
					function(btn) {
						if (btn == 'yes')
							this.removeRecord(record);
					},
					this
				);
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			onEmailEdit: function(record) {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.CMEmailWindow', {
					delegate: this,
					emailGrid: this.emailGrid,
					readOnly: !this.readOnly,
					record: record,
					title: CMDBuild.Translation.editEmail,
					windowMode: 'edit'
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			onEmailReply: function(record) {
				var content = '<p>'
						+ CMDBuild.Translation.onDay + ' ' + record.get(CMDBuild.core.proxy.CMProxyConstants.DATE)
						+ ', <' + record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS) + '> ' + CMDBuild.Translation.hasWrote
					+ ':</p>'
					+ '<blockquote>' + record.get(CMDBuild.core.proxy.CMProxyConstants.CONTENT) + '</blockquote>';

				var replyRecordData = {};
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS] = record.get(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS);
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES] = record.get(CMDBuild.core.proxy.CMProxyConstants.CC_ADDRESSES);
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.CONTENT] = content;
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.DATE] = null;
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS] = null;
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NOTIFY_WITH] = null;
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.NO_SUBJECT_PREFIX] = true;
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.STATUS] = this.emailGrid.emailTypes[CMDBuild.core.proxy.CMProxyConstants.NEW];
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] = 'RE: ' + record.get(CMDBuild.core.proxy.CMProxyConstants.SUBJECT);
				replyRecordData[CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES] = record.get(CMDBuild.core.proxy.CMProxyConstants.FROM_ADDRESS) || record.get(CMDBuild.core.proxy.CMProxyConstants.TO_ADDRESSES);

				var replyRecord = Ext.create('CMDBuild.model.widget.ManageEmail.grid', replyRecordData);

				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.CMEmailWindow', {
					delegate: this,
					emailGrid: this.emailGrid,
					record: replyRecord,
					title: CMDBuild.Translation.replyEmail,
					windowMode: 'reply'
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			onEmailView: function(record) {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.CMEmailWindow', {
					delegate: this,
					emailGrid: this.emailGrid,
					readOnly: true,
					record: record,
					title: CMDBuild.Translation.viewEmail,
					windowMode: 'edit'
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			onItemDoubleClick: function(record) {
				if (this.emailGrid.recordIsEditable(record)) {
					this.onEmailEdit(record);
				} else {
					this.onEmailView(record);
				}
			},

			onUpdateTemplatesButtonClick: function() {
				this.removeUnsentEmails();
				this.emailsWereGenerated = false;
				this.forceRegeneration = true;
				this.addEmailFromTemplateIfNeeded();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.grid} record
			 */
			removeRecord: function(record) {
				// The email has an id only if it was returned by the server. So add it to the deletedEmails only if the server know it
				var id = record.getId();

				if (id)
					this.emailGrid.deletedEmails.push(id);

				this.emailGrid.getStore().remove(record);
			},

		// As CMEmailWindow Delegate
			/**
			 * @param {CMDBuild.view.management.common.widgets.email.CMEmailWindow} emailWindow
			 * @param {Object} form
			 * @param {CMDBuild.model.widget.ManageEmail.grid} emailRecord
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
			 * @param {CMDBuild.view.management.common.widgets.email.CMEmailWindow} emailWindow
			 * @param {CMDBuild.model.widget.ManageEmail.grid} emailRecord
			 */
			onAddAttachmentFromDmsButtonClick: function(emailWindow, emailRecord) {
				Ext.create('CMDBuild.view.management.common.widgets.CMDMSAttachmentPicker', {
					title: CMDBuild.Translation.choose_attachment_from_db,
					emailRecord: emailRecord,
					emailWindow: emailWindow,
					delegate: this
				}).show();
			},

			onEmailWindowAbortButtonClick: function() {
				this.emailWindow.destroy();
			},

			/**
			 * Updates record object adding id (time in milliseconds), Description and attachments array and adds email record to grid store
			 */
			onEmailWindowConfirmButtonClick: function() {
				if (this.emailWindow.formPanel.getNonValidFields().length > 0) {
					CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				} else {
					var formValues = this.emailWindow.formPanel.getForm().getValues();
					var record = this.emailWindow.record;
					var attachments = this.emailWindow.attachmentPanelsContainer.getFileNames();

					for (var key in formValues)
						record.set(key, formValues[key]);

					this.generateTemporaryId(record);

					record.set(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, attachments);

					this.emailGrid.addToStoreIfNotInIt(record);
					this.onEmailWindowAbortButtonClick();
				}
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
			 * @param {CMDBuild.model.widget.ManageEmail.grid} emailRecord,
			 * @param {CMDBuild.view.management.common.widgets.email.CMEmailWindow} emailWindow,
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