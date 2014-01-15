(function() {
//	var activePanel;
//	var tr = CMDBuild.Translation.administration.modreport.importJRFormStep2;

	Ext.define("CMDBuild.controller.administration.tasks.CMTasksGridAndPanelController", {
		extend: "CMDBuild.controller.CMBasePanelController",
		constructor: function() {
			this.callParent(arguments);
			
			this.currentTask = null;
			this.currentTaskType = null;
			
			this.grid = this.view.grid;
			this.gridSM = this.grid.getSelectionModel();
			this.form = this.view.form;
			
			this.gridSM.on("selectionchange", onSelectionChange, this);
			this.view.addButton.on("click", onAddTaskClick, this);
			this.form.modifyButton.on("click", onModifyButtonClick, this);
			this.form.removeButton.on("click", onDeleteButtonClick, this);
			this.form.saveButton.on("click", onSaveButtonClick, this);
			this.form.abortButton.on("click", onAbortButtonClick, this);
			
//			activePanel = this.form.step1.id;
		},

		onViewOnFront: function(type) {
			this.currentTaskType = type;
//			this.view.onTaskTypeSelected(type);
		}

	});
	
	function onSelectionChange(selection) {
		if (selection.selected.length > 0) {
			activePanel = this.form.step1.id;
			this.currentTask = selection.selected.items[0];
			this.form.onTaskSelected(this.currentTask);
		}
	}
	
	function onAddTaskClick() {
		alert("adding a task");
		this.gridSM.deselectAll();
		this.form.step1.fileField.allowBlank = false;
		this.currentTask = null;
		this.form.reset();
		this.form.enableModify(all = true);
	}
	
	function onModifyButtonClick() {
		alert("modifying a task");
		this.form.step1.fileField.allowBlank = true;
		this.form.enableModify();
	}
	
	function onSaveButtonClick() {
		alert("saving a task");
//		if (activePanel == this.form.step1.id) {
//			analizeReport.call(this);
//		} else {
//			insertJasperReport.call(this);
//		}
	}
	
	function onAbortButtonClick() {
		alert("cancel modifying a task");
		this.form.disableModify();
		this.form.reset();
		if (this.currentTask != null) {
			this.form.onTaskSelected(this.currentTask);
		}
	}
	
	function onDeleteButtonClick() {
		Ext.Msg.show({
			title: "@@ Remove task",
			msg: CMDBuild.Translation.common.confirmpopup.areyousure,
			scope: this,
			buttons: Ext.Msg.YESNO,
			fn: function(button){
				if (button == 'yes'){
//					deleteReport.call(this);
				}
			}
		});
	}

//	function deleteReport() {
//		CMDBuild.LoadMask.get().show();
//		CMDBuild.Ajax.request({
//			url : 'services/json/schema/modreport/deletereport',
//			params : {
//				"id": this.currentReport.get("id")
//			},
//			method : 'POST',
//			scope : this,
//			success : successCB,
//			callback: function() {
//				CMDBuild.LoadMask.get().hide(); 
//			}
//		});
//	}
})();