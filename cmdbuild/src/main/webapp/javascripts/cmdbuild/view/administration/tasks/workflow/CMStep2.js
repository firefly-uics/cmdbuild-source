(function() {

	var tr = CMDBuild.Translation.administration.tasks.taskWorkflow;

	Ext.define('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		parentDelegate: undefined,
		filterWindow: undefined,
		view: undefined,

		/**
		 * Gatherer function to catch events
		 *
		 * @param (String) name
		 * @param (Object) param
		 * @param (Function) callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				default: {
					if (this.parentDelegate)
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		/**
		 * Create CMCronTriggerField
		 *
		 * @param (String) name
		 * @param (String) label
		 * @return (Object) CMDBuild.view.common.field.CMCronTriggerField
		 */
		createCronField: function(name, label) {
			var me = this;

			return Ext.create('CMDBuild.view.common.field.CMCronTriggerField', {
				name: name,
				fieldLabel: label,
				cmImmutable: true,
				disabled: true,
				allowBlank: false,
				listeners: {
					change: function(field, newValue, oldValue) {
						me.setBaseValue(
							me.parentDelegate.parentDelegate.buildCronExpression([
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

		isAdvancedEmpty: function() {
			if (
				Ext.isEmpty(this.view.advancedFields[0].getValue())
				&& Ext.isEmpty(this.view.advancedFields[1].getValue())
				&& Ext.isEmpty(this.view.advancedFields[2].getValue())
				&& Ext.isEmpty(this.view.advancedFields[3].getValue())
				&& Ext.isEmpty(this.view.advancedFields[4].getValue())
			)
				return true;

			return false;
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

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 *
		 * @param (String) value
		 */
		setBaseValue: function(value) {
			var index = this.view.baseCombo.store.find(CMDBuild.ServiceProxy.parameter.VALUE, value);

			if (index > -1) {
				this.view.baseCombo.setValue(value);
			} else {
				this.view.baseCombo.setValue();
			}
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

		initComponent: function() {
			var me = this;

			this.delegate = Ext.create('CMDBuild.view.administration.tasks.workflow.CMStep2Delegate', this);

			// Advanced panel setup
			this.advanceRadio = Ext.create('Ext.form.field.Radio', {
				name: 'cronInputType',
				inputValue: 'advanced',
				boxLabel: tr.advanced,
				width: CMDBuild.LABEL_WIDTH,
				listeners: {
					change: function(radio, value) {
						me.baseCombo.setDisabled(value);
						me.delegate.setDisabledAdvancedFields(!value);
					}
				}
			});

			this.advancedFields = [
				this.delegate.createCronField('minute', tr.minute),
				this.delegate.createCronField('hour', tr.hour),
				this.delegate.createCronField('dayOfMounth', tr.dayOfMounth),
				this.delegate.createCronField('mounth', tr.mounth),
				this.delegate.createCronField('dayOfWeek', tr.dayOfWeek)
			];

			this.advancePanel = Ext.create('Ext.panel.Panel', {
				frame: true,
				layout: 'hbox',
				margin: '0 0 5 0',
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
				boxLabel: tr.basic,
				width: CMDBuild.LABEL_WIDTH,
				listeners: {
					change: function(radio, value) {
						me.baseCombo.setDisabled(!value);
						me.delegate.setDisabledAdvancedFields(value);
					}
				}
			});

			this.baseCombo = Ext.create('Ext.form.ComboBox', {
				name: 'baseCombo',
				store: Ext.create('Ext.data.SimpleStore', {
					fields: [CMDBuild.ServiceProxy.parameter.VALUE, CMDBuild.ServiceProxy.parameter.DESCRIPTION],
					data: [
						['0 * * * ?', tr.everyHour],
						['0 0 * * ?', tr.everyDay],
						['0 0 1 * ?', tr.everyMounth],
						['0 0 1 1 ?', tr.everyYear]
					]
				}),
				valueField: CMDBuild.ServiceProxy.parameter.VALUE,
				displayField: CMDBuild.ServiceProxy.parameter.DESCRIPTION,
				queryMode: 'local',
				forceSelection: true,
				editable: false,
				margins: '0 0 0 ' + (CMDBuild.LABEL_WIDTH - 45),
				listeners: {
					select: function(combo, record, index) {
						me.delegate.setAdvancedValue(record[0].get(CMDBuild.ServiceProxy.parameter.VALUE));
					}
				}
			});

			this.basePanel = Ext.create('Ext.panel.Panel', {
				frame: true,
				layout: 'hbox',
				margin: '0 0 5 0',
				items: [this.baseRadio, this.baseCombo]
			});
			// END: Base panel setup

			Ext.apply(this, {
				items: [this.basePanel, this.advancePanel]
			});

			this.callParent(arguments);
		},

		listeners: {
			/**
			 * To correctly enable radio fields on tab show
			 */
			show: function(view, eOpts) {
				if (Ext.isEmpty(this.baseCombo.getValue()) && !this.delegate.isAdvancedEmpty()) {
					this.advanceRadio.setValue(true);
				} else {
					this.baseRadio.setValue(true);
				}
			}
		}
	});

})();
