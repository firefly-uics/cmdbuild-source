(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		delegateStep: undefined,
		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'workflow',
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

			this.delegateStep[0].setDisabledWorkflowAttributesGrid(true);
		},

		// overwrite
		onModifyButtonClick: function() {
			this.callParent(arguments);

			if (!this.delegateStep[0].checkWorkflowComboSelected())
				this.delegateStep[0].setDisabledWorkflowAttributesGrid(true);
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

							// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find good fix, so i must set all fields manually

							// Set step1 [0] datas
							this.delegateStep[0].setValueActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
							this.delegateStep[0].setValueDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
							this.delegateStep[0].setValueId(record.get(CMDBuild.ServiceProxy.parameter.ID));
							this.delegateStep[0].setValueWorkflowAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES));
							this.delegateStep[0].setValueWorkflowCombo(record.get(CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME));

							// Set step2 [1] datas
							this.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
							this.delegateStep[1].setValueBase(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

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
			var attributesGridValues = this.delegateStep[0].getValueWorkflowAttributeGrid();
			var submitDatas = {};

			// Stop save process if not valid
			if (!this.validate(formData[CMDBuild.ServiceProxy.parameter.ACTIVE]))
				return;

			CMDBuild.LoadMask.get().show();

			submitDatas[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().getValue(
				formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE]
			);

			// Form submit values formatting
			if (!CMDBuild.Utils.isEmpty(attributesGridValues))
				submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_ATTRIBUTES] = Ext.encode(attributesGridValues);

			// Data filtering to submit only right values
			submitDatas[CMDBuild.ServiceProxy.parameter.ACTIVE] = formData[CMDBuild.ServiceProxy.parameter.ACTIVE];
			submitDatas[CMDBuild.ServiceProxy.parameter.DESCRIPTION] = formData[CMDBuild.ServiceProxy.parameter.DESCRIPTION];
			submitDatas[CMDBuild.ServiceProxy.parameter.ID] = formData[CMDBuild.ServiceProxy.parameter.ID];
			submitDatas[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME] = formData[CMDBuild.ServiceProxy.parameter.WORKFLOW_CLASS_NAME];

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
			// Cron field validation
			this.delegateStep[1].getCronDelegate().validate(enable);

			// Workflow form validation
			this.delegateStep[0].getWorkflowDelegate().validate(enable);

			return this.callParent(arguments);
		}
	});

})();