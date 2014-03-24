(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'workflow',

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick();

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onInizializeWizardButtons':
					return this.view.wizard.changeTab(0);

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onNextButtonClick':
					return this.view.wizard.changeTab(+1);

				case 'onPreviousButtonClick':
					return this.view.wizard.changeTab(-1);

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

		onModifyButtonClick: function() {
			this.callParent(arguments);
			this.delegateStep[0].onWorkflowSelected(this.delegateStep[0].getWorkflowComboValue(), true);

			if (this.delegateStep[0].checkWorkflowComboSelected())
				this.delegateStep[0].setDisabledAttributesTable(false);
		},

		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				var me = this;

				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);

				// Selected task asynchronous store query
				this.selectedDataStore = CMDBuild.core.proxy.CMProxyTasks.get(me.taskType);
				this.selectedDataStore.load({
					params: { id: this.selectedId }
				});
				this.selectedDataStore.on('load', function(store, records, successful, eOpts) {
					var record = records[0];

					me.parentDelegate.loadForm(me.taskType);
					me.view.loadRecord(record); // TODO: TO FIX Seems to be useless but i dunno why

					// Set step1 [0] datas
					me.delegateStep[0].fillId(record.get(CMDBuild.ServiceProxy.parameter.ID));
					me.delegateStep[0].fillDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
					me.delegateStep[0].fillAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.ATTRIBUTES));
					me.delegateStep[0].fillActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));

					// Set step2 [1] datas
					me.delegateStep[1].setBaseValue(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
					me.delegateStep[1].setAdvancedValue(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

					me.view.disableModify(true);
				});

				this.view.wizard.changeTab(0);
			}
		},

		onSaveButtonClick: function() {
			var nonvalid = this.view.getNonValidFields();

			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			CMDBuild.LoadMask.get().show();
			var formData = this.view.getData(true);
			var attributesGridValues = Ext.getCmp('workflowAttributesGrid').getData();

			// Form submit values formatting
			if (formData.cronInputType) {
				formData.cronExpression = this.buildCronExpression([
					formData.minute,
					formData.hour,
					formData.dayOfMounth,
					formData.mounth,
					formData.dayOfWeek
				]);
			} else {
				formData.cronExpression = formData.baseCombo;
			}

			if (!CMDBuild.Utils.isEmpty(attributesGridValues))
				formData.attributes = Ext.encode(attributesGridValues);

			// Manual validation of cron field because disabled fields are not validated
			if (this.delegateStep[1].isAdvancedEmpty()) {
				this.delegateStep[1].view.advanceRadio.setValue(true);

				for(item in this.delegateStep[1].view.advancedFields)
					this.delegateStep[1].view.advancedFields[item].markInvalid('Field required.');

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				CMDBuild.LoadMask.get().hide();

				return;
			}

			delete formData.baseCombo;
			delete formData.cronInputType;
			delete formData.dayOfMounth;
			delete formData.dayOfWeek;
			delete formData.hour;
			delete formData.minute;
			delete formData.mounth;
			delete formData.name;
			delete formData.value;

			if (Ext.isEmpty(formData.id)) {
				CMDBuild.core.proxy.CMProxyTasks.create({
					type: this.taskType,
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			} else {
				CMDBuild.core.proxy.CMProxyTasks.update({
					type: this.taskType,
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			}
		},

		success: function(response, options, decodedResult) {
			var me = this,
				store = this.parentDelegate.grid.store;

			store.load();
			store.on('load', function() {
				me.view.reset();
_debug(decodedResult.response);
				var rowIndex = this.find(
					CMDBuild.ServiceProxy.parameter.ID,
					(decodedResult.response) ? decodedResult.response : me.view.getForm().findField(CMDBuild.ServiceProxy.parameter.ID).getValue()
				);

				me.selectionModel.select(rowIndex, true);
			});

			this.onRowSelected();
		},

		/**
		 * @param (Array) fields
		 * @returns (String) cron expression
		 */
		buildCronExpression: function(fields) {
			var cronExp = '';

			for (var i = 0; i < (fields.length - 1); i++) {
				var field = fields[i];
				cronExp += field + ' ';
			}

			cronExp += fields[fields.length -1];

			return cronExp;
		}
	});

})();
