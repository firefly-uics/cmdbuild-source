Ext.define("CMDBuild.Administration.SchedulerUI", {
	extend: "Ext.Panel",
	alias: "schedulerpanel",
	
	translation: CMDBuild.Translation.administration.modWorkflow.scheduler,
	initComponent: function() {
		this.jobPanel = new CMDBuild.Administration.JobPanel({title: this.translation.job});
		this.jobParameter = new CMDBuild.Administration.JobParameterGrid({title: this.translation.params});
		this.jobGrid = new CMDBuild.Administration.JobGrid({
			region: 'north',
			height: '30%',
			split: true
		});

		this.saveButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.save
		});

		this.abortButton = new Ext.Button({
			text: CMDBuild.Translation.common.buttons.abort
		});
		
		Ext.apply(this, {
			layout: 'border',
			frame: false,
			border: false,
			items: [this.jobGrid, {
				xtype: 'tabpanel',
				activeTab: 0,
		    	region: 'center',
		    	frame: false,
		    	border: false,
		    	items: [this.jobPanel, this.jobParameter],
		    	buttonAlign: 'center',
		    	buttons: [this.saveButton, this.abortButton]
			}]
		});
		
	
		this.subscribe('cmdb-select-processclass', this.selectClass, this);
		this.jobGrid.on('jobSelected', this.onJobSelected, this);
		this.jobGrid.on('cmdb-empty-jobgrid', this.onEmptyGrid, this);
		this.jobGrid.on('newJob', this.onNewJob, this);
		this.jobPanel.on('deleteJob', this.deleteJob, this);
		this.jobPanel.on('modify', this.onModify, this);
		CMDBuild.Administration.SchedulerUI.superclass.initComponent.apply(this, arguments);
	},
	
	//private
	onModify: function() {
		this.saveButton.handler = this.modifyJob;
		this.enableButtons();
	},
	
	//private
	onAbort: function() {
		this.saveButton.handler = function() {};
		this.disableButtons();
		this.jobPanel.onAbort();
		this.jobParameter.setDisabled(!this.jobGrid.isSelected());
	},
	
	//private
	onEmptyGrid: function() {
		this.onAbort();
		this.jobPanel.disableActions();
		this.jobPanel.clearForm();
	},
	
	//private
	selectClass: function(eventParams) {
		if (eventParams && eventParams.id) {
			this.processType = eventParams.id;
			this.disableButtons();
			this.jobGrid.loadProcessJob(this.processType);
			this.jobPanel.onSelectClass();
			this.jobParameter.disable();
		}
	},
	
	//private
	onJobSelected: function(record){
		this.jobId = record.data.id;
		this.jobPanel.loadJob(record.data);
		this.jobParameter.loadJobParameter(record.data.params);
		this.jobParameter.enable();
	},
	
	//private
	onNewJob: function(){
		this.saveButton.handler = this.saveJob;
		this.jobPanel.newJob();
		this.jobParameter.removeAll();
		this.jobParameter.enable();
		this.enableButtons();
	},
	
	//private
	saveJob: function() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/scheduler/addprocessjob',
			params: {
				jobParameters: Ext.util.JSON.encode(this.jobParameter.getParametersAsMap()),
				idClass: this.processType,
				cronExpression: this.jobPanel.getCronExpression(),
				jobDescription: this.jobPanel.jobDescriptionField.getValue()
			},
			scope : this,
			success : this.success,
			important: true
		});
	},
	
	//private
	modifyJob: function() {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/scheduler/modifyjob',
			params: {
				jobParameters: Ext.util.JSON.encode(this.jobParameter.getParametersAsMap()),
				jobId: this.jobId,
				cronExpression: this.jobPanel.getCronExpression(),
				jobDescription: this.jobPanel.jobDescriptionField.getValue()
			},
			scope : this,
			success : this.success,
			important: true
		});
	},
	
	//private
	deleteJob: function(jobId) {
		CMDBuild.Ajax.request({
			url : 'services/json/schema/scheduler/deletejob',
			params: {
				jobId: jobId
			},
			scope : this,
			success : this.success,
			important: true
		});
	},
	
	//private
	success: function(response, options, decoded, jobDescription) {
		this.jobGrid.loadProcessJob(this.processType);
		this.jobPanel.clearForm();
		this.jobPanel.disableFields();
		this.disableButtons();
		this.jobParameter.disable();
	},
	
	//private
	enableButtons: function() {
		this.saveButton.enable();
		this.abortButton.enable();
	},
	
	//private
	disableButtons: function() {
		this.saveButton.disable();
		this.abortButton.disable();
	}
});