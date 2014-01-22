(function() {
//	var activePanel;
//	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep2;

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksGridAndPanelController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function(view) {
			this.view = view;
			this.view.delegate = this;
			this.view.taskForm.delegate = new CMDBuild.controller.administration.tasks.CMTasksPanelController();
			this.view.taskForm.delegate.view = this.view.taskForm;
			this.view.taskForm.delegate.parentDelegate = this;
			this.view.taskGrid.delegate = this;
			this.callParent(arguments);
		},
		// override
		onViewOnFront: function(p) {
			if (p) {
				CMDBuild.log.info("onPanelActivate " + this.view.title, this, p);
				this.view.taskGrid.getSelectionModel().deselectAll(0);
				this.cmOn("onClearForm", {});
				this.cmOn("onLoadGrid", {"type": p.data.type});
			}
		},
		initComponent: function() {
			this.callParent(arguments);
		},
		cmOn: function(name, param, callBack) {
			switch (name) {
				case "onAddButtonClick" :
					this.view.taskGrid.getSelectionModel().deselectAll();
					return this.view.taskForm.delegate.cmOn(name, param, callBack);
				case "onClearForm" :
					return this.view.taskForm.delegate.cmOn(name, param, callBack);
				case "onRowSelected" :
					return this.view.taskForm.delegate.cmOn(name, param, callBack);
				case "onStartTask" :
					return alert(name + " id = " + param.record.id);
				case "onLoadGrid" :
					return this.view.taskGrid.load(param.type);
				case "onStopTask" :
					return alert(name + " id = " + param.record.id);
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		}
	});

	/**
	 * @param {xx} xx
	 * Description
	 */
	function onRowSelected(sm, selection, record) {
		if (selection.length > 0) {
			this.currentTask = selection[0];
			this.form.onTaskSelected(this.currentTask);
		}
	}
})();