(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormBaseController', {

		parentDelegate: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: undefined,

		cmOn: function(name, param, callBack) {
			throw 'CMTasksFormBaseController: cmOn() unimplemented method';
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
			this.view.disableTypeField();
			this.view.wizard.changeTab(0);
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

		onRowSelected: function() {
			throw 'CMTasksFormBaseController: onRowSelected() unimplemented method';
		},

		onSaveButtonClick: function() {
			throw 'CMTasksFormBaseController: onSaveButtonClick() unimplemented method';
		},

		removeItem: function() {
			throw 'CMTasksFormBaseController: removeItem() unimplemented method';
		},

		success: function(result, options, decodedResult) {
			throw 'CMTasksFormBaseController: success() unimplemented method';
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		}
	});

})();