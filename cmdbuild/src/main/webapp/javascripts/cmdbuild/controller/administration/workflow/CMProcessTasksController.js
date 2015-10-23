(function() {

	Ext.define('CMDBuild.controller.administration.workflow.CMProcessTasksController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		currentProcess: undefined,
		currentProcessTaskId: undefined,
		grid: undefined,
		selectionModel: undefined,
		targetAccordion: undefined,
		targetController: undefined,
		view: undefined,

		/**
		 * @param (Object) view
		 */
		// Overwrite
		constructor: function(view) {
			// Handlers exchange
			this.view = view;
			this.grid = view.grid;
			this.view.delegate = this;
			this.grid.delegate = this;

			this.selectionModel = this.grid.getSelectionModel();
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(name, param, callBack);

				case 'onModifyButtonClick':
					return this.onModifyButtonClick(name, param, callBack);

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick(name, param, callBack);

				case 'onRowSelected':
					return this.onRowSelected();

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		onAddButtonClick: function(name, param, callBack) {
			// Security checks
				if (Ext.isEmpty(this.targetAccordion))
					this.targetAccordion = _CMMainViewportController.findAccordionByCMName('tasks');

				if (Ext.isEmpty(this.targetController))
					this.targetController = _CMMainViewportController.panelControllers['tasks'];

			this.targetAccordion.expand();

			Ext.Function.createDelayed(function() {
				this.targetAccordion.selectNodeById(param.type);

				Ext.Function.createDelayed(function() {
					this.targetController.cmOn(name, param, callBack);
					this.targetController.form.delegate.delegateStep[0].setValueWorkflowCombo(
						this.currentProcess.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					);
					this.targetController.form.delegate.onModifyButtonClick();
				}, 100, this)();
			}, 500, this)();
		},

		/**
		 * On grid row double click to select task in administration tasks form
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		onItemDoubleClick: function(name, param, callBack) {
			this.targetAccordion.expand();

			Ext.Function.createDelayed(function() {
				this.targetAccordion.selectNodeById(param.type);

				this.targetController.grid.getStore().load({
					scope: this,
					callback: function() {
						var selectionIndex = this.targetController.grid.getStore().find(CMDBuild.core.proxy.CMProxyConstants.ID, param.id);

						if (selectionIndex >= 0) {
							this.targetController.grid.getSelectionModel().select(
								selectionIndex,
								true
							);
						} else {
							CMDBuild.Msg.error(
								CMDBuild.Translation.common.failure,
								Ext.String.format('Cannot find taks with id ' + param.id + ' in store')
							);

							this.targetController.form.delegate.selectedId = null;
							this.targetController.form.disableModify();
						}
					}
				});
			}, 500, this)();
		},

		/**
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		onModifyButtonClick: function(name, param, callBack) {
			if (this.currentProcessTaskId) {
				param.id = this.currentProcessTaskId;

				this.onItemDoubleClick(null, param);

				Ext.Function.createDelayed(function() {
					if (this.targetController.form.delegate.selectedId != null)
						this.targetController.form.delegate.onModifyButtonClick();
				}, 1000, this)();
			}
		},

		/**
		 * @param (Int) processId
		 * @param (Object) process
		 */
		onProcessSelected: function(processId, process) {
			this.currentProcess = process;

			if (!process || process.get('superclass')) {
				this.view.disable();
			} else {
				this.view.enable();

				this.grid.reconfigure(CMDBuild.core.proxy.CMProxyTasks.getStoreByWorkflow());
				this.grid.store.load({
					params: {
						workflowClassName: process.get(CMDBuild.core.proxy.CMProxyConstants.NAME)
					}
				});
			}
		},

		/**
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		onRemoveButtonClick: function(name, param, callBack) {
			if (this.currentProcessTaskId) {
				param.id = this.currentProcessTaskId;

				this.onItemDoubleClick(null, param);

				Ext.Function.createDelayed(function() {
					if (this.targetController.form.delegate.selectedId != null)
						this.targetController.form.delegate.onRemoveButtonClick();
				}, 1000, this)();
			}
		},

		onRowSelected: function() {
			if (this.selectionModel.hasSelection()) {
				this.currentProcessTaskId = this.selectionModel.getSelection()[0].get(CMDBuild.core.proxy.CMProxyConstants.ID);
				this.view.enableCMTbar();

				// This declaration positioned in constructor doesn't works for targetAccordion
				this.targetAccordion = _CMMainViewportController.findAccordionByCMName('tasks');
				this.targetController = _CMMainViewportController.panelControllers['tasks'];
			}
		}
	});

})();