(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		advancedPanel: undefined,
		basePanel: undefined,

		/**
		 * @param (Array) fields
		 * @returns (String) cron expression
		 */
		buildCronExpression: function(fields) {
			var cronExpression = '';

			for (var i = 0; i < fields.length; i++) {
				cronExpression += fields[i] + ' ';

				if (i < fields.length)
					cronExpression += ' ';
			}

			return cronExpression;
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
							me.buildCronExpression([
								me.advancedPanel.advancedFields[0].getValue(),
								me.advancedPanel.advancedFields[1].getValue(),
								me.advancedPanel.advancedFields[2].getValue(),
								me.advancedPanel.advancedFields[3].getValue(),
								me.advancedPanel.advancedFields[4].getValue()
							])
						);
					}
				}
			});
		},

		getBaseCombo: function() {
			return this.basePanel.baseCombo;
		},

		isAdvancedEmpty: function() {
			if (
				Ext.isEmpty(this.advancedPanel.advancedFields[0].getValue())
				&& Ext.isEmpty(this.advancedPanel.advancedFields[1].getValue())
				&& Ext.isEmpty(this.advancedPanel.advancedFields[2].getValue())
				&& Ext.isEmpty(this.advancedPanel.advancedFields[3].getValue())
				&& Ext.isEmpty(this.advancedPanel.advancedFields[4].getValue())
			)
				return true;

			return false;
		},

		isBaseEmpty: function() {
			return Ext.isEmpty(this.basePanel.baseCombo.getValue());
		},

		markInvalidAdvancedFields: function(message) {
			for(item in this.advancedPanel.advancedFields)
				this.advancedPanel.advancedFields[item].markInvalid(message);
		},

		setAdvancedValue: function(cronExpression) {
			var values = cronExpression.split(' ');
			var fields = this.advancedPanel.advancedFields;

			for (var i = 0; i < fields.length; i++) {
				if (values[i])
					fields[i].setValue(values[i]);
			}
		},

		setAdvancedRadioValue: function(value) {
			this.advancedPanel.advanceRadio.setValue(value);
		},

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 *
		 * @param (String) value
		 */
		setBaseValue: function(value) {
			var index = this.basePanel.baseCombo.store.find(CMDBuild.ServiceProxy.parameter.VALUE, value);

			if (index > -1) {
				this.basePanel.baseCombo.setValue(value);
			} else {
				this.basePanel.baseCombo.setValue();
			}
		},

		setDisabledAdvancedFields: function(value) {
			for (var key in this.advancedPanel.advancedFields)
				this.advancedPanel.advancedFields[key].setDisabled(value);
		},

		setDisabledBaseCombo: function(value) {
			this.basePanel.baseCombo.setDisabled(value);
		}
	});

})();