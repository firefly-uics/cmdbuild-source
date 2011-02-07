CMDBuild.CronEditWindow = Ext.extend(Ext.Window, {
	modal : true,
	translation : CMDBuild.Translation.administration.modWorkflow.scheduler,
	width: 360, // TODO should be computed at runtime
	initComponent : function() {
		this.valueToReturn = undefined; //select one of the possible values to return when a radio is checked
		
		this.each = new Ext.form.Radio( {
			boxLabel : this.translation.every + this.title.toLowerCase(),
			labelSeparator : '',
			name : 'criteria',
			inputValue : 'each'
		});
		
		this.each.on('check', function(radio, value){
			if (value) {
				this.valueToReturn = this.possibleValue.each;
			}
		}, this);
		
		this.step = new Ext.form.Radio( {
			boxLabel : this.translation.inastepwith,
			labelSeparator : '',
			name : 'criteria',
			inputValue : 'step'
		});
		
		this.stepField = new Ext.form.TextField({
			fieldLabel: this.title,
			name: 'rangeField',
			disabled: true
		});

		this.step.on('check', function(radio, value){
			this.stepField.setDisabled(!value);
			if (value) {
				this.valueToReturn = this.possibleValue.step;
			}
		}, this);
		
		this.range = new Ext.form.Radio( {
			boxLabel : this.translation.range,
			labelSeparator : '',
			name : 'criteria',
			inputValue : 'range'
		});
		
		this.rangeFrom = new Ext.form.TextField({
			fieldLabel: this.translation.from,
			name: 'rangeFrom',
			disabled: true
		});
		
		this.rangeTo = new Ext.form.TextField({
			fieldLabel: this.translation.to,
			name: 'rangeTo',
			disabled: true
		});

		this.range.on('check', function(radio, value){
			this.rangeFrom.setDisabled(!value);
			this.rangeTo.setDisabled(!value);
			if (value) {
				this.valueToReturn = this.possibleValue.range;
			}
		}, this);
		
		this.exactly = new Ext.form.Radio( {
			boxLabel : this.translation.exactly,
			labelSeparator : '',
			name : 'criteria',
			inputValue : 'exactly'
		});
		
		this.exactField = new Ext.form.TextField({
			fieldLabel: this.title,
			name: 'rangeTo',
			disabled: true
		});
		
		this.exactly.on('check', function(radio, value){
			this.exactField.setDisabled(!value);
			if (value) {
				this.valueToReturn = this.possibleValue.exactly;
			}
		}, this);

		var labelWidth = 150;

		this.form = new Ext.Panel({
			layout: 'form',
			frame : true,
			border : true,
			labelWidth : 1,
			items : [
			   this.each,
			   this.step, {
				   xtype: 'panel',
				   layout: 'form',
				   labelAlign: 'right',
				   labelWidth : labelWidth,
				   style: {padding: '0 0 0 5px'},
				   items: [ this.stepField ]
			   },
			  
			   this.range, {
				   xtype: 'panel',
				   layout: 'form',
				   labelAlign: 'right',
				   labelWidth : labelWidth,
				   style: {padding: '0 0 0 5px'},
				   items: [ 
				      this.rangeFrom,
				      this.rangeTo
				   ]
			   },
			   
			   this.exactly, {
				   xtype: 'panel',
				   layout: 'form',
				   labelAlign: 'right',
				   labelWidth : labelWidth,
				   style: {padding: '0 0 0 5px'},
				   items: [this.exactField]
			   }
			],
			buttonAlign: 'center',
			buttons : [ {
				scope: this,
				text : CMDBuild.Translation.common.buttons.save,
				handler: this.onSave
			}, {
				scope: this,
				text : CMDBuild.Translation.common.buttons.abort,
				handler: function() {
					this.destroy();
				}
			} ]
		});

		
		this.possibleValue = {
				each: function() {return '*';},
				step: function() {
					return '0/'+this.stepField.getValue();
				},
				range: function() {
					var from = this.rangeFrom.getValue();
					var to = this.rangeTo.getValue();
					return from+'-'+to;
				},
				exactly: function() {
					return this.exactField.getValue();
				}
		};
		
		Ext.apply(this, {
			items : [ this.form ],
			frame : true
		});

		CMDBuild.CronEditWindow.superclass.initComponent.apply(this);
	},
	
	onSave: function() {
		var valueToSend = this.valueToReturn();
		if (valueToSend) {
			this.parentField.setValue(valueToSend);
		}
		this.destroy();
	}
});