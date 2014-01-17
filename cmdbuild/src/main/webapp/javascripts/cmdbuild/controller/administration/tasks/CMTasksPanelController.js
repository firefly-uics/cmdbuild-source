(function() {
//	var activePanel;
//	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep2;

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksPanelController", {
		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onModifyButtonClick" :
					return alert(name);
				case "onRemoveButtonClick" :
					return alert(name);
				case "onSaveButtonClick" :
					return alert(name);
				case "onCancelButtonClick" :
					return alert(name);
				case "onPreviousButtonClick" :
					return changeTab(this.view, -1);
				case "onNextButtonClick" :
					return changeTab(this.view, +1);
				case "onInizializeWizardButtons" :
					return changeTab(this.view, 0);
				case "onRowSelected" :
					return alert(name + "----" + param.index);
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	function changeTab(view, step) {
		var activeTabComponent = view.wizard.tabbedPanel.activeTab;
		var activeTab = view.wizard.tabbedPanel.items.indexOf(activeTabComponent);
		var numberOfTabs = view.wizard.tabbedPanel.numberOfTabs;
		if (activeTab + step >= 0 && activeTab + step < numberOfTabs) {
			activeTab = activeTab + step;
			view.wizard.tabbedPanel.setActiveTab(activeTab);
		}
		if (activeTab == 0) {
			view.previousButton.disable();
		}
		else  {
			view.previousButton.enable();
		}
		if (activeTab == numberOfTabs - 1) {
			view.nextButton.disable();
		}
		else  {
			view.nextButton.enable();
		}

	}
	/**
	 * @param {xx} xx
	 * Description
	 */
	function onRowSelected(sm, selection, record) {
		if (selection.length > 0) {
			this.currentTask = selection[0];
			this.form.onTaskSelected(this.currentTask);
		}
	}
})();