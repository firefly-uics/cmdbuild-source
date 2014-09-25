(function() {

	Ext.require('CMDBuild.model.widget.CMModelOpenReport');

	Ext.define('CMDBuild.controller.administration.widget.CMOpenReportController', {
		extend: 'CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController',

		statics: {
			WIDGET_NAME: CMDBuild.view.administration.widget.form.CMOpenReportDefinitionForm.WIDGET_NAME
		},

		constructor: function() {
			this.callParent(arguments);

			this.mon(this.view, 'cm-selected-report', this.onReportSelected, this);

			// To enable/disable the combo-box with the related check
			this.view.forceFormatCheck.setValue = Ext.Function.createSequence(this.view.forceFormatCheck.setValue,
				function(value) {
					if (!this.forceFormatCheck.disabled) {
						this.forceFormatOptions.setDisabled(!value);

						if (value && typeof this.forceFormatOptions.getValue() != 'string')
							this.forceFormatOptions.setValue(this.forceFormatOptions.store.first().get(this.forceFormatOptions.valueField));
					}
				},
				this.view
			);
		},

		/**
		 * @override
		 */
		setDefaultValues: function() {
			this.callParent(arguments);
			this.view.forceFormatCheck.setValue(true);
		},

		/**
		 * @param {Object} selectedReport
		 */
		onReportSelected: function(selectedReport) {
			var reportCode = this.getReportCode(selectedReport);

			Ext.Ajax.request({
				scope: this,
				url: 'services/json/management/modreport/createreportfactory',
				params: {
					id: reportCode,
					type: 'CUSTOM',
					extension: 'pdf'
				},
				success: function(result, options, decodedResult) {
					var ret = Ext.JSON.decode(result.responseText);
					var hasAttributeToSet = !ret.filled;
					var data = [];

					if (hasAttributeToSet)
						data = this.cleanServerAttributes(ret.attribute);

					this.view.fillPresetWithData(data);
				}
			});
		},

		/**
		 * @param {Object} selectedReport
		 */
		getReportCode: function(selectedReport) {
			var reportCode = selectedReport;

			if (Ext.isArray(selectedReport))
				reportCode = selectedReport[0];

			if (reportCode.self && reportCode.self.$className == 'CMDBuild.model.CMReportAsComboItem')
				reportCode = reportCode.get(CMDBuild.model.CMReportAsComboItem._FIELDS.id);

			return reportCode;
		},

		/**
		 * @param {Array} attributes
		 */
		cleanServerAttributes: function(attributes) {
			var out = {};

			for (var i = 0; i < attributes.length; ++i) {
				var attr = attributes[i];

				out[attr.name] = '';
			}

			return out;
		}
	});

})();