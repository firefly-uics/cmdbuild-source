(function() {

	Ext.define('CMDBuild.controller.administration.tasks.common.cronForm.CMCronFormController', {

		advancedField: undefined,
		baseField: undefined,

		/**
		 * @param (Array) fields
		 * @returns (String) cron expression
		 */
		buildCronExpression: function(fields) {
			var cronExpression = '';

			for (var i = 0; i < fields.length; i++) {
				cronExpression += fields[i];

				if (i < (fields.length - 1))
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
						me.setValueBase(
							me.buildCronExpression([
								me.advancedField.advancedFields[0].getValue(),
								me.advancedField.advancedFields[1].getValue(),
								me.advancedField.advancedFields[2].getValue(),
								me.advancedField.advancedFields[3].getValue(),
								me.advancedField.advancedFields[4].getValue()
							])
						);
					}
				}
			});
		},

		getBaseCombo: function() {
			return this.baseField.baseCombo;
		},

		isEmptyAdvanced: function() {
			if (
				Ext.isEmpty(this.advancedField.advancedFields[0].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[1].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[2].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[3].getValue())
				&& Ext.isEmpty(this.advancedField.advancedFields[4].getValue())
			)
				return true;

			return false;
		},

		isEmptyBase: function() {
			return Ext.isEmpty(this.baseField.baseCombo.getValue());
		},

		markInvalidAdvancedFields: function(message) {
			for(item in this.advancedField.advancedFields)
				this.advancedField.advancedFields[item].markInvalid(message);
		},

		setValueAdvancedFields: function(cronExpression) {
			var values = cronExpression.split(' ');
			var fields = this.advancedField.advancedFields;

			for (var i = 0; i < fields.length; i++) {
				if (values[i])
					fields[i].setValue(values[i]);
			}
		},

		setValueAdvancedRadio: function(value) {
			this.advancedField.advanceRadio.setValue(value);
		},

		setDisabledAdvancedFields: function(value) {
			for (var key in this.advancedField.advancedFields)
				this.advancedField.advancedFields[key].setDisabled(value);
		},

		setDisabledBaseCombo: function(value) {
			this.baseField.baseCombo.setDisabled(value);
		},

		/**
		 * Try to find the correspondence of advanced cronExpression in baseCombo's store
		 *
		 * @param (String) value
		 */
		setValueBase: function(value) {
			var index = this.baseField.baseCombo.store.find(CMDBuild.ServiceProxy.parameter.VALUE, value);

			if (index > -1) {
				this.baseField.baseCombo.setValue(value);
			} else {
				this.baseField.baseCombo.setValue();
			}
		}
	});

})();