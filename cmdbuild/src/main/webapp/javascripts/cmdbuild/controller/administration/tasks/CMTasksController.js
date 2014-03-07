(function() {

	Ext.define('CMDBuild.controller.administration.tasks.CMTasksController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		tasksDatas: undefined,

		// Overwrite
		constructor: function(view) {
			this.tasksDatas = ['all', 'email', 'event', 'workflow']; // Used to check task existence

			// Handlers exchange + controller setup
			this.grid = view.grid;
			this.form = view.form;
			this.view = view;
			this.view.delegate = this;
			this.grid.delegate = this;

			this.callParent(arguments);
		},

		initComponent: function() {
			this.callParent(arguments);
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onRowSelected':
					return this.onRowSelected(name, param, callBack);

				case 'onStartTask':
					return alert(name + ' id = ' + param.record.id);

				case 'onStopTask':
					return alert(name + ' id = ' + param.record.id);

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		buildFormController: function(type) {
			if (this.tasksDatas.indexOf(type) >= 0) {
				this.form.delegate = Ext.create('CMDBuild.controller.administration.tasks.CMTasksForm' + this.capitaliseFirstLetter(type) + 'Controller');
				this.form.delegate.view = this.form;
				this.form.delegate.parentDelegate = this;
				this.form.delegate.selectionModel = this.grid.getSelectionModel();
			}
		},

		capitaliseFirstLetter: function(string) {
			if (typeof string == 'string') {
				return string.charAt(0).toUpperCase() + string.slice(1);
			}

			return string;
		},

		loadForm: function(type) {
			if (this.tasksDatas.indexOf(type) >= 0) {
				this.form.wizard.removeAll();
				var items = Ext.create('CMDBuild.view.administration.tasks.' + type + '.CMTaskTabs').getTabs();

				for (var i = 0; i < items.length; i++) {
					this.form.wizard.add(items[i]);
				}
_debug(this.form.wizard);
				this.form.wizard.numberOfTabs = items.length;
				this.form.wizard.setActiveTab(0);
			}
		},

		onAddButtonClick: function(name, param, callBack) {
			this.grid.getSelectionModel().deselectAll();
			this.buildFormController(param.type);
			return this.form.delegate.cmOn(name, param, callBack);
		},

		onRowSelected: function(name, param, callBack) {
			this.buildFormController(param.record.data.type);

			if (this.form.delegate)
				this.form.delegate.cmOn(name, param, callBack);
		}
	});

})();
