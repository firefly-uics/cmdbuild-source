(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController', {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep1: undefined,
		delegateStep2: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'workflow',

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
				this.selectedDataStore = CMDBuild.core.serviceProxy.CMProxyTasks.get(me.taskType);
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
					me.view.disableTypeField();
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
			var formData = this.view.getData(true),
				attributesGridValues = Ext.getCmp('workflowAttributesGrid').getData();

			// Form submit values formatting
				if (formData.cronInputType) {
					formData.cronExpression = me.buildCronExpression([
						formData.minute,
						formData.hour,
						formData.dayOfMounth,
						formData.mounth,
						formData.dayOfWeek
					]);
				} else {
					formData.cronExpression = formData.base;
				}
				if (!CMDBuild.Utils.isEmpty(attributesGridValues))
					formData.attributes = Ext.encode(attributesGridValues);

			delete formData.base;
			delete formData.cronInputType;
			delete formData.dayOfMounth;
			delete formData.dayOfWeek;
			delete formData.hour;
			delete formData.minute;
			delete formData.mounth;
			delete formData.name;
			delete formData.value;

			if (formData.id == null || formData.id == '') {
				CMDBuild.core.serviceProxy.CMProxyTasks.create({
					type: this.taskType,
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			} else {
				CMDBuild.core.serviceProxy.CMProxyTasks.update({
					type: this.taskType,
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			}
		},

		removeItem: function() {
			if (this.selectedId == null) {
				// Nothing to remove
				return;
			}

			var me = this,
				store = this.parentDelegate.grid.store;

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.serviceProxy.CMProxyTasks.remove({
				type: this.taskType,
				params: { id: this.selectedId },
				scope: this,
				success: function() {
					me.view.reset();

					store.load();
					store.on('load', function() {
						me.selectionModel.select(0, true);
					});

					this.view.disableModify();
					this.view.wizard.changeTab(0);
				},
				callback: this.callback
			});
		},

		success: function(response, options, decodedResult) {
			var me = this,
				store = this.parentDelegate.grid.store;

			store.load();
			store.on('load', function() {
				me.view.reset();
				var rowIndex = this.find(
					CMDBuild.ServiceProxy.parameter.ID,
					(decodedResult.response) ? decodedResult.response : me.view.getForm().findField(CMDBuild.ServiceProxy.parameter.ID).getValue()
				);

				me.selectionModel.select(rowIndex, true);
			});

			this.view.disableModify();
			this.view.wizard.changeTab(0);
		},

		/**
		 * @param Array fields
		 * @returns String cron expression
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