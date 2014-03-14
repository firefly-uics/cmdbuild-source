(function() {

	var translation = CMDBuild.Translation.administration.modWorkflow.scheduler;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate', {

		parentDelegate: undefined,
		filterWindow: undefined,
		view: undefined,

		cmOn: function(name, param, callBack) {
			switch (name) {

				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		createCronField: function(name, label) {
			var me = this;

			return Ext.create('CMDBuild.view.common.field.CMCronTriggerField', {
				name: name,
				fieldLabel: label,
				cmImmutable: true,
				disabled: true,
				listeners: {
					change: function(field, newValue, oldValue) {
						me.setBaseValue(
							me.parentDelegate.buildCronExpression([
								me.view.advancedFields[0].getValue(),
								me.view.advancedFields[1].getValue(),
								me.view.advancedFields[2].getValue(),
								me.view.advancedFields[3].getValue(),
								me.view.advancedFields[4].getValue()
							])
						);
					}
				}
			});
		},

		setAdvancedValue: function(cronExpression) {
			var values = cronExpression.split(' '),
				fields = this.view.advancedFields;

			for (var i = 0; i < fields.length; i++) {
				var field = fields[i];

				if (values[i])
					field.setValue(values[i]);
			}
		},

		setBaseValue: function(value) { // TODO: final test and fix
//			var index = this.view.base.store.find(CMDBuild.ServiceProxy.parameter.VALUE, value);
//			_debug(value);
//_debug(index);
//			if (index > -1) {
//				this.view.base.setValue(value);
//			}
//			else {
//				this.view.base.setValue('');
//			}
//
//			this.view.baseExpression = index > -1;
		},

		setDisabledAdvancedFields: function(value) {
			for (var key in this.view.advancedFields)
				this.view.advancedFields[key].setDisabled(value);
		}
	});

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep2', {
		extend: 'Ext.panel.Panel',

		delegate: undefined,
		taskType: 'workflow',

		border: false,
		height: '100%',
		overflowY: 'auto',

		defaults: {
			labelWidth: CMDBuild.LABEL_WIDTH,
			xtype: 'textfield'
		},

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate');
			this.delegate.view = this;

			// Advanced panel setup
			this.advanceRadio = Ext.create('Ext.form.field.Radio', {
				name: 'cronInputType',
				inputValue: 'advanced',
				boxLabel: translation.advanced,
				width: CMDBuild.LABEL_WIDTH,
				listeners: {
					'change': function(radio, value) {
						me.base.setDisabled(value);
					}
				}
			});

			this.advancedFields = [
				this.delegate.createCronField('minute', translation.minute),
				this.delegate.createCronField('hour', translation.hour),
				this.delegate.createCronField('dayOfMounth', translation.dayOfMounth),
				this.delegate.createCronField('mounth', translation.mounth),
				this.delegate.createCronField('dayOfWeek', translation.dayOfWeek)
			];

			this.advancePanel = Ext.create('Ext.panel.Panel', {
				frame: true,
				layout: 'hbox',
				margin: '0px 0px 5px 0px',
				items: [
					this.advanceRadio,
					{
						xtype: 'container',
						frame: false,
						border: false,
						items: this.advancedFields
					}
				]
			});
			// END: Advanced panel setup

			// Base panel setup
			this.baseRadio = Ext.create('Ext.form.Radio', {
				name: 'cronInputType',
				inputValue: 'base',
				boxLabel: translation.basic,
				width: CMDBuild.LABEL_WIDTH,
				checked: true,
				listeners: {
					'change': function(radio, value) {
						me.delegate.setAdvancedValue(value);
					}
				}
			});

			this.base = Ext.create('Ext.form.ComboBox', {
				name: 'base',
				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						['0 * * * ?', translation.everyHour],
						['0 0 * * ?', translation.everyDay],
						['0 0 1 * ?', translation.everyMounth],
						['0 0 1 1 ?', translation.everyYear]
					]
				}),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				queryMode: 'local',
				forceSelection: true,
				editable: false,
				margins: '0px 0px 0px ' + (CMDBuild.LABEL_WIDTH - 45) + 'px',
				listeners: {
					'select': function(combo, record, index) {
						me.delegate.setAdvancedValue(record[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
					}
				}
			});

			this.basePanel = Ext.create('Ext.panel.Panel', {
				frame: true,
				layout: 'hbox',
				margin: '0px 0px 5px 0px',
				items: [this.baseRadio, this.base]
			});
			// END: Base panel setup

			Ext.apply(this, {
				items: [this.basePanel, this.advancePanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(view, eOpts) {
				this.delegate.setDisabledAdvancedFields(true);
			}
		}
	});

})();
