(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormEventController", {

		parentDelegate: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(param);

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
					return this.onRowSelected(param);

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

		onAddButtonClick: function(parameter) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.loadForm(parameter.type);
			this.view.reset();
			this.view.enableTabbedModify(true);
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

		onRowSelected: function(param) {
			this.loadForm(param.record.getData().type);
			this.view.disableModify(true);

			return this.view.wizard.changeTab(0);

// Just switch when serverside will be completed
//			if (this.selectionModel.hasSelection()) {
//				var me = this;
//				this.selectedId = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.ID);
//
//				// Selected user asynchronous store query
//				this.selectedDataStore = CMDBuild.ServiceProxy.configuration.tasks.get();
//				this.selectedDataStore.load({
//					params: { id: this.selectedId }
//				});
//				this.selectedDataStore.on('load', function() {
//					me.loadForm(param.record.getData().type);
//					me.loadRecord(this.getAt(0));
//				});
//
//				this.view.disableModify(true);
//				this.view.wizard.changeTab(0);
//			}
		},

		onSaveButtonClick: function() {
			var nonvalid = this.view.getNonValidFields();

			if (nonvalid.length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return;
			}

			var formData = this.view.getData();
			formData.fromAddressFilter = Ext.encode(formData.fromAddressFilter.split(' OR '));
			formData.subjectFilter = Ext.encode(formData.subjectFilter.split(' OR '));
_debug(formData);
//			if (formData.id == null || formData.id == '') {
//				CMDBuild.ServiceProxy.tasks.create({
//					params: formData,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			} else {
//				CMDBuild.ServiceProxy.tasks.update({
//					params: formData,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			}
		},

		removeItem: function() {
			if (this.selectedId == null) {
				// Nothing to remove
				return;
			}

			var me = this,
				store = this.parentDelegate.grid.store;

			CMDBuild.ServiceProxy.tasks.remove({
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
		},

		loadForm: function(type) {
			if (this.parentDelegate.tasksDatas.indexOf(type) > -1) {
				this.view.wizard.removeAll();
				var wizardPanel = Ext.create('CMDBuild.view.administration.tasks.' + type + '.CMTaskTabs');
				var items = wizardPanel.getTabs();

				for (var i = 0; i < items.length; i++) {
					this.view.wizard.add(items[i]);
				}

				this.view.wizard.numberOfTabs = items.length;
				this.view.wizard.setActiveTab(0);
			}
		}
	});

})();