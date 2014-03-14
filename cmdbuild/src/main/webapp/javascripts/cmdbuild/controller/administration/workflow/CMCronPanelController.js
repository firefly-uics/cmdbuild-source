(function() {

	Ext.define('CMDBuild.controller.administration.workflow.CMCronPanelController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		currentProcessId: undefined,

		// Overwrite
		constructor: function(view) {

			// Handlers exchange
			this.view = view;
			this.grid = view.grid;
			this.view.delegate = this;
			this.grid.delegate = this;
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onItemDoubleClick':
					return this.onItemDoubleClick(param);

				case 'onModifyButtonClick':
					return this.onModifyButtonClick(name, param, callBack);

				case 'onRemoveButtonClick':
					return this.onRemoveButtonClick(name, param, callBack);

				case 'onRowSelected':
					return this.onRowSelected(name, param, callBack);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onAddButtonClick: function() {
			_debug('onAddButtonClick');
		},

		onItemDoubleClick: function(itemId) {
			var domainAccordion = _CMMainViewportController.findAccordionByCMName('tasks');

			domainAccordion.expand();

			Ext.Function.createDelayed(function() {
				domainAccordion.selectNodeById(record.get('workflow'));
			}, 100)();

		},

		onModifyButtonClick: function() {_debug('onModifyButtonClick');
			if (this.currentProcessId) {
				this.onItemDoubleClick(this.currentProcessId);

				Ext.Function.createDelayed(function() {
					_CMMainViewportController.panelControllers['tasks'].view.form.delegate.onModifyButtonClick();
				}, 500)();
			}
		},

		onProcessSelected: function(processId, process) {
			var me = this;
			this.currentProcessId = processId;

			if (!process || process.get('superclass')) {
				this.view.disable();
			} else {
				this.view.enable();

				// TODO: reconfigura on server side implementation
				this.grid.reconfigure(CMDBuild.core.serviceProxy.CMProxyTasks.getStoreByWorkflow());
//				this.grid.store.load({
//					params: { id: processId },
//					callback: function() {
//						_debug('store loaded');
//						me.grid.getSelectionModel().select(0, true);
//					}
//				});
			}
		},

		onRemoveButtonClick: function() {
			_debug('onRemoveButtonClick');
		},

		onRowSelected: function() {_debug('onRowSelected');
//			this.currentProcessId = null; // TODO: to complete
			this.view.enableCMTbar();
		}

	});

})();