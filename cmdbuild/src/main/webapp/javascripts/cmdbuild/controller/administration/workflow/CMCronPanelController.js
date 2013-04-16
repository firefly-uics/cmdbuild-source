(function() {

	Ext.define("CMDBuild.controller.administration.workflow.CMCronPanelController", {
		constructor: function(view) {
			this.currentProcessId = null;
			this.currentJob = null;

			this.view = view;
			this.jobGrid = view.jobGrid;
			this.jobGridSM = view.jobGrid.getSelectionModel();
			this.jobPanel = view.jobPanel;
			this.jobParemetersGrid = view.jobParemetersGrid;

			this.jobGridSM.on('selectionchange', onJobGridSelected, this);
			this.jobPanel.saveButton.on("click", onSaveButtonClick, this);
			this.jobPanel.abortButton.on("click", onAbortButtonClick, this);
			this.jobPanel.modifyJobButton.on("click", onModifyJobClick, this);
			this.jobPanel.deleteJobButton.on("click", onDeleteJobClick, this);
			this.jobGrid.addJobButton.on("click", onAddJobButtonClick, this);
		},

		onProcessSelected: function(processId, process) {
			this.currentProcessId = processId;
			if (!process || process.get("superclass")) {
				this.view.disable();
			} else {
				this.view.enable();
				this.jobGrid.load(processId);
				this.jobPanel.disableModify();
				this.jobParemetersGrid.addParameterButton.disable();
			}
		},

		onAddClassButtonClick: function() {
			this.currentProcessId = null;
			this.currentJob = null;

			this.view.disable();
		}

	});

	function onJobGridSelected(sm, selection) {
		if (selection.length > 0) {
			this.currentJob = selection[0];

			this.jobPanel.onJobSelected(this.currentJob);
			this.jobParemetersGrid.onJobSelected(this.currentJob.get("params"));
		}
	}

	function onAddJobButtonClick() {
		this.currentJob = null;

		this.jobGridSM.deselectAll();
		this.jobPanel.onAddJobButtonClick();
		this.jobParemetersGrid.onAddJobButtonClick();
	}

	function onModifyJobClick() {
		this.jobPanel.enableModify();
		this.jobParemetersGrid.addParameterButton.enable();
	}
	
	function onDeleteJobClick() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/scheduler/deletejob',
			params: {
				jobId: this.currentJob.get("id")
			},
			scope : this,
			success : successCB,
			important: true
		});
	}
	
	function onSaveButtonClick() {
		if (this.currentJob == null) {
			var params = {
					jobParameters: Ext.JSON.encode(this.jobParemetersGrid.getParametersAsMap()),
					cronExpression: this.jobPanel.getCronExpression(),
					jobDescription: this.jobPanel.jobDescriptionField.getValue()
			};
			params[CMDBuild.ServiceProxy.parameter.CLASS_NAME] = _CMCache.getEntryTypeNameById(this.currentProcessId);

			CMDBuild.Ajax.request({
				url : 'services/json/schema/scheduler/addprocessjob',
				params: params,
				scope : this,
				success : successCB,
				important: true
			});
		} else {
			CMDBuild.Ajax.request({
				url : 'services/json/schema/scheduler/modifyjob',
				params: {
					jobParameters: Ext.JSON.encode(this.jobParemetersGrid.getParametersAsMap()),
					jobId: this.currentJob.get("id"),
					cronExpression: this.jobPanel.getCronExpression(),
					jobDescription: this.jobPanel.jobDescriptionField.getValue()
				},
				scope : this,
				success : successCB,
				important: true
			});
		}
	}

	function onAbortButtonClick() {
		this.jobPanel.disableModify();
		this.jobPanel.reset();
		if (this.currentJob != null) {
			this.jobPanel.onJobSelected(this.currentJob)
		}
		this.jobParemetersGrid.addParameterButton.disable();
		this.jobParemetersGrid.removeAll();
	}

	function successCB() {
		this.jobGrid.load(this.currentProcessId);
		this.jobPanel.disableModify();
		this.jobPanel.reset();
		this.jobParemetersGrid.addParameterButton.disable();
		this.jobParemetersGrid.removeAll();
	}
})();
	