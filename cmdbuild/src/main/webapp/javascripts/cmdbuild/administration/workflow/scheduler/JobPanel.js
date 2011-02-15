CMDBuild.Administration.JobPanel = Ext.extend(Ext.Panel, {
	jobId : undefined,
	translation : CMDBuild.Translation.administration.modWorkflow.scheduler,

	initComponent : function() {
		
		this.modifyJobAction = new Ext.Action( {
			iconCls : 'modify',
			text : this.translation.modifyJob,
			scope : this,
			handler : this.onModify
		});

		this.deleteJobAction = new Ext.Action( {
			iconCls : 'delete',
			text : this.translation.deleteJob,
			scope : this,
			handler : function() {
				this.fireEvent('deleteJob', this.jobId);
			}
		});

		this.jobDescriptionField = new Ext.form.TextField( {
			fieldLabel : this.translation.description,
			name : 'description'
		});

		this.base = new Ext.form.ComboBox( {
			hiddenName : 'base',
			labelSeparator : '',
			store : new Ext.data.SimpleStore( {
				fields : [ 'value', 'description' ],
				data : [ [ '0 * * * ?', this.translation.everyHour ],
						 [ '0 0 * * ?', this.translation.everyDay ],
						 [ '0 0 1 * ?', this.translation.everyMounth ],
						 [ '0 0 1 1 ?', this.translation.everyYear ] ]
			}),
			valueField : 'value',
			displayField : 'description',
			typeAhead : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true
		});

		this.base.on('select', function(combo, record, index) {
			this.setValueOfAdvanced(record.data.value);
		}, this);

		this.cronExpressionField = new Ext.form.TextField( {
			fieldLabel : this.translation.cronexpression,
			name : 'criteria'
		});
		
		this.buildAdvancedFields();		
		
		this.descriptionPanel = new Ext.form.FieldSet( {			
			defaultType : 'textfield',
			layout : 'form',
			autoHeight : true,
			labelWidth : 150,
			border : false,
			items : [ this.jobDescriptionField ]
		});
		
		this.advanceRadio = new Ext.form.Radio({
			name : 'input_type',
			inputValue : 'advance',
			boxLabel : this.translation.advanced,
			width : 150,
			labelSeparator : ''
		});
		
		this.advanceRadio.on('check', function(radio, value){
			if (this.editMode) {
				this.setDisabledAdvancedFields(!value);
			}
		},this); 
		
		this.advance = new Ext.form.FieldSet({			
			defaultType : 'textfield',
			layout : 'table',
			layoutConfig : {columns : 2},
			autoHeight : true,
			frame: true,
			items : [{
				xtype : 'panel',
				layout : 'form',
				labelWidth : 1,
				items : [this.advanceRadio]
			},{
				xtype : 'panel',
				layout : 'form',
				labelWidth : 170,
				items : this.advancedFields
			}]
		});

		this.baseRadio = new Ext.form.Radio({
			name : 'input_type',
			inputValue : 'advance',
			boxLabel : this.translation.basic,
			labelSeparator : '',
			width : 150,
			checked : true
		});
		
		this.baseRadio.on('check', function(radio, value) {
			if (this.editMode) {
				this.base.setDisabled(!value);
			}
		},this); 
		
		this.basePanel = new Ext.form.FieldSet({
			defaultType : 'textfield',
			layout : 'table',
			layoutConfig : {columns : 2},
			frame: true,
			items : [ {
				xtype : 'panel',
				layout : 'form',
				labelWidth : 1,
				items : [this.baseRadio]
			},{
				xtype : 'panel',
				layout : 'form',
				labelWidth : 1,
				items : [this.base]
			}]
		});
		
		Ext.apply(this, {
			region : 'center',
			layout: 'fit',
			tbar : [ this.modifyJobAction, this.deleteJobAction ],
			frame : false,
			border : false,
			items : [ {
				xtype : 'panel',
				layout : 'form',
				autoScroll : true,
				border : false,
				frame : true,
				style: {padding: '5px', background: CMDBuild.Constants.colors.gray.background},
				items : [this.descriptionPanel, this.basePanel, this.advance]
			} ]
		});
		CMDBuild.Administration.JobPanel.superclass.initComponent.apply(this, arguments);
	},
	
	newJob: function() {
		this.editMode = true;
		this.baseExpression = true;
		this.currentJob = undefined;
		this.clearForm();
		this.enableFields();
		this.disableActions();
	},

	onAbort: function() {
		this.editMode = false;
		this.disableFields();
		this.reset();
		if (this.currenJob) {
			this.enableActions();
		}
	},
	
	onSelectClass: function() {
		this.disableAll();
		this.clearForm();
	},
	
	// private
	onModify : function() {
		this.editMode = true;
		this.enableFields();
		this.disableActions();
		this.fireEvent('modify');
	},
	
	//private
	loadJob : function(job) {
		this.currentJob = job;
		this.baseExpression = false; //flag to idenfity an expression in base form
		this.jobId = job.id;
		this.jobDescriptionField.setValue(job.description);
		this.cronExpressionField.setValue(job.cronExpression);
		this.setValueOfBaseIfPossible(job.cronExpression);
		this.setValueOfAdvanced(job.cronExpression);
		this.enableActions();
	},

	// private
	setValueOfBaseIfPossible : function(value) {
		var index = this.base.store.find('value', value);
		if (index > -1) {
			this.base.setValue(value);			
		} else {
			this.base.setValue('');
		}
		this.baseExpression = index > -1;
	},
	
	// private
	buildAdvancedFields: function() {
		this.advancedFields = [
   			this.minutes = new CMDBuild.CronTriggerField( {
   				fieldLabel : this.translation.minute
   			}),
   			this.hour = new CMDBuild.CronTriggerField( {
   				fieldLabel : this.translation.hour
   			}),
   			this.dayOfMounth = new CMDBuild.CronTriggerField( {
   				fieldLabel : this.translation.dayOfMounth
   			}),
   			this.mount = new CMDBuild.CronTriggerField( {
   				fieldLabel : this.translation.mounth
   			}),
   			this.dayOfWeek = new CMDBuild.CronTriggerField( {
   				fieldLabel : this.translation.dayOfWeek
   			})
   		];
   		this.addListenerToAdvancedFields();
	},

	setValueOfAdvanced: function(cronExpression) {
		var values = cronExpression.split(" ");
		var fields = this.advancedFields;
		for (var i=0, len=fields.length; i<len; i++) {
			var field = fields[i];
			if (values[i]) {
				field.setValue(values[i]);
			}
		}
	},
	
	getCronExpression: function() {
		var expression = "";
		var fields = this.advancedFields;
		for (var i=0, len=fields.length-1; i<len; i++) {
			var field = fields[i];
			expression += field.getValue()+" ";
		}
		expression += fields[fields.length -1].getValue();
		return expression;
	},
	
	addListenerToAdvancedFields: function() {
		var fields = this.advancedFields;
		for (var i=0, len=fields.length; i<len; i++) {
			var field = fields[i];
			field.on('change', function(field, newValue, oldValue){
				this.setValueOfBaseIfPossible(this.getCronExpression());
			}, this);
		}
	},
	
	setDisabledAdvancedFields: function(enable) {
		var fields = this.advancedFields;
		for (var i=0, len=fields.length; i<len; i++) {
			fields[i].setDisabled(enable);
		}
	},
	
	disableAll : function() {
		this.disableActions();
		this.disableFields();
	},
	
	enableActions : function() {
		this.modifyJobAction.enable();
		this.deleteJobAction.enable();
	},

	disableActions : function() {
		this.modifyJobAction.disable();
		this.deleteJobAction.disable();
	},
	
	disableFields : function() {
		this.jobDescriptionField.disable();
		this.baseRadio.disable();
		this.advanceRadio.disable();
		this.base.disable();
		var fields = this.advancedFields;
		for (var i=0, len=fields.length; i<len; i++) {
			fields[i].disable();
		}
	},

	enableFields : function() {
		this.jobDescriptionField.enable();
		this.baseRadio.enable();
		this.advanceRadio.enable();
		if (this.baseExpression) {
			this.baseRadio.setValue(true);
			this.advanceRadio.setValue(false);
			this.base.enable();
		} else {
			this.advanceRadio.setValue(true);
			this.baseRadio.setValue(false);
			this.setDisabledAdvancedFields(false);
		}
	},
	
	clearForm : function() {
		this.jobDescriptionField.setValue('');
		this.base.reset();
		this.cronExpressionField.setValue('');
		var fields = this.advancedFields;
		for (var i=0, len=fields.length; i<len; i++) {
			fields[i].setValue('');
		}
	},
	
	reset : function() {
		this.clearForm();
		if (this.currentJob) {
			this.loadJob(this.currentJob);
		}
	}
});

CMDBuild.CronTriggerField = Ext.extend(Ext.form.TriggerField, {
	triggerClass : 'trigger-edit',
	onTriggerClick : function() {
		if (!this.disabled) {
			new CMDBuild.CronEditWindow({
				title : this.fieldLabel,
				parentField: this
			}).show();
		}
	}
});