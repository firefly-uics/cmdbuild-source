(function() {

	/**
	 * Abstract class to extends for form controller implementation
	 */
	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormBaseController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		parentDelegate: undefined,
		view: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: undefined,

		cmOn: function() {
			throw 'CMTasksFormBaseController: cmOn() unimplemented method';
		},

		disableTypeField: function() {
			this.delegateStep[0].setDisabledTypeField(true);
		},

		onAbortButtonClick: function(name, param, callBack) {
			if (this.selectedId != null) {
				this.onRowSelected();
			} else {
				this.view.reset();
				this.view.disableModify();
				this.view.wizard.changeTab(0);
			}
		},

		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(this.taskType);
			this.view.reset();
			this.view.enableTabbedModify();
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onCloneButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.resetIdField();
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onModifyButtonClick: function(name, param, callBack) {
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onRemoveButtonClick: function(name, param, callBack) {
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
			if (this.selectedId == null) {
				// Nothing to remove
				return;
			}

			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.CMProxyTasks.remove({
				type: this.taskType,
				params: { id: this.selectedId },
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		resetIdField: function() {
			this.delegateStep[0].setValueId();
		},

		success: function() {
			throw 'CMTasksFormBaseController: success() unimplemented method';
		}
	});

})();