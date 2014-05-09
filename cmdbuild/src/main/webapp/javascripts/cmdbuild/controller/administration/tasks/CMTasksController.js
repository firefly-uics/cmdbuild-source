(function() {

	Ext.require('CMDBuild.core.proxy.CMProxyTasks');

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksController', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		parentDelegate: undefined,

		form: undefined,
		grid: undefined,
		selectionModel: undefined,
		tasksDatas: [ // Used to validate tasks
			'all',
			'connector',
			'email',
			'event',
			'event_asynchronous',
			'event_synchronous',
			'workflow'
		],
		view: undefined,

		// overwrite
		constructor: function(view) {
			// Handlers exchange and controller setup
			this.view = view;
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;

			this.selectionModel = this.grid.getSelectionModel();

			this.callParent(arguments);
		},

		// overwrite
		onViewOnFront: function(parameters) {
			if (!Ext.isEmpty(parameters)) {
				this.taskType = (this.correctTaskTypeCheck(parameters.internalId)) ? parameters.internalId : this.tasksDatas[0];

				this.grid.reconfigure(CMDBuild.core.proxy.CMProxyTasks.getStore(this.taskType));
				this.grid.store.load({
					scope: this,
					callback: function() {
						if (!this.selectionModel.hasSelection())
							this.selectionModel.select(0, true);
					}
				});

				// Fire show event on accordion click
				this.view.fireEvent('show');
			}

			this.callParent(arguments);
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
					return this.onItemDoubleClick();

				case 'onNextButtonClick':
					return this.form.wizard.changeTab(+1);

				case 'onPreviousButtonClick':
					return this.form.wizard.changeTab(-1);

				case 'onRowSelected':
					return this.onRowSelected(name, param, callBack);

				case 'onStartButtonClick':
					return this.onStartButtonClick(param);

				case 'onStopButtonClick':
					return this.onStopButtonClick(param);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Automated form controller constructor
		 *
		 * @param (String) type
		 */
		buildFormController: function(type) {
			if (this.correctTaskTypeCheck(type)) {
				this.form.delegate = Ext.create(
					'CMDBuild.controller.administration.tasks.CMTasksForm' + this.capitaliseFirstLetter(this.typeSerialize(type, 1)) + 'Controller',
					this.form
				);
				this.form.delegate.parentDelegate = this;
				this.form.delegate.selectionModel = this.selectionModel;
			}
		},

		/**
		 * @param (String) string
		 * @return (String) string - capitalized string
		 */
		capitaliseFirstLetter: function(string) {
			if (typeof string == 'string') {
				return string.charAt(0).toUpperCase() + string.slice(1);
			}

			return string;
		},

		// overwrite
		callback: function() {
			this.grid.store.load();

			this.callParent(arguments);
		},

		/**
		 * @param (String) type - form type identifier
		 *
		 * @return (Boolean) type recognition state
		 */
		correctTaskTypeCheck: function(type) {
			return (type != '' && (this.tasksDatas.indexOf(type) >= 0)) ? true : false;
		},

		/**
		 * Form wizard creator
		 *
		 * @param (String) type - form type identifier
		 */
		loadForm: function(type) {
			if (this.correctTaskTypeCheck(type)) {

				// Clear all old tabs listeners
				this.form.wizard.items.each(function(item) {
					item.clearListeners();
				});

				this.form.wizard.removeAll();
				this.form.delegate.delegateStep = [];

				var items = Ext.create('CMDBuild.view.administration.tasks.' + this.typeSerialize(type, 0) + '.CMTaskTabs').getTabs();

				for (key in items) {
					items[key].delegate.parentDelegate = this.form.delegate; // Controller relations propagation

					this.form.delegate.delegateStep.push(items[key].delegate);
					this.form.wizard.add(items[key]);
				}

				this.form.wizard.numberOfTabs = items.length;
				this.form.wizard.changeTab(0);
			}
		},

		onAddButtonClick: function(name, param, callBack) {
			this.selectionModel.deselectAll();
			this.buildFormController(param.type);

			return this.form.delegate.cmOn(name, param, callBack);
		},

		onItemDoubleClick: function() {
			return this.form.delegate.onModifyButtonClick();
		},

		/**
		 * Check for a right form controller and/or creates it and then calls delegate's onRowSelected function
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		onRowSelected: function(name, param, callBack) {
			var selectedType = this.selectionModel.getSelection()[0].get(CMDBuild.ServiceProxy.parameter.TYPE);

			if (
				!this.form.delegate
				|| (this.form.delegate.taskType != selectedType)
			) {
				this.buildFormController(selectedType);
			}

			if (this.form.delegate)
				this.form.delegate.cmOn(name, param, callBack);
		},

		/**
		 * @param (Object) record
		 */
		onStartButtonClick: function(record) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.CMProxyTasks.start({
				scope: this,
				params: {
					id: record.get(CMDBuild.ServiceProxy.parameter.ID)
				},
				success: this.success(record.get(CMDBuild.ServiceProxy.parameter.ID)),
				callback: this.callback
			});
		},

		/**
		 * @param (Object) record
		 */
		onStopButtonClick: function(record) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.CMProxyTasks.stop({
				scope: this,
				params: {
					id: record.get(CMDBuild.ServiceProxy.parameter.ID)
				},
				success: this.success(record.get(CMDBuild.ServiceProxy.parameter.ID)),
				callback: this.callback
			});
		},

		/**
		 * @param (Int) id
		 */
		success: function(id) {
			var me = this;

			this.grid.store.load({
				callback: function() {
					me.form.reset();

					var rowIndex = this.find(CMDBuild.ServiceProxy.parameter.ID, id);

					me.selectionModel.deselectAll();
					me.selectionModel.select(
						(rowIndex < 0) ? 0 : rowIndex,
						true
					);
				}
			});
		},

		/**
		 * Function to serialize type and return as class path string (without header and footer dots)
		 *
		 * @param (String) type
		 * @param (Imt) itemsToReturn
		 * @return (String) class path string for type
		 */
		typeSerialize: function(type, itemsToReturn) {
			var splittedType = type.split('_');
			var returnString;

			if (
				splittedType.length > 1
				&& typeof itemsToReturn == 'number'
				&& itemsToReturn > 0
				&& itemsToReturn <= splittedType.length
			) {
				splittedType = splittedType.slice(0, itemsToReturn);
			} else {
				splittedType = splittedType.slice(0, splittedType.length);
			}

			return splittedType.join('.');
		}
	});

})();
