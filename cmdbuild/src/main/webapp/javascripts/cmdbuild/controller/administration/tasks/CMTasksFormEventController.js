(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyEmailTemplates');

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormEventController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
//		taskType: 'event',

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
					this.onClassSelected(param.className);

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
		 * @param (String) type
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
		onModifyButtonClick: function() {
			this.callParent(arguments);

			_debug('onModifyButtonClick to implement');
		},

		// overwrite
		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);
				this.selectedType = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.TYPE);

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
							_debug(this.selectedType);
							this.parentDelegate.loadForm(this.selectedType);

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually
// TODO
							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.ServiceProxy.parameter.ID));
//
//							// Set step2 [1] datas
//							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
//							this.delegateStep[1].setValueBase(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

							this.view.disableModify(true);
						}
					}
				});

				this.view.wizard.changeTab(0);
			}
		},

		// overwrite
		onSaveButtonClick: function() {
			var nonvalid = this.view.getNonValidFields();

			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

//			CMDBuild.LoadMask.get().show();
			var filterData = this.delegateStep[1].getDataFilters();
			var formData = this.view.getData(true);
			var submitDatas = {};

			// Form validating by type
				switch (this.delegateStep[0].taskType) {
					case 'event_asynchronous': {

						// Cron field validation
							if (!this.delegateStep[1].getCronDelegate().validate(this.parentDelegate.form.wizard))
								return;

						submitDatas[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
							formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE]
						);
					} break;

					case 'event_synchronous': {
						submitDatas[CMDBuild.ServiceProxy.parameter.PHASE] = formData[CMDBuild.ServiceProxy.parameter.PHASE];
						submitDatas[CMDBuild.ServiceProxy.parameter.GROUPS] = Ext.encode(this.delegateStep[0].getValueGroups());
					} break;

					default:
						throw 'CMTasksFormEventController error: task type not recognized';
				}

			// Fieldset submitting filter to avoid to send datas if fieldset are collapsed
				var notificationFieldsetCheckboxValue = this.delegateStep[2].getValueNotificationFieldsetCheckbox();
				if (notificationFieldsetCheckboxValue) {
					submitDatas[CMDBuild.ServiceProxy.parameter.NOTIFICATION_ACTIVE] = notificationFieldsetCheckboxValue;
					submitDatas[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_ACCOUNT] = formData[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_ACCOUNT];
					submitDatas[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE] = formData[CMDBuild.ServiceProxy.parameter.NOTIFICATION_EMAIL_TEMPLATE];
				}

				var workflowFieldsetCheckboxValue = this.delegateStep[2].getValueWorkflowFieldsetCheckbox();
				if (workflowFieldsetCheckboxValue) {
					var attributesGridValues = this.delegateStep[2].getValueAttributeGrid();

					if (!CMDBuild.Utils.isEmpty(attributesGridValues))
						submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

					submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_ACTIVE] = workflowFieldsetCheckboxValue;
					submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME];
				}

			// Filters validation
				if (!Ext.isEmpty(filterData))
					submitDatas[CMDBuild.ServiceProxy.parameter.FILTER] = Ext.encode(filterData);

			// Data filtering to submit only right values
			submitDatas[CMDBuild.ServiceProxy.parameter.ACTIVE] = formData[CMDBuild.ServiceProxy.parameter.ACTIVE];
			submitDatas[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.CLASS_NAME];
			submitDatas[CMDBuild.ServiceProxy.parameter.DESCRIPTION] = formData[CMDBuild.ServiceProxy.parameter.DESCRIPTION];
			submitDatas[CMDBuild.ServiceProxy.parameter.ID] = formData[CMDBuild.ServiceProxy.parameter.ID];
_debug(filterData);
_debug(formData);
_debug(submitDatas);
//			if (Ext.isEmpty(formData[CMDBuild.ServiceProxy.parameter.ID])) {
//				CMDBuild.core.proxy.CMProxyTasks.create({
//					type: this.delegateStep[0].taskType,
//					params: submitDatas,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			} else {
//				CMDBuild.core.proxy.CMProxyTasks.update({
//					type: this.delegateStep[0].taskType,
//					params: submitDatas,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			}
		}
	});

})();