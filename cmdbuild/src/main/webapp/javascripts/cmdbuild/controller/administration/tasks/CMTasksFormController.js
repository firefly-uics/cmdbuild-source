(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormController", {

		parentDelegate: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick': {
					this.isNew = true;
					loadForm(this.view, param.type, -1);
					this.view.reset();
					this.view.enableTabbedModify(false);
					this.view.disableTypeField();

					return changeTab(this.view, 0);
				}

				case 'onCancelButtonClick': {
					this.view.disableModify(true);

					return changeTab(this.view, 0);
				}

				case 'onClearForm': {
					loadForm(this.view, 'null', -1);
					this.view.disableModify(false);

					return changeTab(this.view, 0);
				}

				case 'onCloneButtonClick': {
					this.isNew = true;
					this.view.enableTabbedModify(false);

					return changeTab(this.view, 0);
				}

				case 'onInizializeWizardButtons':
					return changeTab(this.view, 0);

				case 'onModifyButtonClick': {
					this.isNew = false;
					this.view.enableTabbedModify(false);

					return changeTab(this.view, 0);
				}

				case 'onNextButtonClick':
					return changeTab(this.view, +1);

				case 'onPreviousButtonClick':
					return changeTab(this.view, -1);

				case 'onRemoveButtonClick': {
					this.view.disableModify(true);

					return changeTab(this.view, 0);
				}

				case 'onRowSelected': {
					loadForm(this.view, param.record.getData().type, param.record.getData().id);
					this.view.disableModify(true);

					return changeTab(this.view, 0);
				}

				case 'onSaveButtonClick': {
					alert(this.isNew);
					this.view.disableModify(true);

					return changeTab(this.view, 0);
				}

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
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