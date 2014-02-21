(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		parentDelegate: undefined,
		tasksDatas: undefined,

		// Overwrite
		constructor: function(view) {
			var me = this;

			this.tasksDatas = ['email', 'event', 'workflow']; // Used to check task exiting
			this.view = view;
			this.view.delegate = this;

			this.view.form.delegate = Ext.create('CMDBuild.controller.administration.tasks.CMTasksFormController');
			this.view.form.delegate.view = this.view.form;
			this.view.form.delegate.parentDelegate = this;
			this.view.form.delegate.selectionModel = this.view.grid.getSelectionModel();

			this.view.grid.delegate = this;

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
					return this.view.grid.load(param.type);

				case 'onRowSelected':
					return this.view.form.delegate.cmOn(name, param, callBack);

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
			this.view.grid.getSelectionModel().deselectAll();
			return this.view.form.delegate.cmOn(name, param, callBack);
		}
	});

})();