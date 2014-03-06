(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormWorkflowController", {

		parentDelegate: undefined,
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

		onAbortButtonClick: function() {
			if (this.selectedId != null) {
				this.onRowSelected();
			} else {
				this.view.reset();
				this.view.disableModify();
				this.view.wizard.changeTab(0);
			}
		},

		onAddButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(this.taskType);
			this.view.reset();
			this.view.enableTabbedModify();
			this.view.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onCloneButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.view.wizard.changeTab(0);
			this.view.disableTypeField();
		},

		onModifyButtonClick: function() {
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.view.wizard.changeTab(0);
			this.view.disableTypeField();
		},

		onRemoveButtonClick: function() {
			Ext.Msg.show({
				title: CMDBuild.Translation.administration.setup.remove,
				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
				scope: this,
				buttons: Ext.Msg.YESNO,
				fn: function(button) {
					if (button == 'yes') {
						this.removeItem();
					}
				}
			});
		},

		onRowSelected: function() {_debug('ci sono2');
			if (this.selectionModel.hasSelection()) {_debug('ci sono1');
				var me = this;
				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);

				// Selected user asynchronous store query
				this.selectedDataStore = CMDBuild.core.serviceProxy.CMProxyTasks.get();
				this.selectedDataStore.load({
					params: { id: this.selectedId }
				});
				this.selectedDataStore.on('load', function() {
					_debug('ci sono');
					me.parentDelegate.loadForm(me.taskType);
					me.loadRecord(this.getAt(0));
				});

				this.view.disableModify(true);
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
				formData.className = _CMCache.getEntryTypeNameById(formData.className);
				if (formData.cronInputType) {
					formData.cronExpression = CMDBuild.Utils.buildCronExpression([
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
					type: 'workflow',
					params: formData,
					scope: this,
					success: this.success,
					callback: this.callback
				});
			} else {
				CMDBuild.core.serviceProxy.CMProxyTasks.update({
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
				params: { id: this.selectedId },
				scope: this,
				success: function() {
					me.view.reset();

					store.load();
					store.on('load', function() {
						me.selectionModel.select(0, true);
						me.onRowSelected();
					});

					this.view.disableModify();
					this.view.wizard.changeTab(0);
				},
				callback: this.callback()
			});
		},

		success: function(result, options, decodedResult) {
			var me = this,
				store = this.parentDelegate.grid.store;

			store.load();
			store.on('load', function() {
				me.view.reset();
				var rowIndex = this.find(
					CMDBuild.ServiceProxy.parameter.ID,
					me.view.getForm().findField(CMDBuild.ServiceProxy.parameter.ID).getValue()
				);
				me.selectionModel.select(rowIndex, true);
				me.onRowSelected();
			});

			this.view.disableModify();
			this.view.wizard.changeTab(0);
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		}
	});

})();
