(function() {

	/**
	 * Base class to extends to create form controller implementation
	 */
	// abstract
	Ext.define('CMDBuild.controller.administration.tasks.CMTasksFormBaseController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		parentDelegate: undefined,
		selectedId: undefined,
		selectionModel: undefined,
		taskType: undefined,
		view: undefined,

		// abstract
		cmOn: function() {
			throw 'CMTasksFormBaseController: cmOn() unimplemented method';
		},

		disableTypeField: function() {
			this.delegateStep[0].setDisabledTypeField(true);
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

		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.parentDelegate.loadForm(param.type);
			this.view.reset();
			this.view.enableTabbedModify();
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onCloneButtonClick: function() {
			this.selectionModel.deselectAll();
			this.selectedId = null;
			this.resetIdField();
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
		},

		onModifyButtonClick: function() {
			this.view.disableCMTbar();
			this.view.enableCMButtons();
			this.view.enableTabbedModify(true);
			this.disableTypeField();
			this.view.wizard.changeTab(0);
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

		// abstract
		onRowSelected: function() {
			throw 'CMTasksFormBaseController: onRowSelected() unimplemented method';
		},

		// abstract
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
				params: {
					id: this.selectedId
				},
				scope: this,
				success: this.success,
				callback: this.callback
			});
		},

		resetIdField: function() {
			this.delegateStep[0].setValueId();
		},

		/**
		 * @param (Boolean) state
		 */
		setDisabledButtonNext: function(state) {
			this.view.nextButton.setDisabled(state);
		},

		success: function(result, options, decodedResult) {
			var me = this;
			var taskId = this.delegateStep[0].getValueId();

			this.parentDelegate.grid.store.load({
				callback: function() {
					me.view.reset();

					var rowIndex = this.find(
						CMDBuild.ServiceProxy.parameter.ID,
						(decodedResult.response) ? decodedResult.response : taskId
					);

					me.selectionModel.deselectAll();
					me.selectionModel.select(
						(rowIndex < 0) ? 0 : rowIndex,
						true
					);
				}
			});

			this.view.disableModify(true);
			this.view.wizard.changeTab(0);
		},

		/**
		 * Task validation
		 *
		 * @param (Boolean) enable
		 *
		 * @return (Boolean)
		 */
		validate: function(enable, type) {
			// Final check for invalid fields
			if (this.view.getNonValidFields().length > 0) {
				CMDBuild.Msg.error(CMDBuild.Translation.common.failure, CMDBuild.Translation.errors.invalid_fields, false);
				return false;
			}

			return true;
		}
	});

})();