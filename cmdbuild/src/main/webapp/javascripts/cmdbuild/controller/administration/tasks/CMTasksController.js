(function() {

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksController", {
		extend: "CMDBuild.controller.CMBasePanelController",

		parentDelegate: undefined,

		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.view.form.delegate = Ext.create('CMDBuild.controller.administration.tasks.CMTasksFormController');
			this.view.form.delegate.view = this.view.form;
			this.view.form.delegate.parentDelegate = this;
			this.view.grid.delegate = this;

			this.callParent(arguments);
		},

		initComponent: function() {
			this.callParent(arguments);
		},

		onViewOnFront: function(p) {
			if (p) {
				this.view.grid.getSelectionModel().deselectAll(0);
				this.cmOn('onClearForm', {});
				this.cmOn('onLoadGrid', { 'type': p.data.type });
			}
		},

		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onAddButtonClick': {
					this.view.grid.getSelectionModel().deselectAll();
					return this.view.form.delegate.cmOn(name, param, callBack);
				}

				case 'onClearForm':
					return this.view.form.delegate.cmOn(name, param, callBack);

				case 'onLoadGrid':
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
		}
	});

})();