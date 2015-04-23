(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormEventController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyTasks'
		],

		/**
		 * @cfg {Array} array of all step delegates
		 */
		delegateStep: undefined,

		/**
		 * @cfg {CMDBuild.controller.administration.tasks.CMTasksController}
		 */
		parentDelegate: undefined,

		/**
		 * @property {Int}
		 */
		selectedId: undefined,

		/**
		 * @property {Ext.selection.Model}
		 */
		selectionModel: undefined,

		/**
		 * @property {CMDBuild.view.administration.tasks.CMTasksForm}
		 */
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
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
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 *
		 * @override
		 */
		onAddButtonClick: function(name, param, callBack) {
			this.callParent(arguments);

			switch (param.type) {
				case 'event_asynchronous':
					return this.delegateStep[3].eraseWorkflowForm();

				case 'event_synchronous':
					return this.delegateStep[2].eraseWorkflowForm();

				default:
					_debug('CMTasksFormEventController error: onAddButtonClick task type not recognized');
			}
		},

		/**
		 * @param {String} className
		 */
		onClassSelected: function(className) {
			this.setDisabledButtonNext(false);
			this.delegateStep[1].className = className;
		},

		/**
		 * @override
		 */
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

							this.parentDelegate.loadForm(this.selectedType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually

							switch (this.selectedType) {
								case 'event_asynchronous': { // TODO
									// Set step1 [0] datas
									this.delegateStep[0].setValueActive(record.get(CMDBuild.core.proxy.CMProxyConstants.ACTIVE));
									this.delegateStep[0].setValueClassName(record.get(CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME));
									this.delegateStep[0].setValueDescription(record.get(CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION));
									this.delegateStep[0].setValueId(record.get(CMDBuild.core.proxy.CMProxyConstants.ID));

									// Set step2 [1] datas
									this.delegateStep[1].setValueFilters(
										Ext.decode(record.get(CMDBuild.core.proxy.CMProxyConstants.FILTER))
									);

									// Set step3 [2] datas
									this.delegateStep[2].setValueAdvancedFields(record.get(CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION));
									this.delegateStep[2].setValueBase(record.get(CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION));

									// Set step4 [3] datas
									this.delegateStep[3].setValueNotificationFieldsetCheckbox(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE));
									this.delegateStep[3].setValueNotificationAccount(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT));
									this.delegateStep[3].setValueNotificationTemplate(record.get(CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE));
// TODO: future implementation
//									this.delegateStep[3].setValueWorkflowAttributesGrid(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES));
//									this.delegateStep[3].setValueWorkflowCombo(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME));
//									this.delegateStep[3].setValueWorkflowFieldsetCheckbox(record.get(CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE));
								} break;

								case 'event_synchronous': {
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
								} break;

								default:
									_debug('CMTasksFormEventController error: onRowSelected task type not recognized');
							}

							this.view.disableModify(true);
						}
					}
				});

				this.parentDelegate.changeItem(0);
			}
		},

		/**
		 * @override
		 */
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
							submitDatas[CMDBuild.core.proxy.CMProxyConstants.CRON_EXPRESSION] = this.delegateStep[2].getCronDelegate().getValue();

							// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
								var notificationFieldsetCheckboxValue = this.delegateStep[3].getValueNotificationFieldsetCheckbox();
								if (notificationFieldsetCheckboxValue) {
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_ACCOUNT];
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.core.proxy.CMProxyConstants.NOTIFICATION_EMAIL_TEMPLATE];
								}
// TODO: future implementation
//								var workflowFieldsetCheckboxValue = this.delegateStep[3].getValueWorkflowFieldsetCheckbox();
//								if (workflowFieldsetCheckboxValue) {
//									var attributesGridValues = this.delegateStep[3].getValueWorkflowAttributeGrid();
//
//									if (!Ext.Object.isEmpty(attributesGridValues))
//										submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);
//
//									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
//									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME];
//								}
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

									if (!Ext.Object.isEmpty(attributesGridValues))
										submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
									submitDatas[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.WORKFLOW_CLASS_NAME];
								}
						} break;

						default:
							_debug('CMTasksFormEventController error: onSaveButtonClick task type not recognized');
					}

				// Form submit values formatting
				if (!Ext.isEmpty(filterData))
					submitDatas[CMDBuild.core.proxy.CMProxyConstants.FILTER] = Ext.encode(filterData);

				// Data filtering to submit only right values
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ACTIVE] = formData[CMDBuild.core.proxy.CMProxyConstants.ACTIVE];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME] = formData[CMDBuild.core.proxy.CMProxyConstants.CLASS_NAME];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION] = formData[CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION];
				submitDatas[CMDBuild.core.proxy.CMProxyConstants.ID] = formData[CMDBuild.core.proxy.CMProxyConstants.ID];

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

		/**
		 * @override
		 */
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
		 * @param {Boolean} enable
		 * @param {String} type
		 *
		 * @return {Boolean}
		 *
		 * @override
		 */
		validate: function(enable, type) {
			switch (type) {
				case 'event_asynchronous': {
					// Cron field validation
					this.delegateStep[2].getCronDelegate().validate(enable);

					// Notification validation
					this.delegateStep[3].getNotificationDelegate().validate(
						this.delegateStep[3].getValueNotificationFieldsetCheckbox()
						&& enable
					);

// TODO: future implementation
//					// Workflow form validation
//					this.delegateStep[3].getWorkflowDelegate().validate(
//						this.delegateStep[3].getValueWorkflowFieldsetCheckbox()
//						&& enable
//					);
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
					_debug('CMTasksFormEventController error: validate task type not recognized');
			}

			return this.callParent(arguments);
		}
	});

})();