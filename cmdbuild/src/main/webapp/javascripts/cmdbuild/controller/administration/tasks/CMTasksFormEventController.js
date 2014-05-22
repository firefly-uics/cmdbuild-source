(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormEventController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		delegateStep: undefined,
		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
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

				case 'onClassSelected':
					return this.onClassSelected(param.className);

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

			switch (param.type) {
				case 'event_asynchronous':
					return this.delegateStep[3].setDisabledWorkflowAttributesGrid(true);

				case 'event_synchronous':
					return this.delegateStep[2].setDisabledWorkflowAttributesGrid(true);

				default:
					throw 'CMTasksFormEventController error: task type not recognized';
			}
		},

		/**
		 * @param (String) className
		 */
		onClassSelected: function(className) {
			this.setDisabledButtonNext(false);
			this.delegateStep[1].className = className;
		},

		// overwrite
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.ID);
				this.selectedType = this.selectionModel.getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.TYPE);

				// Selected task asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyTasks.get(this.selectedType);
				this.selectedDataStore.load({
					scope: this,
					params: {
						id: this.selectedId
					},
					callback: function(records, operation, success) {
						if (!Ext.isEmpty(records)) {
							var record = records[0];

// TODO: to check if response has phase data or not to extends taskType value

							this.parentDelegate.loadForm(this.selectedType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].selectGroups(record.get(CMDBuild.core.proxy.CMProxyConstants.GROUPS));
							this.delegateStep[0].setValueActive(record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVE));
							this.delegateStep[0].setValueClassName(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));
							this.delegateStep[0].setValuePhase(record.get(CMDBuild.core.proxy.CMProxyConstants.PHASE));

							// Set step2 [1] datas
							this.delegateStep[1].setValueFilters(
								Ext.decode(record.get(CMDBuild.core.proxy.CMProxyConstants.FILTER))
							);

							// Set step3 [2] datas
							this.delegateStep[2].setValueNotificationFieldsetCheckbox(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE));
							this.delegateStep[2].setValueNotificationAccount(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT));
							this.delegateStep[2].setValueNotificationTemplate(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE));
							this.delegateStep[2].setValueWorkflowAttributesGrid(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES));
							this.delegateStep[2].setValueWorkflowCombo(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME));
							this.delegateStep[2].setValueWorkflowFieldsetCheckbox(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE));

							this.view.disableModify(true);
						}
					}
				});

				this.view.wizard.changeTab(0);
			}
		},

		// overwrite
		onSaveButtonClick: function() {
			var filterData = this.delegateStep[1].getDataFilters();
			var formData = this.view.getData(true);
			var submitDatas = {};
			var taskType = this.delegateStep[0].taskType;

			// Validate before save
			if (this.validate(formData[CMDBuild.core.proxy.CMProxyConstants.ACTIVE], taskType)) {
				CMDBuild.LoadMask.get().show();

				// Form actions by type
					switch (taskType) {
						case 'event_asynchronous': {
							submitDatas[CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
								formData[CMDBuild.core.proxy.CMProxyConstants.CRON_INPUT_TYPE]
							);

							// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
								var notificationFieldsetCheckboxValue = this.delegateStep[3].getValueNotificationFieldsetCheckbox();
								if (notificationFieldsetCheckboxValue) {
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT];
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE];
								}

								var workflowFieldsetCheckboxValue = this.delegateStep[3].getValueWorkflowFieldsetCheckbox();
								if (workflowFieldsetCheckboxValue) {
									var attributesGridValues = this.delegateStep[3].getValueWorkflowAttributeGrid();

									if (!CMDBuild.Utils.isEmpty(attributesGridValues))
										submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME];
								}

								// TODO
						} break;

						case 'event_synchronous': {
							submitDatas[CMDBuild.core.proxy.CMProxyConstants.PHASE] = formData[CMDBuild.core.proxy.CMProxyConstants.PHASE];
							submitDatas[CMDBuild.core.proxy.CMProxyConstants.GROUPS] = Ext.encode(this.delegateStep[0].getValueGroups());

							// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
								var notificationFieldsetCheckboxValue = this.delegateStep[2].getValueNotificationFieldsetCheckbox();
								if (notificationFieldsetCheckboxValue) {
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT];
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE];
								}

								var workflowFieldsetCheckboxValue = this.delegateStep[2].getValueWorkflowFieldsetCheckbox();
								if (workflowFieldsetCheckboxValue) {
									var attributesGridValues = this.delegateStep[2].getValueWorkflowAttributeGrid();

									if (!CMDBuild.Utils.isEmpty(attributesGridValues))
										submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME];
								}
						} break;

						default:
							throw 'CMTasksFormEventController error: task type not recognized';
					}

				// Form submit values formatting
				if (!Ext.isEmpty(filterData))
					submitDatas[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode(filterData);

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = formData[CMDBuild.core.proxy.CMProxyConstants.ACTIVE];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = formData[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ID] = formData[CMDBuild.core.proxy.CMProxyConstants.ID];
_debug(filterData);
_debug(formData);
_debug(submitDatas);
				if (Ext.isEmpty(formData[CMDBuild.core.proxy.CMProxyConstants.ID])) {
					CMDBuild.core.proxy.CMProxyTasks.create({
						type: taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				} else {
					CMDBuild.core.proxy.CMProxyTasks.update({
						type: taskType,
						params: submitDatas,
						scope: this,
						success: this.success,
						callback: this.callback
					});
				}
			}
		},

		// overwrite
		removeItem: function() {
			if (!Ext.isEmpty(this.selectedId)) {
				CMDBuild.LoadMask.get().show();

				CMDBuild.core.proxy.CMProxyTasks.remove({
					type: this.selectedType,
					params: {
						id: this.selectedId
					},
					scope: this,
					success: this.success,
					callback: this.callback
				});
			}
		},

		/**
		 * Task validation
		 *
		 * @param (Boolean) enable
		 * @param (String) type
		 *
		 * @return (Boolean)
		 */
		// overwrite
		validate: function(enable, type) {
			switch (type) {
				case 'event_asynchronous': {
					// Cron field validation
					this.delegateStep[3].getCronDelegate().validate(enable);

//					// Notification validation
//					this.delegateStep[3].getNotificationDelegate().validate(
//						this.delegateStep[3].getValueNotificationFieldsetCheckbox()
//						&& enable
//					);

//					// Workflow form validation
//					this.delegateStep[3].getWorkflowDelegate().validate(
//						this.delegateStep[3].getValueWorkflowFieldsetCheckbox()
//						&& enable
//					);

					// TODO
				} break;

				case 'event_synchronous': {
					// Phase validation
					this.delegateStep[0].setAllowBlankPhaseCombo(!enable);

					// Notification validation
					this.delegateStep[2].getNotificationDelegate().validate(
						this.delegateStep[2].getValueNotificationFieldsetCheckbox()
						&& enable
					);

					// Workflow form validation
					this.delegateStep[2].getWorkflowDelegate().validate(
						this.delegateStep[2].getValueWorkflowFieldsetCheckbox()
						&& enable
					);
				} break;

				default:
					throw 'CMTasksFormEventController validate error: type not recognized';
			}

			return this.callParent(arguments);
		}
	});

})();