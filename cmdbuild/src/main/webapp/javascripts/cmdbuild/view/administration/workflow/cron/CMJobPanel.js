Ext.define("CMDBuild.view.administration.workflow.cron.CMJobPanel", {
	extend: "Ext.form.Panel",
	mixins: {
		cmFormFunctions: "CMDBUild.view.common.CMFormFunctions"
	},
	jobId : undefined,
	translation : CMDBuild.Translation.administration.modWorkflow.scheduler,

	initComponent : function() {
		this.cmTBar = [
			this.modifyJobButton = new Ext.button.Button( {
				iconCls : 'modify',
				text : this.translation.modifyJob
			}),
		
			this.deleteJobButton = new Ext.button.Button( {
				iconCls : 'delete',
				text : this.translation.deleteJob
			})
		];

		this.cmButtons = [
			this.saveButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.save
			}),
	
			this.abortButton = new Ext.button.Button({
				text: CMDBuild.Translation.common.buttons.abort
			})
		];

		this.jobDescriptionField = new Ext.form.TextField( {
			fieldLabel : this.translation.description,
			name : 'description'
		});

		this.base = new Ext.form.ComboBox( {
			name: "base",
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
			queryMode : 'local',
			selectOnFocus : true,
			margins: "0 0 0 105"
		});

		this.base.on('select', function(combo, record, index) {
			this.setValueOfAdvanced(record[0].data.value);
		}, this);

		this.cronExpressionField = new Ext.form.TextField( {
			fieldLabel : this.translation.cronexpression,
			name : 'criteria'
		});
		
		this.buildAdvancedFields();

		this.advanceRadio = new Ext.form.Radio({
			name : 'input_type',
			inputValue : 'advance',
			boxLabel : this.translation.advanced,
			width : 150,
			labelSeparator : ''
		});
		
		this.advanceRadio.on('change', function(radio, value) {
			if (this.editing) {
				this.setDisabledAdvancedFields(!value);
			}
		},this);

		this.baseRadio = new Ext.form.Radio({
			name : 'input_type',
			inputValue : 'advance',
			boxLabel : this.translation.basic,
			labelSeparator : '',
			width : 150,
			checked : true
		});

		this.baseRadio.on('change', function(radio, value) {
			if (this.editing) {
				this.base.setDisabled(!value);
			}
		},this); 

		this.basePanel = new Ext.panel.Panel({
			frame: true,
			layout: "hbox",
			margin: "0 0 5 0",
			items : [this.baseRadio, this.base]
		});
		
		this.advance = new Ext.panel.Panel({
			frame: true,
			layout: "hbox",
			margin: "0 0 5 0",
			items : [this.advanceRadio,
				{
					xtype: "panel",
					bodyCls: 'cmgraypanel',
					frame: false,
					border: false,
					items: this.advancedFields
				}
			]
		});

		Ext.apply(this, {
			frame : false,
			border : false,
			cls: "x-panel-body-default-framed",
			bodyCls: 'cmgraypanel',
			tbar: this.cmTBar,
			autoScroll: true,
			buttonAlign: "center",
			buttons: this.cmButtons,
			items : [ {
				xtype: "panel",
				frame: true,
				padding: "5 5 0 155",
				margin: "0 0 5 0",
				items: this.jobDescriptionField
			}, this.basePanel, this.advance]
		});

		this.callParent(arguments);
		this.disableModify();
	},

	onAddJobButtonClick: function() {
		this.reset();
		this.baseRadio.setValue(true);
		this.enableModify();
	},

	onProcessSelected: function() {
		this.reset();
		this.disableModify();
	},
	
	enableModify: function(all) {
		this.editing = true;
		this.mixins.cmFormFunctions.enableModify.call(this, all);

		if (this.baseRadio.getValue()) {
			this.setDisabledAdvancedFields(true);
		} else {
			this.base.disable();
		}
	},
	
	disableModify: function(enableCMTBar) {
		this.editing = false;

		this.mixins.cmFormFunctions.disableModify.call(this, enableCMTBar);
	},

	onJobSelected : function(job) {
		this.baseExpression = false; //flag to idenfity an expression in base form
		this.jobId = job.get("id");
		this.jobDescriptionField.setValue(job.get("description"));
		this.cronExpressionField.setValue(job.get("cronExpression"));
		this.setValueOfBaseIfPossible(job.get("cronExpression"));
		this.setValueOfAdvanced(job.get("cronExpression"));
		this.disableModify(enableCMTBar = true);
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
	}
});

Ext.define("CMDBuild.CronTriggerField", {
	extend: "Ext.form.field.Trigger",

	triggerCls : 'trigger-edit',
	onTriggerClick : function() {
		if (!this.disabled) {
			new CMDBuild.CronEditWindow({
				title : this.fieldLabel,
				parentField: this
			}).show();
		}
	}
});