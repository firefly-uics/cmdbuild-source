(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		parentDelegate: undefined,
		tasksDatas: undefined,

		// Overwrite
		constructor: function(view) {
			var me = this;

			this.tasksDatas = ['email', 'event', 'workflow']; // Used to check task existence

			// Handlers exchange + controller setup
			this.grid = view.grid;
			this.form = view.form;
			this.view = view;
			this.view.delegate = this;

			this.form.delegate = Ext.create('CMDBuild.controller.administration.tasks.CMTasksFormController');
			this.form.delegate.view = this.form;
			this.form.delegate.parentDelegate = this;
			this.form.delegate.selectionModel = this.grid.getSelectionModel();

			this.grid.delegate = this;

			this.callParent(arguments);
		},

		initComponent: function() {
			this.callParent(arguments);
		},

		// Overwrite
		onViewOnFront: function(parameters) {
			if (parameters) {
				this.cmOn('onGridLoad', { 'type': parameters.data.type });
			}
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick':
					return this.onAddButtonClick(name, param, callBack);

				case 'onGridLoad':
					return this.grid.load(param.type);

				case 'onRowSelected':
					return this.form.delegate.cmOn(name, param, callBack);

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

		onAddButtonClick: function(name, param, callBack) {
			this.grid.getSelectionModel().deselectAll();
			return this.form.delegate.cmOn(name, param, callBack);
		}
	});

})();