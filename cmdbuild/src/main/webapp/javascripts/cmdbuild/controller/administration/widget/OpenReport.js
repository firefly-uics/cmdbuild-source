(function() {

	Ext.define('CMDBuild.controller.administration.widget.OpenReport', {
		extend: 'CMDBuild.controller.administration.widget.CMBaseWidgetDefinitionFormController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widgets.OpenReport',
			'CMDBuild.model.widget.openReport.ReportCombo'
		],

		statics: {
			WIDGET_NAME: '.OpenReport'
		},

		/**
		 * @property {CMDBuild.view.administration.widget.form.OpenReport}
		 */
		view: undefined,

		/**
		 * @param {Object} configuration
		 * @param {Number} configuration.classId
		 * @param {CMDBuild.view.administration.widget.form.OpenReport} configuration.view
		 */
		constructor: function(configuration) {
			this.callParent(arguments);

			// Handlers exchange
			this.view.delegate = this;

			this.mon(this.view, 'cm-selected-report', this.onReportSelected, this);

			// To enable/disable the combo-box with the related check
			this.view.forceFormatCheck.setValue = Ext.Function.createSequence(
				this.view.forceFormatCheck.setValue,
				function(value) {
					if (!this.forceFormatCheck.disabled) {
						this.forceFormatOptions.setDisabled(!value);

						if (value && typeof this.forceFormatOptions.getValue() != 'string') {
							this.forceFormatOptions.setValue(
								this.forceFormatOptions.store.first().get(this.forceFormatOptions.valueField)
							);
						} else {
							this.forceFormatOptions.setValue();
						}
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
		 * Calls fillPresetWithData to fill presetGrid store with empty fields and then recall for fill with real data
		 *
		 * @param {Object} selectedReport
		 * @param {Object} presets
		 * @param {Object} readOnlyAttributes
		 */
		onReportSelected: function(selectedReport, presets, readOnlyAttributes) {
			var reportCode = this.getReportCode(selectedReport);

			// Reset presetGrid store
			this.view.presetGrid.getStore().removeAll();

			CMDBuild.core.proxy.widgets.OpenReport.create({
				scope: this,
				params: {
					id: reportCode,
					type: 'CUSTOM',
					extension: 'pdf'
				},
				success: function(result, options, decodedResult) {
					var data = [];

					if (!decodedResult.filled)
						data = this.cleanServerAttributes(decodedResult.attribute);

					this.view.fillPresetWithData(data);

					if (!Ext.Object.isEmpty(presets))
						this.view.fillPresetWithData(presets, readOnlyAttributes);
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

			if (reportCode.self && reportCode.self.$className == 'CMDBuild.model.widget.openReport.ReportCombo')
				reportCode = reportCode.get(CMDBuild.core.constants.Proxy.ID);

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