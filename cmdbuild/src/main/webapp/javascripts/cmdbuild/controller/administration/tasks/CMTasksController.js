(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		tasksDatas: ['all', 'email', 'event', 'workflow'], // Used to check task existence
		taskType: undefined,

		// Overwrite
		constructor: function(view) {

			// Handlers exchange and controller setup
			this.view = view;
			this.grid = view.grid;
			this.form = view.form;
			this.view.delegate = this;
			this.grid.delegate = this;

			this.callParent(arguments);
		},

		onViewOnFront: function(parameters) {
			if (typeof parameters !== 'undefined') {
				var me = this;
				this.taskType = (this.correctTaskTypeCheck(parameters.internalId)) ? parameters.internalId : this.tasksDatas[0];

				this.grid.reconfigure(CMDBuild.core.serviceProxy.CMProxyTasks.getStore(this.taskType));
				this.grid.store.load({
					callback: function() {
						me.grid.getSelectionModel().select(0, true);
					}
				});
			}
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

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

		buildFormController: function(type) {
			if (this.correctTaskTypeCheck(type)) {
				this.form.delegate = Ext.create('CMDBuild.controller.administration.tasks.CMTasksForm' + this.capitaliseFirstLetter(type) + 'Controller');
				this.form.delegate.view = this.form;
				this.form.delegate.parentDelegate = this;
				this.form.delegate.selectionModel = this.grid.getSelectionModel();
			}
		},

		callback: function() {
			CMDBuild.LoadMask.get().hide();
		},

		capitaliseFirstLetter: function(string) {
			if (typeof string === 'string') {
				return string.charAt(0).toUpperCase() + string.slice(1);
			}

			return string;
		},

		correctTaskTypeCheck: function(type) {
			return (type != '' && (this.tasksDatas.indexOf(type) >= 0)) ? true : false;
		},

		loadForm: function(type) {
			if (this.correctTaskTypeCheck(type)) {
				this.form.wizard.removeAll();
				this.form.delegate.delegateStep = [];

				var items = Ext.create('CMDBuild.view.administration.tasks.' + type + '.CMTaskTabs').getTabs();

				for (var i = 0; i < items.length; i++) {

					// Controllers relation propagation
					items[i].delegate.parentDelegate = this.form.delegate;
					this.form.delegate.delegateStep.push(items[i].delegate);

					this.form.wizard.add(items[i]);
				}

				this.form.wizard.numberOfTabs = items.length;
				this.form.wizard.setActiveTab(0);
			}
		},

		success: function() {
			this.grid.store.load();
		},

		onAddButtonClick: function(name, param, callBack) {
			this.grid.getSelectionModel().deselectAll();
			this.buildFormController(param.type);

			return this.form.delegate.cmOn(name, param, callBack);
		},

		onRowSelected: function(name, param, callBack) {
			this.buildFormController(param.record.get(CMDBuild.ServiceProxy.parameter.TYPE));

			if (this.form.delegate)
				this.form.delegate.cmOn(name, param, callBack);
		},

		onStartButtonClick: function(record) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.serviceProxy.CMProxyTasks.start({
				scope: this,
				params: { id: record.get(CMDBuild.ServiceProxy.parameter.ID) },
				success: this.success,
				callback: this.callback
			});
		},

		onStopButtonClick: function(record) {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.serviceProxy.CMProxyTasks.stop({
				scope: this,
				params: { id: record.get(CMDBuild.ServiceProxy.parameter.ID) },
				success: this.success,
				callback: this.callback
			});
		}
	});

})();
