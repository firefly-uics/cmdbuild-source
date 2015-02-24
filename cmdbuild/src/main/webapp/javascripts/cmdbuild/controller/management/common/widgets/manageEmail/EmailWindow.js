(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.manageEmail.EmailWindow', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		requires: [
			'CMDBuild.controller.management.common.widgets.CMWidgetController',
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.EmailTemplates',
			'CMDBuild.core.proxy.widgets.ManageEmail'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.manageEmail.Grid}
		 */
		parentDelegate: undefined,

		attachmentsController: undefined,

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
		 * @cfg {String}
		 */
		windowMode: 'create',

		/**
		 * @cfg {Array}
		 */
		windowModeAvailable: ['create', 'edit', 'reply', 'view'],

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.manageEmail.Main} configObject.parentDelegate
		 * @param {CMDBuild.model.widget.ManageEmail.email} configObject.record
		 * @param {String} configObject.windowMode
		 */
		constructor: function(configObject) {
			var me = this;
			var windowClassName = null;

			Ext.apply(this, configObject); // Apply config

			if (Ext.Array.contains(this.windowModeAvailable, this.windowMode)) {
				switch (this.windowMode) {
					case 'view': {
						windowClassName = 'CMDBuild.view.management.common.widgets.manageEmail.emailWindow.ViewWindow';
					} break;

					default: { // Default window class to build
						windowClassName = 'CMDBuild.view.management.common.widgets.manageEmail.emailWindow.EditWindow';
					}
				}

				this.view = Ext.create(windowClassName, {
					delegate: this,

					listeners: {
						scope: this,
						close: function(window, eOpts) {
							this.cmOn('onEmailWindowCloseButtonClick');
						}
					}
				});

				// Fill from template button store configuration
				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.EmailTemplates.getAll({
					scope: this,
					success: function(response, options, decodedResponse) {
						var templatesArray = decodedResponse.response.elements;

						if (templatesArray.length > 0) {
							// Sort templatesArray by description ascending
							Ext.Array.sort(templatesArray, function(item1, item2) {
								if (item1[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] < item2[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION])
									return -1;

								if (item1[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] > item2[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION])
									return 1;

								return 0;
							});

							Ext.Array.forEach(templatesArray, function(template, index, allItems) {
								this.view.fillFromTemplateButton.menu.add({
									text: template[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
									templateName: template[CMDBuild.core.proxy.CMProxyConstants.NAME],

									handler: function(button, e) {
										me.cmOn('onEmailWindowFillFromTemplateButtonClick', button[CMDBuild.core.proxy.CMProxyConstants.TEMPLATE_NAME]);
									}
								});
							}, this);
						} else { // To disable button if the aren't templates
							this.view.fillFromTemplateButton.setDisabled(true);
						}
					},
					callback: function(options, success, response) {
						CMDBuild.LoadMask.get().hide();
					}
				});

				if (CMDBuild.Config.dms.enabled == 'true') { // TODO: use a model for CMDBuild.Config to convert attributes from string
					// Build attachments controller
					this.attachmentsController = Ext.create('CMDBuild.controller.management.common.widgets.manageEmail.Attachments', {
						parentDelegate: this,
						record: this.record,
						view: this.view.attachmentContainer
					});

					// Get all email attachments
					var params = {};
					params[CMDBuild.core.proxy.CMProxyConstants.EMAIL_ID] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID);
					params[CMDBuild.core.proxy.CMProxyConstants.TEMPORARY] = this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPORARY);

					this.view.setLoading(true);
					CMDBuild.core.proxy.widgets.ManageEmail.attachmentGetAll({
						params: params,
						scope: this,
						success: function(response, options, decodedResponse) {
							_debug('decodedResponse', decodedResponse);
							Ext.Array.forEach(decodedResponse.response, function(item, index, allItems) {
								if(!Ext.Object.isEmpty(item))
									this.attachmentsController.attachmentAddPanel(item[CMDBuild.core.proxy.CMProxyConstants.FILE_NAME]);
							}, this);
						},
						callback: function(records, operation, success) {
							this.view.setLoading(false);
						}
					});
				}

				// If email has template enable keep-synch checkbox
				if (!Ext.isEmpty(this.record.get(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE)) && this.windowMode != 'view')
					this.view.formPanel.keepSynchronizationCheckbox.setDisabled(false);

				// Show window
				if (!Ext.isEmpty(this.view))
					this.view.show();
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
				case 'onEmailWindowCloseButtonClick':
				case 'onEmailWindowAbortButtonClick':
					return this.onEmailWindowAbortButtonClick();

				case 'onEmailWindowConfirmButtonClick':
					return this.onEmailWindowConfirmButtonClick();

				case 'onEmailWindowFieldChange':
					return this.onEmailWindowFieldChange();

				case 'onEmailWindowFillFromTemplateButtonClick':
					return this.onEmailWindowFillFromTemplateButtonClick(param);

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
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
				clientForm: this.cmOn('getWidgetController').clientForm,
				xaVars: xaVars,
				serverVars: this.cmOn('getWidgetController').getTemplateResolverServerVars(this.cmOn('getWidgetController').card)
			});
_debug('this.templateResolver', this.templateResolver);
			this.templateResolver.resolveTemplates({
				attributes: Ext.Object.getKeys(xaVars),
				callback: function(values, ctx) {
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
								id: CMDBuild.core.proxy.CMProxyConstants.FROM,
								value: values[CMDBuild.core.proxy.CMProxyConstants.FROM]
							},
							{
								id: CMDBuild.core.proxy.CMProxyConstants.TO,
								value: values[CMDBuild.core.proxy.CMProxyConstants.TO]
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
							},
							{ // It's last one to avoid Notification pop-up display on setValues action
								id: CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION,
								value: values[CMDBuild.core.proxy.CMProxyConstants.KEEP_SYNCHRONIZATION]
							}
						);
					}

					me.view.formPanel.getForm().setValues(setValueArray);

					// Updates record's prompt synchronizations flag
					me.record.set(CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION, values[CMDBuild.core.proxy.CMProxyConstants.PROMPT_SYNCHRONIZATION]);
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
_debug('### onEmailWindowConfirmButtonClick', this.record);
			// Validate before save
			if (this.validate(this.view.formPanel)) {
				var formValues = this.view.formPanel.getForm().getValues();
_debug('formValues', formValues);
				// Apply formValues to record object
				for (var key in formValues)
					this.record.set(key, formValues[key]);

				// Setup attachments only if DMS is enabled
				if (CMDBuild.Config.dms.enabled == 'true') // TODO: use a model for CMDBuild.Config to convert attributes from string
					this.record.set(CMDBuild.core.proxy.CMProxyConstants.ATTACHMENTS, this.attachmentsController.getAttachmentsNames());

				this.record.set(CMDBuild.core.proxy.CMProxyConstants.ACTIVITY_ID, this.cmOn('getWidgetController').getActivityId());
_debug('this.record', this.record);
				if (Ext.isEmpty(this.record.get(CMDBuild.core.proxy.CMProxyConstants.ID))) {
					this.parentDelegate.addRecord(this.record);
				} else {
					this.parentDelegate.editRecord(this.record);
				}

				if (!Ext.Object.isEmpty(this.templateResolver))
					this.cmOn('getWidgetController').bindLocalDepsChangeEvent(this.record, this.templateResolver, this.cmOn('getWidgetController'));

				this.onEmailWindowAbortButtonClick();
			}
		},

		/**
		 * Change event management to catch email content edit
		 */
		onEmailWindowFieldChange: function() {
_debug('onEmailWindowFieldChange');
			if (!this.isAdvicePrompted && this.isKeepSynchronizationChecked()) {
				this.isAdvicePrompted = true;

				CMDBuild.Msg.warn(null, CMDBuild.Translation.errors.emailChangedWithAutoSynch);
			}
		},

		/**
		 * Using FillFromTemplateButton I put only tempalteName in emailObject and get template data to fill email form
		 *
		 * @param {String} templateName
		 */
		onEmailWindowFillFromTemplateButtonClick: function(templateName) {
_debug('onEmailWindowFillFromTemplateButtonClick', this.record);
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.EmailTemplates.get({
				params: {
					name: templateName
				},
				scope: this,
				failure: function(response, options, decodedResponse) {
					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						Ext.String.format(CMDBuild.Translation.errors.getTemplateWithNameFailure, this.selectedName),
						false
					);
				},
				success: function(response, options, decodedResponse) {
					this.loadFormValues(Ext.create('CMDBuild.model.widget.ManageEmail.template', decodedResponse.response));
					this.record.set(CMDBuild.core.proxy.CMProxyConstants.TEMPLATE, templateName); // Bind templateName to email record
					this.view.formPanel.keepSynchronizationCheckbox.setDisabled(false);
				},
				callback: function(options, success, response) {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});

})();