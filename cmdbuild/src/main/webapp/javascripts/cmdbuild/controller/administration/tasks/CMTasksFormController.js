(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksFormController", {

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
			this.view.disableModify(true);

			return this.view.wizard.changeTab(0);

//			if (this.selectedName != null) {
//				this.onRowSelected();
//			} else {
//				this.form.reset();
//				this.form.disableModify();
//			}
		},

		onAddButtonClick: function(param) {
			this.isNew = true;
			this.loadForm(param.type, -1);
			this.view.reset();
			this.view.enableTabbedModify(false);
			this.view.disableTypeField();

			return this.view.wizard.changeTab(0);

//			this.selectionModel.deselectAll();
//			this.selectedName = null;
//			this.form.reset();
//			this.form.enableModify(true);
		},

		onCloneButtonClick: function() {
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
			this.view.disableModify(true);

			return this.view.wizard.changeTab(0);

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
			this.loadForm(param.record.getData().type);
			this.view.disableModify(true);

			return this.view.wizard.changeTab(0);

// Just switch when serverside will be completed2
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
			alert(this.isNew);
			this.view.disableModify(true);

			return this.view.wizard.changeTab(0);

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
//		},

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