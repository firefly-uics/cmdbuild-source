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
			WIDGET_NAME: CMDBuild.view.management.common.widgets.email.CMManageEmail.WIDGET_NAME,

			/**
			 * Searches for CQL variables resolved by client
			 *
			 * @param {String} inspectingVariable - variable where to check presence of CQL variables
			 * @param {Mixed} inspectingVariableKey - identifier of inspecting variable
			 * @param {Array} searchedVariablesNames - searched variables names
			 * @param {Array} foundedKeysArray - where to push keys of variables witch contains CQL
			 *
			 * @return {Boolean} found
			 */
			searchForCqlClientVariables: function(inspectingVariable, inspectingVariableKey, searchedVariablesNames, foundedKeysArray) {
				var found = false;
				var cqlTags = ['{client:', '{cql:', '{xa:', '{js:'];

				for (var y in searchedVariablesNames) {
					for (var i in cqlTags) {
						if (
							inspectingVariable.indexOf(cqlTags[i] + searchedVariablesNames[y]) > -1
							&& !Ext.Array.contains(foundedKeysArray, inspectingVariableKey)
						) {
							foundedKeysArray.push(inspectingVariableKey);
						}
					}
				}

				return found;
			}
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
		 * @property {CMDBuild.view.management.common.widgets.email.Grid}
		 */
		emailGrid: undefined,

		/**
		 * WidgetConf convenience shorthand
		 *
		 * @property {Object} variables
		 */
		emailTemplates: undefined,

		/**
		 * @property {Object} variables
		 */
		emailTemplatesData: undefined,

		/**
		 * @cfg {Boolean}
		 */
		relatedAttributeChanged: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.email.EmailWindow}
		 */
		emailWindow: undefined,

		/**
		 * Flag used to check first widget load time
		 *
		 * @cfg {Boolean}
		 */
		isFirstRegenerationDone: false,

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
		 * @property {Boolean}
		 */
		templateResolverIsBusy: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.linkCards.LinkCards}
		 */
		view: undefined,

		/**
		 * @cfg {Object}
		 */
		widgetConf: undefined,

		/**
		 * @param {CMDBuild.view.management.common.widgets.email.CMManageEmail} view
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
_debug('this.widgetConf', this.widgetConf);
			// Generate templates id
			this.emailTemplates = this.widgetConf[CMDBuild.core.proxy.CMProxyConstants.EMAIL_TEMPLATES] || [];
			for (var index in this.emailTemplates) {
				var template = this.emailTemplates[index];

				if (Ext.isEmpty(template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID]))
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
				// EmailGrid delegate
				case 'onEmailAddButtonClick':
					return this.onEmailAddButtonClick(param);

				// EmailGrid delegate
				case 'onEmailDelete':
					return this.onEmailDelete(param);

				// EmailGrid delegate
				case 'onEmailEdit':
					return this.onEmailEdit(param);

				// EmailGrid delegate
				case 'onEmailRegeneration':
					return this.onEmailRegeneration(param);

				// EmailGrid delegate
				case 'onEmailReply':
					return this.onEmailReply(param);

				// EmailGrid delegate
				case 'onEmailView':
					return this.onEmailView(param);

				// EmailWindow delegate
				case 'onEmailWindowAbortButtonClick':
					return this.onEmailWindowAbortButtonClick();

				// EmailWindow delegate
				case 'onEmailWindowConfirmButtonClick':
					return this.onEmailWindowConfirmButtonClick();

				// EmailGrid delegate
				case 'onGlobalRegenerationButtonClick':
					return this.onGlobalRegenerationButtonClick();

				// EmailGrid delegate
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
		 * If grid store is already loaded regenerate all email if needed, otherwise load grid and do it
		 *
		 * @override
		 */
		beforeActiveView: function() {
			if (this.emailGrid.isStoreLoaded()) {
				this.checkToRegenerateAllEmails();
			} else {
				this.onEditMode();
			}
		},

		/**
		 * Builds templatesToRegenerate array in relation of dirty fields
		 *
		 * @return {Array} templatesToRegenerate
		 */
		checkTemplatesToRegenerate: function() {
			var templatesToRegenerate = [];
			var dirtyVariables = Ext.Object.getKeys(this.ownerController.view.mainView.getValues(false, true));

			// Complete dirtyVariables array also with multilevel variables (ex. var1 = '... {client:var2} ...')
			for (var i in this.templateResolver.xaVars) {
				var variable = this.templateResolver.xaVars[i] || [];

				if (
					!Ext.isEmpty(variable)
					&& !Ext.isObject(variable)
					&& typeof variable == 'string'
				) {
					this.self.searchForCqlClientVariables(
						variable,
						i,
						dirtyVariables,
						dirtyVariables
					);
				}
			}

			// Check templates attributes looking for dirtyVariables as client variables (ex. {client:varName})
			for (var i in this.emailTemplates) {
				var template = this.emailTemplates[i];

				if (!Ext.Object.isEmpty(template))
					for (var j in template) {
						var templateAttribute = template[j] || [];

						if (
							!Ext.isObject(templateAttribute)
							&& typeof templateAttribute == 'string'
						) {
							// Check all types of CQL variables that can contains client variables
							this.self.searchForCqlClientVariables(
								templateAttribute,
								template[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_ID],
								dirtyVariables,
								templatesToRegenerate
							);
						}
					}
			}

			return templatesToRegenerate;
		},

		/**
		 * @WIP TODO
		 *
		 * Regenerates email resolving all internal CQL templates
		 *
		 * @param {Boolean} forceRegeneration
		 */
		checkToRegenerateAllEmails: function(forceRegeneration) {
			forceRegeneration = forceRegeneration || false;

			if (
				forceRegeneration
				|| !this.isFirstRegenerationDone // Regenerate first time widget is loaded
				|| this.relatedAttributeChanged
			) {
				var regeneratedEmails = [];
				var templatesToRegenerate = this.checkTemplatesToRegenerate();

				// Array with all store New and Draft records and all emailTemplates from widget configuration
				var objectsToRegenerate = [];
				var emailTemplatesRegenerated = [];
				var newEmails = this.emailGrid.getNewEmails();
				var draftEmails = this.emailGrid.getDraftEmails();
_debug('newEmails', newEmails);
_debug('draftEmails', draftEmails);
				for (var i in newEmails) {
					var emailTemplateId = newEmails[i].get('template')['id'];

					if (!Ext.isEmpty(emailTemplateId))
						emailTemplatesRegenerated.push(emailTemplateId);

					objectsToRegenerate.push(newEmails[i]);
				}

				for (var i in draftEmails) {
					var emailTemplateId = draftEmails[i].get('template')['id'];

					if (!Ext.isEmpty(emailTemplateId))
						emailTemplatesRegenerated.push(emailTemplateId);

					objectsToRegenerate.push(draftEmails[i]);
				}

				for (var i in this.emailTemplates) {
					var emailTemplateId = this.emailTemplates[i]['id'];

					if (!Ext.isEmpty(emailTemplateId) && !Ext.Array.contains(emailTemplatesRegenerated, emailTemplateId))
						objectsToRegenerate.push(this.emailTemplates[i]);
				}

				Ext.Array.each(objectsToRegenerate, function(item, index, allItems) {
					if (item instanceof CMDBuild.model.widget.ManageEmail.email) {
						if (Ext.Array.contains(templatesToRegenerate, item.get('template')['id']) || forceRegeneration)
							regeneratedEmails.push(this.regenerateEmail(item, item.get('@@ emailObjectTemplate'))); // Regenerate a grid record
					} else {
						if (Ext.Array.contains(templatesToRegenerate, item['id']) || forceRegeneration)
							regeneratedEmails.push(this.regenerateEmail(null, item)); // Regenerate a widget configuration template
					}
				}, this);

				this.relatedAttributeChanged = false;
				this.isFirstRegenerationDone = true;

				this.ownerController.view.mainView.form.initValues(); // Clear form fields dirty state to reset state after regeneration
_debug('checkToRegenerateAllEmails regeneratedEmails', regeneratedEmails);
				// Add all templates to store
				for (var i in regeneratedEmails)
					this.emailGrid.addTemplateToStore(regeneratedEmails[i]);
			}
		},

		/**
		 * Extract the variables of each EmailTemplate, add a suffix to them with the index, and put them all in the templates map.
		 * This is needed to be passed as a unique map to the template resolver.
		 *
		 * @return {Object} variables
		 */
		extractVariablesForTemplateResolver: function() {
			var variables = {};

			for (var i = 0; i < this.emailTemplates.length; ++i) {
				var t = this.emailTemplates[i].variables;

				for (var key in t)
					variables[key] = t[key];

				t = this.emailTemplates[i];

				for (var key in t)
					variables[key + (i + 1)] = t[key];
			}

			return variables;
		},

		/**
		 * Generates temporary id from time in milliseconds
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} targetObject
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
		 * @return {Boolean} templateResolverIsBusy
		 *
		 * @override
		 */
		isBusy: function() {
			this.checkToRegenerateAllEmails();

			return this.templateResolverIsBusy;
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
		 * Initialize widget on widget configuration to apply all events on form fields
		 *
		 * @override
		 */
		onEditMode: function() {
			if (!this.emailGrid.isStoreLoaded() && !this.emailGrid.getStore().isLoading()) {
				var pi = _CMWFState.getProcessInstance();

				this.view.setLoading(true);
				this.emailGrid.getStore().load({
					params: {
						ProcessId: pi.getId()
					},
					scope: this,
					callback: function(records, operation, success) {
						this.view.setLoading(false);
						this.checkToRegenerateAllEmails();
					}
				});
			}
		},

		/**
		 * @WIP TODO: toReport implementation (asking pop-up)
		 *
		 * @param {CMDBuild.model.widget.ManageEmail.email} emailObject
		 * @param {CMDBuild.model.CMModelEmailTemplates.singleTemplate} sourceTemplate
		 * @param {Boolean} toReport
		 *
		 * @return {Array} regeneratedEmailObject
		 */
		regenerateEmail: function(emailObject, sourceTemplate, toReport) {
			var regeneratedEmailObject = {};
_debug(!this.templateResolverIsBusy + ' ' + Ext.Object.isEmpty(sourceTemplate) + ' ' + emailObject.get('@@ autoSync'));
			if (
				!this.templateResolverIsBusy
				&& Ext.Object.isEmpty(sourceTemplate)
				&& emailObject.get('@@ autoSync')
			) {
				var me = this;

				this.templateResolverIsBusy = true;
_debug('this.emailGrid.getStore()', this.emailGrid.getStore());
_debug('emailObject', emailObject);
				this.emailGrid.getStore().remove(emailObject); // Delete old email from store

				this.templateResolver.resolveTemplates({
					attributes: Ext.Object.getKeys(sourceTemplate),
					callback: function(values, ctx) {
						var conditionExpr = values[CMDBuild.core.proxy.CMProxyConstants.CONDITION];

						if (!conditionExpr || me.templateResolver.safeJSEval(conditionExpr)) {
							_msg('Email with subject "' + values[CMDBuild.core.proxy.CMProxyConstants.SUBJECT] + '" regenerated');

							regeneratedEmailObject.push(values);
						}

						me.templateResolver.bindLocalDepsChange(function() {
							if (!me.relatedAttributeChanged) {
								me.relatedAttributeChanged = true;

								CMDBuild.Msg.warn(null, "@@ Attribute related with email templates changed, some mail could be regenerated.");
							}
						});

						me.templateResolverIsBusy = false;
					}
				});
			}
_debug('regeneratedEmailObject', regeneratedEmailObject);
			return regeneratedEmailObject;
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

			onEmailAddButtonClick: function() {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowEdit', {
					emailGrid: this.emailGrid,
					delegate: this,
					record: this.emailGrid.createRecord()
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
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
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 */
			onEmailEdit: function(record) {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowEdit', {
					delegate: this,
					emailGrid: this.emailGrid,
					record: record,
					title: CMDBuild.Translation.editEmail,
					windowMode: 'edit'
				}).show();
			},

			/**
			 * @WIP TODO
			 *
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 */
			onEmailRegeneration: function(record) {
				this.emailWindowConfirmRegeneration = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowConfirmRegeneration', {
					delegate: this,
					records: this.emailGrid.getStore().getRange()
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
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

				var replyRecord = Ext.create('CMDBuild.model.widget.ManageEmail.email', replyRecordData);

				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowEdit', {
					delegate: this,
					emailGrid: this.emailGrid,
					record: replyRecord,
					title: CMDBuild.Translation.replyEmail,
					windowMode: 'reply'
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 */
			onEmailView: function(record) {
				this.emailWindow = Ext.create('CMDBuild.view.management.common.widgets.email.EmailWindowView', {
					delegate: this,
					record: record
				}).show();
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 */
			onItemDoubleClick: function(record) {
				if (this.emailGrid.recordIsEditable(record)) {
					this.onEmailEdit(record);
				} else {
					this.onEmailView(record);
				}
			},

			onGlobalRegenerationButtonClick: function() {
				this.checkToRegenerateAllEmails(true);
			},

			/**
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 *
			 * @return {Boolean}
			 */
			recordIsReceived: function(record) {
				return (record.get(CMDBuild.core.proxy.CMProxyConstants.STATUS) == this.emailGrid.emailTypes[CMDBuild.core.proxy.CMProxyConstants.RECEIVED]);
			},

			/**
			 * Check if temporaryId is false, email is known from server so put id in deletedEmails array
			 *
			 * @param {CMDBuild.model.widget.ManageEmail.email} record
			 */
			removeRecord: function(record) {
				var id = record.getId();

				if (id > 0 && !record.get(CMDBuild.core.proxy.CMProxyConstants.IS_ID_TEMPORARY))
					this.emailGrid.deletedEmails.push(id);

				this.emailGrid.getStore().remove(record);
			},

		// As CMEmailWindow Delegate
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
			 * @param {CMDBuild.view.management.common.widgets.email.EmailWindow} emailWindow
			 * @param {CMDBuild.model.widget.ManageEmail.email} emailRecord
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