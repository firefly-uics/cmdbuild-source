(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormController", {

		parentDelegate: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAbortButtonClick':
					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(param);

				case 'onCloneButtonClick':
					return this.onCloneButtonClick();

				case 'onInizializeWizardButtons':
					return changeTab(this.view, 0);

				case 'onModifyButtonClick':
					return this.onModifyButtonClick();

				case 'onNextButtonClick':
					return changeTab(this.view, +1);

				case 'onPreviousButtonClick':
					return changeTab(this.view, -1);

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
			this.view.disableModify(true);

			return changeTab(this.view, 0);

//			if (this.selectedName != null) {
//				this.onRowSelected();
//			} else {
//				this.form.reset();
//				this.form.disableModify();
//			}
		},

		onAddButtonClick: function(param) {
			this.isNew = true;
			loadForm(this.view, param.type, -1);
			this.view.reset();
			this.view.enableTabbedModify(false);
			this.view.disableTypeField();

			return changeTab(this.view, 0);

//			this.selectionModel.deselectAll();
//			this.selectedName = null;
//			this.form.reset();
//			this.form.enableModify(true);
		},

		onCloneButtonClick: function() {
			this.isNew = true;
			this.view.enableTabbedModify(false);

			return changeTab(this.view, 0);
		},

		onModifyButtonClick: function() {
			this.isNew = false;
			this.view.enableTabbedModify(false);

			return changeTab(this.view, 0);

//			this.form.disableCMTbar();
//			this.form.enableCMButtons();
//			this.form.enableModify(true);
//			this.form.disableNameField();
		},

		onRemoveButtonClick: function() {
			this.view.disableModify(true);

			return changeTab(this.view, 0);

//			Ext.Msg.show({
//				title: CMDBuild.Translation.administration.setup.remove,
//				msg: CMDBuild.Translation.common.confirmpopup.areyousure,
//				scope: this,
//				buttons: Ext.Msg.YESNO,
//				fn: function(button) {
//					if (button == 'yes') {
//						this.removeItem();
//					}
//				}
//			});
		},

		onRowSelected: function(param) {
			loadForm(this.view, param.record.getData().type, param.record.getData().id);
			this.view.disableModify(true);

			return changeTab(this.view, 0);

//			if (this.selectionModel.hasSelection()) {
//				var me = this;
//				this.selectedName = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.NAME);
//
//				// Selected user asynchronous store query
//				this.selectedDataStore = CMDBuild.ServiceProxy.configuration.email.accounts.get();
//				this.selectedDataStore.load({
//					params: { name: this.selectedName }
//				});
//				this.selectedDataStore.on('load', function() {
//					me.form.loadRecord(this.getAt(0));
//					me.form.disableSetDefaultAndRemoveButton();
//				});
//
//				this.form.disableModify(true);
//			}
		},

		onSaveButtonClick: function() {
			alert(this.isNew);
			this.view.disableModify(true);

			return changeTab(this.view, 0);

//			var nonvalid = this.form.getNonValidFields();
//
//			if (nonvalid.length > 0) {
//				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
//				return;
//			}
//
//			var formData = this.form.getData(true);
//
//			if (formData.id == null || formData.id == '') {
//				CMDBuild.ServiceProxy.configuration.email.accounts.create({
//					params: formData,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			} else {
//				CMDBuild.ServiceProxy.configuration.email.accounts.update({
//					params: formData,
//					scope: this,
//					success: this.success,
//					callback: this.callback
//				});
//			}
		},
//
//		removeItem: function() {
//			if (this.selectedName == null) {
//				// Nothing to remove
//				return;
//			}
//
//			var me = this,
//				store = this.grid.store;
//
//			CMDBuild.ServiceProxy.configuration.email.accounts.remove({
//				params: { name: this.selectedName },
//				scope: this,
//				success: function() {
//					me.form.reset();
//
//					store.load();
//					store.on('load', function() {
//						me.selectionModel.select(0, true);
//						me.onRowSelected();
//					});
//
//					me.form.disableModify();
//				},
//				callback: this.callback()
//			});
//		},
//
//		success: function(result, options, decodedResult) {
//			var me = this,
//				store = this.grid.store;
//
//			store.load();
//			store.on('load', function() {
//				me.form.reset();
//				var rowIndex = this.find(
//					CMDBuild.ServiceProxy.parameter.NAME,
//					me.form.getForm().findField(CMDBuild.ServiceProxy.parameter.NAME).getValue()
//				);
//				me.selectionModel.select(rowIndex, true);
//				me.onRowSelected();
//			});
//
//			this.form.disableModify(true);
//		},
//
//		callback: function() {
//			CMDBuild.LoadMask.get().hide();
//		}
	});

	function changeTab(view, step) {
		var activeTabComponent = view.wizard.activeTab;
		var activeTab = view.wizard.items.indexOf(activeTabComponent);
		var numberOfTabs = view.wizard.numberOfTabs;

		if (
			activeTab + step >= 0
			&& activeTab + step < numberOfTabs
		) {
			activeTab = activeTab + step;
			view.wizard.setActiveTab(activeTab);
		}

		if (activeTab == 0) {
			view.previousButton.disable();
		} else {
			view.previousButton.enable();
		}

		if (activeTab == numberOfTabs - 1) {
			view.nextButton.disable();
		} else {
			view.nextButton.enable();
		}

	}

	/**
	 * @param {type} String
	 * @param {id} Int
	 */
	function loadForm(view, type, id) {
		switch (type) {
			case 'email': {
				view.wizard.removeAll();
				var emailPanels = Ext.create('CMDBuild.view.administration.tasks.email.CMTaskTabs');
				var items = emailPanels.getTabs();

				for (var i = 0; i < items.length; i++) {
					view.wizard.add(items[i]);
				}

				view.wizard.numberOfTabs = items.length;
				view.wizard.setActiveTab(0);
			} break;

			case 'event': {
				view.wizard.removeAll();
				var eventPanels = Ext.create('CMDBuild.view.administration.tasks.event.CMTaskTabs');
				var items = eventPanels.getTabs();

				for (var i = 0; i < items.length; i++) {
					view.wizard.add(items[i]);
				}

				view.wizard.numberOfTabs = items.length;
				view.wizard.setActiveTab(0);
			} break;

			case 'null': {
				view.wizard.removeAll();
				var newPanel = Ext.create('CMDBuild.view.administration.tasks.null.CMTaskTabs');
				view.wizard.add(newPanel.getTabs()[0]);
				view.wizard.numberOfTabs = 1;
				view.wizard.setActiveTab(0);
			} break;

			case 'workflow': {
				view.wizard.removeAll();
				var workflowPanels = Ext.create('CMDBuild.view.administration.tasks.workflow.CMTaskTabs');
				var items = workflowPanels.getTabs();

				for (var i = 0; i < items.length; i++) {
					view.wizard.add(items[i]);
				}

				view.wizard.numberOfTabs = items.length;
				view.wizard.setActiveTab(0);
			} break;
		}
	}

})();