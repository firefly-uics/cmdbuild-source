(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormEmailController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'email',

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
//			this.delegateStep[0].onWorkflowSelected(this.delegateStep[0].getWorkflowComboValue(), true);

//			if (this.delegateStep[0].checkWorkflowComboSelected())
//				this.delegateStep[0].setDisabledAttributesTable(false);
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

					// HOPING FOR A FIX: loadRecord() fails with comboboxes, and i can't find a working fix, so i must set all fields manually

					// Set step1 [0] datas
					me.delegateStep[0].fillId(record.get(CMDBuild.ServiceProxy.parameter.ID));
					me.delegateStep[0].fillDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
					me.delegateStep[0].fillActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
					me.delegateStep[0].fillEmailAccount(record.get(CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT));
					me.delegateStep[0].fillFilterFromAddress(record.get(CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS));
					me.delegateStep[0].fillFilterSunbject(record.get(CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT));

					// Set step2 [1] datas
					me.delegateStep[1].setBaseValue(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
					me.delegateStep[1].setAdvancedValue(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

					// Set step4 [3] datas
					me.delegateStep[3].fillAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.ATTRIBUTES));

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
			var attributesGridValues = this.delegateStep[3].getAttributeTableValues();
_debug(formData);
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

			// Manual validation of cron fields because disabled fields are not validated
			if (this.delegateStep[1].isAdvancedEmpty()) {

				for(item in this.delegateStep[1].view.advancedFields)
					this.delegateStep[1].view.advancedFields[item].markInvalid('This field is required');

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				CMDBuild.LoadMask.get().hide();

				this.parentDelegate.form.wizard.changeTab(1);
				this.delegateStep[1].view.advanceRadio.setValue(true);

				return;
			}

			delete formData.baseCombo;
			delete formData.cronInputType;
			delete formData.dayOfMounth;
			delete formData.dayOfWeek;
			delete formData.hour;
			delete formData.minute;
			delete formData.mounth;

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

		success: function(result, options, decodedResult) {
			var me = this,
				store = this.parentDelegate.grid.store;

			store.load();
			store.on('load', function() {
				me.view.reset();
				var rowIndex = this.find(
					CMDBuild.ServiceProxy.parameter.ID,
					(decodedResult.response) ? decodedResult.response : me.delegateStep[0].getId()
				);
				me.selectionModel.select(rowIndex, true);
				me.onRowSelected();
			});

			this.view.disableModify();
			this.view.wizard.changeTab(0);
		}
	});

})();