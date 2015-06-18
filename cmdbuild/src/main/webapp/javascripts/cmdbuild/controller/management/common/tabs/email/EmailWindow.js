(function () {

	Ext.define('CMDBuild.controller.management.common.tabs.email.EmailWindow', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.common.tabs.email.Attachment',
			'CMDBuild.core.proxy.email.Templates',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.tabs.email.Grid}
		 */
		parentDelegate: undefined,

		/**
		 * @property {CMDBuild.controller.management.common.tabs.email.attachments.Attachments}
		 */
		attachmentsDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onEmailWindowAbortButtonClick = onEmailWindowCloseButtonClick',
			'onEmailWindowConfirmButtonClick',
			'onEmailWindowFieldChange',
			'onEmailWindowFillFromTemplateButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.tabs.email.emailWindow.EditForm}
		 */
		form: undefined,

		/**
		 * Used as flag to avoid pop-up spam
		 *
		 * @cfg {Boolean}
		 */
		isAdvicePrompted: false,

		/**
		 * @property {CMDBuild.model.common.tabs.email.Email}
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
		 * @cfg {String}
		 */
		windowMode: 'create',

		/**
		 * @cfg {Array}
		 */
		windowModeAvailable: ['create', 'edit', 'reply', 'view'],

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.tabs.email.Email} configurationObject.parentDelegate
		 * @param {Mixed} configurationObject.record
		 * @param {String} configurationObject.windowMode
		 */
		constructor: function(configurationObject) {
			var me = this;
			var windowClassName = null;

			this.callParent(arguments);

			if (Ext.Array.contains(this.windowModeAvailable, this.windowMode)) {
				switch (this.windowMode) {
					case 'view': {
						windowClassName = 'CMDBuild.view.management.common.tabs.email.emailWindow.ViewWindow';
					} break;

					default: { // Default window class to build
						windowClassName = 'CMDBuild.view.management.common.tabs.email.emailWindow.EditWindow';
					}
				}

				this.view = Ext.create(windowClassName, {
					delegate: this,

					listeners: {
						scope: this,
						close: function(window, eOpts) {
							this.cmfg('onEmailWindowCloseButtonClick');
						}
					}
				});

				// Shorthands
				this.form = this.view.form;

				// Fill from template button store configuration
				CMDBuild.core.proxy.email.Templates.getAll({
					scope: this,
					loadMask: true,
					success: function(response, options, decodedResponse) {
						var templatesArray = decodedResponse.response.elements;

						if (templatesArray.length > 0) {
							// Sort templatesArray by description ascending
							CMDBuild.core.Utils.objectArraySort(templatesArray, CMDBuild.core.proxy.Constants.DESCRIPTION);

							Ext.Array.forEach(templatesArray, function(template, index, allItems) {
								this.view.fillFromTemplateButton.menu.add({
									text: template[CMDBuild.core.proxy.Constants.DESCRIPTION],
									templateName: template[CMDBuild.core.proxy.Constants.NAME],

									handler: function(button, e) {
										me.cmfg('onEmailWindowFillFromTemplateButtonClick', button[CMDBuild.core.proxy.Constants.TEMPLATE_NAME]);
									}
								});
							}, this);
						} else { // To disable button if the aren't templates
							this.view.fillFromTemplateButton.setDisabled(true);
						}
					}
				});

				if (CMDBuild.Config.dms.enabled) {
					// Build attachments controller
					this.attachmentsDelegate = Ext.create('CMDBuild.controller.management.common.tabs.email.attachments.Attachments', {
						parentDelegate: this,
						record: this.record,
						view: this.view.attachmentContainer
					});

					// Get all email attachments
					var params = {};
					params[CMDBuild.core.proxy.Constants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.Constants.ID);
					params[CMDBuild.core.proxy.Constants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.Constants.TEMPORARY);

					this.view.setLoading(true);
					CMDBuild.core.proxy.common.tabs.email.Attachment.getAll({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							Ext.Array.forEach(decodedResponse.response, function(item, index, allItems) {
								if(!Ext.Object.isEmpty(item))
									this.attachmentsDelegate.attachmentAddPanel(item[CMDBuild.core.proxy.Constants.FILE_NAME]);
							}, this);
						},
						callback: function(records, operation, success) {
							this.view.setLoading(false);
						}
					});
				}

				this.form.loadRecord(this.record); // Fill view form with record data

				// If email has template enable keep-synch checkbox
				if (!Ext.isEmpty(this.record.get(CMDBuild.core.proxy.Constants.TEMPLATE)) && this.windowMode != 'view')
					this.form.keepSynchronizationCheckbox.setDisabled(false);

				// Show window
				if (!Ext.isEmpty(this.view))
					this.view.show();
			}
		},

		/**
		 * @return {Boolean}
		 */
		isKeepSynchronizationChecked: function() {
			return this.form.keepSynchronizationCheckbox.getValue();
		},

		/**
		 * @return {Boolean}
		 */
		isPromptSynchronizationChecked: function() {
			return this.record.get(CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION);
		},

		/**
		 * @param {CMDBuild.model.common.tabs.email.Template} record
		 */
		loadFormValues: function(record) {
			var me = this;
			var xaVars = Ext.apply({}, record.getData(), record.get(CMDBuild.core.proxy.Constants.VARIABLES));

			this.templateResolver = new CMDBuild.Management.TemplateResolver({
				clientForm: this.cmfg('getMainController').parentDelegate.getFormForTemplateResolver(),
				xaVars: xaVars,
				serverVars: CMDBuild.controller.management.common.widgets.CMWidgetController.getTemplateResolverServerVars(
					this.cmfg('getMainController').selectedEntity.get(CMDBuild.core.proxy.Constants.ENTITY)
				)
			});

			this.templateResolver.resolveTemplates({
				attributes: Ext.Object.getKeys(xaVars),
				callback: function(values, ctx) {
					var setValueArray = [];
					var content = values[CMDBuild.core.proxy.Constants.BODY];

					if (me.windowMode == 'reply') {
						setValueArray.push({
							id: CMDBuild.core.proxy.Constants.BODY,
							value: content + '<br /><br />' + me.record.get(CMDBuild.core.proxy.Constants.BODY)
						});
					} else {
						setValueArray.push(
							{
								id: CMDBuild.core.proxy.Constants.FROM,
								value: values[CMDBuild.core.proxy.Constants.FROM]
							},
							{
								id: CMDBuild.core.proxy.Constants.TO,
								value: values[CMDBuild.core.proxy.Constants.TO]
							},
							{
								id: CMDBuild.core.proxy.Constants.CC,
								value: values[CMDBuild.core.proxy.Constants.CC]
							},
							{
								id: CMDBuild.core.proxy.Constants.BCC,
								value: values[CMDBuild.core.proxy.Constants.BCC]
							},
							{
								id: CMDBuild.core.proxy.Constants.SUBJECT,
								value: values[CMDBuild.core.proxy.Constants.SUBJECT]
							},
							{
								id: CMDBuild.core.proxy.Constants.BODY,
								value: content
							},
							{ // It's last one to avoid Notification pop-up display on setValues action
								id: CMDBuild.core.proxy.Constants.KEEP_SYNCHRONIZATION,
								value: values[CMDBuild.core.proxy.Constants.KEEP_SYNCHRONIZATION]
							}
						);
					}

					me.form.getForm().setValues(setValueArray);

					me.form.delayField.setValue(values[CMDBuild.core.proxy.Constants.DELAY]);

					// Updates record's prompt synchronizations flag
					me.record.set(CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION, values[CMDBuild.core.proxy.Constants.PROMPT_SYNCHRONIZATION]);
				}
			});
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
			// Validate before save
			if (this.validate(this.form)) {
				var formValues = this.form.getForm().getValues();

				// Apply formValues to record object
				for (var key in formValues)
					this.record.set(key, formValues[key]);

				// Setup attachments only if DMS is enabled
				if (CMDBuild.Config.dms.enabled)
					this.record.set(CMDBuild.core.proxy.Constants.ATTACHMENTS, this.attachmentsDelegate.getAttachmentsNames());

				this.record.set(CMDBuild.core.proxy.Constants.REFERENCE, this.cmfg('selectedEntityIdGet'));

				if (Ext.isEmpty(this.record.get(CMDBuild.core.proxy.Constants.ID))) {
					this.parentDelegate.addRecord(this.record);
				} else {
					this.parentDelegate.editRecord(this.record);
				}

				if (!Ext.Object.isEmpty(this.templateResolver))
					this.cmfg('getMainController').bindLocalDepsChangeEvent(this.record, this.templateResolver, this.cmfg('getMainController'));

				this.onEmailWindowAbortButtonClick();
			}
		},

		/**
		 * Change event management to catch email content edit
		 */
		onEmailWindowFieldChange: function() {
			if (!this.isAdvicePrompted && this.isKeepSynchronizationChecked()) {
				this.isAdvicePrompted = true;

				CMDBuild.core.Message.warn(null, CMDBuild.Translation.errors.emailChangedWithAutoSynch);
			}
		},

		/**
		 * Using FillFromTemplateButton I put only tempalteName in emailObject and get template data to fill email form
		 *
		 * @param {String} templateName
		 */
		onEmailWindowFillFromTemplateButtonClick: function(templateName) {
			CMDBuild.core.proxy.email.Templates.get({
				params: {
					name: templateName
				},
				scope: this,
				loadMask: true,
				failure: function(response, options, decodedResponse) {
					CMDBuild.core.Message.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure),
						false
					);
				},
				success: function(response, options, decodedResponse) {
					var response = decodedResponse.response;

					this.loadFormValues(Ext.create('CMDBuild.model.common.tabs.email.Template', response));

					// Bind extra form fields to email record
					this.record.set(CMDBuild.core.proxy.Constants.TEMPLATE, response[CMDBuild.core.proxy.Constants.NAME]);
					this.record.set(CMDBuild.core.proxy.Constants.ACCOUNT, response[CMDBuild.core.proxy.Constants.DEFAULT_ACCOUNT]);

					this.form.keepSynchronizationCheckbox.setDisabled(false);
				}
			});
		}
	});

})();