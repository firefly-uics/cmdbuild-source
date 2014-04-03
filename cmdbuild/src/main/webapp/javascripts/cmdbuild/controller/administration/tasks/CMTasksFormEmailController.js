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
					return this.onAddButtonClick(name, param, callBack);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

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
			// TODO: finish the setup of this function when server will answer correctly with all checkboxes values
//			this.delegateStep[0].onWorkflowSelected(this.delegateStep[0].getValueWorkflowCombo(), true);

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
					me.delegateStep[0].setValueActive(record.get(CMDBuild.ServiceProxy.parameter.ACTIVE));
					me.delegateStep[0].setValueDescription(record.get(CMDBuild.ServiceProxy.parameter.DESCRIPTION));
					me.delegateStep[0].setValueEmailAccount(record.get(CMDBuild.ServiceProxy.parameter.EMAIL_ACCOUNT));
					me.delegateStep[0].setValueFilterFromAddress(record.get(CMDBuild.ServiceProxy.parameter.FILTER_FROM_ADDRESS));
					me.delegateStep[0].setValueFilterSubject(record.get(CMDBuild.ServiceProxy.parameter.FILTER_SUBJECT));
					me.delegateStep[0].setValueId(record.get(CMDBuild.ServiceProxy.parameter.ID));

					// Set step2 [1] datas
					me.delegateStep[1].setValueAdvancedFields(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));
					me.delegateStep[1].setValueBase(record.get(CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION));

					// Set step4 [3] datas
					me.delegateStep[3].setValueAttributesGrid(record.get(CMDBuild.ServiceProxy.parameter.ATTRIBUTES));

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
			var attributesGridValues = this.delegateStep[3].getValueAttributeGrid();

			// Form submit values formatting
				if ( !Ext.isEmpty(formData.filterFromAddress)) {
					formData.filterFromAddress = Ext.encode(formData.filterFromAddress.split(' OR '));
				}

				if ( !Ext.isEmpty(formData.filterSubject)) {
					formData.filterSubject = Ext.encode(formData.filterSubject.split(' OR '));
				}

				if (formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE]) {
					formData[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = this.delegateStep[1].getCronDelegate().buildCronExpression([
						formData[CMDBuild.ServiceProxy.parameter.MINUTE],
						formData[CMDBuild.ServiceProxy.parameter.HOUR],
						formData[CMDBuild.ServiceProxy.parameter.DAY_OF_MOUNTH],
						formData[CMDBuild.ServiceProxy.parameter.MOUNTH],
						formData[CMDBuild.ServiceProxy.parameter.DAY_OF_WEEK]
					]);
				} else {
					formData[CMDBuild.ServiceProxy.parameter.CRON_EXPRESSION] = formData['baseCombo'];
				}

				if (!CMDBuild.Utils.isEmpty(attributesGridValues))
					formData[CMDBuild.ServiceProxy.parameter.ATTRIBUTES] = Ext.encode(attributesGridValues);

			// Manual validation of cron fields because disabled fields are not validated
			if (this.delegateStep[1].isEmptyAdvanced()) {
				this.delegateStep[1].getCronDelegate().markInvalidAdvancedFields('This field is required');

				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);

				CMDBuild.LoadMask.get().hide();

				this.parentDelegate.form.wizard.changeTab(1);
				this.delegateStep[1].getCronDelegate().setValueAdvancedRadio(true);

				return;
			}

			delete formData['baseCombo'];
			delete formData[CMDBuild.ServiceProxy.parameter.CRON_INPUT_TYPE];
			delete formData[CMDBuild.ServiceProxy.parameter.DAY_OF_MOUNTH];
			delete formData[CMDBuild.ServiceProxy.parameter.DAY_OF_WEEK];
			delete formData[CMDBuild.ServiceProxy.parameter.HOUR];
			delete formData[CMDBuild.ServiceProxy.parameter.MINUTE];
			delete formData[CMDBuild.ServiceProxy.parameter.MOUNTH];

			if (Ext.isEmpty(formData[CMDBuild.ServiceProxy.parameter.ID])) {
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
			var me = this;
			var store = this.parentDelegate.grid.store;

			store.load();
			store.on('load', function() {
				me.view.reset();
				var rowIndex = this.find(
					CMDBuild.ServiceProxy.parameter.ID,
					(decodedResult.response) ? decodedResult.response : me.delegateStep[0].getValueId()
				);
				me.selectionModel.select(rowIndex, true);
				me.onRowSelected();
			});

			this.view.disableModify();
			this.view.wizard.changeTab(0);
		}
	});

})();