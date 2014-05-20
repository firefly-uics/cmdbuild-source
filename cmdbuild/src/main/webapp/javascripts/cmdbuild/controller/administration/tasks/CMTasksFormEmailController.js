(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailAccounts');

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormEmailController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		delegateStep: undefined,
		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'email',
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick();

				case 'onRowSelected':
					return this.onRowSelected();

				case 'onSaveButtonClick':
					return this.onSaveButtonClick();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		// overwrite
		onAddButtonClick: function(name, param, callBack) {
			this.callParent(arguments);

			this.delegateStep[3].setDisabledWorkflowAttributesGrid(true);
		},

		// overwrite
		onModifyButtonClick: function() {
			this.callParent(arguments);

			if (!this.delegateStep[3].checkWorkflowComboSelected())
				this.delegateStep[3].setDisabledWorkflowAttributesGrid(true);
		},

		// overwrite
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);

				// Selected task asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyTasks.get(this.taskType);
				this.selectedDataStore.load({
					scope: this,
					params: {
						id: this.selectedId
					},
					callback: function(records, operation, success) {
						if (!Ext.isEmpty(records)) {
							var record = records[0];

							this.parentDelegate.loadForm(this.taskType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find a working fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
							this.delegateStep[0].setValueEmailAccount(record.get(CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT));
							this.delegateStep[0].setValueFilterFromAddress(
								this.delegateStep[0].getFromAddressFilterDelegate().filterStringBuild(
									record.get(CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS)
								)
							);
							this.delegateStep[0].setValueFilterSubject(
								this.delegateStep[0].getSubjectFilterDelegate().filterStringBuild(
									record.get(CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT)
								)
							);
							this.delegateStep[0].setValueId(record.get(CMDBuild.ServiceProxy.parameter.ID));

							// Set step2 [1] datas
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
							this.delegateStep[1].setValueBase(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

							// Set step3 [2] datas
							this.delegateStep[2].setValueAttachmentsFieldsetCheckbox(record.get(CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE));
							this.delegateStep[2].setValueAttachmentsCombo(record.get(CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY));
							this.delegateStep[2].setValueParsingFieldsetCheckbox(record.get(CMDBuild.ServiceProxy.parameter.PARSING_ACTIVE));
							this.delegateStep[2].setValueParsingFields(
								record.get(CMDBuild.ServiceProxy.parameter.PARSING_KEY_END),
								record.get(CMDBuild.ServiceProxy.parameter.PARSING_KEY_INIT),
								record.get(CMDBuild.ServiceProxy.parameter.PARSING_VALUE_END),
								record.get(CMDBuild.ServiceProxy.parameter.PARSING_VALUE_INIT)
							);

							// Set step4 [3] datas
							this.delegateStep[3].setValueWorkflowAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES));
							this.delegateStep[3].setValueWorkflowCombo(record.get(CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME));
							this.delegateStep[3].setValueWorkflowFieldsetCheckbox(record.get(CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE));

							this.view.disableModify(true);
						}
					}
				});

				this.view.wizard.changeTab(0);
			}
		},

		// overwrite
		onSaveButtonClick: function() {
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Validate before save
			if (this.validate(formData[CMDBuild.ServiceProxy.parameter.ACTIVE])) {
				CMDBuild.LoadMask.get().show();

				submitDatas[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
					formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE]
				);

				// Form submit values formatting
					if (!Ext.isEmpty(formData.filterFromAddress))
						submitDatas[CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS] = Ext.encode(
							formData[CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS].split(
								this.delegateStep[0].getFromAddressFilterDelegate().getTextareaConcatParameter()
							)
						);

					if (!Ext.isEmpty(formData.filterSubject))
						submitDatas[CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT] = Ext.encode(
							formData[CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT].split(
								this.delegateStep[0].getSubjectFilterDelegate().getTextareaConcatParameter()
							)
						);

				// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
					var attachmentsFieldsetCheckboxValue = this.delegateStep[2].getValueAttachmentsFieldsetCheckbox();
					if (attachmentsFieldsetCheckboxValue) {
						submitDatas[CMDBuild.ServiceProxy.parameter.ATTACHMENTS_ACTIVE] = attachmentsFieldsetCheckboxValue;
						submitDatas[CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY] = formData[CMDBuild.ServiceProxy.parameter.ATTACHMENTS_CATEGORY];
					}

					var notificationFieldsetCheckboxValue = this.delegateStep[2].getValueNotificationFieldsetCheckbox();
					if (notificationFieldsetCheckboxValue) {
						submitDatas[CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
						submitDatas[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE];
					}

					var parsingFieldsetCheckboxValue = this.delegateStep[2].getValueParsingFieldsetCheckbox();
					if (parsingFieldsetCheckboxValue) {
						submitDatas[CMDBuild.ServiceProxy.parameter.PARSING_ACTIVE] = parsingFieldsetCheckboxValue;
						submitDatas[CMDBuild.ServiceProxy.parameter.PARSING_KEY_END] = formData[CMDBuild.ServiceProxy.parameter.PARSING_KEY_END];
						submitDatas[CMDBuild.ServiceProxy.parameter.PARSING_KEY_INIT] = formData[CMDBuild.ServiceProxy.parameter.PARSING_KEY_INIT];
						submitDatas[CMDBuild.ServiceProxy.parameter.PARSING_VALUE_END] = formData[CMDBuild.ServiceProxy.parameter.PARSING_VALUE_END];
						submitDatas[CMDBuild.ServiceProxy.parameter.PARSING_VALUE_INIT] = formData[CMDBuild.ServiceProxy.parameter.PARSING_VALUE_INIT];
					}

					var workflowFieldsetCheckboxValue = this.delegateStep[3].getValueWorkflowFieldsetCheckbox();
					if (workflowFieldsetCheckboxValue) {
						var attributesGridValues = this.delegateStep[3].getValueWorkflowAttributeGrid();

						if (!CMDBuild.Utils.isEmpty(attributesGridValues))
							submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

						submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
						submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME];
					}

				// Data filtering to submit only right values
				submitDatas[CMDBuild.ServiceProxy.parameter.ACTIVE] = formData[CMDBuild.ServiceProxy.parameter.ACTIVE];
				submitDatas[CMDBuild.ServiceProxy.parameter.DESCRIPTION] = formData[CMDBuild.ServiceProxy.parameter.DESCRIPTION];
				submitDatas[CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT] = formData[CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT];
				submitDatas[CMDBuild.ServiceProxy.parameter.ID] = formData[CMDBuild.ServiceProxy.parameter.ID];
_debug(formData);
_debug(submitDatas);
				if (Ext.isEmpty(formData[CMDBuild.ServiceProxy.parameter.ID])) {
					CMDBuild.core.proxy.CMProxyTasks.create({
						type: this.taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.core.proxy.CMProxyTasks.update({
						type: this.taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		/**
		 * Task validation
		 *
		 * @param (Boolean) enable
		 *
		 * @return (Boolean)
		 */
		// overwrite
		validate: function(enable) {
			// Email account validation
			this.delegateStep[0].setAllowBlankEmailAccountCombo(!enable);

			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			// Parsing validation
			if (this.delegateStep[2].getValueParsingFieldsetCheckbox() && enable) {
				this.delegateStep[2].setAllowBlankParsingFields(false);
			} else {
				this.delegateStep[2].setAllowBlankParsingFields(true);
			}

			// Notification validation
			this.delegateStep[2].getNotificationDelegate().validate(
				this.delegateStep[2].getValueNotificationFieldsetCheckbox()
				&& enable
			);

			// Attachments validation
			if (this.delegateStep[2].getValueAttachmentsFieldsetCheckbox() && enable) {
				this.delegateStep[2].setAllowBlankAttachmentsField(false);
			} else {
				this.delegateStep[2].setAllowBlankAttachmentsField(true);
			}

			// Workflow form validation
			this.delegateStep[3].getWorkflowDelegate().validate(
				this.delegateStep[3].getValueWorkflowFieldsetCheckbox()
				&& enable
			);

			return this.callParent(arguments);
		}
	});

})();