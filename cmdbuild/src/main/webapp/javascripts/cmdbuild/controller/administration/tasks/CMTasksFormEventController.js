(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormEventController", {
		extend: 'CMDBuild.controller.administration.tasks.CMTasksFormBaseController',

		parentDelegate: undefined,
		delegateStep: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: 'event',


		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
//				case 'onAbortButtonClick':
//					return this.onAbortButtonClick();

				case 'onAddButtonClick':
					return this.onAddButtonClick(param.type);

//				case 'onCloneButtonClick':
//					return this.onCloneButtonClick();
//
//				case 'onInizializeWizardButtons':
//					return this.view.wizard.changeTab(0);
//
//				case 'onModifyButtonClick':
//					return this.onModifyButtonClick();
//
//				case 'onNextButtonClick':
//					return this.view.wizard.changeTab(+1);
//
//				case 'onPreviousButtonClick':
//					return this.view.wizard.changeTab(-1);
//
//				case 'onRemoveButtonClick':
//					return this.onRemoveButtonClick();
//
//				case 'onRowSelected':
//					return this.onRowSelected();
//
//				case 'onSaveButtonClick':
//					return this.onSaveButtonClick();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAddButtonClick: function(type) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(this.taskType);
			this.view.reset();
			this.view.enableTabbedModify();
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		}
	});

})();